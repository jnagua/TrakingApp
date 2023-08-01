package com.example.tracking.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
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
import com.example.tracking.data.model.LoginUser
import com.example.tracking.data.model.OrdenesData
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
    private lateinit var mBinding: FragmentNotificacionBinding
    private lateinit var ordenAdapter: OrdenesAdapter
    private lateinit var linearLayoutManager: RecyclerView.LayoutManager
    private lateinit var dialogView: View
    private lateinit var viewModel: NotificacionViewModel
    private lateinit var alertDialog: AlertDialog
    private lateinit var dialog: AlertDialog
    private lateinit var alertDialogImprevisto: AlertDialog
    private lateinit var dialogViewImprevisto: View
    private lateinit var repo: RepoImpl

    private var count = 1


    override fun onStop() {
        super.onStop()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentNotificacionBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this.requireActivity()).get(NotificacionViewModel::class.java)
        dialog = (this.requireActivity() as MainActivity).dialog
        repo = (this.activity as MainActivity).repo


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startRecyclerView()
        setUpObservable()
        zelda()
        mBinding.swipeLayoute.setOnRefreshListener {
            count = 0
            mBinding.swipeLayoute.isRefreshing = false
            zelda()
        }


    }

    private fun startRecyclerView() {
        var bandera = false
        if ((this.activity as MainActivity).pref.getString("rol", "") == "Client")
            bandera = true
        ordenAdapter = OrdenesAdapter(mutableListOf(), this, bandera)
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

    private fun createDialog(numeroOrden: String,estado:String) {
        dialogView = layoutInflater.inflate(R.layout.formulario_final, null)
        alertDialog =
            MaterialAlertDialogBuilder(requireContext()).setTitle("Confirmacion de entrega")
                .setView(dialogView)
                .setPositiveButton("enviar", { dialogInterface, i ->
                    if (estado =="EP") {
                        updateOrden(numeroOrden,"FN")
                        enviarFormulario(dialogView,numeroOrden,false)
                    } else {
                        Snackbar.make(
                            mBinding.root, "no es la orden que se encuentra en proceso",
                            BaseTransientBottomBar.LENGTH_LONG
                        ).show()
                    }
                    dialogInterface.dismiss()
                }).create()
        alertDialog.show()
        dialogView.findViewById<Button>(R.id.foto).setOnClickListener {
            (this.activity as MainActivity).onClick(dialogView.findViewById<ImageView>(R.id.imagen))
            dialogView.findViewById<ImageView>(R.id.imagen).visibility = View.VISIBLE
        }
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val current = LocalDateTime.now().format(formatter)
        dialogView.findViewById<EditText>(R.id.etUnidad).setText(current)
        dialogView.findViewById<EditText>(R.id.etName)
            .setText((this.activity as MainActivity).pref.getString("nombre", "")!!)

    }

    private fun createDialogInforme(numeroOrden: String) {
        dialogViewImprevisto = layoutInflater.inflate(R.layout.formulario_inprevisto, null)
        alertDialogImprevisto =
            MaterialAlertDialogBuilder(requireContext()).setTitle("Informe Imprevisto")
                .setView(dialogViewImprevisto)
                .setPositiveButton("enviar", { dialogInterface, i ->
                    updateOrden(numeroOrden,"CN")
                    enviarFormulario(dialogViewImprevisto,numeroOrden,false)
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

    private fun setUpObservable() {
        count = 1
        viewModel.getOrdenes.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Resource.Success -> {
                    dialog.dismiss()
                    var lista = ArrayList<OrdenesData>()
                    result.data.orders.forEach {

                        lista.add(
                            OrdenesData(
                                it.id,
                                it.destination,
                                it.destinoLongitud,
                                it.destinoLatitud,
                                it.client.name,
                                it.content,
                                it.peso,
                                it.files,
                                it.status
                            )
                        )
                    }
                    ordenAdapter.setOrdenes(lista)
                    if (ordenAdapter.itemCount == 0) {
                        mBinding.recyclerView.visibility = View.GONE
                        mBinding.textNoOrdenes.visibility = View.VISIBLE
                    } else {
                        mBinding.recyclerView.visibility = View.VISIBLE
                        mBinding.textNoOrdenes.visibility = View.GONE
                    }

                }

                is Resource.Failure -> {
                    dialog.dismiss()
                    Snackbar.make(
                        mBinding.root, "error en la base",
                        BaseTransientBottomBar.LENGTH_LONG
                    ).show()
                    mBinding.recyclerView.visibility = View.GONE
                    mBinding.textNoOrdenes.visibility = View.VISIBLE
                }

                is Resource.Loading -> {
                    dialog.show()
                }

            }
        })
        viewModel.getOrdenUpdate.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Resource.Success -> {
                    if (count == 0) {
                        if (result.data.orden.estado == "EP") {
                            val action =
                                NotificacionFragmentDirections.actionNotificacionFragmentToFragmentMap(
                                    true
                                )
                            findNavController().navigate(action)
                        }
                    }

                }

                is Resource.Failure -> {
                    if (count == 0) {
                        Snackbar.make(
                            mBinding.root, "mal",
                            BaseTransientBottomBar.LENGTH_LONG
                        ).show()
                    }

                }

                is Resource.Loading -> {
                }

            }
        })

    }

    override fun onOrdenesClick(numeroOrden: String,estado:String) {
        createDialog(numeroOrden,estado)

    }

    override fun onImprevistoClick(numeroOrden: String) {
        createDialogInforme(numeroOrden)
    }

    override fun onAceptClick(longitud: String, latitud: String, numeroOrden: String,enProceso:String) {
        if(enProceso==""){
            if ((this.activity as MainActivity).pref.getString("rol", "") == "Drive") {
                var builder = MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Comenzar Recorrido")
                    .setPositiveButton("Aceptar", { dialogInterface, i ->
                        Storage.setLongitud(longitud)
                        Storage.setLatitud(latitud)
                        dialogInterface.dismiss()
                        updateOrden(numeroOrden,"EP")
                    })
                    .setNegativeButton("Rechazar", { dialogInterface, i ->
                        dialogInterface.dismiss()
                    })
                    .create()
                builder.show()
            }
        }else{
            Snackbar.make(
                mBinding.root, "Ya tiene una orden en proceso",
                BaseTransientBottomBar.LENGTH_LONG
            ).show()
        }


    }



    private fun enviarFormulario(dialogView: View,numeroOrden:String,imprevisto:Boolean) {
        var entrega:Entrega
        var observacion:String
        val fragment = this
        var strBase64=""
        if(imprevisto){
             observacion = dialogView.findViewById<EditText>(R.id.teObservar).text.toString()

        }else{
            var imageView = dialogView.findViewById<ImageView>(R.id.imagen)
            if(imageView.getDrawable() != null){
                val bitmap = Bitmap.createBitmap(
                    imageView.width, imageView.height, Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                imageView.draw(canvas)


                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val byteArray = stream.toByteArray()
                strBase64= Base64.encodeToString(byteArray, 0)
            }



            observacion = dialogView.findViewById<EditText>(R.id.textObservar).text.toString()




        }
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formatter1 = DateTimeFormatter.ofPattern("HH:mm")

        val fecha = LocalDateTime.now().format(formatter)
        val hora = LocalDateTime.now().format(formatter1)
        entrega =
            Entrega(hora, fecha, observacion, numeroOrden, "Orden #${numeroOrden}", "jpg", strBase64)

        lifecycleScope.launch {
            var dato = withContext(Dispatchers.IO) {

                repo.confirmaEntrega(
                    "Bearer ${(fragment.activity as MainActivity).pref.getString("token", "")!!}",
                    entrega
                )
            }
            when (dato) {
                is Resource.Success -> {
                    Snackbar.make(
                        mBinding.root, "Se envio el reporte",
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

    private fun zelda() {
        val fragment = this
        lifecycleScope.launch {
            var result = withContext(Dispatchers.IO) {
                repo.getOrdenes(
                    "Bearer ${
                        (fragment.activity as MainActivity).pref.getString(
                            "token",
                            ""
                        )!!
                    }",
                    LoginUser((fragment.activity as MainActivity).pref.getString("id", "")!!, "")
                )
            }
            when (result) {
                is Resource.Success -> {
                    dialog.dismiss()
                    var lista = ArrayList<OrdenesData>()
                    result.data.orders.forEach {
                        lista.add(
                            OrdenesData(
                                it.id,
                                it.destination,
                                it.destinoLongitud,
                                it.destinoLatitud,
                                it.client.name,
                                it.content,
                                it.peso,
                                it.files,
                                it.status
                            )
                        )
                    }
                    ordenAdapter.setOrdenes(lista)
                    if (ordenAdapter.itemCount == 0) {
                        mBinding.recyclerView.visibility = View.GONE
                        mBinding.textNoOrdenes.visibility = View.VISIBLE
                    } else {
                        mBinding.recyclerView.visibility = View.VISIBLE
                        mBinding.textNoOrdenes.visibility = View.GONE
                    }
                }

                is Resource.Failure -> {
                    dialog.dismiss()
                    Snackbar.make(
                        mBinding.root, "error en la base",
                        BaseTransientBottomBar.LENGTH_LONG
                    ).show()
                    mBinding.recyclerView.visibility = View.GONE
                    mBinding.textNoOrdenes.visibility = View.VISIBLE
                }

                is Resource.Loading -> {
                    dialog.show()
                }
            }
        }
    }

    private fun updateOrden(numeroOrden: String,status:String){
        if(status=="EP"){
            zelda()
        }
        val fragment = this
        lifecycleScope.launch {
            var result = withContext(Dispatchers.IO) {
                repo.updateOrden("Bearer ${(fragment.activity as MainActivity).pref.getString("token", "")!!}",DataUpdate(numeroOrden,status))
            }
            when (result) {
                is Resource.Success -> {
                        val action =
                            NotificacionFragmentDirections.actionNotificacionFragmentToFragmentMap(
                                true
                            )
                        findNavController().navigate(action)
                }

                is Resource.Failure -> {
                    Snackbar.make(
                        mBinding.root, "mal",
                        BaseTransientBottomBar.LENGTH_LONG
                    ).show()
                }

                is Resource.Loading -> {
                }
            }
        }

    }
    override fun onPdf(pdf: String) {
        if(!"".equals(pdf)){
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(pdf))
            startActivity(browserIntent)
        }
           }
}
