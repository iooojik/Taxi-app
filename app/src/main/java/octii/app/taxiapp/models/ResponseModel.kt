package octii.app.messenger.models

data class ResponseModel(
    var type: MessageType? = null,
    var body : Any? = null
)
