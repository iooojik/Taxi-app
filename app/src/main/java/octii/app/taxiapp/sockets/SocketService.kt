package octii.app.taxiapp.sockets

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import octii.app.taxiapp.web.SocketHelper
import octii.app.messenger.models.MessageType
import octii.app.messenger.models.ResponseModel
import octii.app.taxiapp.MyPreferences
import octii.app.taxiapp.Static
import octii.app.taxiapp.models.UserModel
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.scripts.logInfo
import ua.naiksoftware.stomp.dto.StompMessage
import java.util.*


class SocketService : Service() {

    companion object{
        @JvmStatic
        var serviceRunning = false
        @JvmStatic
        val updateTimer = Timer()
        @JvmStatic
        val userInfoUpdate = SocketConnection()
        @JvmStatic
        val handler = Handler()
        @JvmStatic
        fun socketMaintenance(){
            //if (!MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TOKEN, "").isNullOrEmpty())
            //    SocketHelper.authorization()
        }
    }

    private val gson = Gson()
    private var topic : Disposable = CompositeDisposable()
    private val uuid : String = UserModel.mUuid
    private val mainTopic : String = "/topic/${UserModel.mUuid}"

    override fun onCreate() {
        super.onCreate()
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
            when(responseModel.type){

                MessageType.AUTHORIZATION -> {

                }

                else -> logInfo("No matchable types")
            }
            logInfo("Application was connected to WebSockets path: $path")

        }, { throwable ->
            logError(throwable)
            throwable.printStackTrace()
        })

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    class SocketConnection : TimerTask() {

        companion object {
            var isRunning = false
        }

        override fun run() {
            if (isRunning) socketMaintenance()
        }
    }
}