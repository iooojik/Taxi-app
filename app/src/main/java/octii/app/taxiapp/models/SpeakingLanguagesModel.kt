/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 16:50                       *
 ******************************************************************************/

package octii.app.taxiapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import octii.app.taxiapp.locale.LocaleUtils
import octii.app.taxiapp.models.user.UserModel

@Entity
class SpeakingLanguagesModel(
	@ColumnInfo(name = "language")
	var language: String = LocaleUtils.SERBIAN,
	@ColumnInfo(name = "language")
	var userId: Long = UserModel.uID,
)
