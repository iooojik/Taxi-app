package octii.app.taxiapp.web.requests

import android.app.Activity
import android.view.View
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.web.HttpHelper

class UserRequests(private val view : View?, private val activity: Activity) {



    fun updateUser() : UserModel {
        val response = HttpHelper.USER_API.update(UserModel()).execute()
        if (response.isSuccessful){
            if (response.body() != null){
                val user = response.body()!!
                return user
            }
        } else {
            if (view != null)
                HttpHelper.errorProcessing(view, response.errorBody())
        }
        return UserModel()
    }
}