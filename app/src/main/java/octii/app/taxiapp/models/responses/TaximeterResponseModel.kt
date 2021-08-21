package octii.app.taxiapp.models.responses

import octii.app.taxiapp.constants.sockets.TaximeterStatus
import octii.app.taxiapp.constants.sockets.TaximeterType
import octii.app.taxiapp.models.coordinates.CoordinatesModel

data class TaximeterResponseModel(
	var type: TaximeterType? = null,
	var coordinates: CoordinatesModel? = null,
	var status: String = TaximeterStatus.ACTION_NO,
	var isWaiting: Boolean = false,
)