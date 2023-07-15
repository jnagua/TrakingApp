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
import com.example.tracking.vo.Resource

interface Repo {
    suspend fun getUser(user:LoginUser): Resource<User>
    suspend fun getRoute(inicio: String,fin: String):Resource<RouteResponse>
    suspend fun getRol(token: String,username: LoginUser): Resource<UserInformation>
    suspend fun getOrdenes(token: String,username: LoginUser): Resource<OrdersData>
    suspend fun getOrdenActiva(token: String,username: LoginUser): Resource<OrdenActiva>
    suspend fun updateOrden(token: String,datosUpdate: DataUpdate): Resource<OrdenActiva>
    suspend fun updateLocation(token: String,location: LocationUpdate): Resource<Orders>
    suspend fun updateTruck(token: String,location: TruckUpdate): Resource<Trunck>
    suspend fun confirmaEntrega(token: String,entrega: Entrega): Resource<ResponseEntrega>
    suspend fun informarImprevisto(token: String,entrega: Entrega): Resource<ResponseEntrega>




}