package octii.app.taxiapp.models

enum class MessageType {
    CHAT, JOIN, LEAVE, AUTHORIZATION, TOKEN_AUTHORIZATION, ORDER, ORDER_REQUEST,
    ORDER_ACCEPT, ORDER_REJECT, ORDER_FINISHED, NO_ORDERS
}