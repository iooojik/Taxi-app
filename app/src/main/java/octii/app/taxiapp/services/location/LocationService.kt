package octii.app.taxiapp.services.location

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder


class LocationService : Service() {

    private var running = false
    private lateinit var preferences : SharedPreferences

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        try {
            setTimer()
            setLocationListener()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun setLocationListener() {
        MyLocationListener.setUpLocationListener(applicationContext)
    }

    private fun setTimer(){

    }

}