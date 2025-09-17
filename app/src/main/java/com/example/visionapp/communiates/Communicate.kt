package com.example.visionapp.communiates

enum class CommunicateCategory{
    LIGHTS,
    SIGNS,
    PASSAGE,
    CROSSING
}

enum class CommunicateType(val message: String, val priority: Int, val category: CommunicateCategory) {
    RED_LIGHT("Warning, red light", 1, CommunicateCategory.LIGHTS),
    NO_PASSAGE("Warning, turn back", 2, CommunicateCategory.PASSAGE),
    GREEN_LIGHT("Green light", 3, CommunicateCategory.LIGHTS),
    CROSSING("Warning, entering a crossing", 4, CommunicateCategory.CROSSING),
    NARROW_PASSAGE("Warning, narrow passage", 5, CommunicateCategory.PASSAGE),
    MOVE_LEFT("Move left", 6, CommunicateCategory.PASSAGE),
    MOVE_RIGHT("Move right", 6, CommunicateCategory.PASSAGE),
    NO_PEDESTRIANS("Warning, pedestrians forbidden", 2, CommunicateCategory.SIGNS),
    PEDESTRIANS_TO_THE_LEFT("Warning, pedestrians to the left", 7, CommunicateCategory.SIGNS),
    PEDESTRIANS_TO_THE_RIGHT("Warning, pedestrians to the right", 7, CommunicateCategory.SIGNS),
    COMMON_AREA("You are entering a area for pedestrians and bikes", 7, CommunicateCategory.SIGNS),
    OBSTACLE("Warning, obstacle ahead", 6, CommunicateCategory.PASSAGE),
    WARNING_ROAD("Warning, entering a road", 6, CommunicateCategory.PASSAGE),
    WARNING_BIKE_PATH("Warning, entering a bike path", 6, CommunicateCategory.PASSAGE),
    WARNING_PERSON("Warning, you are walking into someone", 8, CommunicateCategory.PASSAGE);
}


data class Communicate(
    val communicateType: CommunicateType,
    val timestamp: Long = System.currentTimeMillis()
) : Comparable<Communicate> {

    override fun compareTo(other: Communicate): Int {
        val priorityCompare = this.communicateType.priority.compareTo(other.communicateType.priority)
        return if (priorityCompare != 0) {
            priorityCompare
        } else {
            other.timestamp.compareTo(this.timestamp)
        }
    }
}
