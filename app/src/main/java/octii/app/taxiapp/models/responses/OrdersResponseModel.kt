/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 16:50                       *
 ******************************************************************************/

package octii.app.taxiapp.models.responses

import octii.app.taxiapp.constants.sockets.MessageType
import octii.app.taxiapp.models.coordinates.CoordinatesModel
import octii.app.taxiapp.models.orders.OrdersModel


data class OrdersResponseModel(
	var type: MessageType? = null,
	var order: OrdersModel? = null,
	var coordinates: CoordinatesModel? = null,
)