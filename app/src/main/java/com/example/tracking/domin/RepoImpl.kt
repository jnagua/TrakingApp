package com.example.tracking.domin

import com.example.tracking.data.model.DataSource
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
import javax.inject.Inject

class RepoImpl (private val dataSource: DataSourceRepo):Repo {
    override suspend fun getUser(user:LoginUser): Resource<User> {
        return dataSource.getUser(user)


    }
    override suspend fun getRoute(inicio: String,fin: String):Resource<RouteResponse>{
        return dataSource.getRoute(inicio,fin)
    }

    override suspend fun getRol(token: String, username: LoginUser): Resource<UserInformation> {
        return dataSource.getRol(token,username)
    }

    override suspend fun getOrdenes(token: String, username: LoginUser): Resource<OrdersData> {
        return dataSource.getOrdenes(token,username)
    }

    override suspend fun getOrdenActiva(token: String, username: LoginUser): Resource<OrdenActiva> {
        return dataSource.getOrdenActiva(token,username)
    }

    override suspend fun updateOrden(
        token: String,
        datosUpdate: DataUpdate
    ): Resource<OrdenActiva> {
        return dataSource.updateOrden(token,datosUpdate)
    }

    override suspend fun updateLocation(token: String, location: LocationUpdate): Resource<Orders> {
        return dataSource.updateLocation(token,location)    }

    override suspend fun updateTruck(token: String, location: TruckUpdate): Resource<Trunck> {
        return dataSource.updateTruck(token,location)
    }

    override suspend fun confirmaEntrega(token: String, entrega: Entrega): Resource<ResponseEntrega> {
        return dataSource.confirmaEntrega(token,entrega)
    }

    override suspend fun informarImprevisto(token: String, entrega: Entrega): Resource<ResponseEntrega> {
        return dataSource.informarImprevisto(token,entrega)
    }


}