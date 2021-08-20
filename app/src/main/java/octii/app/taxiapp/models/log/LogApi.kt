package octii.app.taxiapp.models.log

import octii.app.taxiapp.models.files.FileModel
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface LogApi {
    @Multipart
    //@POST("/files/uploadImage?")
    @POST("/taxi/logs/send.log?")
    fun sendLogs(
        @Part file: MultipartBody.Part, @Query("userUUID") uuid: String): Call<LogModel>
}