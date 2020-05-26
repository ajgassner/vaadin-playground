package at.agsolutions.demo

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/message")
class MessageRestService(private val dispatcher: UiAwareBufferingEventDispatcher) {

    @PostMapping
    fun postMessage(@RequestParam("message") message: String): ResponseEntity<String> {
        val event = MessagePostedEvent(UUID.randomUUID(), message)
        (1..100).forEach { _ ->
            // dispatch this event multiple times for demo purposes
            dispatcher.dispatch(event)
        }
        return ResponseEntity.ok("message posted")
    }
}
