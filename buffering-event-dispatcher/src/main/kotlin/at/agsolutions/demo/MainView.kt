package at.agsolutions.demo

import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.router.Route

@Push
@Route("")
class MainView(private val dispatcher: UiAwareBufferingEventDispatcher) : VerticalLayout() {

    init {
        add(H3("Posted Messages"), Span("Call REST service to post new messages here"))
    }

    override fun onAttach(event: AttachEvent) {
        dispatcher.register(this, MessagePostedEvent::class) { events ->
            val username = SecurityUtils.user?.username ?: "unauthorized"
            events.forEach { event -> add(Span("ID: ${event.id}, Message: ${event.message}, Username: $username")) }
        }
    }

    override fun onDetach(event: DetachEvent) {
        // do not forget to unregister the consumer!
        dispatcher.unregister(this)
    }
}
