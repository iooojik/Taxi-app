package octii.app.taxiapp

class Static {
    companion object{
        private const val SERVER_IP = "192.168.0.100"
        private const val SERVER_PORT = "8080"
        const val WEB_SOCKET_URL = "ws://$SERVER_IP:$SERVER_PORT/ws/websocket"
        const val REST_URL = "http://$SERVER_IP:$SERVER_PORT/"
        const val SHARED_PREFERENCES_USER = "USER PREFERENCES"
        const val SHARED_PREFERENCES_USER_UUID = "USER PREFERENCES UUID"
        const val SHARED_PREFERENCES_USER_TOKEN = "USER PREFERENCES TOKEN"
        const val SHARED_PREFERENCES_USER_TYPE = "USER PREFERENCES TYPE"
        const val SHARED_PREFERENCES_APPLICATION = "APPLICATION PREFERENCES"
        const val DRIVER_TYPE = "driver"
        const val CLIENT_TYPE = "client"
        //@JvmStatic
        //fun updateUUID() : String = if (UserModel.uuid.isNotEmpty()) UserModel.uuid else "${UUID.randomUUID()}"
    }
}