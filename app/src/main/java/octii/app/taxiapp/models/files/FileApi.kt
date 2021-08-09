package octii.app.taxiapp.models.files

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface FileApi {
    @Multipart
    @POST("/files/uploadImage?")
    //@POST("/taxi/files/uploadImage?")
    fun uploadImage(@Part file: MultipartBody.Part, @Query("type") type : String,
                    @Query("userUUID") uuid : String): Call<FileModel>
}