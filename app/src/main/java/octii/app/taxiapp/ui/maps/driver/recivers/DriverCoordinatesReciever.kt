package octii.app.taxiapp.ui.maps.driver.recivers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import octii.app.taxiapp.constants.StaticCoordinates
import octii.app.taxiapp.models.coordinates.RemoteCoordinates
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.ui.maps.driver.DriverMapFragment
import octii.app.taxiapp.ui.utils.CoordinatesReceiverUtil

class DriverCoordinatesReciever(
	private val activity: Activity,
	private val driverMapFragment: DriverMapFragment,
	private val fragmentContext: Context,
) : BroadcastReceiver(), CoordinatesReceiverUtil {
	
	override fun onReceive(context: Context?, intent: Intent?) {
		if (intent != null) {
			when (intent.getStringExtra(StaticCoordinates.COORDINATES_STATUS_UPDATE)) {
				StaticCoordinates.COORDINATES_STATUS_UPDATE_ -> {
					logInfo("order status ${StaticCoordinates.COORDINATES_STATUS_UPDATE_}")
					logInfo("${RemoteCoordinates.remoteLat} ${RemoteCoordinates.remoteLon}")
					updateModelOnMap(
						UserModel.uType,
						driverMapFragment.googleMap,
						activity,
						fragmentContext,
						driverMapFragment = driverMapFragment)
				}
			}
		}
	}
	
}