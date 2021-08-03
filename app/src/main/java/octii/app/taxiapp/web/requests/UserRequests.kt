package octii.app.taxiapp.web.requests

import android.app.Activity
import android.view.View
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import octii.app.taxiapp.MyPreferences
import octii.app.taxiapp.R
import octii.app.taxiapp.Static
import octii.app.taxiapp.models.driverAvailable.DriverAvailable
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.web.HttpHelper

class UserRequests(private val view : View? = null, private val activity: Activity? = null) {

    private val orderRequests = OrderRequests(view, activity)

    private fun setUserInfo(model: UserModel?) : UserModel{
        if (model != null && model.token.isNotEmpty()) {
            setDriverInfo(model.driver)

            UserModel.uID = model.id
            UserModel.uIsViber = model.isViber
            UserModel.uIsWhatsapp = model.isWhatsapp
            UserModel.uType = model.type
            UserModel.uPhoneNumber = model.phone
            UserModel.uToken = model.token
            UserModel.nUserName = model.userName!!
            UserModel.mUuid = model.uuid
            UserModel.mIsOnlyClient = model.isOnlyClient
            UserModel.mAvatarURL = model.avatarURL
            UserModel.mLanguages = model.languages
            UserModel.mCoordinates = model.coordinates

            MyPreferences.userPreferences?.let {
                MyPreferences.saveToPreferences(
                    it, Static.SHARED_PREFERENCES_USER_TYPE, model.type)
            }

            MyPreferences.userPreferences?.let {
                MyPreferences.saveToPreferences(
                    it, Static.SHARED_PREFERENCES_USER_TOKEN, model.token)
            }
        }
        return UserModel()
    }

    fun loginWithToken(token : String) : UserModel{

        try {
            val response = HttpHelper.USER_API.loginWithToken(UserModel(token = token)).execute()
            if (response.isSuccessful){
                val model = response.body()?.user
                if (response.isSuccessful){
                    if (model != null && model.token.isNotEmpty()){
                        setUserInfo(model)

                        if (response.body()?.order != null){
                            val order = response.body()?.order
                            if (order != null){
                                orderRequests.getOrderModel(order, false, if (!order.isFinished) !order.isFinished else false)
                            }

                        }


                    }
                }
                activity?.runOnUiThread {
                    if (UserModel.uToken.isEmpty()) activity.findNavController(R.id.nav_host_fragment).navigate(R.id.authMessengersFragment)
                    if (UserModel.uType == Static.DRIVER_TYPE) activity.findNavController(R.id.nav_host_fragment).navigate(R.id.driverMapFragment)
                    else activity.findNavController(R.id.nav_host_fragment).navigate(R.id.clientMapFragment)
                }

            } else {
                showSnackBarError()
                activity?.runOnUiThread { activity.findNavController(R.id.nav_host_fragment).navigate(R.id.authMessengersFragment) }

            }
        } catch (e : Exception){
            showSnackBarError()
            e.printStackTrace()
        }
        return UserModel()

    }

    private fun setDriverInfo(driver : DriverAvailable) {
        DriverAvailable.mId = driver.id
        DriverAvailable.mIsWorking = driver.isWorking
        DriverAvailable.mPricePerMinute = driver.pricePerMinute
        DriverAvailable.mRideDistance = driver.rideDistance
        DriverAvailable.mPricePerKm = driver.pricePerKm
        DriverAvailable.mPriceWaitingMin = driver.priceWaitingMin
    }

    fun login(phoneNum : String, name : String) : UserModel{
        try {
            val response = HttpHelper.USER_API.login(UserModel(phone = phoneNum, userName = name)).execute()
            if (response.isSuccessful){
                val model = response.body()
                if (model != null && model.token.isNotEmpty()){
                    setUserInfo(model)
                }
            }
        }catch (e : Exception){
            showSnackBarError()
            e.printStackTrace()
        }
        return UserModel()
    }
    private fun showSnackBarError(){
        if (view != null && activity != null){
            activity.runOnUiThread {
                Snackbar.make(view, activity.resources.getString(R.string.error), Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    fun update() : UserModel{
        logError(Gson().toJson(UserModel()))
        try {
            val response = HttpHelper.USER_API.update(UserModel()).execute()
            if (response.isSuccessful){
                val model = response.body()
                if (model != null && model.token.isNotEmpty()){
                    setUserInfo(model)
                }
            }
        }catch (e : Exception){
            showSnackBarError()
            e.printStackTrace()
        }
        return UserModel()
    }
}