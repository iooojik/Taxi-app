/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 23.08.2021, 21:32                       *
 ******************************************************************************/

package octii.app.taxiapp.web

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.constants.StaticWeb
import octii.app.taxiapp.models.TaximeterUpdate
import octii.app.taxiapp.models.coordinates.CoordinatesModel
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.web.requests.Requests
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent


class SocketHelper {
	companion object {
		
		val orderStompClient: StompClient =
			Stomp.over(Stomp.ConnectionProvider.OKHTTP, StaticWeb.WEB_SOCKET_URL)
		
		val taximeterStompClient: StompClient =
			Stomp.over(Stomp.ConnectionProvider.OKHTTP, StaticWeb.WEB_SOCKET_URL)
		private val gson = Gson()
		var activity: Activity? = null
		
		@JvmStatic
		var compositeDisposable: CompositeDisposable = CompositeDisposable()
		
		
		fun connect() {
			orderStompClient.withClientHeartbeat(StaticWeb.SERVER_HEARTBEAT).withServerHeartbeat(StaticWeb.SERVER_HEARTBEAT)
			taximeterStompClient.withClientHeartbeat(StaticWeb.SERVER_HEARTBEAT).withServerHeartbeat(StaticWeb.SERVER_HEARTBEAT)
			resetSubscriptions()
			stompLifecycle(orderStompClient)
			stompLifecycle(taximeterStompClient)
		}
		
		private fun stompLifecycle(stomp : StompClient){
			val disposableLifecycle: Disposable? =
				stomp.lifecycle()
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe({ lifecycleEvent ->
						if (lifecycleEvent.type != null) {
							when (lifecycleEvent.type!!) {
								LifecycleEvent.Type.OPENED -> {
									activity?.sendBroadcast(Intent(Static.CONNECTION_INTENT_FILTER)
										.putExtra(Static.CONNECTION_STATUS, Static.CONNECTION_EST))
									logInfo("Stomp connection opened")
								}
								
								LifecycleEvent.Type.ERROR -> logError("Stomp connection error ${lifecycleEvent.exception}")
								
								LifecycleEvent.Type.CLOSED -> {
									logInfo("Stomp connection closed")
									activity?.sendBroadcast(Intent(Static.CONNECTION_INTENT_FILTER)
										.putExtra(Static.CONNECTION_STATUS, Static.CONNECTION_LOST))
								}
								
								LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> logError("Stomp failed server heartbeat")
								
							}
						}
					}, { throwable ->
						logError("STOMP REQUEST ERROR :$throwable")
						throwable.printStackTrace()
						activity?.sendBroadcast(Intent(Static.CONNECTION_INTENT_FILTER)
							.putExtra(Static.CONNECTION_STATUS, Static.CONNECTION_LOST))
					})
			//mStompClient.connect()
			logError("isConnected: ${stomp.isConnected}")
			compositeDisposable.add(disposableLifecycle!!)
		}
		
		private fun resetSubscriptions() {
			compositeDisposable.dispose()
			compositeDisposable = CompositeDisposable()
		}
		
		@SuppressLint("CheckResult")
		fun orderUpdate(dealPrice : Float) {
			logInfo("order update")
			
			orderStompClient.send("/requests/order.update.${UserModel.mUuid}",
				toJSON(OrdersModel(uuid = OrdersModel.mUuid, dealPrice = dealPrice)))
				.compose(
					applySchedulers()).subscribe({
					logInfo("success")
				}, { throwable ->
					logError("STOMP REQUEST ERROR :$throwable")
					throwable.printStackTrace()
				})
		}
		
		@SuppressLint("CheckResult")
		fun makeOrder() {
			orderStompClient.send("/requests/order.make.${UserModel.mUuid}", toJSON(UserModel()))
				.compose(
					applySchedulers()).subscribe({
					logInfo("success")
				}, { throwable ->
					logError("STOMP REQUEST ERROR :$throwable")
					throwable.printStackTrace()
				})
			
		}
		
		@SuppressLint("CheckResult")
		fun acceptOrder(order: OrdersModel) {
			orderStompClient.send("/requests/order.accept.${UserModel.mUuid}", toJSON(order))
				.compose(applySchedulers()).subscribe({
					logInfo("success")
				}, { throwable ->
					logError("STOMP REQUEST ERROR :$throwable")
					throwable.printStackTrace()
				})
		}
		
		@SuppressLint("CheckResult")
		fun rejectOrder(order: OrdersModel) {
			orderStompClient.send("/requests/order.reject.${UserModel.mUuid}", toJSON(order))
				.compose(applySchedulers()).subscribe({
					logInfo("success")
				}, { throwable ->
					logError("STOMP REQUEST ERROR :$throwable")
					throwable.printStackTrace()
				})
		}
		
		@SuppressLint("CheckResult")
		fun finishOrder(order: OrdersModel) {
			orderStompClient.send("/requests/order.finish.${UserModel.mUuid}", toJSON(order))
				.compose(applySchedulers()).subscribe({
					logInfo("success")
				}, { throwable ->
					logError("STOMP REQUEST ERROR :$throwable")
					throwable.printStackTrace()
				})
			UserModel.mDriver.isWorking = true
			Requests().userRequests.update {}
		}
		
		@SuppressLint("CheckResult")
		fun updateCoordinates(coordinatesModel: CoordinatesModel) {
			orderStompClient.send("/requests/navigation.coordinates.update.${UserModel.mUuid}",
				toJSON(coordinatesModel)).compose(applySchedulers()).subscribe({
				logInfo("success")
			}, { throwable ->
				logError("STOMP REQUEST ERROR :$throwable")
				throwable.printStackTrace()
			})
		}
		
		@SuppressLint("CheckResult")
		fun taximeterUpdateCoordinates(taximeterUpdate: TaximeterUpdate) {
			taximeterStompClient.send("/requests/taximeter.update.coordinates.${UserModel.mUuid}",
				toJSON(taximeterUpdate)).compose(applySchedulers()).subscribe({
			}, { throwable ->
				logError("STOMP REQUEST ERROR :$throwable")
				throwable.printStackTrace()
			})
		}
		
		@SuppressLint("CheckResult")
		fun taximeterStart(taximeterUpdate: TaximeterUpdate) {
			taximeterStompClient.send("/requests/taximeter.start.${UserModel.mUuid}",
				toJSON(taximeterUpdate)).compose(applySchedulers()).subscribe({
			}, { throwable ->
				logError("STOMP REQUEST ERROR :$throwable")
				throwable.printStackTrace()
			})
		}
		
		@SuppressLint("CheckResult")
		fun taximeterStop(taximeterUpdate: TaximeterUpdate) {
			taximeterStompClient.send("/requests/taximeter.stop.${UserModel.mUuid}",
				toJSON(taximeterUpdate)).compose(applySchedulers()).subscribe({
			}, { throwable ->
				logError("STOMP REQUEST ERROR :$throwable")
				throwable.printStackTrace()
			})
		}
		
		@SuppressLint("CheckResult")
		fun taximeterWaiting(taximeterUpdate: TaximeterUpdate, isWaiting: Boolean) {
			taximeterStompClient.send("/requests/taximeter.waiting.${UserModel.mUuid}.$isWaiting",
				toJSON(taximeterUpdate)).compose(applySchedulers()).subscribe({
			}, { throwable ->
				logError("STOMP REQUEST ERROR :$throwable")
				throwable.printStackTrace()
			})
		}
		
		private fun toJSON(model: Any): String {
			return gson.toJson(model)
		}
		
		private fun applySchedulers(): CompletableTransformer {
			return CompletableTransformer { upstream: Completable ->
				upstream
					.unsubscribeOn(Schedulers.newThread())
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
			}
		}
	}
	
}