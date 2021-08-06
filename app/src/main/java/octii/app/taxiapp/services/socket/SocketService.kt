package octii.app.taxiapp.services.socket

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import octii.app.taxiapp.MyPreferences
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.models.MessageType
import octii.app.taxiapp.models.TaximeterModel
import octii.app.taxiapp.models.coordinates.RemoteCoordinates
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.responses.ResponseModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.services.taximeter.TaximeterService
import octii.app.taxiapp.web.SocketHelper
import octii.app.taxiapp.web.requests.Requests
import ua.naiksoftware.stomp.dto.StompMessage

class SocketService : Service() {

    private val gson = Gson()
    private val uuid : String = UserModel.mUuid
    private val mainTopic : String = "/topic/${UserModel.mUuid}"
    private lateinit var requests: Requests
    private val timer = Handler()
    private val handler = Handler()
    private var running = true

    override fun onCreate() {
        super.onCreate()
        requests = Requests()

        SocketHelper.connect()
        connectToMainTopics()
        setTimer()
    }

    private fun setTimer(){
        //таймер с записью в бд
        timer.post(object : Runnable {
            override fun run() {
                if (running){
                    //if (!SocketHelper.mStompClient.isConnected) SocketHelper.connect()
                    //logInfo(SocketHelper.mStompClient.isConnected)
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
        logInfo(mainTopic)
        mainTopic(mainTopic)
        taximeterTopic("$mainTopic/taximeter")
    }

    private fun mainTopic(path : String) {

        val topic = SocketHelper.mStompClient.topic(path)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ topicMessage : StompMessage ->

            logInfo("topic msg: $topicMessage")

            val responseModel : ResponseModel = gson.fromJson(topicMessage.payload, ResponseModel::class.java)

            when(responseModel.type){

                MessageType.ORDER_ACCEPT -> {
                    logInfo("order accepted")
                    MyPreferences.userPreferences?.let {
                        MyPreferences.saveToPreferences(it, Static.SHARED_PREFERENCES_ORDER_TIME, 0L)}
                    if (responseModel.order != null){
                        val order = requests.orderRequests
                            .getOrderModel(responseModel.order!! as OrdersModel,
                                isOrdered = false,
                                isAccepted = true)
                    }

                }

                MessageType.ORDER_REJECT -> {
                    logInfo("order rejected")
                    if (responseModel.order != null){
                        //logError(responseModel.body.toString())
                        val order = requests.orderRequests
                            .getOrderModel(responseModel.order!! as OrdersModel, false)
                    }
                }

                MessageType.ORDER_FINISHED -> {
                    logInfo("order finished")
                    logError(responseModel.toString())
                    MyPreferences.userPreferences?.let {
                        MyPreferences.saveToPreferences(it, Static.SHARED_PREFERENCES_ORDER_TIME, 0L) }
                    OrdersModel.isOrdered = false
                    OrdersModel.isAccepted = false
                    if (responseModel.order != null){
                        logError(responseModel.order.toString())
                        val order = requests.orderRequests
                            .getOrderModel(responseModel.order!! as OrdersModel,
                                isOrdered = false,
                                isAccepted = false)
                    }
                }

                MessageType.NO_ORDERS -> {
                    OrdersModel.isOrdered = false
                    Toast.makeText(applicationContext,
                        resources.getString(R.string.all_drivers_are_busy), Toast.LENGTH_SHORT).show()
                }

                MessageType.ORDER_REQUEST -> {
                    logInfo("order request")
                    logInfo("resp: $responseModel")
                    logInfo(responseModel.order != null)
                    if (responseModel.order != null){
                        //logError(responseModel.body.toString())
                        val order = requests.orderRequests
                            .getOrderModel(responseModel.order!!, true)
                    }
                    SocketHelper.resetSubscriptions()

                }

                MessageType.COORDINATES_UPDATE -> {

                }

                else -> {
                    logInfo("No matchable types")
                    Toast.makeText(applicationContext, resources.getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }

                connectToMainTopics()
            logInfo("Application was connected to WebSockets path: $path")
        }, { throwable ->
                logError("ttt :$throwable")
                throwable.printStackTrace()
                connectToMainTopics()
                SocketHelper.resetSubscriptions()
            })
        SocketHelper.compositeDisposable.add(topic)
        SocketHelper.mStompClient.connect()
    }

    private fun taximeterTopic(path: String){
        val topic = SocketHelper.mStompClient.topic(path)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ topicMessage : StompMessage ->

                logInfo("taximeter: $topicMessage")

                val responseModel : ResponseModel = gson.fromJson(topicMessage.payload, ResponseModel::class.java)

                when(responseModel.type){

                    MessageType.TAXIMETER_UPDATE -> {

                        val coordinatesModel = responseModel.coordinates
                        if (coordinatesModel != null){
                            RemoteCoordinates.remoteLat = coordinatesModel.latitude
                            RemoteCoordinates.remoteLon = coordinatesModel.longitude
                        }
                    }

                    else -> {
                        logInfo("No matchable types")
                        Toast.makeText(applicationContext, resources.getString(R.string.error), Toast.LENGTH_SHORT).show()
                    }
                }

                //taximeterTopic(path)
                logInfo("Application was connected to WebSockets path: $path")
            }, { throwable ->
                logError("ttt :$throwable")
                throwable.printStackTrace()
                taximeterTopic(path)
                SocketHelper.resetSubscriptions()
            })
        SocketHelper.compositeDisposable.add(topic)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        logInfo("destroy")
    }
}