package octii.app.taxiapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CoordinatesModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = -1,
    @ColumnInfo(name = "driver_id")
    var driverId : Long = -1,
    @ColumnInfo(name = "latitude")
    var latitude : Double = 0.0,
    @ColumnInfo(name = "longitude")
    var longitude : Double = 0.0,
)
