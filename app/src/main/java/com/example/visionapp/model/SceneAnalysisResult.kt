package com.example.visionapp.model

enum class SceneAnalysisResult(val id: Int) {
    NO_OBSTACLE(0),
    NARROW_PASSAGE(1),
    MOVE_RIGHT(2),
    MOVE_LEFT(3),
    OBSTACLE_FRONT(4),
    TURN_AROUND(5);

    companion object {
        fun fromId(id: Int): SceneAnalysisResult {
            return entries.find { it.id == id }
                ?: throw IllegalArgumentException("Invalid SceneAnalysisResult ID: $id")
        }
    }
}
