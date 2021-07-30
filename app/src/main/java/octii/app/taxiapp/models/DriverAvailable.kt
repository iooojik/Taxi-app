package octii.app.taxiapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import octii.app.taxiapp.models.user.UserModel

@Entity
data class DriverAvailable(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = -1,
    @ColumnInfo(name = "driver_id")
    var driverID : Long = (-1).toLong(),
    @ColumnInfo(name = "ride_distance")
    var rideDistance : Int = 15,
    @ColumnInfo(name = "price_per_minute")
    var pricePerMinute : Int = 1,
    @ColumnInfo(name = "price_per_km")
    var pricePerKm : Int = 10,
    @ColumnInfo(name = "price_waiting_min")
    var priceWaitingMin : Int = 1,
    @ColumnInfo(name = "is_working")
    var isWorking : Boolean = false,
    var driver : UserModel

)
