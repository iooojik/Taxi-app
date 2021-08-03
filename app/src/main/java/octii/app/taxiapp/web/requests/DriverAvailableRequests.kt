package octii.app.taxiapp.web.requests

import android.app.Activity
import android.view.View
import octii.app.taxiapp.models.driverAvailable.DriverAvailable
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.web.HttpHelper
import retrofit2.Response

class DriverAvailableRequests(private val view : View? = null, private val activity: Activity? = null) {

    fun updateDriverAvailableModel(model : DriverAvailable) : DriverAvailable {
        val resp = HttpHelper.DRIVER_AVAILABLE_API.update(model).execute()
        return localUpdateDriverAvailable(resp)
    }

    fun getDriverAvailableModel() : DriverAvailable {
        val response = HttpHelper.DRIVER_AVAILABLE_API.getDriver(DriverAvailable(driverID = UserModel.uID)).execute()
        return localUpdateDriverAvailable(response)
    }

    private fun localUpdateDriverAvailable(response : Response<DriverAvailable>) : DriverAvailable {
        if (response.isSuccessful){
            if (response.body() != null){
                val newDriver = response.body()!!

                DriverAvailable.mDriverID = newDriver.driverID
                DriverAvailable.mId = newDriver.id
                DriverAvailable.mIsWorking = newDriver.isWorking
                DriverAvailable.mPricePerKm = newDriver.pricePerKm
                DriverAvailable.mPricePerMinute = newDriver.pricePerMinute
                DriverAvailable.mPriceWaitingMin = newDriver.pricePerMinute
                DriverAvailable.mRideDistance = newDriver.rideDistance
                return newDriver
            }
        } else {
            if (view != null)
                HttpHelper.errorProcessing(view, response.errorBody(), activity)
        }
        return DriverAvailable()

    }
}