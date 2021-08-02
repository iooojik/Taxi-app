package octii.app.taxiapp.models.responses

import octii.app.taxiapp.models.MessageType


data class ResponseModel(
    var type: MessageType? = null,
    var body : Any? = null
)