package com.example.tracking.data.model

import com.google.gson.annotations.SerializedName

data class DataUpdate( @SerializedName("order_id")
                       val id :String="",
                       @SerializedName("status")
                       val status:String="",
                       val token:String="")
