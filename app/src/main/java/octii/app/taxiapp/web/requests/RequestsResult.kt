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
            getStartLocation(activity)
            Services(activity, Static.MAIN_SERVICES).start()
        } else activity.findNavController(R.id.nav_host_fragment)
            .navigate(R.id.authorizationActivity)
    }

    private fun getStartLocation(callingActivity: Activity) {
        val uType = userType
        logError("token : $userToken")
        if (userToken != null) {
            if (uType == Static.DRIVER_TYPE) activity.findNavController(R.id.nav_host_fragment)
                .navigate(R.id.driverMapActivity)
            else activity.findNavController(R.id.nav_host_fragment).navigate(R.id.clientMapActivity)
            callingActivity.finish()
        } else activity.findNavController(R.id.nav_host_fragment)
            .navigate(R.id.authorizationActivity)
    }
}