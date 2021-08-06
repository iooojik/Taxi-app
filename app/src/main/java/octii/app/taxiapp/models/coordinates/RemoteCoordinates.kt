package octii.app.taxiapp.models.coordinates

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
class RemoteCoordinates(
    @ColumnInfo(name = "latitude")
    var latitude : Double = remoteLat,
    @ColumnInfo(name = "longitude")
    var longitude : Double = remoteLon,
){
    companion object{
        var remoteLat : Double = 0.0
        var remoteLon : Double = 0.0
    }
}
