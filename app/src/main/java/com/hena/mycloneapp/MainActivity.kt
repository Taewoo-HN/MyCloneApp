package com.hena.mycloneapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager

import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.hena.mycloneapp.databinding.ActivityMainBinding
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var locationProvider: LocationProvider
    lateinit var getGPSPermissionLaucher: ActivityResultLauncher<Intent>
    private val PERMISSION_REQUEST_CODE = 100

    val REQUST_PERMISSION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAllPermission()

        updateUI()
    }

    private fun checkAllPermission() {
        if (!isLocationServiceAvailable()) {
            showDialogForLocationServingSetting()
        } else {
            isRuntimePermissionsGranted()
        }
    }

    private fun isLocationServiceAvailable(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        ))
    }

    private fun isRuntimePermissionsGranted() {
        val hasFineLcoationPermission =
            ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        val hasCorseLcoationPermission =
            ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        if (hasFineLcoationPermission != PackageManager.PERMISSION_GRANTED || hasCorseLcoationPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                REQUST_PERMISSION,
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.size == REQUST_PERMISSION.size) {
            var checkResult = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false
                    break
                }
                if (checkResult) {
                    // 위치를 가져올 수 있음

                } else {
                    Toast.makeText(this, R.string.retry_location, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun showDialogForLocationServingSetting() {
        getGPSPermissionLaucher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (isLocationServiceAvailable()) {
                    isRuntimePermissionsGranted()
                }
            } else {
                Toast.makeText(
                    this,
                    R.string.cannot_use_location,
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle(R.string.no_location_setting)
        builder.setMessage(R.string.no_location_alert)
        builder.setCancelable(true)
        builder.setPositiveButton(
            R.string.settings,
            DialogInterface.OnClickListener { dialogInterface, i ->
                val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                getGPSPermissionLaucher.launch(callGPSSettingIntent)
            })
        builder.setNegativeButton(
            R.string.cancel,
            DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.cancel()
                Toast.makeText(this@MainActivity, R.string.no_location_service, Toast.LENGTH_LONG)
                    .show()
                finish()
            })
        builder.create().show()
    }

    fun updateUI() {
        locationProvider = LocationProvider(this@MainActivity)

        val latitude: Double? = locationProvider.getLocationLatitude()
        val longtitude: Double? = locationProvider.getLocationLongtitude()

        if (latitude != null && longtitude != null) {
            // get currentLocation check && UI update
            val address = getCurrentAddress(latitude, longtitude)
            address?.let{
                binding.tvLocationTitle.text = "${it.thoroughfare}"
                binding.tvLocationSub.text = "${it.countryName}"
            }
            // get micro-dust && UI update
        } else {
            Toast.makeText(this, R.string.no_langlongtude, Toast.LENGTH_LONG).show()
        }
    }

    private fun getCurrentAddress(latitude: Double, longtitude: Double): Address? {
        val getZipCode = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>

        addresses = try {
            getZipCode.getFromLocation(latitude, longtitude, 7)
        } catch (ioException: IOException) {
            Toast.makeText(this, R.string.cant_use_geocoder, Toast.LENGTH_LONG).show()
            return null
        } catch (illegalArugmentException: IllegalArgumentException) {
            Toast.makeText(this, R.string.wrong_location, Toast.LENGTH_LONG).show()
            return null
        }!!

        if (addresses == null || addresses.size == 0) {
            Toast.makeText(this, "주소가 발견되지 않았습니다.", Toast.LENGTH_LONG).show()
            return null
        }
        return addresses[0]
    }


}
