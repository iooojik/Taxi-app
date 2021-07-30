package octii.app.taxiapp.sockets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SocketReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val intentService = Intent(context, SocketService::class.java)
        context.startService(intentService)
    }
}