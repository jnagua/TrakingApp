package com.example.tracking.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracking.MainActivity
import com.example.tracking.OrdenesAdapter
import com.example.tracking.R
import com.example.tracking.data.Storage
import com.example.tracking.data.model.DataUpdate
import com.example.tracking.data.model.Entrega
import com.example.tracking.data.model.OrdenesData
import com.example.tracking.data.model.User
import com.example.tracking.databinding.FragmentNotificacionBinding
import com.example.tracking.domin.RepoImpl
import com.example.tracking.ui.viewmodel.NotificacionViewModel
import com.example.tracking.vo.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class NotificacionFragment : Fragment(), OrdenesAdapter.OrdenesOnClickListener {
    private lateinit var mBinding:FragmentNotificacionBinding
    private lateinit var ordenAdapter :OrdenesAdapter
    private lateinit var linearLayoutManager: RecyclerView.LayoutManager
    private lateinit var dialogView:View
    private lateinit var viewModel: NotificacionViewModel
    private lateinit var alertDialog: AlertDialog
    private lateinit var dialog: AlertDialog
    private lateinit var alertDialogImprevisto: AlertDialog
    private lateinit var dialogViewImprevisto: View
    private lateinit var repo: RepoImpl

    private var count=1



    override fun onStop() {
        super.onStop()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentNotificacionBinding.inflate(inflater,container,false)
        return mBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel= ViewModelProvider(this.requireActivity()).get(NotificacionViewModel::class.java)
        dialog=  ( this.requireActivity() as MainActivity).dialog
        repo=(this.activity as MainActivity).repo


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startRecyclerView()
        setUpObservable()
        viewModel.setOrdenes(User((this.activity as MainActivity).pref.getString("id","")!!,"Bearer ${(this.activity as MainActivity).pref.getString("token","")!!}"))
        mBinding.swipeLayoute.setOnRefreshListener{
            mBinding.swipeLayoute.isRefreshing=false
            viewModel.setOrdenes(User((this.activity as MainActivity).pref.getString("id","")!!,"Bearer ${(this.activity as MainActivity).pref.getString("token","")!!}"))
        }
        //var lista = ArrayList<OrdenesData>()
        //lista.add(OrdenesData("123","guayaqil"))
        //lista.add(OrdenesData("124","quito"))
        //ordenAdapter.setOrdenes(lista)
        //if(ordenAdapter.itemCount==0){
         //  mBinding.recyclerView.visibility=View.GONE
         //   mBinding.textNoOrdenes.visibility=View.VISIBLE
        //}else{
        //    mBinding.recyclerView.visibility=View.VISIBLE
        //    mBinding.textNoOrdenes.visibility=View.GONE
        //}

    }

    private fun startRecyclerView(){
        var bandera = false
        if((this.activity as MainActivity).pref.getString("rol","") =="Client")
            bandera=true
        ordenAdapter = OrdenesAdapter(mutableListOf(),this,bandera)
        linearLayoutManager = LinearLayoutManager(context)

        mBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = ordenAdapter
        }
    }

    interface OnClickListenerCamera {

        fun onClick(imageView: ImageView)

    }
    private fun createDialog(numeroOrden: String){
        dialogView = layoutInflater.inflate(R.layout.formulario_final,null)
             alertDialog= MaterialAlertDialogBuilder (requireContext()).setTitle("Confirmacion de entrega")
                .setView(dialogView)
                .setPositiveButton("enviar",{dialogInterface,i->
                    if(Storage.getNumeroOrden()==""){
                        Snackbar.make(
                            mBinding.root, "no tiene orden en proceso",
                            BaseTransientBottomBar.LENGTH_LONG
                        ).show()
                    }else if (Storage.getNumeroOrden()==numeroOrden){
                        count=0
                        viewModel.updateOrden( DataUpdate(numeroOrden,"FN","Bearer ${(this.activity as MainActivity).pref.getString("token","")!!}"))

                        enviarFormulario( dialogView)
                    }else{
                        Snackbar.make(
                            mBinding.root, "no es la orden que se encuentra en proceso",
                            BaseTransientBottomBar.LENGTH_LONG
                        ).show()
                    }
                    dialogInterface.dismiss()
                }).create()
        alertDialog.show()
        dialogView.findViewById<Button>(R.id.foto).setOnClickListener {
            (this.activity as MainActivity).onClick( dialogView.findViewById<ImageView>(R.id.imagen))
            dialogView.findViewById<ImageView>(R.id.imagen).visibility=View.VISIBLE
        }
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val current = LocalDateTime.now().format(formatter)
        dialogView.findViewById<EditText>(R.id.etUnidad).setText(current)
        dialogView.findViewById<EditText>(R.id.etName).setText((this.activity as MainActivity).pref.getString("nombre","")!!)

    }

    private fun createDialogInforme(numeroOrden: String){
        dialogViewImprevisto = layoutInflater.inflate(R.layout.formulario_inprevisto,null)
        alertDialogImprevisto= MaterialAlertDialogBuilder (requireContext()).setTitle("Informe Imprevisto")
            .setView(dialogViewImprevisto)
            .setPositiveButton("enviar",{dialogInterface,i->
                dialogInterface.dismiss()
            }).create()
        alertDialogImprevisto.show()
        val formatterFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
        val currentTime = LocalDateTime.now().format(formatterTime)
        val currentFecha = LocalDateTime.now().format(formatterFecha)
        dialogViewImprevisto.findViewById<EditText>(R.id.etIncidente).setText(currentTime)

        dialogViewImprevisto.findViewById<EditText>(R.id.etFechaIncidente).setText(currentFecha)

    }
    private fun setUpObservable(){
        count=1
        viewModel.getOrdenes.observe(viewLifecycleOwner, Observer { result->
            when(result){
                is Resource.Success ->{
                    dialog.dismiss()
                    var lista = ArrayList<OrdenesData>()
                   result.data.orders.forEach {
                       lista.add(OrdenesData(it.id,it.destination,it.destinoLongitud,it.destinoLatitud))
                   }
                    ordenAdapter.setOrdenes(lista)
                    if(ordenAdapter.itemCount==0){
                            mBinding.recyclerView.visibility=View.GONE
                            mBinding.textNoOrdenes.visibility=View.VISIBLE
                        }else{
                          mBinding.recyclerView.visibility=View.VISIBLE
                        mBinding.textNoOrdenes.visibility=View.GONE
                        }

                }
                is Resource.Failure ->{
                    dialog.dismiss()
                    Snackbar.make(
                      mBinding.root, "error en la base",
                        BaseTransientBottomBar.LENGTH_LONG
                    ).show()
                    mBinding.recyclerView.visibility=View.GONE
                    mBinding.textNoOrdenes.visibility=View.VISIBLE
                }
                is Resource.Loading ->{
                    dialog.show()
                }

            }
        })
        viewModel.getOrdenUpdate.observe(viewLifecycleOwner, Observer { result->
            when(result){
                is Resource.Success ->{
                    if(count==0){
                    if(result.data.orden.estado=="EP"){
                        findNavController().navigate(R.id.action_notificacionFragment_to_fragmentMap)
                    }
                    }

                }
                is Resource.Failure ->{
                    if(count==0){
                        Snackbar.make(
                            mBinding.root, "mal",
                            BaseTransientBottomBar.LENGTH_LONG
                        ).show()
                    }

                }
                is Resource.Loading ->{
                }

            }
        })

    }

    override fun onOrdenesClick(numeroOrden: String) {
        createDialog(numeroOrden)

    }

    override fun onImprevistoClick(numeroOrden: String) {
        createDialogInforme(numeroOrden)
    }

    override fun onAceptClick(longitud :String,latitud :String,numeroOrden:String) {
        if((this.activity as MainActivity).pref.getString("rol","") =="Drive"){
            var builder= MaterialAlertDialogBuilder (requireContext())
                .setTitle("Comenzar Recorrido")
                .setPositiveButton("Aceptar",{dialogInterface,i->
                    Storage.setLongitud(longitud)
                    Storage.setLatitud(latitud)
                    dialogInterface.dismiss()
                    count=0
                    viewModel.updateOrden( DataUpdate(numeroOrden,"EP","Bearer ${(this.activity as MainActivity).pref.getString("token","")!!}"))


                })
                .setNegativeButton("Rechazar",{dialogInterface,i->
                    dialogInterface.dismiss()
                })
                .create()
            builder.show()
        }

    }
    private fun enviarFormulario( dialogView: View){
        var imageView=dialogView.findViewById<ImageView>(R.id.imagen)
        val bitmap = Bitmap.createBitmap(
            imageView.width, imageView.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        imageView.draw(canvas)


        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()
        val strBase64: String = Base64.encodeToString(byteArray, 0)

        val fragment =this
        var entrega=Entrega("23:05:10","05/07/2023","Hola luis","","Orden #20","jpg",strBase64)
        Log.d("Jamil",strBase64)
        lifecycleScope.launch{
            var dato= withContext(Dispatchers.IO){

                repo.confirmaEntrega("Bearer ${(fragment.activity as MainActivity).pref.getString("token","")!!}",
                    entrega)
            }
            when(dato) {
                is Resource.Success -> {
                    Snackbar.make(
                        mBinding.root, "vamos bien",
                        BaseTransientBottomBar.LENGTH_LONG
                    ).show()
                }
                is Resource.Loading -> {

                }
                is Resource.Failure -> {

                }
            }


        }
    }
}
