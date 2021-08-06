package octii.app.taxiapp.services.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import octii.app.taxiapp.models.coordinates.CoordinatesModel
import octii.app.taxiapp.scripts.logDebug
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.web.SocketHelper


class MyLocationListener : LocationListener {

    override fun onLocationChanged(loc: Location) {
        imHere = loc

        latitude = loc.latitude
        longitude = loc.longitude

        //получение скорости движения
        speed = (loc.speed *3600/1000).toDouble()

        //подсчёт дистанции от предыдущей точки
        distance += calcDistance(imHere, prevLocation)

        prevLocation = loc
        try {
            SocketHelper.updateCoordinates(CoordinatesModel(latitude = latitude, longitude = longitude))
            logError("coords: ${CoordinatesModel(latitude = latitude, longitude = longitude)}")
        } catch (e : Exception){
            logError(e)
            e.printStackTrace()
        }

        logDebug("speed $speed " +
                    "distance $distance " +
                    "coordinates $longitude $latitude"
        )

    }

    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    companion object {
        // здесь будет всегда доступна самая последняя информация о местоположении пользователя.
        var preferences : SharedPreferences? = null

        var prevLocation : Location? = null
        var imHere : Location? = null
        var latitude : Double = 0.0
        var longitude : Double = 0.0
        var speed : Double = 0.0
        var distance : Float = 0f

        // это нужно запустить в самом начале работы программы
        @SuppressLint("MissingPermission")
        fun setUpLocationListener(context: Context){
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationListener: LocationListener = MyLocationListener()

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000, 10f,
                locationListener
            ) // здесь можно указать другие более подходящие вам параметры

            imHere = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }
    }

    private fun calcDistance(currLocation : Location?, prevLocation : Location?): Float {
        //подсчёт пройденной дистанции в км
        return if (prevLocation != null && currLocation != null)
            prevLocation.distanceTo(currLocation)/1000
        else 0f
    }
}