package at.agsolutions.demo

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/message")
class TestRestService(private val dispatcher: UiAwareBufferingEventDispatcher) {

    @PostMapping
    fun postMessage(@RequestParam("message") message: String): ResponseEntity<String> {
        dispatcher.dispatch(MessagePostedEvent(UUID.randomUUID(), message))
        return ResponseEntity.ok("message posted")
    }
}
