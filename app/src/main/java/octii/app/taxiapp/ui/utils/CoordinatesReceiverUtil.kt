package octii.app.taxiapp.ui.utils

import android.app.Activity
import android.content.Context
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.models.coordinates.RemoteCoordinates
import octii.app.taxiapp.ui.maps.client.ClientMapFragment
import octii.app.taxiapp.ui.maps.driver.DriverMapFragment

interface CoordinatesReceiverUtil : FragmentHelper {
	fun updateModelOnMap(
		accountType: String, googleMap: GoogleMap?, activity: Activity, fragmentContext: Context,
		clientMapFragment: ClientMapFragment? = null, driverMapFragment: DriverMapFragment? = null,
	) {
		if (RemoteCoordinates.remoteLat != 0.0 && RemoteCoordinates.remoteLon != 0.0) {
			if (googleMap != null) {
				if (accountType == Static.DRIVER_TYPE && clientMapFragment != null) {
					
					drawMarker(googleMap, activity.resources.getString(R.string.driver),
						bitmapFromVector(fragmentContext, R.drawable.car), clientMapFragment)
				} else if (accountType == Static.CLIENT_TYPE && driverMapFragment != null) {
					
					drawMarker(googleMap, activity.resources.getString(R.string.customer),
						bitmapFromVector(fragmentContext, R.drawable.user), clientMapFragment)
				}
			}
		}
	}
	
	private fun drawMarker(
		googleMap: GoogleMap, name: String, bitmap: BitmapDescriptor?,
		clientMapFragment: ClientMapFragment? = null, driverMapFragment: DriverMapFragment? = null,
	) {
		val latLng = LatLng(RemoteCoordinates.remoteLat, RemoteCoordinates.remoteLon)
		googleMap.clear()
		googleMap.addMarker(MarkerOptions().position(latLng).title(name).icon(bitmap))
		googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
		
		if (clientMapFragment != null && !clientMapFragment.cameraisMoved)
			clientMapFragment.cameraisMoved = true
		else if (driverMapFragment != null && !driverMapFragment.cameraisMoved)
			driverMapFragment.cameraisMoved = true
		
	}
}