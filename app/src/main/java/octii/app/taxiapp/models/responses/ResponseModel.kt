package octii.app.taxiapp.models.responses

import octii.app.taxiapp.models.MessageType
import octii.app.taxiapp.models.TaximeterModel
import octii.app.taxiapp.models.coordinates.CoordinatesModel
import octii.app.taxiapp.models.orders.OrdersModel


data class ResponseModel(
    var type: MessageType? = null,
    var order : OrdersModel? = null,
    var taximeterModel: TaximeterModel? = null,
    var coordinates: CoordinatesModel? = null
)