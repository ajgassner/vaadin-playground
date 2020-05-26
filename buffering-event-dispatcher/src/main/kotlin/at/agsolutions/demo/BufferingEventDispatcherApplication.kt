package at.agsolutions.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BufferingEventDispatcherApplication

fun main(args: Array<String>) {
	runApplication<BufferingEventDispatcherApplication>(*args)
}
