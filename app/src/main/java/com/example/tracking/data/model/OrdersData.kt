package com.example.tracking.data.model

import com.google.gson.annotations.SerializedName

data class OrdersData(
    @SerializedName("orders")
    val orders:List<Orders>)
data class Orders(
    @SerializedName("id")
    val id:String,
    @SerializedName("client")
    val client:Client,
    @SerializedName("truck")
    val truck:Truck,
    @SerializedName("content")
    val content:String,
    @SerializedName("origin")
    val origin:String,
    @SerializedName("origin_coord_lat")
    val originLatitud:String,
    @SerializedName("origin_coord_long")
    val originLongitud:String,
    @SerializedName("destination")
    val destination:String,
    @SerializedName("status")
    val status:String,
    @SerializedName("destination_coord_lat")
    val destinoLatitud:String,
    @SerializedName("destination_coord_long")
    val destinoLongitud:String,
    @SerializedName("is_active")
    val isActivo:Boolean
    )
data class Client(
    @SerializedName("identification")
    val identification:String,
    @SerializedName("full_name")
    val name:String)

data class Truck(
    @SerializedName("license")
    val license:String,
    @SerializedName("capacity")
    val capacity:String,
    @SerializedName("measurement")
    val measurement:String,
    @SerializedName("color")
    val color:String,
    @SerializedName("brand")
    val brand:String,
    @SerializedName("year")
    val year:String
)