/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 16:54                       *
 ******************************************************************************/

package octii.app.taxiapp.services.taximeter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaximeterReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context?, intent: Intent?) {
		val intentService = Intent(context, TaximeterService::class.java)
		context!!.startService(intentService)
	}
}