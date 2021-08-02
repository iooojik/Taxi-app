package octii.app.taxiapp.web.requests

import android.app.Activity
import android.view.View
import octii.app.taxiapp.MyPreferences
import octii.app.taxiapp.Static
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.web.HttpHelper

class UserRequests(private val view : View? = null, private val activity: Activity? = null) {

    private val orderRequests = OrderRequests(view, activity)

    fun updateUser() : UserModel {
        val response = HttpHelper.USER_API.update(UserModel()).execute()
        if (response.isSuccessful){
            return setUserInfo(response.body())
        } else {
            if (view != null)
                HttpHelper.errorProcessing(view, response.errorBody())
        }
        return UserModel()
    }

    private fun setUserInfo(model: UserModel?) : UserModel{
        if (model != null && model.token.isNotEmpty()) {
            UserModel.uID = model.id
            UserModel.uIsViber = model.isViber
            UserModel.uIsWhatsapp = model.isWhatsapp
            UserModel.uType = model.type
            UserModel.uPhoneNumber = model.phone!!
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

    fun loginWithToken(token : String){
        val response = HttpHelper.USER_API.loginWithToken(UserModel(token = token)).execute()
        if (response.isSuccessful){
            val model = response.body()?.user
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
    }

    fun login(phoneNum : String, name : String) : UserModel{
        val response = HttpHelper.USER_API.login(UserModel(phone = phoneNum, userName = name)).execute()
        if (response.isSuccessful){
            val model = response.body()
            if (model != null && model.token.isNotEmpty()){
                setUserInfo(model)
            }
        }
        return UserModel()
    }
}