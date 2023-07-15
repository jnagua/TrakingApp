package com.example.tracking.ui


import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.tracking.MainActivity
import com.example.tracking.R
import com.example.tracking.data.Storage
import com.example.tracking.data.model.LoginUser
import com.example.tracking.data.model.User
import com.example.tracking.databinding.FragmentLoginBinding
import com.example.tracking.ui.viewmodel.MainViewModel
import com.example.tracking.vo.Resource
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth



class FragmentLogin : Fragment() {
    private lateinit var mBinding:FragmentLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel:MainViewModel
    private lateinit var dialog: AlertDialog
    private lateinit var url:String
    private lateinit var url2:String




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel=ViewModelProvider(this.requireActivity()).get(MainViewModel::class.java)
        // ...

        dialog=  ( this.requireActivity() as MainActivity).dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentLoginBinding.inflate(inflater,container,false)
        return mBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservable()
        mBinding.btnPasar.setOnClickListener {
            if( mBinding.etUrl.text.toString().isEmpty()||  mBinding.etPassword.text.toString().isEmpty()){
                Snackbar.make(
                    mBinding.root, "Usuario o contraseña vacio",
                    BaseTransientBottomBar.LENGTH_SHORT
                ).show()
            }
            else{
                var loginUser=LoginUser( mBinding.etUrl.text.toString(),mBinding.etPassword.text.toString())
                viewModel.setLoginUser(loginUser)
            }


            }

        }


    private fun setUpObservable(){
        viewModel.getUser.observe(viewLifecycleOwner, Observer { result->
            when(result){
                is Resource.Success ->{
                    (this.activity as MainActivity).editor.putString("token",result.data.token)
                    (this.activity as MainActivity).editor.putString("id",(mBinding.etUrl.text.toString()))

                    viewModel.setUserRol(User(mBinding.etUrl.text.toString(),"Bearer ${result.data.token}"))
                }
                is Resource.Failure ->{
                    Snackbar.make(
                        mBinding.root, "Usuario o contraseña incorrecta",
                        BaseTransientBottomBar.LENGTH_LONG
                    ).show()
                    dialog.hide()
                }
                is Resource.Loading ->{
                    dialog.show()
                }

            }
        })
        viewModel.getUserRol.observe(viewLifecycleOwner,Observer{ result->
            when(result){
                is Resource.Success ->{
                    dialog.hide()
                    if(result.data.rol!="Drive" && result.data.rol!="Client"){

                        Snackbar.make(
                            mBinding.root, "Usuario con rol incorrecto",
                            BaseTransientBottomBar.LENGTH_LONG
                        ).show()
                    }


                    else{
                        (this.activity as MainActivity).editor.putString("rol",result.data.rol)
                        (this.activity as MainActivity).editor.putString("nombre","${result.data.nombre} ${result.data.apellido}")
                        if(result.data.rol=="Drive"){
                            (this.activity as MainActivity).editor.putString("placa","${result.data?.camion?.get(0)?.license}")
                            (this.activity as MainActivity).editor.putString("idTruck",result.data.camion.get(0)?.idTruck)
                        }
                        (this.activity as MainActivity).editor.apply()
                        findNavController().navigate(FragmentLoginDirections.actionFragmentLoginToFragmentMap())
                    }
                 }
                is Resource.Failure ->{
                    Snackbar.make(
                        mBinding.root, "Error desconocido",
                        BaseTransientBottomBar.LENGTH_LONG
                    ).show()

                }
                is Resource.Loading ->{
                    dialog.show()
                }
            }
        })
    }

    }

