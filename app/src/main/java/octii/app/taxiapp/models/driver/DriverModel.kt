package octii.app.taxiapp.models.driver

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DriverModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = mId,
    @ColumnInfo(name = "driver_id")
    var driverID : Long = mDriverID,
    @ColumnInfo(name = "ride_distance")
    var rideDistance : Float = mRideDistance,
    @ColumnInfo(name = "is_working")
    var isWorking : Boolean = mIsWorking,
    var prices: Prices = mPrices
) {
    companion object {
        @JvmStatic
        var mId: Long = -1
        @JvmStatic
        var mDriverID : Long = (-1).toLong()
        @JvmStatic
        var mRideDistance : Float = 15f
        @JvmStatic
        var mIsWorking : Boolean = false
        @JvmStatic
        var mPrices : Prices = Prices()
    }
}
