/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 17.08.2021, 22:43                       *
 ******************************************************************************/

package octii.app.taxiapp.models.files

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class FileModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = -1,
    @ColumnInfo(name = "url")
    var url: String = "",
    @ColumnInfo(name = "file_name")
    var fileName: String = "",
    @ColumnInfo(name = "file_extension")
    var fileExtension: String = "",
    @ColumnInfo(name = "file_type")
    var type: String = "",
    @ColumnInfo(name = "is_new")
    var isNew: Boolean = true,
)