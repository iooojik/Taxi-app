package octii.app.taxiapp.models.user

import octii.app.taxiapp.models.AuthorizationModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {

    @POST("/users/login")
    fun login(@Body userModel: UserModel) : Call<AuthorizationModel>

    @POST("/users/login.token")
    fun loginWithToken(@Body userModel: UserModel) : Call<AuthorizationModel>

    @POST("/users/update")
    fun update(@Body userModel: UserModel) : Call<UserModel>

}