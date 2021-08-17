package octii.app.taxiapp.web.requests

import android.app.Activity
import android.view.View
import octii.app.taxiapp.models.driver.DriverModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.web.HttpHelper
import retrofit2.Response

class DriverAvailableRequests(
    private val view: View? = null,
    private val activity: Activity? = null,
) {

    fun updateDriverAvailableModel(model: DriverModel): DriverModel {
        val resp = HttpHelper.driverApi.update(model).execute()
        return localUpdateDriverAvailable(resp)
    }

    fun getDriverAvailableModel(): DriverModel {
        val response =
            HttpHelper.driverApi.getDriver(DriverModel(driverID = UserModel.uID)).execute()
        return localUpdateDriverAvailable(response)
    }

    private fun localUpdateDriverAvailable(response: Response<DriverModel>): DriverModel {
        if (response.isSuccessful) {
            if (response.body() != null) {
                val newDriver = response.body()!!

                DriverModel.mDriverID = newDriver.driverID
                DriverModel.mId = newDriver.id
                DriverModel.mIsWorking = newDriver.isWorking
                DriverModel.mPrices.pricePerKm = newDriver.prices.pricePerKm
                DriverModel.mPrices.priceWaitingMin = newDriver.prices.priceWaitingMin
                DriverModel.mPrices.pricePerMinute = newDriver.prices.pricePerMinute
                DriverModel.mRideDistance = newDriver.rideDistance
                return newDriver
            }
        } else {
            if (view != null)
                HttpHelper.errorProcessing(view, response.errorBody(), activity)
        }
        return DriverModel()

    }
}