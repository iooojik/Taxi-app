package octii.app.taxiapp.web.requests

import android.app.Activity
import androidx.navigation.findNavController
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.services.Services

class RequestsResult(
	var success: Boolean,
	val activity: Activity, private val userType: String, private val userToken: String?,
) : Runnable {
	
	override fun run() {
		if (success) {
			getStartLocation()
			Services(activity, Static.MAIN_SERVICES).start()
		} else activity.findNavController(R.id.nav_host_fragment).navigate(R.id.welcomeFragment)
	}
	
	private fun getStartLocation() {
		val uType = userType
		logError("token : $userToken")
		if (userToken != null) {
			if (uType == Static.DRIVER_TYPE) activity.findNavController(R.id.nav_host_fragment)
				.navigate(R.id.driverMapFragment)
			else activity.findNavController(R.id.nav_host_fragment).navigate(R.id.clientMapFragment)
		} else activity.findNavController(R.id.nav_host_fragment).navigate(R.id.welcomeFragment)
	}
}