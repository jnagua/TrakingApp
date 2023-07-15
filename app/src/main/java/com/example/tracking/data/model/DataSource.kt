package com.example.tracking.data.model

import com.example.tracking.BuildConfig.MAPS_API_KEY
import com.example.tracking.domin.DataSourceRepo
import com.example.tracking.vo.Resource
import com.example.tracking.vo.RetrofitClient

class DataSource(): DataSourceRepo {
    var retrofi= RetrofitClient()
    override suspend fun getUser(user:LoginUser):Resource<User>{


        return  Resource.Success(retrofi.webservice.getUserToken(user))
    }
     suspend fun getUser2():Resource<User>{
        var hola=User("Jaml","A")
        return Resource.Success(hola)
    }
    override suspend fun getRoute(inicio: String,fin: String):Resource<RouteResponse>{

        return  Resource.Success(retrofi.webservicerutas.getRoute("${MAPS_API_KEY}",inicio,fin))
    }

    override suspend fun getRol(token: String, username: LoginUser): Resource<UserInformation> {
        return  Resource.Success(retrofi.webservice.getRol(token,username))
    }

    override suspend fun getOrdenes(token: String, username: LoginUser): Resource<OrdersData> {
        return  Resource.Success(retrofi.webservice.getOrdenes(token,username))

    }

    override suspend fun getOrdenActiva(token: String, username: LoginUser): Resource<OrdenActiva> {
        return  Resource.Success(retrofi.webservice.getActiveOrden(token,username))
    }

    override suspend fun updateOrden(token: String, ordenUpdate: DataUpdate): Resource<OrdenActiva> {
        return  Resource.Success(retrofi.webservice.updateOrden(token,ordenUpdate))
    }

    override suspend fun updateLocation(token: String, location: LocationUpdate): Resource<Orders> {
        return  Resource.Success(retrofi.webservice.updateLocation(token,location))
    }

    override suspend fun updateTruck(token: String, location: TruckUpdate): Resource<Trunck> {
        return  Resource.Success(retrofi.webservice.updateTruck(token,location))
    }

    override suspend fun confirmaEntrega(token: String, entrega: Entrega): Resource<ResponseEntrega> {
        return  Resource.Success(retrofi.webservice.confirmaEntrega(token,entrega))
    }

    override suspend fun informarImprevisto(token: String, entrega: Entrega): Resource<ResponseEntrega> {
        return  Resource.Success(retrofi.webservice.informarImprevisto(token,entrega))

    }

}