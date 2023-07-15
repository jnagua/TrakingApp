package com.example.tracking.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.example.tracking.data.model.DataSource
import com.example.tracking.data.model.LoginUser
import com.example.tracking.data.model.Ruta
import com.example.tracking.data.model.User
import com.example.tracking.domin.RepoImpl
import com.example.tracking.vo.Resource
import kotlinx.coroutines.Dispatchers

class MapViewModel  (): ViewModel() {
    val repo= RepoImpl(DataSource())
    private val ruta = MutableLiveData<Ruta>()
    private val usario = MutableLiveData<User>()

    fun setRuta(rutas: Ruta){
        ruta.value=rutas
    }
    fun setUser(user: User){
        usario.value=user
    }
    val getRuta=ruta.distinctUntilChanged().switchMap{rutas ->
        liveData(Dispatchers.IO){
            emit(Resource.Loading())
            try {
                emit(repo.getRoute(rutas.inico,rutas.fin))
            }catch (e: Exception){
                emit(Resource.Failure(e))
            }
        }
    }

    val getOrdenActiva=usario.distinctUntilChanged().switchMap{user ->
        liveData(Dispatchers.IO){
            emit(Resource.Loading())
            try {

                emit(repo.getOrdenActiva(user.token, LoginUser(user.nombre,"")))
            }catch (e: Exception){
                emit(Resource.Failure(e))
            }
        }
    }
}