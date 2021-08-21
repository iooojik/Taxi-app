package octii.app.taxiapp.models

import octii.app.taxiapp.models.coordinates.CoordinatesModel

data class TaximeterUpdate(
	var coordinates: CoordinatesModel? = null,
	var recipientUUID: String? = null,
	var orderUUID: String,
	var isWaiting: Boolean = false,
)
