package octii.app.taxiapp.scripts

import android.util.Log

const val PROJECT_IDENTIFIER = "IOOOJIK"

fun logDebug(message : Any){
    Log.d(PROJECT_IDENTIFIER, message.toString())
}

fun logError(message: Any){
    Log.e(PROJECT_IDENTIFIER, message.toString())
}

fun logInfo(message: Any){
    Log.i(PROJECT_IDENTIFIER, message.toString())
}