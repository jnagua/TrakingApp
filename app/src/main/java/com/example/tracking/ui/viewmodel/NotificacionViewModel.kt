package com.example.tracking.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.example.tracking.data.model.DataSource
import com.example.tracking.data.model.DataUpdate
import com.example.tracking.data.model.LoginUser
import com.example.tracking.data.model.OrdenActiva
import com.example.tracking.data.model.User
import com.example.tracking.domin.RepoImpl
import com.example.tracking.vo.Resource
import kotlinx.coroutines.Dispatchers

class NotificacionViewModel: ViewModel() {
    val repo= RepoImpl(DataSource())
    private val ordenes = MutableLiveData<User>()
    private val ordenUpdate = MutableLiveData<DataUpdate>()
    fun updateOrden(orden: DataUpdate){
        ordenUpdate.value=orden
    }
    fun setOrdenes(user:User){
        ordenes.value=user
    }
    val getOrdenes=ordenes.switchMap{user ->
            liveData(Dispatchers.IO){
                emit(Resource.Loading())
                try {

                    emit(repo.getOrdenes(user.token, LoginUser(user.nombre,"")))
                }catch (e: Exception){
                    emit(Resource.Failure(e))
                }
            }

    }
    var getOrdenUpdate=ordenUpdate.distinctUntilChanged().switchMap{update ->
        liveData(Dispatchers.IO){
            emit(Resource.Loading())
            try {
                emit(repo.updateOrden(update.token,update))
            }catch (e: Exception){
                emit(Resource.Failure(e))
            }
        }
    }

}