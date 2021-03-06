/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 23.08.2021, 12:30                       *
 ******************************************************************************/

package octii.app.taxiapp.services.socket

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.constants.StaticCoordinates
import octii.app.taxiapp.constants.StaticOrders
import octii.app.taxiapp.constants.StaticTaximeter
import octii.app.taxiapp.constants.sockets.MessageType
import octii.app.taxiapp.constants.sockets.TaximeterType
import octii.app.taxiapp.models.coordinates.CoordinatesModel
import octii.app.taxiapp.models.coordinates.RemoteCoordinates
import octii.app.taxiapp.models.driver.DriverModel
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.responses.OrdersResponseModel
import octii.app.taxiapp.models.responses.TaximeterResponseModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.*
import octii.app.taxiapp.services.location.MyLocationListener
import octii.app.taxiapp.web.SocketHelper
import octii.app.taxiapp.web.requests.Requests
import ua.naiksoftware.stomp.dto.StompMessage

class SocketService : Service() {
	
	private val gson = Gson()
	private lateinit var requests: Requests
	private val timer = Handler(Looper.getMainLooper())
	private val handler = Handler(Looper.getMainLooper())
	private var running = true
	private val orderIntent = Intent(StaticOrders.ORDER_STATUS_INTENT_FILTER)
	private val taximeterIntent = Intent(StaticTaximeter.TAXIMETER_STATUS_INTENT_FILTER)
	private val coordinatesIntent = Intent(StaticOrders.ORDER_STATUS_COORDINATES_STATUS)
	
	override fun onCreate() {
		super.onCreate()
		requests = Requests()
		logService("socket service is running")
		connectToMainTopics()
		setTimer()
	}
	
	private fun setTimer() {
		//???????????? ?? ?????????????? ?? ????
		timer.post(object : Runnable {
			override fun run() {
				if (running) {
					handler.postDelayed(this, 10000)
				}
			}
		})
	}
	
	private fun connectToMainTopics() {
		/**
		 * ???????? ???????????????????????? ???? ??????????????????, ???? ???????????????????????? ?? ???????????? ???????????? ???? ???????????????? ???????????????????? UUID
		 * ?????????? ???????????????????????? ???????????????? ??????????????????????, ???? ?????????????????? UUID ?? ?????????? ?? SharedPrefs,
		 * ?? ?????????????? ?????????????? ???? ?????????????????????? ???????????? N ????????????, ?? ?????????????????? ????????????
		 */
		SocketHelper.connect()
		val userUUUID = getUserUUID()
		if (userUUUID != null && userUUUID.trim().isNotEmpty()) {
			val mainTopicURL = "/topic/$userUUUID"
			orderTopic(mainTopicURL)
			taximeterTopic("$mainTopicURL/taximeter")
			SocketHelper.orderStompClient.connect()
			SocketHelper.taximeterStompClient.connect()
			logInfo(mainTopicURL)
		}
	}
	
	private fun orderTopic(path: String) {
		val topic = SocketHelper.orderStompClient.topic(path)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe({ topicMessage: StompMessage ->
				
				logError("topic msg: $topicMessage")
				
				val responseModel: OrdersResponseModel =
					gson.fromJson(topicMessage.payload, OrdersResponseModel::class.java)
				
				when (responseModel.type) {
					
					MessageType.ORDER_ACCEPT -> {
						logInfo("order accepted")
						if (responseModel.order != null) {
							requests.orderRequests.getOrderModel(responseModel.order!!,
								isOrdered = false)
						}
						MyPreferences.clearTaximeter()
						MyLocationListener.distance = 0f
						MyPreferences.taximeterPreferences?.let {
							MyPreferences.saveToPreferences(it,
								StaticTaximeter.SHARED_PREFERENCES_UPDATING_COORDINATES, true)
						}
						sendBroadcast(orderIntent.putExtra(StaticOrders.ORDER_STATUS,
							StaticOrders.ORDER_STATUS_ACCEPTED))
					}
					
					MessageType.ORDER_REJECT -> {
						logInfo("order rejected")
						if (responseModel.order != null) {
							requests.orderRequests.getOrderModel(responseModel.order!!, false)
						}
						orderIntent.putExtra(StaticOrders.ORDER_STATUS,
							StaticOrders.ORDER_STATUS_REJECTED)
						sendBroadcast(orderIntent)
					}
					
					MessageType.ORDER_FINISHED -> {
						logInfo("order finished")
						SocketHelper.updateCoordinates(CoordinatesModel(longitude = MyLocationListener.longitude,
							latitude = MyLocationListener.latitude))
						logInfo("${MyLocationListener.longitude} ${MyLocationListener.latitude}")
						OrdersModel.isOrdered = false
						OrdersModel.mIsAccepted = false
						OrdersModel.mId = -1
						if (responseModel.order != null) {
							requests.orderRequests.getOrderModel(responseModel.order!!,
								isOrdered = false)
						}
						MyPreferences.taximeterPreferences?.let {
							MyPreferences.saveToPreferences(it,
								StaticTaximeter.SHARED_PREFERENCES_UPDATING_COORDINATES, false)
						}
						MyPreferences.taximeterPreferences?.let {
							MyPreferences.saveToPreferences(it,
								StaticTaximeter.SHARED_PREFERENCES_TIMER_STATUS, false)
						}
						MyPreferences.taximeterPreferences?.let {
							MyPreferences.saveToPreferences(it,
								StaticOrders.SHARED_PREFERENCES_ORDER_IS_WAITING,
								false)
						}
						orderIntent.putExtra(StaticOrders.ORDER_STATUS,
							StaticOrders.ORDER_STATUS_FINISHED)
						sendBroadcast(orderIntent)
					}
					
					MessageType.NO_ORDERS -> {
						logInfo("no orders")
						OrdersModel.isOrdered = false
						orderIntent.putExtra(StaticOrders.ORDER_STATUS,
							StaticOrders.ORDER_STATUS_NO_ORDERS)
						sendBroadcast(orderIntent)
					}
					
					MessageType.ORDER_REQUEST -> {
						logError("order request")
						logError("resp: $responseModel")
						orderIntent.putExtra(StaticOrders.ORDER_STATUS,
							StaticOrders.ORDER_STATUS_REQUEST)
						sendBroadcast(orderIntent)
						logInfo(responseModel.order != null)
						if (responseModel.order != null) {
							requests.orderRequests.getOrderModel(responseModel.order!!, true)
						}
					}
					
					MessageType.ORDER_UPDATE -> {
						logInfo("order update")
						logInfo("order update model $responseModel")
						if (responseModel.order != null) {
							requests.orderRequests.getOrderModel(responseModel.order!!)
						}
					}
					
					else -> {
						logInfo("No matchable types")
						showSnackbar(this, resources.getString(R.string.error))
					}
				}
				logInfo("Application was connected to WebSockets path: $path")
			}, { throwable ->
				logError("ttt :$throwable")
				throwable.printStackTrace()
			})
		SocketHelper.compositeDisposable.add(topic)
	}
	
