package com.example.tracking.data.model

import com.google.gson.annotations.SerializedName

data class LocationUpdate(@SerializedName("order_id")
                          val orderid :String="",
                          @SerializedName("latitud")
                          val latitud:String="",
                          @SerializedName("longitud")
                          val longitud:String="")

data class TruckUpdate(@SerializedName("truck_id")
                          val truckId :String="",
                          @SerializedName("latitud")
                          val latitud:String="",
                          @SerializedName("longitud")
                          val longitud:String="")