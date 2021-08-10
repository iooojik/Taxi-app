package octii.app.taxiapp.models.user

import octii.app.taxiapp.models.AuthorizationModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {

    //@POST("/users/login")
    @POST("/taxi/users/login")
    fun login(@Body userModel: UserModel) : Call<AuthorizationModel>

    @POST("/taxi/users/login.token")
    //@POST("/users/login.token")
    fun loginWithToken(@Body userModel: UserModel) : Call<AuthorizationModel>

    @POST("/taxi/users/update")
    //@POST("/users/update")
    fun update(@Body userModel: UserModel) : Call<UserModel>

}