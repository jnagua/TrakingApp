package com.example.tracking.data.model

import com.google.gson.annotations.SerializedName

data class UserInformation(
    @SerializedName("username")
    val username:String="",
    @SerializedName("rol")
    val rol:String="",
    @SerializedName("first_name")
    val nombre:String="",
    @SerializedName("last_name")
    val apellido:String="",
    @SerializedName("trucks")
    val camion:List<Trunck>)


data class Trunck(
    @SerializedName("license")
    val license:String="",
    @SerializedName("id")
    val idTruck:String="")

