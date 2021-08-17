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
        var mPricePerMinute: Float = 15f
        var mPricePerKm: Float = 15f
        var mPriceWaitingMin: Float = 15f
    }
}