package octii.app.taxiapp.services.socket

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import octii.app.taxiapp.R
import octii.app.taxiapp.models.MessageType
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.responses.ResponseModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.scripts.logInfo
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
        SocketHelper.resetSubscriptions()

        connectToMainTopic()
        setTimer()
    }

    private fun setTimer(){
        //таймер с записью в бд
        timer.post(object : Runnable {
            override fun run() {
                if (running){
                    if (!SocketHelper.mStompClient.isConnected) SocketHelper.connect()
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun connectToMainTopic(){
        /**
         * Если пользователь не залогинен, то подключаемся к новому топику по рандомно созданному UUID
         * Когда пользователь проходит авторизацию, то сохраняем UUID и токен в SharedPrefs,
         * и слушаем события об авторизации каждые N секунд, и обновляем данные
         */
        logInfo(mainTopic)
        topic(mainTopic)
    }

    private fun topic(path : String) {

        val topic = SocketHelper.mStompClient.topic(path)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ topicMessage : StompMessage ->

            logInfo("topic msg: $topicMessage")

            val responseModel : ResponseModel = gson.fromJson(topicMessage.payload, ResponseModel::class.java)

            when(responseModel.type){

                MessageType.ORDER_ACCEPT -> {
                    logInfo("order accepted")
                    if (responseModel.body != null){
                        val order = requests.orderRequests
                            .getOrderModel(responseModel.body!! as OrdersModel,
                                isOrdered = false,
                                isAccepted = true)
                    }
                }

                MessageType.ORDER_REJECT -> {
                    logInfo("order rejected")
                    if (responseModel.body != null){
                        //logError(responseModel.body.toString())
                        val order = requests.orderRequests
                            .getOrderModel(responseModel.body!! as OrdersModel, false)
                    }
                }

                MessageType.ORDER_FINISHED -> {
                    logInfo("order finished")
                    if (responseModel.body != null){
                        //logError(responseModel.body.toString())
                        val order = requests.orderRequests
                            .getOrderModel(responseModel.body!! as OrdersModel,
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
                    if (responseModel.body != null){
                        //logError(responseModel.body.toString())
                        val order = requests.orderRequests
                            .getOrderModel(responseModel.body!!, true)
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

                connectToMainTopic()
            logInfo("Application was connected to WebSockets path: $path")
        }, { throwable ->
                logError("ttt :$throwable")
                throwable.printStackTrace()
                connectToMainTopic()
            })
        SocketHelper.compositeDisposable.add(topic)
        SocketHelper.mStompClient.connect()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        logInfo("destroy")
    }
}