/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 23.08.2021, 21:34                       *
 ******************************************************************************/

package octii.app.taxiapp.models.user

import octii.app.taxiapp.models.AuthorizationModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {
	
	@POST("/users/login")
	//@POST("/taxi/users/login")
	fun login(@Body userModel: UserModel): Call<AuthorizationModel>
	
	//@POST("/taxi/users/login.token")
	@POST("/users/login.token")
	fun loginWithToken(@Body userModel: UserModel): Call<AuthorizationModel>
	
	//@POST("/taxi/users/update")
	@POST("/users/update")
	fun update(@Body userModel: UserModel): Call<UserModel>
	
	//@POST("/taxi/users/update.driver.state")
	@POST("/users/update.driver.state")
	fun updateDriverState(@Body userModel: UserModel): Call<UserModel>
	
	
}