/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 23:42                       *
 ******************************************************************************/

package octii.app.taxiapp.services.location

import android.app.Service
import android.content.Intent
import android.os.IBinder
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.scripts.logService
import octii.app.taxiapp.ui.Permissions


class LocationService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        if (Permissions(applicationContext).checkPermissions()) {
            setLocationListener()
            logService("location service is running")
        }
    }

    private fun setLocationListener() {
        MyLocationListener.setUpLocationListener(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        logInfo("service ${this.javaClass.name} destroy")
    }
}