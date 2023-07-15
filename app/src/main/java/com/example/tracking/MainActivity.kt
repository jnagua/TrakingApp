package com.example.tracking

//import android.R

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tracking.data.model.DataSource
import com.example.tracking.data.model.LocationUpdate
import com.example.tracking.data.model.TruckUpdate
import com.example.tracking.databinding.ActivityMainBinding
import com.example.tracking.domin.RepoImpl
import com.example.tracking.ui.NotificacionFragment
import com.example.tracking.vo.Resource
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream


class MainActivity : AppCompatActivity(), NotificacionFragment.OnClickListenerCamera {
    private val CAMERA_PERMISSION_CODE=1
    private lateinit var mBinding : ActivityMainBinding
    private lateinit var navController : NavController
    private lateinit var appBarConfiguration:AppBarConfiguration
    var idOrden=0L
    private  var myLongitud:Double=0.0
    private  var myLatitud:Double=0.0
    private  val intervalo:Long=30000
    private  val final:Long=3600000
    lateinit var  mybutton: Button
    lateinit var  imageView: ImageView
    lateinit var image_uri: Uri
    lateinit var dialog: AlertDialog
    lateinit var pref:SharedPreferences
    lateinit var editor:Editor
    lateinit var mCountDownTimer :CountDownTimer
    private  var tiempoContador=final
    val repo= RepoImpl(DataSource())
    private var currentLocation: Location? = null
    lateinit var locationManager: LocationManager
    lateinit var locationByGps:Location
    lateinit var locationByNetwork: Location








    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        getSharePreferences()
        setupBottomNav()
        setProgressDialog()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED || checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                == PackageManager.PERMISSION_DENIED
            ) {
                val permission =
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                requestPermissions(permission, 110)
            }


        }
        mCountDownTimer=object:CountDownTimer(final,intervalo){
            override fun onTick(p0: Long) {
                if(myLatitud!=0.0){
                    lifecycleScope.launch{
                        var dato= withContext(Dispatchers.IO){

                            repo.updateLocation("Bearer ${pref.getString("token","")}", LocationUpdate(
                                "$idOrden","$myLatitud","$myLongitud"
                            ))


                        }
                        when(dato) {
                            is Resource.Success -> {
                                Snackbar.make(
                                    mBinding.root, "Posicion Encontrada",
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

            override fun onFinish() {
                onRestartContador()
            }
        }

        if(myLatitud!=0.0 && pref.getString("idTruck","")!=""){
            lifecycleScope.launch{
                var dato= withContext(Dispatchers.IO){

                    repo.updateTruck("Bearer ${pref.getString("token","")}", TruckUpdate(
                        "${pref.getString("idTruck","")}","$myLatitud","$myLongitud"
                    )
                    )


                }
                when(dato) {
                    is Resource.Success -> {

                    }
                    is Resource.Loading -> {

                    }
                    is Resource.Failure -> {

                    }
                }

            }
        }

    }

    fun onRestartContador(){
        mCountDownTimer.cancel()
        mCountDownTimer.start()
    }
    override fun onResume() {
        super.onResume()
        getSharePreferences()
    }
    private fun getSharePreferences(){
        pref=this.getSharedPreferences("myPref",0)
        editor=pref.edit()
    }
     fun deleteSharePrefences(){
        pref=this.getSharedPreferences("myPref",0)
        pref.edit().clear().apply()
    }
private fun setupBottomNav(){
    val navView: BottomNavigationView = mBinding.bottomNav


    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
    navController = navHostFragment!!.navController
    //navController = findNavController(R.id.nav_host_fragment)
    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.
    //appBarConfiguration=AppBarConfiguration.Builder(navController.graph).build();
    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.
     appBarConfiguration = AppBarConfiguration(
        setOf(
            R.id.fragmentLogin,R.id.fragmentMap, R.id.reporteFragment,
            R.id.notificacionFragment
        )
     )
    setupActionBarWithNavController(navController, appBarConfiguration)
    navView.setupWithNavController(navController)
    setBottomNavVisibility()
    getSupportActionBar()?.hide()
}
private fun setBottomNavVisibility(){
    navController.addOnDestinationChangedListener { controller, destination, arguments ->
        when(destination.id){
            R.id.fragmentLogin -> hideBottonNav()
            else -> showBottonNav()
        }
    }
}

private fun showBottonNav(){
    mBinding.bottomNav.visibility = View.VISIBLE
}
private fun hideBottonNav(){
    mBinding.bottomNav.visibility = View.GONE
}

override fun onSupportNavigateUp(): Boolean {
    return navController.navigateUp(appBarConfiguration)
}
    //TODO opens camera so that user can capture image
    private fun openCamera(imageView2: ImageView) {
        imageView=imageView2
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        cameraActivityResultLauncher.launch(cameraIntent)

    }
    var cameraActivityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            //var myBitmap = BitmapFactory.decodeFile(image_uri.path)

            imageView.setImageURI(image_uri);
        }
    }

    //TODO capture the image using camera and display it


    override fun onClick(imageView: ImageView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    == PackageManager.PERMISSION_DENIED
                ) {
                    val permission = arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    requestPermissions(permission, 112)
                } else {
                    openCamera(imageView)
                }
            } else {
                openCamera(imageView)
            }

    }
    fun setProgressDialog() {

        // Creating a Linear Layout
        val llPadding = 30
        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        // Creating a ProgressBar inside the layout
        val progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam
        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER

        // Creating a TextView inside the layout
        val tvText = TextView(this)
        tvText.text = "Loading ..."
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20f
        tvText.layoutParams = llParam
        ll.addView(progressBar)
        ll.addView(tvText)

        // Setting the AlertDialog Builder view
        // as the Linear layout created above
        val builder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        builder.setCancelable(false)
        builder.setView(ll)

        // Displaying the dialog
        dialog= builder.create()
    }

    fun getLoacation(){
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//------------------------------------------------------//
        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        val gpsLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                myLatitud=location.latitude
                myLongitud=location.longitude
                locationByGps= location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
//------------------------------------------------------//
        val networkLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationByNetwork= location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        if (hasGps) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                0F,
                gpsLocationListener
            )
        }
//------------------------------------------------------//
        if (hasNetwork) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000,
                0F,
                networkLocationListener
            )
        }
        val lastKnownLocationByGps =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lastKnownLocationByGps?.let {
            locationByGps = lastKnownLocationByGps
        }
//------------------------------------------------------//
        val lastKnownLocationByNetwork =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        lastKnownLocationByNetwork?.let {
            locationByNetwork = lastKnownLocationByNetwork
        }
//------------------------------------------------------//
        if (locationByGps != null && locationByNetwork != null) {
            if (locationByGps.accuracy > locationByNetwork!!.accuracy) {
                currentLocation = locationByGps

                myLatitud = currentLocation!!.latitude
                myLongitud = currentLocation!!.longitude
                // use latitude and longitude as per your need
            } else {
                currentLocation = locationByNetwork
                myLatitud = currentLocation!!.latitude
                myLongitud = currentLocation!!.longitude
                // use latitude and longitude as per your need
            }
        }
}
}