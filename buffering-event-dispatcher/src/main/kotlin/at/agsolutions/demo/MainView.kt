package at.agsolutions.demo

import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.router.Route

@Push
@Route("")
class MainView(private val dispatcher: UiAwareBufferingEventDispatcher) : VerticalLayout() {

    init {
        add(H3("Posted Messages"))
    }

    override fun onAttach(event: AttachEvent) {
        dispatcher.register(this, MessagePostedEvent::class) { events ->
            logger().info("got events")
            events.forEach { event -> add(Span("${event.id}: ${event.message}, ${SecurityUtils.user?.username ?: "Niemand"}")) }
        }
    }

    override fun onDetach(event: DetachEvent) {
        // TODO: check this gts called when session gets destroyed
        dispatcher.unregister(this)
    }
}
