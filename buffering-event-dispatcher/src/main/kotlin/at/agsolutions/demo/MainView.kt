package at.agsolutions.demo

import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.router.Route

@Push
@Route("")
class MainView(private val dispatcher: UiAwareBufferingEventDispatcher) : VerticalLayout() {

    private val clearButton = Button("Clear messages", VaadinIcon.CLOSE.create()) { clear() }

    init {
        clear()
    }

    private fun clear() {
        removeAll()
        add(H3("Posted Messages"), clearButton, Span("Call REST service to post new messages here"))
    }

    override fun onAttach(event: AttachEvent) {
        dispatcher.register(this, MessagePostedEvent::class) { bufferedEvents ->
            // usually for an UI update only the last of the buffered events is interesting for us
            val username = SecurityUtils.user?.username ?: "unauthorized"
            add(Span("ID: ${bufferedEvents.last().id}, Message: ${bufferedEvents.last().message}, Username: $username"))
        }
    }

    override fun onDetach(event: DetachEvent) {
        // do not forget to unregister the consumer!
        dispatcher.unregister(this)
    }
}
