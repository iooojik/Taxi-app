/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 16:50                       *
 ******************************************************************************/

package octii.app.taxiapp.models

import octii.app.taxiapp.models.coordinates.CoordinatesModel

data class TaximeterUpdate(
	var coordinates: CoordinatesModel? = null,
	var recipientUUID: String? = null,
	var orderUUID: String,
	var isWaiting: Boolean = false,
)
