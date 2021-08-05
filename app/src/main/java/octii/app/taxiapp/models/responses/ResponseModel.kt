package octii.app.taxiapp.models.responses

import octii.app.taxiapp.models.MessageType
import octii.app.taxiapp.models.orders.OrdersModel


data class ResponseModel(
    var type: MessageType? = null,
    var body : OrdersModel? = null
)