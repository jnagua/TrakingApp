package com.example.tracking.data.model

import com.google.gson.annotations.SerializedName

data class RouteResponse(
    @SerializedName("routes")
    val routes:List<Routes>,
    @SerializedName("status")
    val status:String
)
data class Routes(
    @SerializedName("legs")
    val legs:List<Legs>

)
data class Legs(
    @SerializedName("steps")
    val steps:List<Steps>

)
data class Steps(
    @SerializedName("polyline")
    val polyline: Points

)
data class Points(
    @SerializedName("points")
    val points: String

)

