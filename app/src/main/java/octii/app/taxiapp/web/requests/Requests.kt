package octii.app.taxiapp.web.requests

import android.app.Activity
import android.view.View
import octii.app.taxiapp.web.HttpHelper

class Requests(private val view : View? = null, private val activity: Activity? = null) {

    val driverAvailableRequests = DriverAvailableRequests(view, activity)
    val userRequests = UserRequests(view, activity)
    val orderRequests = OrderRequests(view, activity)

    init {
        HttpHelper.doRetrofit()
    }




}