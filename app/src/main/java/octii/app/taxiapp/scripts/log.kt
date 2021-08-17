package octii.app.taxiapp.scripts

import android.util.Log

const val PROJECT_IDENTIFIER = "IOOOJIK"
const val SERVICE_IDENTIFIER = "IOOOJIK SERVICE"
const val ERROR_IDENTIFIER = "IOOOJIK EXEPTION"

fun logDebug(message: Any) {
    Log.d(PROJECT_IDENTIFIER, message.toString())
}

fun logError(message: Any) {
    Log.e(PROJECT_IDENTIFIER, message.toString())
}

fun logInfo(message: Any) {
    Log.i(PROJECT_IDENTIFIER, message.toString())
}

fun logService(message: Any) {
    Log.d(SERVICE_IDENTIFIER, message.toString())
}

fun logExeption(message: Any) {
    Log.e(ERROR_IDENTIFIER, message.toString())
}