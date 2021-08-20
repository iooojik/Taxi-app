package octii.app.taxiapp.scripts

import android.content.Context
import android.util.Log
import octii.app.taxiapp.R
import octii.app.taxiapp.models.files.CountingFileRequestBody
import octii.app.taxiapp.models.log.LogModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.web.HttpHelper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.file.Files
import java.util.*

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

fun sendLogs(context : Context){
    try {
        HttpHelper.doRetrofit()
        val fileName = "${UserModel.mUuid}_${Date().toString().replace(" ", "-")}.log"
        val file = File(context.getExternalFilesDir(null), fileName)
        file.createNewFile()
        file.writeBytes(LogSender().getLogs().toByteArray())
        logError(LogSender().getLogs().length)
        val requestFile: RequestBody =
            file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData("file", fileName, requestFile)

        HttpHelper.LOG_API.sendLogs(body, UserModel.mUuid).enqueue(object : Callback<LogModel>{
            override fun onResponse(call: Call<LogModel>, response: Response<LogModel>) {
                if (response.isSuccessful) showSnackbar(context, context.resources.getString(R.string.send))
                else showSnackbar(context, context.resources.getString(R.string.error))
            }

            override fun onFailure(call: Call<LogModel>, t: Throwable) {
                showSnackbar(context, context.resources.getString(R.string.error))
            }
        })
    }catch (e : Exception){
        e.printStackTrace()
        showSnackbar(context, context.resources.getString(R.string.error))
    }
}