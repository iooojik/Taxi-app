package octii.app.taxiapp.scripts

import android.app.Activity
import android.content.Context
import android.content.Intent
import octii.app.taxiapp.constants.Static

fun showSnackbarLong(activity: Activity, message : String){

}

fun showSnackbarShort(activity: Activity, message : String){

}

fun showSnackbar(context: Context, message: String){
    if (message.length < 25){
        context.sendBroadcast(Intent(Static.SNACKBAR_INTENT_FILTER)
            .putExtra(Static.SNACKBAR_MESSAGE, message).putExtra(Static.SNACKBAR_MESSAGE_LENGTH, -1))
    } else {
        context.sendBroadcast(Intent(Static.SNACKBAR_INTENT_FILTER)
            .putExtra(Static.SNACKBAR_MESSAGE, message).putExtra(Static.SNACKBAR_MESSAGE_LENGTH, 0))
    }
}