/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 17.08.2021, 22:43                       *
 ******************************************************************************/

package octii.app.taxiapp.models.coordinates

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import octii.app.taxiapp.services.location.MyLocationListener

@Entity
data class CoordinatesModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = -1,
    @ColumnInfo(name = "latitude")
    var latitude: Double = MyLocationListener.latitude,
    @ColumnInfo(name = "longitude")
    var longitude: Double = MyLocationListener.longitude,
)
