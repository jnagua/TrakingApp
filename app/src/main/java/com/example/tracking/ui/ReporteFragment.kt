package com.example.tracking.ui

import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tracking.MainActivity
import com.example.tracking.data.Storage
import com.example.tracking.databinding.FragmentReporteBinding

class ReporteFragment : Fragment() {

    private lateinit var mBinding: FragmentReporteBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentReporteBinding.inflate(inflater,container,false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if((this.activity as MainActivity).pref.getString("rol","")=="Drive"){
            mBinding.tvName.text=(this.activity as MainActivity).pref.getString("nombre","")
            mBinding.tvRol.text="Conductor"
            mBinding.tvLicencia.text="Placa: ${(this.activity as MainActivity).pref.getString("placa","")}"
        }else if((this.activity as MainActivity).pref.getString("rol","")=="Client"){
            mBinding.tvName.text=(this.activity as MainActivity).pref.getString("nombre","")
            mBinding.tvRol.text="Cliente"
            mBinding.tvLicencia.visibility=View.GONE
        }

        else{
            mBinding.tvLicencia.visibility=View.GONE
        }
        mBinding.btnCerrar.setOnClickListener {
            (this.activity as MainActivity).deleteSharePrefences()
            (this.activity as MainActivity).finish()
        }

    }


}