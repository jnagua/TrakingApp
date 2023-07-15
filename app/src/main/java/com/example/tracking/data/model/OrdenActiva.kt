package com.example.tracking.data.model

import com.google.gson.annotations.SerializedName

data class OrdenActiva(
    @SerializedName("active_order")
    val orden:Orden,
    @SerializedName("error_message")
    val error:String=""
)


data class Orden(
    @SerializedName("id")
    val id:Long,
    @SerializedName("destination_coord_lat")
    val latitud:String,
    @SerializedName("destination_coord_long")
    val longitud:String,
    @SerializedName("status")
    val estado:String,
    @SerializedName("location_coord_lat")
    val latitudConductor:String,
    @SerializedName("location_coord_long")
    val longitudConductor:String
    )

