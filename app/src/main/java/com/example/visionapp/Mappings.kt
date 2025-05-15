package com.example.visionapp

import com.example.visionapp.communiates.CommunicateType

object Mappings {
    enum class DetectionNmsGroup {
        GREEN_LIGHT,
        SIGN,
        RED_LIGHT,
        ZEBRA
    }

    enum class DetectionDistanceGroup {
        PEDESTRIAN_LIGHT,
        PEDESTRIANS_LANE_SIGN,
        NO_PEDESTRIANS,
        CROSSWALK,
        BIKE_GREEN_LIGHT,
        BIKE_RED_LIGHT,
        CAR_GREEN_LIGHT,
        CAR_RED_LIGHT
    }

    val DETECTION_NMS_GROUPS = mapOf(
        0 to DetectionNmsGroup.GREEN_LIGHT,
        1 to DetectionNmsGroup.SIGN,
        2 to DetectionNmsGroup.SIGN,
        3 to DetectionNmsGroup.RED_LIGHT,
        4 to DetectionNmsGroup.SIGN,
        5 to DetectionNmsGroup.SIGN,
        6 to DetectionNmsGroup.ZEBRA,
        7 to DetectionNmsGroup.GREEN_LIGHT,
        8 to DetectionNmsGroup.RED_LIGHT,
        9 to DetectionNmsGroup.GREEN_LIGHT,
        10 to DetectionNmsGroup.RED_LIGHT,
        11 to DetectionNmsGroup.SIGN,
        12 to DetectionNmsGroup.SIGN
    )

    val DETECTION_DISTANCE_ESTIMATION_GROUPS = mapOf(
        0 to DetectionDistanceGroup.PEDESTRIAN_LIGHT,
        1 to DetectionDistanceGroup.PEDESTRIANS_LANE_SIGN,
        2 to DetectionDistanceGroup.PEDESTRIANS_LANE_SIGN,
        3 to DetectionDistanceGroup.PEDESTRIAN_LIGHT,
        4 to DetectionDistanceGroup.PEDESTRIANS_LANE_SIGN,
        5 to DetectionDistanceGroup.NO_PEDESTRIANS,
        6 to DetectionDistanceGroup.CROSSWALK,
        7 to DetectionDistanceGroup.BIKE_GREEN_LIGHT,
        8 to DetectionDistanceGroup.BIKE_RED_LIGHT,
        9 to DetectionDistanceGroup.CAR_GREEN_LIGHT,
        10 to DetectionDistanceGroup.CAR_RED_LIGHT,
        11 to DetectionDistanceGroup.PEDESTRIANS_LANE_SIGN,
        12 to DetectionDistanceGroup.PEDESTRIANS_LANE_SIGN
    )

    val DETECTION_COMMUNICATE_CLASSES = mapOf(
        0 to CommunicateType.GREEN_LIGHT,
        1 to CommunicateType.PEDESTRIANS_TO_THE_LEFT,
        2 to CommunicateType.PEDESTRIANS_TO_THE_RIGHT,
        3 to CommunicateType.RED_LIGHT,
        4 to CommunicateType.COMMON_AREA,
        5 to CommunicateType.NO_PEDESTRIANS,
        6 to CommunicateType.CROSSING
    )

    val TRIANGLE_COMMUNICATE_CLASSES = mapOf(
        1 to CommunicateType.NARROW_PASSAGE,
        2 to CommunicateType.MOVE_RIGHT,
        3 to CommunicateType.MOVE_LEFT,
        4 to CommunicateType.OBSTACLE,
        5 to CommunicateType.NO_PASSAGE
    )
}