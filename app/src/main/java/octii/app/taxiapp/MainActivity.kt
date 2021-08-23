/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 23:39                       *
 ******************************************************************************/

package octii.app.taxiapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.constants.StaticTaximeter
import octii.app.taxiapp.constants.StaticWeb
import octii.app.taxiapp.databinding.ActivityMainBinding
import octii.app.taxiapp.databinding.DialogConnectionLostBinding
import octii.app.taxiapp.locale.Application
import octii.app.taxiapp.models.driver.DriverModel
import octii.app.taxiapp.models.user.UserApi
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.MyPreferences
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.services.Services
import octii.app.taxiapp.ui.utils.AdHelper
import octii.app.taxiapp.ui.utils.SharedPrefsUtil
import octii.app.taxiapp.web.SocketHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity(), SharedPrefsUtil {
	
	lateinit var binding: ActivityMainBinding
	private lateinit var adHelper: AdHelper
	lateinit var dialog: MaterialAlertDialogBuilder
	private var dialogShown = false
	var alertDialog: AlertDialog? = null
	
	
	private var snackbarsReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (intent != null) {
				val message = intent.getStringExtra(Static.SNACKBAR_MESSAGE)
				val length = intent.getIntExtra(Static.SNACKBAR_MESSAGE_LENGTH, -1)
				if (!message?.trim().isNullOrEmpty())
					runOnUiThread {
						Snackbar.make(findViewById(R.id.drawer), message!!, length).show()
					}
			}
		}
	}
	
	private var connectionLostReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (intent != null) {
				try {
					when (intent.getStringExtra(Static.CONNECTION_STATUS)) {
						Static.CONNECTION_LOST -> {
							if (!dialogShown) alertDialog = dialog.show()
							dialogShown = true
						}
						Static.CONNECTION_EST -> {
							dialogShown = false
							alertDialog?.dismiss()
							alertDialog = null
						}
					}
				} catch (e: java.lang.Exception) {
					logInfo(e.stackTrace)
				}
				
			}
		}
	}
	
	private fun connectionLostDialog(): MaterialAlertDialogBuilder {
		val dialog = MaterialAlertDialogBuilder(this)
		//dialog.setMessage(resources.getString(R.string.connection_lost))
		dialog.setCancelable(false)
		val dialogBinding = DialogConnectionLostBinding.inflate(layoutInflater)
		dialog.setView(dialogBinding.root)
		dialogBinding.button.setOnClickListener {
			this.recreate()
		}
		return dialog
	}
	
	override fun attachBaseContext(newBase: Context?) {
		setLanguage(context = newBase)
		super.attachBaseContext(newBase)
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		adHelper = AdHelper(this, binding.appBarMain.include.adView)
		registerReceiver(snackbarsReceiver, IntentFilter(Static.SNACKBAR_INTENT_FILTER))
		registerReceiver(connectionLostReceiver, IntentFilter(Static.CONNECTION_INTENT_FILTER))
		getSharedPrefernces()
		dialog = connectionLostDialog()
		SocketHelper.activity = this
		setLanguage(this)
		setContentView(binding.root)
		findNavController(R.id.nav_host_fragment).navigate(R.id.splashFragment)
		logInfo("on create BaseActivity")
	}
	
	private fun getSharedPrefernces() {
		MyPreferences.userPreferences =
			getSharedPreferences(Static.SHARED_PREFERENCES_USER, Context.MODE_PRIVATE)
		MyPreferences.applicationPreferences =
			getSharedPreferences(Static.SHARED_PREFERENCES_APPLICATION, Context.MODE_PRIVATE)
		MyPreferences.taximeterPreferences =
			getSharedPreferences(StaticTaximeter.SHARED_PREFERENCES_TAXIMETER, Context.MODE_PRIVATE)
	}
	
	private fun setLanguage(context: Context?) {
		if (context != null) Application.getInstance().initAppLanguage(context)
	}
	
	override fun onResumeFragments() {
		super.onResumeFragments()
		logInfo("onResumeFragments")
		if (getToken()?.trim()?.isNotEmpty() == true) {
			Services(this).restart()
		}
	}
	
	override fun onPause() {
		try {
			if (getSavedUserType() == Static.DRIVER_TYPE) {
				Retrofit.Builder()
					.baseUrl(StaticWeb.REST_URL)
					.addConverterFactory(GsonConverterFactory.create())
					.build().create(UserApi::class.java)
					.updateDriverState(UserModel(uuid = getUserUUID(),
						driver = DriverModel(isWorking = false)))
					.enqueue(object : Callback<UserModel> {
						override fun onResponse(
                            call: Call<UserModel>,
                            response: Response<UserModel>,
                        ) {
						}
						
						override fun onFailure(call: Call<UserModel>, t: Throwable) {
						}
					})
			}
			unregisterReceiver(snackbarsReceiver)
			unregisterReceiver(connectionLostReceiver)
		} catch (e: Exception) {
			e.printStackTrace()
		}
		super.onPause()
	}
	
}