package com.example.tracking.domin

import com.example.tracking.data.model.DataUpdate
import com.example.tracking.data.model.Entrega
import com.example.tracking.data.model.LocationUpdate
import com.example.tracking.data.model.LoginUser
import com.example.tracking.data.model.OrdenActiva
import com.example.tracking.data.model.Orders
import com.example.tracking.data.model.OrdersData
import com.example.tracking.data.model.ResponseEntrega
import com.example.tracking.data.model.RouteResponse
import com.example.tracking.data.model.Truck
import com.example.tracking.data.model.TruckUpdate
import com.example.tracking.data.model.Trunck
import com.example.tracking.data.model.User
import com.example.tracking.data.model.UserInformation
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface WebService {
    @POST("/heraldo/api/token/")
    suspend fun getUserToken(@Body loginUser: LoginUser) : User
    @POST("/heraldo/user_information")
    suspend fun getRol(@Header("Authorization") token: String,
               @Body loginUser: LoginUser
    ): UserInformation

    @POST("/heraldo/get_orders")
    suspend fun getOrdenes(@Header("Authorization") token: String,
                       @Body loginUser: LoginUser
    ): OrdersData

    @POST("/heraldo/get_active_order")
    suspend fun getActiveOrden(@Header("Authorization") token: String,
                           @Body loginUser: LoginUser
    ): OrdenActiva

    @POST("/heraldo/update_order")
    suspend fun updateOrden(@Header("Authorization") token: String,
                               @Body ordenStatus: DataUpdate
    ): OrdenActiva

    @GET("/v2/directions/driving-car")
    suspend fun getRoute121(@Query("api_key")key:String,
                         @Query("start", encoded = true)start:String,
                         @Query("end", encoded = true)end:String) : RouteResponse

    @POST("/heraldo/order_location")
    suspend fun updateLocation(@Header("Authorization") token: String,
                            @Body location: LocationUpdate
    ): Orders

    @POST("/heraldo/update_truck_location?truck_id")
    suspend fun updateTruck(@Header("Authorization") token: String,
                               @Body location: TruckUpdate
    ): Trunck

    @POST("/heraldo/confirmar_entrega")
    suspend fun confirmaEntrega(@Header("Authorization") token: String,
                            @Body entrega: Entrega
    ): ResponseEntrega

    @GET("/maps/api/directions/json")
    suspend fun getRoute(@Query("key",encoded = true) key:String,
                         @Query("origin", encoded = true) origin:String,
                         @Query("destination", encoded = true) destination: String
    ) : RouteResponse

    @POST("/heraldo/informar_imprevisto")
    suspend fun informarImprevisto(@Header("Authorization") token: String,
                                @Body entrega: Entrega
    ): ResponseEntrega
}