package at.agsolutions.demo

import java.util.*

data class MessagePostedEvent(
    val id: UUID,
    val message: String
)
