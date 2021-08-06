package octii.app.taxiapp.constants

import octii.app.taxiapp.services.location.LocationService
import octii.app.taxiapp.services.socket.SocketService
import octii.app.taxiapp.services.taximeter.TaximeterService

class Static {
    companion object{
        private const val SERVER_IP = "192.168.0.101"
        private const val SERVER_PORT = "8080"
        //const val WEB_SOCKET_URL = "wss://$SERVER_IP:8443/taxi/ws/websocket"
        const val WEB_SOCKET_URL = "ws://$SERVER_IP:$SERVER_PORT/ws/websocket"
        //const val REST_URL = "https://$SERVER_IP:$SERVER_PORT/"
        const val REST_URL = "http://$SERVER_IP:$SERVER_PORT/"
        const val SHARED_PREFERENCES_USER = "USER PREFERENCES"
        const val SHARED_PREFERENCES_USER_UUID = "USER PREFERENCES UUID"
        const val SHARED_PREFERENCES_USER_TOKEN = "USER PREFERENCES TOKEN"
        const val SHARED_PREFERENCES_USER_TYPE = "USER PREFERENCES TYPE"
        const val SHARED_PREFERENCES_APPLICATION = "APPLICATION PREFERENCES"
        const val DRIVER_TYPE = "driver"
        const val CLIENT_TYPE = "client"
        val MAIN_SERVICES = listOf(SocketService::class, LocationService::class, TaximeterService::class)
        val PHOTO_TYPES = listOf("avatar", "car", "car_number", "license")
        const val PICK_IMAGE_AVATAR = 9
        const val PICK_CROP = 2
    }
}