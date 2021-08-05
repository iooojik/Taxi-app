package octii.app.taxiapp.models.orders

import octii.app.taxiapp.models.AuthorizationModel
import octii.app.taxiapp.models.user.UserModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface OrdersApi  {
    @POST("/orders/check")
    fun ordersCheck(@Body userModel: UserModel) : Call<OrdersModel>
}