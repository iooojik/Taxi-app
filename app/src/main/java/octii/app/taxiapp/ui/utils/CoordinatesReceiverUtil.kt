/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 23.08.2021, 12:33                       *
 ******************************************************************************/

package octii.app.taxiapp.ui.utils

import android.app.Activity
import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.models.coordinates.RemoteCoordinates
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.ui.maps.client.ClientMapFragment
import octii.app.taxiapp.ui.maps.driver.DriverMapFragment

interface CoordinatesReceiverUtil : FragmentHelper {
	fun updateModelOnMap(
		accountType: String, googleMap: GoogleMap?, activity: Activity, fragmentContext: Context,
		clientMapFragment: ClientMapFragment? = null, driverMapFragment: DriverMapFragment? = null,) {
		if (RemoteCoordinates.remoteLat != 0.0 && RemoteCoordinates.remoteLon != 0.0) {
			logInfo("${googleMap != null} ${accountType == Static.DRIVER_TYPE} ")
			if (googleMap != null) {
				if (accountType == Static.CLIENT_TYPE && clientMapFragment != null) {
					
					drawMarker(googleMap, activity.resources.getString(R.string.driver),
						bitmapFromVector(fragmentContext, R.drawable.car), clientMapFragment)
				} else if (accountType == Static.DRIVER_TYPE && driverMapFragment != null) {
					
					drawMarker(googleMap, activity.resources.getString(R.string.customer),
						bitmapFromVector(fragmentContext, R.drawable.user),null, driverMapFragment)
				}
			}
		}
	}
	
	private fun drawMarker(
		googleMap: GoogleMap, name: String, bitmap: BitmapDescriptor?,
		clientMapFragment: ClientMapFragment? = null, driverMapFragment: DriverMapFragment? = null,) {
		val latLng = LatLng(RemoteCoordinates.remoteLat, RemoteCoordinates.remoteLon)
		googleMap.clear()
		googleMap.addMarker(MarkerOptions().position(latLng).title(name).icon(bitmap))
		
		if (clientMapFragment != null && !clientMapFragment.cameraisMoved)
			clientMapFragment.cameraisMoved = true
		else if (driverMapFragment != null && !driverMapFragment.cameraisMoved)
			driverMapFragment.cameraisMoved = true
	}
}