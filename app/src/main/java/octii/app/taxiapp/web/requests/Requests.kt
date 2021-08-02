package octii.app.taxiapp.web.requests

import android.app.Activity
import android.view.View
import octii.app.taxiapp.models.driverAvailable.DriverAvailable
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.web.HttpHelper
import retrofit2.Response

class Requests(private val view : View?, private val activity: Activity) {

    val driverAvailableRequests = DriverAvailableRequests(view, activity)
    val userRequests = UserRequests(view, activity)

    init {
        HttpHelper.doRetrofit()
    }




}