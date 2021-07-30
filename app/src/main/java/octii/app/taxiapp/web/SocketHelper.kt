package octii.app.taxiapp.web

import com.google.gson.Gson
import octii.app.taxiapp.Static
import octii.app.taxiapp.models.UserModel
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient


class SocketHelper {
    companion object{

        val mStompClient: StompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, Static.WEB_SOCKET_URL)
        @JvmStatic
        var isConnected = mStompClient.isConnected
        private val gson = Gson()


        fun connect(){
            mStompClient.connect()
            //connectToRoom()
        }

        fun authorization(){
            //mStompClient.send("/messenger/authorization.${UserModel.mUuid}", gson.toJson(UserModel())).subscribe()
        }

        private fun connectToRoom(){

        }
    }

}