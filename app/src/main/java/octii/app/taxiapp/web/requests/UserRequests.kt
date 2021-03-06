/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 17:34                       *
 ******************************************************************************/

package octii.app.taxiapp.web.requests

import android.app.Activity
import android.view.View
import android.widget.ProgressBar
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.constants.StaticWeb
import octii.app.taxiapp.models.AuthorizationModel
import octii.app.taxiapp.models.coordinates.CoordinatesModel
import octii.app.taxiapp.models.driver.DriverModel
import octii.app.taxiapp.models.user.UserApi
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.*
import octii.app.taxiapp.web.HttpHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserRequests(private val view: View? = null, private val activity: Activity? = null) {
	
	private val orderRequests = OrderRequests(activity)
	
	private fun setUserInfo(model: UserModel?): UserModel {
		if (model != null && model.token.isNotEmpty()) {
			setDriverInfo(model.driver)
			
			UserModel.uID = model.id
			UserModel.uIsViber = model.isViber
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
			
			MyPreferences.userPreferences?.let {
				MyPreferences.saveToPreferences(
					it, Static.SHARED_PREFERENCES_USER_UUID, model.uuid)
			}
		}
		return UserModel()
	}
	
	private fun setDriverInfo(driver: DriverModel?) {
		if (driver != null) {
			logDebug(driver)
			logDebug(driver.prices)
			DriverModel.mId = driver.id
			DriverModel.mIsWorking = driver.isWorking
			DriverModel.mPrices.pricePerMinute = driver.prices.pricePerMinute
			DriverModel.mRideDistance = driver.rideDistance
			DriverModel.mPrices.pricePerKm = driver.prices.pricePerKm
			DriverModel.mPrices.priceWaitingMin = driver.prices.priceWaitingMin
		}
	}
	
	fun loginWithToken(token: String, runnable: RequestsResult) {
		logInfo("login with token")
		HttpHelper.USER_API.loginWithToken(UserModel(token = token))
			.enqueue(object : Callback<AuthorizationModel> {
				override fun onResponse(
					call: Call<AuthorizationModel>,
					response: Response<AuthorizationModel>,
				) {
					logInfo("login with token response: ${response.raw()}")
					if (response.isSuccessful) {
						val model = response.body()?.user
						if (response.isSuccessful) {
							if (model != null && model.token.isNotEmpty()) {
								setUserInfo(model)
								orderRequests.orderCheck(model)
							}
						}
						runnable.success = true
					} else {
						runnable.success = false
						showSnackBarError()
					}
					logInfo("success: ${runnable.success}")
					runnable.run()
				}
				
				override fun onFailure(call: Call<AuthorizationModel>, t: Throwable) {
					t.printStackTrace()
					showSnackBarError()
				}
				
			})
	}
	
	fun login(
		phoneNum: String,
		name: String,
		latLng: LatLng,
		progressBar: ProgressBar? = null,
		runnable: Runnable,
	) {
		showProgressBar(progressBar)
		val reqModel = UserModel(id = -1, uuid = "", phone = phoneNum, userName = name,
			coordinates = CoordinatesModel(latitude = latLng.latitude,
				longitude = latLng.longitude), isViber = true)
		logInfo(reqModel)
		HttpHelper.USER_API.login(reqModel).enqueue(object :
			Callback<AuthorizationModel> {
			override fun onResponse(
				call: Call<AuthorizationModel>,
				response: Response<AuthorizationModel>,
			) {
				logInfo("login request: ${response.raw()}")
				if (response.isSuccessful) {
					val model = response.body()
					if (model?.user != null && model.user.token.isNotEmpty()) {
						logError("${model.user.coordinates!!.latitude}")
						setUserInfo(model.user)
						orderRequests.orderCheck(model.user)
						runnable.run()
					}
				} else {
					hideProgressBar(progressBar)
					showSnackBarError()
				}
			}
			
			override fun onFailure(call: Call<AuthorizationModel>, t: Throwable) {
				logInfo("login error: ${t.stackTrace}")
				t.printStackTrace()
				showSnackBarError()
				hideProgressBar(progressBar)
			}
		})
	}
	
	private fun showProgressBar(progressBar: ProgressBar? = null) {
		if (activity != null && progressBar != null)
			activity.runOnUiThread { progressBar.visibility = View.VISIBLE }
	}
	
	private fun hideProgressBar(progressBar: ProgressBar? = null) {
		if (activity != null && progressBar != null)
			activity.runOnUiThread { progressBar.visibility = View.INVISIBLE }
	}
	
	private fun showSnackBarError() {
		if (view != null && activity != null) {
			activity.runOnUiThread {
				Snackbar.make(view,
					activity.resources.getString(R.string.error),
					Snackbar.LENGTH_SHORT).show()
			}
		}
	}
	
	fun update(runnable: Runnable): UserModel {
		HttpHelper.USER_API.update(UserModel()).enqueue(object : Callback<UserModel> {
			override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
				logInfo("user update ${response.raw()}")
				if (response.isSuccessful) {
					val model = response.body()
					if (model != null && model.token.isNotEmpty()) {
						setUserInfo(model)
					}
					runnable.run()
				} else {
					logError(response.raw())
					showSnackBarError()
				}
			}
			
			override fun onFailure(call: Call<UserModel>, t: Throwable) {
				showSnackBarError()
				logExeption(t)
			}
		})
		return UserModel()
	}
	
	fun updateDriverState(userModel: UserModel) {
		Retrofit.Builder()
			.baseUrl(StaticWeb.REST_URL)
			.addConverterFactory(GsonConverterFactory.create())
			.build().create(UserApi::class.java).updateDriverState(userModel)
			.enqueue(object : Callback<UserModel> {
				override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
				}
				
				override fun onFailure(call: Call<UserModel>, t: Throwable) {
				}
			})
	}
}