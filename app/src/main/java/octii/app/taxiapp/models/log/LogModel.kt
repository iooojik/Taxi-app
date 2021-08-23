/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 20.08.2021, 11:32                       *
 ******************************************************************************/

package octii.app.taxiapp.models.log

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey


data class LogModel (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = -1,
    @ColumnInfo(name = "user_uuid")
    var userUUID : String,
    @ColumnInfo(name = "path")
    var path : String
)