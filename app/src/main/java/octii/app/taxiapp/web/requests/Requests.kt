package octii.app.taxiapp.web.requests

import android.app.Activity
import android.view.View
import octii.app.taxiapp.web.HttpHelper

class Requests(view: View? = null, activity: Activity? = null) {

    val userRequests = UserRequests(view, activity)
    val orderRequests = OrderRequests(activity)

    init {
        HttpHelper.doRetrofit()
    }


}