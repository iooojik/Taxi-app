package octii.app.taxiapp.sockets

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import octii.app.taxiapp.R
import octii.app.taxiapp.web.SocketHelper
import octii.app.taxiapp.models.MessageType
import octii.app.taxiapp.models.OrdersModel
import octii.app.taxiapp.models.responses.ResponseModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.web.requests.Requests
import ua.naiksoftware.stomp.dto.StompMessage
import java.util.*

class SocketService : Service() {

    companion object{
        @JvmStatic
        var serviceRunning = false
        @JvmStatic
        val updateTimer = Timer()
        @JvmStatic
        val handler = Handler()
    }

    private val gson = Gson()
    private var topic : Disposable = CompositeDisposable()
    private val uuid : String = UserModel.mUuid
    private val mainTopic : String = "/topic/${UserModel.mUuid}"
    private lateinit var requests: Requests

    override fun onCreate() {
        super.onCreate()
        requests = Requests()
        SocketHelper.connect()
        connectToMainTopic()
        //doTask()
    }

    private fun doTask() {

        handler.post(object : Runnable{
            override fun run() {
                if (SocketHelper.mStompClient.isConnected) socketMaintenance()
                handler.postDelayed(this, (0.25*60*1000).toLong())
            }
        })

        //SocketHelper.mStompClient.send("/messenger/chat.addUser.public", "{\"sender\": \"user\", \"type\": \"JOIN\"}").subscribe()
    }

    fun socketMaintenance(){
        //if (!MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TOKEN, "").isNullOrEmpty())
        //    SocketHelper.authorization()
    }

    private fun connectToMainTopic(){
        /**
         * Если пользователь не залогинен, то подключаемся к новому топику по рандомно созданному UUID
         * Когда пользователь проходит авторизацию, то сохраняем UUID и токен в SharedPrefs,
         * и слушаем события об авторизации каждые N секунд, и обновляем данные
         */
        //Static.updateUUID()

        logInfo(mainTopic)
        topic(mainTopic)
    }

    private fun topic(path : String) {

        topic = SocketHelper.mStompClient.topic(path).subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()).subscribe({ topicMessage : StompMessage ->
            val responseModel : ResponseModel = gson.fromJson(topicMessage.payload, ResponseModel::class.java)
            logInfo(topicMessage)

            when(responseModel.type){

                MessageType.ORDER_ACCEPT -> {
                    if (responseModel.body != null){
                        val order = requests.orderRequests.getOrderModel(responseModel.body as OrdersModel,
                            isOrdered = false,
                            isAccepted = true)
                    }
                }

                MessageType.ORDER_REJECT -> {
                    if (responseModel.body != null){
                        val order = requests.orderRequests.getOrderModel(responseModel.body as OrdersModel, false)
                    }
                }

                MessageType.ORDER_FINISHED -> {
                    if (responseModel.body != null){
                        val order = requests.orderRequests.getOrderModel(responseModel.body as OrdersModel, false)
                    }
                }

                MessageType.NO_ORDERS -> {
                    Toast.makeText(applicationContext,
                        resources.getString(R.string.all_drivers_are_busy), Toast.LENGTH_SHORT).show()
                }

                MessageType.ORDER_REQUEST -> {
                    if (responseModel.body != null){
                        //val order = requests.orderRequests.getOrderModel(responseModel.body, true)
                    }

                }

                else -> {
                    logInfo("No matchable types")
                    Toast.makeText(applicationContext,
                        resources.getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }
            logInfo("Application was connected to WebSockets path: $path")

        }, { throwable ->
            logError(throwable)
            throwable.printStackTrace()
            SocketHelper.resetSubscriptions()
        })
        SocketHelper.compositeDisposable.add(topic)
    }



    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketHelper.compositeDisposable.dispose()
    }
}