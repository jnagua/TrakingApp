package com.example.tracking

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tracking.data.Storage
import com.example.tracking.data.model.OrdenesData
import com.example.tracking.databinding.CardOrdenesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class OrdenesAdapter (private var ordenes:List<OrdenesData>,private val ordenesOnClickListener:OrdenesOnClickListener,private var bandera:Boolean) : RecyclerView.Adapter<OrdenesAdapter.ViewHolder>(){

    private lateinit var mContext: Context

    interface OrdenesOnClickListener{
        fun onOrdenesClick(numeroOrden:String)
        fun onImprevistoClick(numeroOrden:String)
        fun onAceptClick(longitud :String,latitud :String,numeroOrden:String)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = CardOrdenesBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        val view = LayoutInflater.from(mContext).inflate(R.layout.card_ordenes,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orden = ordenes.get(position)
        with(holder){
            if(Storage.getNumeroOrden()==orden.numeroOrdem){
                binding.card.setBackgroundColor(Color.GREEN)
            }
            binding.idTextView.text = orden.numeroOrdem
            binding.idTextView1.text = orden.destino
            binding.editarImage.setOnClickListener {
                ordenesOnClickListener.onOrdenesClick(orden.numeroOrdem)
            }
            binding.eliminarImage.setOnClickListener {
                ordenesOnClickListener.onImprevistoClick(orden.numeroOrdem)
            }
            binding.card.setOnClickListener {
                //binding.card.setBackgroundColor(Color.GREEN)
                ordenesOnClickListener.onAceptClick(orden.destinoLong,orden.destinoLat,orden.numeroOrdem)
            }
            if(bandera){
                binding.editarImage.visibility=View.GONE
                    binding.eliminarImage.visibility=View.GONE
            }

        }


    }
    fun setOrdenes(ordenes: List<OrdenesData>) {
        this.ordenes = ordenes
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int = ordenes.size
}