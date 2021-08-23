/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 23:42                       *
 ******************************************************************************/

package octii.app.taxiapp.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class Permissions(val context: Context, val activity: Activity? = null) {
	
	var permissionsGranted = false
	
	fun requestPermissions() {
		if (ActivityCompat.checkSelfPermission(
				context,
				Manifest.permission.ACCESS_FINE_LOCATION
			) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
				context,
				Manifest.permission.ACCESS_COARSE_LOCATION
			) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
				context,
				Manifest.permission.READ_EXTERNAL_STORAGE
			) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
				context,
				Manifest.permission.WRITE_EXTERNAL_STORAGE
			) != PackageManager.PERMISSION_GRANTED && activity != null
		) {
			ActivityCompat.requestPermissions(
				activity,
				listOf(
					Manifest.permission.ACCESS_FINE_LOCATION,
					Manifest.permission.ACCESS_COARSE_LOCATION,
					Manifest.permission.READ_EXTERNAL_STORAGE,
					Manifest.permission.WRITE_EXTERNAL_STORAGE
				).toTypedArray(),
				101
			)
		} else permissionsGranted = true
	}
	
	fun checkPermissions(): Boolean {
		return !(ActivityCompat.checkSelfPermission(
			context,
			Manifest.permission.ACCESS_FINE_LOCATION
		) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
			context,
			Manifest.permission.ACCESS_COARSE_LOCATION
		) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
			context,
			Manifest.permission.READ_EXTERNAL_STORAGE
		) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
			context,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
		) != PackageManager.PERMISSION_GRANTED)
	}
}