package octii.app.taxiapp

import java.util.*

class Static {
    companion object{
        private const val SERVER_IP = "192.168.0.105"
        private const val SERVER_PORT = "8080"
        const val WEB_SOCKET_URL = "ws://$SERVER_IP:$SERVER_PORT/ws/websocket"
        const val SHARED_PREFERENCES_USER = "USER PREFERENCES"
        const val SHARED_PREFERENCES_USER_UUID = "USER PREFERENCES UUID"
        const val SHARED_PREFERENCES_USER_TOKEN = "USER PREFERENCES TOKEN"
        const val SHARED_PREFERENCES_APPLICATION = "APPLICATION PREFERENCES"
        //var uuid : String = updateUUID()
        //@JvmStatic
        //val MAIN_TOPIC = "/topic/${uuid}"
        //@JvmStatic
        //fun updateUUID() : String = if (UserModel.uuid.isNotEmpty()) UserModel.uuid else "${UUID.randomUUID()}"
    }
}