package octii.app.taxiapp.services.location

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.scripts.logService


class LocationService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        setLocationListener()
        logService("location service is running")
    }

    private fun setLocationListener() {
        MyLocationListener.setUpLocationListener(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        logInfo("service ${this.javaClass.name} destroy")
    }
}