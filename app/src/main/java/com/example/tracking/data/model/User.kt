package com.example.tracking.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val nombre:String="",
    @SerializedName("access")
    val token:String="")
