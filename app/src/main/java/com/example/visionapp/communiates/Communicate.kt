package com.example.visionapp.communiates

enum class CommunicateCategory{
    LIGHTS,
    SIGNS,
    PASSAGE,
    CROSSING
}

enum class CommunicateType(val message: String, val priority: Int, val category: CommunicateCategory) {
    RED_LIGHT("Warning, red light", 1, CommunicateCategory.LIGHTS),
    NO_PASSAGE("Warning, no passage (turn back)", 2, CommunicateCategory.PASSAGE),
    GREEN_LIGHT("Green light", 3, CommunicateCategory.LIGHTS),
    CROSSING("Warning, you are entering a pedestrian crossing", 4, CommunicateCategory.CROSSING),
    NARROW_PASSAGE("Warning, narrow passage", 5, CommunicateCategory.PASSAGE),
    MOVE_LEFT("Move left, you're too close to the edge / obstacle can't be bypassed", 6, CommunicateCategory.PASSAGE),
    MOVE_RIGHT("Move right, you're too close to the edge / obstacle can't be bypassed", 6, CommunicateCategory.PASSAGE),
    NO_PEDESTRIANS("Warning, pedestrians are forbidden to enter this area", 2, CommunicateCategory.SIGNS),
    PEDESTRIANS_TO_THE_LEFT("Warning, pedestrians should move on the left side", 7, CommunicateCategory.SIGNS),
    PEDESTRIANS_TO_THE_RIGHT("Warning, pedestrians should move on the right side", 7, CommunicateCategory.SIGNS),
    COMMON_AREA("You are entering a common area for pedestrians and bikes", 7, CommunicateCategory.SIGNS),
    OBSTACLE("Warning, there is an obstacle in front of you", 6, CommunicateCategory.PASSAGE),
    WARNING_ROAD("Warning, you are entering a road", 8, CommunicateCategory.PASSAGE),
    WARNING_BIKE_PATH("Warning, you are entering a bike path", 8, CommunicateCategory.PASSAGE),
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
