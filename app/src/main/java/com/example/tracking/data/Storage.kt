package com.example.tracking.data

object Storage {

    private var numeroOrden=""
    private var longitud=""
    private var latitud=""

    private var longitudConductor=""
    private var latitudConductor=""

    fun setNumeroOrden(numeroOrden:String){
        this.numeroOrden=numeroOrden
    }
    fun getNumeroOrden():String{
        return numeroOrden
    }

    fun setLongitud(longitud:String){
        this.longitud=longitud
    }
    fun getLongitud():String{
        return this.longitud
    }

    fun setLatitud(latitud:String){
        this.latitud=latitud
    }
    fun getLatitud():String{
        return this.latitud
    }

    fun setLongitudConductor(longitud:String){
        this.longitudConductor=longitud
    }
    fun getLongitudConductor():String{
        return this.longitudConductor
    }

    fun setLatitudConductor(latitud:String){
        this.latitudConductor=latitud
    }
    fun getLatitudConductor():String{
        return this.latitudConductor
    }
}