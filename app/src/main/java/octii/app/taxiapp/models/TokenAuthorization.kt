package octii.app.taxiapp.models

import octii.app.taxiapp.models.user.UserModel

data class TokenAuthorization(
    val user : UserModel,
    val order : OrdersModel? = null
)
