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

    val SEGMENTATION_COLOR_MAP = mapOf(
        0   to intArrayOf(0,    0,      0,      192),   // road,        black
        1   to intArrayOf(168,  16,     243,    192),   // sidewalk,    purple
        2   to intArrayOf(250,  250,    55,     192),   // wall,        yellow
        3   to intArrayOf(250,  50,     83,     192),   // obstacle,    red
        4   to intArrayOf(0,    255,    0,      192),   // grass,       light-green
        5   to intArrayOf(51,   221,    255,    192),   // sky,         light-blue
        6   to intArrayOf(245,  147,    49,     192),   // person,      orange
        7   to intArrayOf(65,   65,     232,    192),   // vehicle,     dark-blue
        8   to intArrayOf(2,    100,    27,     192),   // vegetation,  dark-green
        9   to intArrayOf(37,   219,    188,    192),   // bike_path,   cyan
        10  to intArrayOf(255,  253,    208,    192),   // zebra,       beige
        11  to intArrayOf(204,  51,     102,    192)    // train,       claret
    )
}