package com.example.visionapp.communiates

enum class CommunicatePriority(val message: String, val priority: Int) {
    RED_LIGHT("Uwaga, czerwone światło", 1),
    NO_CROSSING("Uwaga, brak przejścia (zawróć)", 2),
    GREEN_LIGHT("Zielone światło", 3),
    CROSSING("Uwaga, wchodzisz na przejście dla pieszych", 4),
    NARROW_PASSAGE("Uwaga, wąskie przejście", 5),
    MOVE_LEFT("Przesuń się w lewo, bo jesteś za blisko krawędzi/nie obejdziesz przeszkody", 6),
    MOVE_RIGHT("Przesuń się w prawo, bo jesteś za blisko krawędzi/nie obejdziesz przeszkody", 6),
    SIGN("Uwaga, jakiś znak", 7);

    companion object {
        fun fromMessage(message: String): CommunicatePriority? =
            values().find { it.message == message }
    }
}

data class Communicate(
    val message: String,
    val priority: Int,
    val timestamp: Long = System.currentTimeMillis()
) : Comparable<Communicate> {

    constructor(message: String) : this(
        message = message,
        priority = CommunicatePriority.fromMessage(message)
            ?.priority
            ?: throw IllegalArgumentException("No priority for: $message")
    )

    override fun compareTo(other: Communicate): Int {
        val priorityCompare = this.priority.compareTo(other.priority)
        return if (priorityCompare != 0) {
            priorityCompare
        } else {
            other.timestamp.compareTo(this.timestamp)
        }
    }
}
