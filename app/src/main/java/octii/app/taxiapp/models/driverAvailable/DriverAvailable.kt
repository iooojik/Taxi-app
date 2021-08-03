package octii.app.taxiapp.models.driverAvailable

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import octii.app.taxiapp.models.user.UserModel

@Entity
data class DriverAvailable(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = mId,
    @ColumnInfo(name = "driver_id")
    var driverID : Long = mDriverID,
    @ColumnInfo(name = "ride_distance")
    var rideDistance : Float = mRideDistance,
    @ColumnInfo(name = "price_per_minute")
    var pricePerMinute : Float = mPricePerMinute,
    @ColumnInfo(name = "price_per_km")
    var pricePerKm : Float = mPricePerKm,
    @ColumnInfo(name = "price_waiting_min")
    var priceWaitingMin : Float = mPriceWaitingMin,
    @ColumnInfo(name = "is_working")
    var isWorking : Boolean = mIsWorking
) {
    companion object {

        var mId: Long = -1

        var mDriverID : Long = (-1).toLong()

        var mRideDistance : Float = 15f

        var mPricePerMinute : Float = 1f

        var mPricePerKm : Float = 10f

        var mPriceWaitingMin : Float = 1f

        var mIsWorking : Boolean = false
    }
}
