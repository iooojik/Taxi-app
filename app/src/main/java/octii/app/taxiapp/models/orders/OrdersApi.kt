/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 23.08.2021, 21:34                       *
 ******************************************************************************/

package octii.app.taxiapp.models.orders

import octii.app.taxiapp.models.user.UserModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface OrdersApi {
	@POST("/orders/check")
	//@POST("/taxi/orders/check")
	fun ordersCheck(@Body userModel: UserModel): Call<OrdersModel>
	
	@POST("/orders/update")
	//@POST("/taxi/orders/update")
	fun orderUpdate(@Body ordersModel: OrdersModel): Call<OrdersModel>
}