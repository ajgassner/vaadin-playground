package at.agsolutions.demo

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UIDetachedException
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import kotlin.reflect.KClass

/**
 * UI, SecurityContext aware and event buffering dispatcher.
 */
@Service
class UiAwareBufferingEventDispatcher(@Qualifier("uiTaskExecutor") val taskExecutor: AsyncTaskExecutor) {

    companion object {
        const val BUFFER_TIMESPAN_IN_MILLIS: Long = 500L
    }

    interface Registration {
        fun remove()
    }

    private data class BufferedEventHandler<T>(val view: Component, val handler: (List<T>) -> Unit)

    private val eventTypeHandlers = mutableMapOf<KClass<*>, MutableList<BufferedEventHandler<*>>>()
    private val subject = PublishSubject.create<Any>()
    private val executor = Executors.newSingleThreadExecutor()
    private val scheduler = Schedulers.from(executor)
    private var subscribe: Disposable? = null

    @PostConstruct
    fun postConstruct() {
        subscribe()
    }

    /** dispatch event (Note: runs in caller thread) */
    fun dispatch(event: Any) {
        subject.onNext(event)
    }

    /** start internal subscription to subject (events, which will be dispatched) */
    fun subscribe() {
        if (subscribe == null || subscribe!!.isDisposed) {
            subscribe = subject.observeOn(scheduler)
                .buffer(BUFFER_TIMESPAN_IN_MILLIS, TimeUnit.MILLISECONDS)
                .subscribe { this.dispatchToHandlers(it) }
        }
    }

    /** stop internal subscription to subject (for test purposes) */
    fun dispose() {
        if (subscribe != null && !subscribe!!.isDisposed) {
            subscribe!!.dispose()
            subscribe = null
        }
    }

    /** for test purposes */
    fun reset() {
        dispose()
        synchronized(eventTypeHandlers) {
            eventTypeHandlers.clear()
        }
    }

    /** registers to an event by type.
     * Registration is bound to a view, which is needed to synchronize view state and obtain SecurityContext.
     * Returns Registration, which can be used to unregister (optional @see .unregister by view can be used). */
    fun <T : Any> register(view: Component, eventType: KClass<T>, handler: (List<T>) -> Unit): Registration {
        synchronized(eventTypeHandlers) {
            val authenticatedHandler = BufferedEventHandler(view, handler)

            val handlers = eventTypeHandlers.computeIfAbsent(eventType) { mutableListOf() }
            handlers.add(authenticatedHandler)

            return object : Registration {
                override fun remove() {
                    handlers.remove(authenticatedHandler)
                }
            }
        }
    }

    /** unregister all handlers for a given view */
    fun unregister(view: Component) {
        synchronized(eventTypeHandlers) {
            val typesToRemove = mutableListOf<KClass<*>>()
            eventTypeHandlers.forEach { (type, handlers) ->
                handlers.removeIf { it.view === view }
                if (handlers.isEmpty()) {
                    typesToRemove.add(type)
                }
            }
            typesToRemove.forEach { eventTypeHandlers.remove(it) }
        }
    }


    /**
     * dispatched list of buffered events to handlers.
     * This will be called within rx-scheduler/threadpool.
     * Dispatching will be done view @see org.springframework.core.task.AsyncTaskExecutor)
     */
    @Suppress("UNCHECKED_CAST")
    private fun dispatchToHandlers(events: List<Any>) {
        pairedByEvents(events).forEach { pair ->
            pair.second?.forEach { eventHandler ->
                taskExecutor.execute {
                    dispatchWithinUIContext(eventHandler.view, eventHandler.handler as ((List<*>) -> Unit), pair.first)
                }
            }
        }
    }

    private fun pairedByEvents(events: List<Any>): List<Pair<List<Any>, MutableList<BufferedEventHandler<*>>?>> {
        return synchronized(eventTypeHandlers) {
            events
                .groupBy({ it::class }, { it })
                .map { (type, events) -> events to eventTypeHandlers[type] }
        }
    }

    /**
     * Sends list of buffered events to registered handlers, and synchronizes call to view state (@see com.vaadin.flow.component.UI.access(com.vaadin.flow.server.Command))
     * and with session bound security context. Runs within TaskExecutor Thread.
     * In case view is not bound to an UI or session, this call ends without any exception (handlers should only update UI and must not trigger and business logic)
     */
    private fun dispatchWithinUIContext(view: Component, handler: (List<*>) -> Unit, events: List<*>) {
        val ui = view.ui.orElse(null) ?: return
        val vaadinSession = ui.session ?: return
        val httpSession = vaadinSession.session ?: return

        val sessionSecurityContext = httpSession.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY)
        val securityContextToUse = if (sessionSecurityContext is SecurityContext) {
            sessionSecurityContext
        } else {
            SecurityContextHolder.createEmptyContext()
        }

        ui.access {
            val origCtx = SecurityContextHolder.getContext()
            try {
                SecurityContextHolder.setContext(securityContextToUse)
                handler(events)
            } catch (e: UIDetachedException) {
                // ignore exceptions (just UI updates)
            } catch (e: Exception) {
                logger().error("unexpected exception while handling events $events bound to view $view", e)
            } finally {
                SecurityContextHolder.setContext(origCtx)
            }
        }
    }
}
