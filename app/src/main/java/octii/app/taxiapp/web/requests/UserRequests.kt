package octii.app.taxiapp.web.requests

import android.app.Activity
import android.view.View
import android.widget.ProgressBar
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import octii.app.taxiapp.MyPreferences
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.models.AuthorizationModel
import octii.app.taxiapp.models.driver.DriverModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logDebug
import octii.app.taxiapp.scripts.logExeption
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.web.HttpHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
            UserModel.mFiles = model.files

            logInfo("token: ${model.token}")

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

    fun loginWithToken(token: String, runnable: Runnable){
        HttpHelper.USER_API.loginWithToken(UserModel(token = token)).enqueue(object : Callback<AuthorizationModel>{
            override fun onResponse(call: Call<AuthorizationModel>, response: Response<AuthorizationModel>) {
                if (response.isSuccessful){
                    val model = response.body()?.user
                    if (response.isSuccessful){
                        if (model != null && model.token.isNotEmpty()){
                            setUserInfo(model)
                            orderRequests.orderCheck(model)
                            if (response.body()?.lastOrder != null){
                                val order = response.body()?.lastOrder
                                if (order != null){
                                    orderRequests
                                        .getOrderModel(order, false, if (!order.isFinished) !order.isFinished else false)
                                }
                            }
                        }
                    }
                    runnable.run()
                    /*
                    activity?.runOnUiThread {
                        if (UserModel.uToken.isEmpty())
                            activity.findNavController(R.id.nav_host_fragment).navigate(R.id.welcomeFragment)

                        if (UserModel.uType == Static.DRIVER_TYPE)
                            activity.findNavController(R.id.nav_host_fragment).navigate(R.id.driverMapFragment)
                        else
                            activity.findNavController(R.id.nav_host_fragment).navigate(R.id.clientMapFragment)
                    }

                     */

                } else {
                    activity?.runOnUiThread { activity.findNavController(R.id.nav_host_fragment).navigate(R.id.welcomeFragment) }
                    showSnackBarError()
                }
            }

            override fun onFailure(call: Call<AuthorizationModel>, t: Throwable) {
                t.printStackTrace()
                showSnackBarError()
            }

        })
    }

    private fun setDriverInfo(driver : DriverModel?) {
        if (driver != null) {
            logDebug(driver)
            DriverModel.mId = driver.id
            DriverModel.mIsWorking = driver.isWorking
            DriverModel.mPrices.pricePerMinute = driver.prices.pricePerMinute
            DriverModel.mRideDistance = driver.rideDistance
            DriverModel.mPrices.pricePerKm = driver.prices.pricePerKm
            DriverModel.mPrices.priceWaitingMin = driver.prices.priceWaitingMin
            logDebug(DriverModel)
        }
    }

    fun login(phoneNum : String, name : String, progressBar: ProgressBar? = null){
        showProgressBar(progressBar)

        HttpHelper.USER_API.login(UserModel(phone = phoneNum, userName = name)).enqueue(object :
            Callback<AuthorizationModel> {
            override fun onResponse(call: Call<AuthorizationModel>, response: Response<AuthorizationModel>) {
                if (response.isSuccessful){
                    val model = response.body()
                    if (model?.user != null && model.user.token.isNotEmpty()) {
                        setUserInfo(model.user)
                        val order = model.lastOrder
                        logInfo("order= ${model.user}")
                        if (order != null && !order.isFinished){
                            orderRequests
                                .getOrderModel(order, false, if (!order.isFinished) !order.isFinished else false)
                        }
                        if (model.user.type == Static.DRIVER_TYPE) navigateToFragment(R.id.driverMapFragment)
                        else navigateToFragment(R.id.clientMapFragment)
                    }
                } else {
                    hideProgressBar(progressBar)
                    showSnackBarError()
                }
            }

            override fun onFailure(call: Call<AuthorizationModel>, t: Throwable) {
                t.printStackTrace()
                showSnackBarError()
                hideProgressBar(progressBar)
            }
        })
    }

    private fun showProgressBar(progressBar: ProgressBar? = null){
        if (activity != null && progressBar != null)
            activity.runOnUiThread { progressBar.visibility = View.VISIBLE }
    }

    private fun hideProgressBar(progressBar: ProgressBar? = null){
        if (activity != null && progressBar != null)
            activity.runOnUiThread { progressBar.visibility = View.INVISIBLE }
    }

    private fun showSnackBarError(){
        if (view != null && activity != null){
            activity.runOnUiThread {
                Snackbar.make(view, activity.resources.getString(R.string.error), Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    fun update() : UserModel{
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
            logExeption(e)
            e.printStackTrace()
        }
        return UserModel()
    }

    fun navigateToFragment(fragment : Int){
        try {
            activity?.findNavController(R.id.nav_host_fragment)?.navigate(fragment)
        } catch (e : java.lang.Exception){
            e.printStackTrace()
            logExeption(e)
            showSnackBarError()
        }

    }
}