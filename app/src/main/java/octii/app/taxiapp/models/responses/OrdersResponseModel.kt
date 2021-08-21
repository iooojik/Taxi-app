package octii.app.taxiapp.models.responses

import octii.app.taxiapp.constants.sockets.MessageType
import octii.app.taxiapp.models.coordinates.CoordinatesModel
import octii.app.taxiapp.models.orders.OrdersModel


data class OrdersResponseModel(
	var type: MessageType? = null,
	var order: OrdersModel? = null,
	var coordinates: CoordinatesModel? = null,
)