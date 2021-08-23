/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 16:50                       *
 ******************************************************************************/

package octii.app.taxiapp.constants

import octii.app.taxiapp.services.location.LocationService
import octii.app.taxiapp.services.socket.SocketService
import octii.app.taxiapp.services.taximeter.TaximeterService

class Static {
	companion object {
		const val SHARED_PREFERENCES_USER = "USER PREFERENCES"
		const val SHARED_PREFERENCES_USER_TOKEN = "USER PREFERENCES TOKEN"
		const val SHARED_PREFERENCES_USER_UUID = "USER PREFERENCES UUID"
		const val SHARED_PREFERENCES_USER_TYPE = "USER PREFERENCES TYPE"
		const val SHARED_PREFERENCES_APPLICATION = "APPLICATION PREFERENCES"
		const val DRIVER_TYPE = "driver"
		const val CLIENT_TYPE = "client"
		val MAIN_SERVICES =
			listOf(SocketService::class, LocationService::class, TaximeterService::class)
		val PHOTO_TYPES = listOf("avatar", "car", "car_number", "license")
		const val PICK_IMAGE_AVATAR = 9
		const val STANDART_ZOOM_LEVEL = 2f
		const val SNACKBAR_INTENT_FILTER = "octii.app.taxiapp.SNACKBAR_INTENT_FILTER"
		const val CONNECTION_INTENT_FILTER = "octii.app.taxiapp.CONNECTION_INTENT_FILTER"
		const val SNACKBAR_MESSAGE = "SNACKBAR_MESSAGE"
		const val CONNECTION_STATUS = "CONNECTION_STATUS"
		const val CONNECTION_LOST = "CONNECTION_LOST"
		const val CONNECTION_EST = "CONNECTION_EST"
		const val SNACKBAR_MESSAGE_LENGTH = "SNACKBAR_MESSAGE_LENGTH"
        const val VIBER_PACKAGE_NAME = "com.viber.voip"
		const val EXPAND_MORE_FAB = "expand more"
		const val EXPAND_LESS_FAB = "expand less"
		const val LOG_TAG = "IOOOJIK"
	}
}