package octii.app.taxiapp.models

import octii.app.taxiapp.models.user.UserModel

data class AuthorizationModel(
    val user : UserModel? = null,
    val order : OrdersModel? = null
)