package octii.app.taxiapp.services.taximeter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import octii.app.taxiapp.services.location.LocationService

class TaximeterReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val intentService = Intent(context, TaximeterService::class.java)
        context!!.startService(intentService)
    }
}