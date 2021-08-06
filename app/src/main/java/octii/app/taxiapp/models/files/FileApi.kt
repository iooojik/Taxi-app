package octii.app.taxiapp.models.files

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface FileApi {
    @Multipart
    //@POST("/files/uploadImage?")
    @POST("/taxi/files/uploadImage?")
    fun uploadImage(@Part file: MultipartBody.Part, @Query("type") type : String,
                    @Query("userUUID") uuid : String): Call<FileModel>
}