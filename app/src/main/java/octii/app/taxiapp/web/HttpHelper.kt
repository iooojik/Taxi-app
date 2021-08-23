/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 17:49                       *
 ******************************************************************************/

package octii.app.taxiapp.web

import android.app.Activity
import android.content.ContentValues.TAG
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import octii.app.taxiapp.constants.StaticWeb
import octii.app.taxiapp.models.files.FileApi
import octii.app.taxiapp.models.log.LogApi
import octii.app.taxiapp.models.orders.OrdersApi
import octii.app.taxiapp.models.user.UserApi
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class HttpHelper {
	
	companion object {
		@JvmStatic
		lateinit var USER_API: UserApi
		
		@JvmStatic
		lateinit var FILE_API: FileApi
		
		@JvmStatic
		lateinit var ORDERS_API: OrdersApi
		
		@JvmStatic
		lateinit var LOG_API: LogApi
		
		@JvmStatic
		lateinit var retrofit: Retrofit
		
		@JvmStatic
		fun doRetrofit() {
			val interceptor = HttpLoggingInterceptor()
			interceptor.apply { interceptor.level = HttpLoggingInterceptor.Level.BODY }
			val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
			retrofit = Retrofit.Builder()
				.baseUrl(StaticWeb.REST_URL)
				.client(client)
				.addConverterFactory(GsonConverterFactory.create())
				.build()
			USER_API = retrofit.create(UserApi::class.java)
			FILE_API = retrofit.create(FileApi::class.java)
			ORDERS_API = retrofit.create(OrdersApi::class.java)
			LOG_API = retrofit.create(LogApi::class.java)
		}
		
		
		fun errorProcessing(view: View?, response: ResponseBody?, activity: Activity? = null) {
			//обработчик ошибок запроса
			try {
				if (view != null) {
					if (response != null) {
						val jsonError = JSONObject(response.string())
						activity?.runOnUiThread {
							Snackbar.make(
								view,
								jsonError.getString("errorMessage").toString(),
								Snackbar.LENGTH_SHORT
							).show()
						}
						Log.e(TAG, response.string())
					}
				} else if (response != null) {
					Log.e(TAG, JSONObject(response.string()).getString("errorMessage").toString())
				}
			} catch (e: Exception) {
				
				Log.e(TAG, e.toString())
			}
		}
		
		fun onFailure(t: Throwable) {
			Log.e(TAG, "FAILURE $t")
		}
		
	}
	
	
}

