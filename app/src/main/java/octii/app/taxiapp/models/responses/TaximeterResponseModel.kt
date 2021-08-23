/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 16:50                       *
 ******************************************************************************/

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