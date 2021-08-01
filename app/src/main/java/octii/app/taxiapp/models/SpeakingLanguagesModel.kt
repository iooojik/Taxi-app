package octii.app.taxiapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SpeakingLanguagesModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = -1,
    @ColumnInfo(name = "driver_id")
    var driverId : Long = -1,
    @ColumnInfo(name = "language")
    var language : String = "ru"
)
