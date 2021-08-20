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