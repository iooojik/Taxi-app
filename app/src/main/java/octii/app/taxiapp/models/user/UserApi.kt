package octii.app.taxiapp.models.user

import octii.app.taxiapp.models.TokenAuthorization
import retrofit2.Call
import retrofit2.http.*

interface UserApi {

    @POST("/users/login")
    fun login(@Body userModel: UserModel) : Call<UserModel>

    @POST("/users/login.token")
    fun loginWithToken(@Body userModel: UserModel) : Call<TokenAuthorization>

    @POST("/users/update")
    fun update(@Body userModel: UserModel) : Call<UserModel>

}