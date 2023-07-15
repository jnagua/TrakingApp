package com.example.tracking.ui.viewmodel

import androidx.lifecycle.*
import com.example.tracking.data.model.DataSource
import com.example.tracking.data.model.LoginUser
import com.example.tracking.data.model.User
import com.example.tracking.domin.RepoImpl
import com.example.tracking.vo.Resource
import kotlinx.coroutines.Dispatchers

class MainViewModel ():ViewModel() {
     val repo= RepoImpl(DataSource())
    private val loginUser = MutableLiveData<LoginUser>()
    private val userRol = MutableLiveData<User>()


    fun setLoginUser(user:LoginUser){
        loginUser.value=user
    }
    fun setUserRol(user:User){
        userRol.value=user
    }

    val getUser=loginUser.switchMap{user ->
        liveData(Dispatchers.IO){
        emit(Resource.Loading())
        try {
            emit(repo.getUser(user!!))
        }catch (e: Exception){
            emit(Resource.Failure(e))
        }
    }
    }
    val getUserRol=userRol.switchMap{user ->
        liveData(Dispatchers.IO){
            emit(Resource.Loading())
            try {

                emit(repo.getRol(user.token, LoginUser(user.nombre,"")))
            }catch (e: Exception){
                emit(Resource.Failure(e))
            }
        }
    }

}