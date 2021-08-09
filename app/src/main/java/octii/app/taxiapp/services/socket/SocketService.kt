package octii.app.taxiapp.services.socket

import android.app.Service
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
import octii.app.taxiapp.models.responses.TaximeterResponseModel
import octii.app.taxiapp.models.coordinates.RemoteCoordinates
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.responses.OrdersResponseModel
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

    private fun setTimer(){
        //таймер с записью в бд
        timer.post(object : Runnable {
            override fun run() {
                if (running){
                    handler.postDelayed(this, 10000)
                }
            }
        })
    }

    private fun connectToMainTopics(){
        /**
         * Если пользователь не залогинен, то подключаемся к новому топику по рандомно созданному UUID
         * Когда пользователь проходит авторизацию, то сохраняем UUID и токен в SharedPrefs,
         * и слушаем события об авторизации каждые N секунд, и обновляем данные
         */
        SocketHelper.connect()
        val userUUUID = getUserUUID()
        if (userUUUID != null && userUUUID.trim().isNotEmpty()) {
            val mainTopicURL = "/topic/$userUUUID"
            orderTopic(mainTopicURL)
            taximeterTopic("$mainTopicURL/taximeter")
            SocketHelper.mStompClient.connect()
            logInfo(mainTopicURL)
        }
    }

    private fun orderTopic(path : String) {
        val topic = SocketHelper.mStompClient.topic(path)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ topicMessage : StompMessage ->

                logError("topic msg: $topicMessage")

                val responseModel : OrdersResponseModel = gson.fromJson(topicMessage.payload, OrdersResponseModel::class.java)

                when(responseModel.type){

                    MessageType.ORDER_ACCEPT -> {
                        logInfo("order accepted")
                        if (responseModel.order != null) {
                            requests.orderRequests.getOrderModel(responseModel.order!!, isOrdered = false, isAccepted = true)
                        }
                        MyPreferences.clearTaximeter()
                        MyLocationListener.distance = 0f
                        MyPreferences.taximeterPreferences?.let {
                            MyPreferences.saveToPreferences(it,
                                StaticTaximeter.SHARED_PREFERENCES_UPDATING_COORDINATES, true)
                        }
                        sendBroadcast(orderIntent.putExtra(StaticOrders.ORDER_STATUS, StaticOrders.ORDER_STATUS_ACCEPTED))
                    }

                    MessageType.ORDER_REJECT -> {
                        logInfo("order rejected")
                        if (responseModel.order != null){
                            requests.orderRequests.getOrderModel(responseModel.order!!, false)
                        }
                    }

                    MessageType.ORDER_FINISHED -> {
                        logInfo("order finished")

                        OrdersModel.isOrdered = false
                        OrdersModel.isAccepted = false
                        if (responseModel.order != null){
                            requests.orderRequests.getOrderModel(responseModel.order!!, isOrdered = false, isAccepted = false)
                        }
                        MyPreferences.taximeterPreferences?.let {
                            MyPreferences.saveToPreferences(it,
                                StaticTaximeter.SHARED_PREFERENCES_UPDATING_COORDINATES, false)
                        }
                        orderIntent.putExtra(StaticOrders.ORDER_STATUS, StaticOrders.ORDER_STATUS_FINISHED)
                        sendBroadcast(orderIntent)
                    }

                    MessageType.NO_ORDERS -> {
                        logInfo("no orders")
                        OrdersModel.isOrdered = false
                        showSnackbar(this, resources.getString(R.string.all_drivers_are_busy))
                        orderIntent.putExtra(StaticOrders.ORDER_STATUS, StaticOrders.ORDER_STATUS_FINISHED)
                        sendBroadcast(orderIntent)
                    }

                    MessageType.ORDER_REQUEST -> {
                        logError("order request")
                        logError("resp: $responseModel")
                        orderIntent.putExtra(StaticOrders.ORDER_STATUS, StaticOrders.ORDER_STATUS_ORDERED)
                        sendBroadcast(orderIntent)
                        logInfo(responseModel.order != null)
                        if (responseModel.order != null){
                            requests.orderRequests.getOrderModel(responseModel.order!!, true)
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

    private fun taximeterTopic(path: String){
        val topic = SocketHelper.mStompClient.topic(path)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ topicMessage : StompMessage ->

                logError("taximeter: $topicMessage")

                val responseModel : TaximeterResponseModel =
                    gson.fromJson(topicMessage.payload, TaximeterResponseModel::class.java)

                when(responseModel.type){

                    TaximeterType.TAXIMETER_UPDATE -> {
                        val coordinatesModel = responseModel.coordinates
                        logError(coordinatesModel.toString())
                        if (coordinatesModel != null){
                            RemoteCoordinates.remoteLat = coordinatesModel.latitude
                            RemoteCoordinates.remoteLon = coordinatesModel.longitude
                        }
                        sendBroadcast(coordinatesIntent.putExtra(StaticCoordinates.COORDINATES_STATUS_UPDATE, StaticCoordinates.COORDINATES_STATUS_UPDATE_))
                    }

                    TaximeterType.TAXIMETER_START -> {
                        sendBroadcast(taximeterIntent.putExtra(StaticTaximeter.TAXIMETER_STATUS, TaximeterType.TAXIMETER_START))
                    }

                    TaximeterType.TAXIMETER_WAITING -> {
                        MyPreferences.taximeterPreferences?.let {
                            MyPreferences.saveToPreferences(it,
                                StaticOrders.SHARED_PREFERENCES_ORDER_IS_WAITING,
                                responseModel.isWaiting)
                        }
                        sendBroadcast(taximeterIntent.putExtra(StaticTaximeter.TAXIMETER_STATUS, TaximeterType.TAXIMETER_WAITING))
                    }

                    TaximeterType.TAXIMETER_STOP -> {
                        sendBroadcast(taximeterIntent.putExtra(StaticTaximeter.TAXIMETER_STATUS, TaximeterType.TAXIMETER_STOP))
                    }

                    else -> {
                        logInfo("No matchable types")
                        Toast.makeText(applicationContext, resources.getString(R.string.error), Toast.LENGTH_SHORT).show()
                    }
                }
                logInfo("Application was connected to WebSockets path: $path")
            }, { throwable ->
                logError("ttt :$throwable")
                throwable.printStackTrace()
            })

        SocketHelper.compositeDisposable.add(topic)
    }

    private fun getUserUUID() : String? =
        MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_UUID, "")

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        logInfo("destroy")
    }
}