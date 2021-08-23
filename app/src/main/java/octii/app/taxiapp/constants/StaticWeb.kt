package octii.app.taxiapp.constants

class StaticWeb {
	companion object {
		//private const val SERVER_IP = "iooojik.ru"
		
		private const val SERVER_IP = "192.168.0.101"
		//private const val SERVER_PORT = "8443"
		
		private const val SERVER_PORT = "8080"
		//const val WEB_SOCKET_URL = "wss://$SERVER_IP:8443/taxi/ws/websocket"
		
		//const val WEB_SOCKET_URL = "wss://iooojik.ru:8443/taxi/ws/websocket"
		const val WEB_SOCKET_URL = "ws://$SERVER_IP:$SERVER_PORT/ws/websocket"
		//const val REST_URL = "https://$SERVER_IP:$SERVER_PORT/"
		const val REST_URL = "http://$SERVER_IP:$SERVER_PORT/"
		const val SERVER_HEARTBEAT = 10000
	}
}