/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 19.08.2021, 22:51                       *
 ******************************************************************************/

package octii.app.taxiapp.models.driver

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Prices(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = -1,
    @ColumnInfo(name = "price_per_minute")
    var pricePerMinute: Float = mPricePerMinute,
    @ColumnInfo(name = "price_per_km")
    var pricePerKm: Float = mPricePerKm,
    @ColumnInfo(name = "price_waiting_min")
    var priceWaitingMin: Float = mPriceWaitingMin,
) {
    companion object {
        @JvmStatic
        var mPricePerMinute: Float = 0.1f
        var mPricePerKm: Float = 0.1f
        var mPriceWaitingMin: Float = 0.1f
    }
}