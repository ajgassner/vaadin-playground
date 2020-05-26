# Buffering Event Dispatcher

This example shows how to update the UI automatically and asynchronously when new data arrives
or data changes. It's a common way in event-driven architectures to react to async
events coming from e.g. Apache Kafka or RabbitMQ.

The class [`at.agsolutions.demo.UiAwareBufferingEventDispatcher`](https://github.com/ajgassner/vaadin-playground/blob/master/buffering-event-dispatcher/src/main/kotlin/at/agsolutions/demo/UiAwareBufferingEventDispatcher.kt)
has following features:

* Buffers events with RxKotlin (https://github.com/ReactiveX/RxKotlin) to avoid data flooding and unresponsive UIs
* Spring SecurityContext aware, means the consuming Vaadin component is able to know if the user has the authority to see the data
* Uses a thread pool provided by Spring
* A simple replacement for a complex event bus

Also see blog post TODO!!!!!!!!!!!!!!

## Run the demo
`mvn spring-boot:run`

Open `http://localhost:8080/` in the browser.

Credentials:  
Username: *user*  
Password: *pw*

For the sake of simplicity we call a simple REST service to simulate data events.
Use following command or any other HTTP client to call the rest service to post messages:  
`curl -v -d "message=This is the message" http://localhost:8080/api/message`

The UI should update automatically and show the messages posted via the API in an async way.
