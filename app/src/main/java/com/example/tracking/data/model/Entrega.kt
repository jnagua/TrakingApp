package com.example.tracking.data.model

import com.google.gson.annotations.SerializedName

data class Entrega( @SerializedName("hora")
                    val hora:String="",
                    @SerializedName("fecha")
                    val fecha:String="",
                    @SerializedName("observacion")
                    val observacion:String="",
                    @SerializedName("order_id")
                    val orderId:String="",
                    @SerializedName("nombre_imagen")
                    val nombreImagen:String="",
                    @SerializedName("tipo_imagen")
                    val TipoImage:String="",
                    @SerializedName("foto_bytes_base64")
                    val foto:String=""
)
data class ResponseEntrega( @SerializedName("error")
                    val error:String="",
                    @SerializedName("msj")
                    val msj:String=""
)






