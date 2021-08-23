/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 18:58                       *
 ******************************************************************************/

package octii.app.taxiapp.web.requests

import android.app.Activity
import androidx.navigation.findNavController
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.scripts.logError

class RequestsResult(
	var success: Boolean,
	val activity: Activity, private val userType: String, private val userToken: String?,
) : Runnable {
	
	override fun run() {
		if (success) {
			getStartLocation()
		} else activity.findNavController(R.id.nav_host_fragment).navigate(R.id.action_splashFragment_to_welcomeFragment)
	}
	
	private fun getStartLocation() {
		val uType = userType
		logError("token : $userToken")
		if (userToken != null) {
			if (uType == Static.DRIVER_TYPE) activity.findNavController(R.id.nav_host_fragment)
				.navigate(R.id.action_splashFragment_to_driverMapFragment)
			else activity.findNavController(R.id.nav_host_fragment).navigate(R.id.action_splashFragment_to_clientMapFragment)
		} else activity.findNavController(R.id.nav_host_fragment).navigate(R.id.action_splashFragment_to_welcomeFragment)
	}
}