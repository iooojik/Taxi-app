/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 16:50                       *
 ******************************************************************************/

package octii.app.taxiapp.models

import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.user.UserModel

data class AuthorizationModel(
	val user: UserModel? = null,
	val lastOrder: OrdersModel? = null,
)
