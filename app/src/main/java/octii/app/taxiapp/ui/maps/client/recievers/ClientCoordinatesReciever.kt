package octii.app.taxiapp.ui.maps.client.recievers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.maps.GoogleMap
import octii.app.taxiapp.constants.StaticCoordinates
import octii.app.taxiapp.models.coordinates.RemoteCoordinates
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.ui.maps.client.ClientMapFragment
import octii.app.taxiapp.ui.utils.CoordinatesReceiverUtil

class ClientCoordinatesReciever(
    private val activity: Activity,
    private val clientMapFragment: ClientMapFragment,
    private val fragmentContext: Context,
) : BroadcastReceiver(), CoordinatesReceiverUtil {
	
	override fun onReceive(context: Context?, intent: Intent?) {
		if (intent != null) {
			when (intent.getStringExtra(StaticCoordinates.COORDINATES_STATUS_UPDATE)) {
				StaticCoordinates.COORDINATES_STATUS_UPDATE_ -> {
					logInfo("order status ${StaticCoordinates.COORDINATES_STATUS_UPDATE_}")
					logInfo("${RemoteCoordinates.remoteLat} ${RemoteCoordinates.remoteLon}")
					updateModelOnMap(UserModel.uType,
						clientMapFragment.googleMap,
						activity,
						fragmentContext,
						clientMapFragment = clientMapFragment)
				}
			}
		}
	}
}