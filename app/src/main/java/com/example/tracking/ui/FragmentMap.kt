package com.example.tracking.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tracking.MainActivity
import com.example.tracking.R
import com.example.tracking.data.Storage
import com.example.tracking.data.model.LocationUpdate
import com.example.tracking.data.model.LoginUser
import com.example.tracking.data.model.Orden
import com.example.tracking.data.model.RouteResponse
import com.example.tracking.data.model.Ruta
import com.example.tracking.data.model.User
import com.example.tracking.databinding.FragmentMapBinding
import com.example.tracking.domin.RepoImpl
import com.example.tracking.ui.viewmodel.MapViewModel
import com.example.tracking.vo.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FragmentMap : Fragment(), OnMapReadyCallback {
    private lateinit var mBinding: FragmentMapBinding
    private lateinit var  mapView: MapView
    private lateinit var  mMap: GoogleMap
    private lateinit var viewModel: MapViewModel
    private var count=1
    private  var inico=""
    private  var fin=""
    private  var poly:Polyline?=null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val DEFAULT_ZOOM = 8f
    private lateinit var repo: RepoImpl
    private  val intervalo:Long=30000
    private  val final:Long=3600000
    lateinit var mContadorTimer :CountDownTimer



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel= ViewModelProvider(this.requireActivity()).get(MapViewModel::class.java)
        repo=(this.activity as MainActivity).repo


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentMapBinding.inflate(inflater,container,false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if( (this.activity as MainActivity).pref.getString("rol","") =="Drive"){
            setUpObservableDriver()
            isDriver(view,savedInstanceState)
        }else if((this.activity as MainActivity).pref.getString("rol","") =="Client"){
            setUpObservableClient()
            isClient(view,savedInstanceState)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mBinding.mapa.visibility=View.VISIBLE


        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-2.2058400,  -79.9079500)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))

        if(Storage.getLongitud()!="" && Storage.getLatitud()!=""){
            val marcador = LatLng(Storage.getLatitud().toDouble(),  Storage.getLongitud().toDouble())
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marcador,2f))
            googleMap.addMarker(MarkerOptions().position(marcador).title("lugar de destino"))
        }
        mMap = googleMap
        //getDeviceLocation()
        //if (ActivityCompat.checkSelfPermission(this.requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
        //        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.requireActivity(),
        //            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        //}
        //mMap.setMyLocationEnabled(true);
            //mMap.getUiSettings().setMyLocationButtonEnabled(false)

    }
    override fun onStart() {
        super.onStart()

        if ((this.activity as MainActivity).pref.getString("rol","")=="")
            findNavController().navigate(FragmentMapDirections.actionFragmentMapToFragmentLogin())

    }
    private fun setUpObservableDriver(){

    }
    private fun setUpObservableClient(){
        viewModel.getRuta.observe(viewLifecycleOwner, Observer { result->
            when(result){
                is Resource.Success ->{
                    if(result.data.status.uppercase()=="OK")
                        drawRoute(result.data)


                }
                is Resource.Failure ->{
                    Toast.makeText(this.requireContext(),"${result.exception}",
                        Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading ->{
                }

            }
        })
    }
    private fun setUpObservable(){

        count=1
        viewModel.getOrdenActiva.observe(viewLifecycleOwner, Observer { result->

                when(result){
                    is Resource.Success ->{
                        if(count==0){

                            if(  result.data.error==null){
                                Snackbar.make(
                                    mBinding.root, "Tiene una orden en Proceso",
                                    BaseTransientBottomBar.LENGTH_LONG
                                ).show()
                                Storage.setNumeroOrden("${result.data.orden.id}")
                                (this.activity as MainActivity).editor.putString("idOrden","${result.data.orden.id}")
                                (this.activity as MainActivity).idOrden=result.data.orden.id
                                Storage.setLatitud(result.data.orden.latitud)
                                Storage.setLongitud(result.data.orden.longitud)
                                (this.activity as MainActivity).getLoacation()
                                (this.activity as MainActivity).mCountDownTimer.start()
                                if(::mMap.isInitialized){
                                    val marcador = LatLng(result.data.orden.latitud.toDouble(),  result.data.orden.longitud.toDouble())
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marcador,DEFAULT_ZOOM))
                                    mMap.addMarker(MarkerOptions().position(marcador).title("lugar de destino")
                                    )
                                }
                            }
                        }

                    }
                    is Resource.Failure ->{
                        if(result.exception.message=="HTTP 403 Forbidden"){
                            (this.activity as MainActivity).deleteSharePrefences()
                            findNavController().navigate(FragmentMapDirections.actionFragmentMapToFragmentLogin())
                        }else{
                            if(count==0){
                                Log.d("Jamil","${result.exception.message}")

                                Toast.makeText(this.requireContext(),"${result.exception}",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    is Resource.Loading ->{
                    }
            }


        })
    }
    private fun drawRoute(routeResponse:RouteResponse){
        val polylineOptions=PolylineOptions()
        val points:ArrayList<LatLng> = ArrayList()

        routeResponse.routes.forEach {
            it.legs.forEach {
                it.steps.forEach {

                    var list = PolyUtil.decode(it.polyline.points)
                    list.forEach {
                        points.add(it)
                    }

                }
            }
        }
        polylineOptions.addAll(points)
        polylineOptions.width(10F)
        polylineOptions.color(this.requireContext().getColor(R.color.purple_200))
        polylineOptions.geodesic(true)
        poly=mMap.addPolyline(polylineOptions)
    }
    private fun getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
        try {

            val location: Task<*> = mFusedLocationProviderClient!!.getLastLocation()
            location.addOnCompleteListener { task ->
                //do something
                if (task.isSuccessful) {

                    val currentLocation: Location = task.result as Location
                    moveCamera(
                        LatLng(
                            currentLocation.getLatitude(),
                            currentLocation.getLongitude()
                        ),
                        DEFAULT_ZOOM
                    )
                } else {
                    Toast.makeText(
                        this.context,
                        "unable to get current location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


        } catch (e: SecurityException) {
            Log.e("TAG", "getDeviceLocation: SecurityException: " + e.message)
        }
    }
    private fun moveCamera(latLng: LatLng, zoom: Float) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }
    fun loadNavigationView(lat: String, lng: String) {
        val navigation = Uri.parse("google.navigation:q=$lat,$lng")
        val navigationIntent = Intent(Intent.ACTION_VIEW, navigation)
        navigationIntent.setPackage("com.google.android.apps.maps")
        startActivity(navigationIntent)
    }
    private fun isDriver(view: View, savedInstanceState: Bundle?){
        initMap(view,savedInstanceState)
        setUpObservable()
        hyrule()
        mBinding.btnCalculateRoute.setOnClickListener {
            if(::mMap.isInitialized){
                if(Storage.getLongitud()!="" && Storage.getLatitud()!=""){
                    loadNavigationView(Storage.getLatitud(), Storage.getLongitud())
                }
                else{
                    Snackbar.make(
                        mBinding.root, "Primero debe elegir una orden",
                        BaseTransientBottomBar.LENGTH_LONG
                    ).show()
                }

            }
        }

    }
    private fun isClient(view: View, savedInstanceState: Bundle?){
        initMap(view,savedInstanceState)
        mBinding.btnCalculateRoute.visibility=View.GONE
        mContadorTimer=object:CountDownTimer(final,intervalo){
            override fun onTick(p0: Long) {
                inicarSeguimiento()

            }

            override fun onFinish() {
                if ( Storage.getNumeroOrden()!="NoOrden"){
                    mContadorTimer.cancel()
                    mContadorTimer.start()
                }

            }
        }
        if ( Storage.getNumeroOrden()!="NoOrden"){
            mContadorTimer.start()
        }




    }

    private fun hyrule(){
        val fragment =this
        lifecycleScope.launch{
            var result= withContext(Dispatchers.IO){
                repo.getOrdenActiva("Bearer ${(fragment.activity as MainActivity).pref.getString("token","")!!}",
                    LoginUser((fragment.activity as MainActivity).pref.getString("id","")!!,""))
            }
            when(result){
                    is Resource.Success -> {
                        if(  result.data.error==null){
                            Snackbar.make(
                                mBinding.root, "Tiene una orden en Proceso",
                                BaseTransientBottomBar.LENGTH_LONG
                            ).show()
                            Storage.setNumeroOrden("${result.data.orden.id}")
                            (fragment.activity as MainActivity).editor.putString("idOrden","${result.data.orden.id}")
                            (fragment.activity as MainActivity).idOrden=result.data.orden.id
                            Storage.setLatitud(result.data.orden.latitud)
                            Storage.setLongitud(result.data.orden.longitud)
                            (fragment.activity as MainActivity).getLoacation()
                            (fragment.activity as MainActivity).mCountDownTimer.start()
                            if(::mMap.isInitialized){
                                val marcador = LatLng(result.data.orden.latitud.toDouble(),  result.data.orden.longitud.toDouble())
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marcador,DEFAULT_ZOOM))
                                mMap.addMarker(MarkerOptions().position(marcador).title("lugar de destino")
                                )
                            }
                        }

                    }
                    is Resource.Failure ->{
                        if(result.exception.message=="HTTP 403 Forbidden"){
                            (fragment.activity as MainActivity).deleteSharePrefences()
                            findNavController().navigate(FragmentMapDirections.actionFragmentMapToFragmentLogin())
                        }else{
                            if(count==0){
                                Log.d("Jamil","${result.exception.message}")

                                Toast.makeText(fragment.requireContext(),"${result.exception}",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    is Resource.Loading ->{
                    }
                }
            }

    }
    private fun inicarSeguimiento(){
        val fragment =this
        lifecycleScope.launch{
            var result= withContext(Dispatchers.IO){
                repo.getOrdenActiva("Bearer ${(fragment.activity as MainActivity).pref.getString("token","")!!}",
                    LoginUser((fragment.activity as MainActivity).pref.getString("id","")!!,""))
            }
            when(result){
                is Resource.Success ->{

                    if(  result.data.error==null){
                        Storage.setNumeroOrden("${result.data.orden.id}")
                        (fragment.activity as MainActivity).editor.putString("idOrden","${result.data.orden.id}")
                        Storage.setLatitud(result.data.orden.latitud)
                        Storage.setLongitud(result.data.orden.longitud)
                        Storage.setLatitudConductor(result.data.orden.latitudConductor)
                        Storage.setLongitudConductor(result.data.orden.longitudConductor)
                        inico="${result.data.orden.latitudConductor},${result.data.orden.longitudConductor}"
                        fin="${result.data.orden.latitud},${result.data.orden.longitud}"
                        var ruta= Ruta(inico,fin)
                        if(::mMap.isInitialized){
                            getRuta(ruta,result.data.orden,fragment.requireContext())
                        }

                    }
                    else if( result.data.error=="No hay pan"){
                        Storage.setNumeroOrden("NoOrden")
                    }

                }
                is Resource.Failure ->{
                    if(result.exception.message=="HTTP 403 Forbidden"){
                        (fragment.activity as MainActivity).deleteSharePrefences()
                        findNavController().navigate(FragmentMapDirections.actionFragmentMapToFragmentLogin())
                    }else{
                        if(count==0){
                            Log.d("Jamil","${result.exception.message}")

                            Toast.makeText(fragment.requireContext(),"${result.exception}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                is Resource.Loading ->{
                }
            }
        }
    }
    private fun getRuta(ruta: Ruta,orden: Orden,context: Context){
        lifecycleScope.launch{
            var dato= withContext(Dispatchers.IO){

                repo.getRoute(ruta.inico,ruta.fin)
            }
            when(dato) {
                is Resource.Success -> {
                    if(dato.data.status.uppercase()=="OK"){
                        mMap.clear()
                        mMap.addMarker(MarkerOptions().position(LatLng(orden.latitud.toDouble(),  orden.longitud.toDouble())).title("lugar de destino"))
                        mMap.addMarker(MarkerOptions().position(LatLng(orden.latitudConductor.toDouble(),
                            orden.longitudConductor.toDouble())).title("Conductor").icon(bitmapDescriptorFromVector(context,R.drawable.conductor)))
                        drawRoute(dato.data)

                    }


                }
                is Resource.Loading -> {

                }
                is Resource.Failure -> {

                }
            }


        }

    }
    private fun initMap(view: View, savedInstanceState: Bundle?){
        mapView = view.findViewById(com.example.tracking.R.id.mapa) as MapView
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this) //when you already implement OnMapReadyCallback in your fragment
    }
    private fun bitmapDescriptorFromVector(
        context: Context,
        @DrawableRes vectorResId: Int
    ): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}