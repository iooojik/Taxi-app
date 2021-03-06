/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 23.08.2021, 12:33                       *
 ******************************************************************************/

package octii.app.taxiapp.ui.maps.client.recievers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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