	private fun taximeterTopic(path: String) {
		val topic = SocketHelper.taximeterStompClient.topic(path)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe({ topicMessage: StompMessage ->
				
				logError("taximeter: $topicMessage")
				
				val responseModel: TaximeterResponseModel =
					gson.fromJson(topicMessage.payload, TaximeterResponseModel::class.java)
				
				when (responseModel.type) {
					
					TaximeterType.TAXIMETER_UPDATE -> {
						val coordinatesModel = responseModel.coordinates
						logError(coordinatesModel.toString())
						if (coordinatesModel != null) {
							RemoteCoordinates.remoteLat = coordinatesModel.latitude
							RemoteCoordinates.remoteLon = coordinatesModel.longitude
						}
						sendBroadcast(coordinatesIntent.putExtra(StaticCoordinates.COORDINATES_STATUS_UPDATE,
							StaticCoordinates.COORDINATES_STATUS_UPDATE_))
					}
					
					TaximeterType.TAXIMETER_START -> {
						sendBroadcast(taximeterIntent.putExtra(StaticTaximeter.TAXIMETER_STATUS,
							TaximeterType.TAXIMETER_START))
					}
					
					TaximeterType.TAXIMETER_WAITING -> {
						MyPreferences.taximeterPreferences?.let {
							MyPreferences.saveToPreferences(it,
								StaticOrders.SHARED_PREFERENCES_ORDER_IS_WAITING,
								responseModel.isWaiting)
						}
						sendBroadcast(taximeterIntent.putExtra(StaticTaximeter.TAXIMETER_STATUS,
							TaximeterType.TAXIMETER_WAITING))
					}
					
					TaximeterType.TAXIMETER_STOP -> {
						sendBroadcast(taximeterIntent.putExtra(StaticTaximeter.TAXIMETER_STATUS,
							TaximeterType.TAXIMETER_STOP))
					}
					
					else -> {
						logInfo("No matchable types")
						Toast.makeText(applicationContext,
							resources.getString(R.string.error),
							Toast.LENGTH_SHORT).show()
					}
				}
				logInfo("Application was connected to WebSockets path: $path")
			}, { throwable ->
				logError("ttt :$throwable")
				throwable.printStackTrace()
			})
		
		SocketHelper.compositeDisposable.add(topic)
	}
	
	private fun getUserUUID(): String? =
		MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_UUID, "")
	
	override fun onBind(intent: Intent?): IBinder? {
		return null
	}
	
	private fun getSharedPrefernces() {
		MyPreferences.userPreferences =
			getSharedPreferences(Static.SHARED_PREFERENCES_USER, Context.MODE_PRIVATE)
		MyPreferences.applicationPreferences =
			getSharedPreferences(Static.SHARED_PREFERENCES_APPLICATION, Context.MODE_PRIVATE)
		MyPreferences.taximeterPreferences =
			getSharedPreferences(StaticTaximeter.SHARED_PREFERENCES_TAXIMETER, Context.MODE_PRIVATE)
	}
	
	override fun onDestroy() {
		getSharedPrefernces()
		if (getUserUUID() != null)
			Requests().userRequests.updateDriverState(UserModel(uuid = getUserUUID()!!,
				driver = DriverModel(isWorking = false)))
		logInfo("destroy SocketService ${getUserUUID()}")
		super.onDestroy()
	}
}