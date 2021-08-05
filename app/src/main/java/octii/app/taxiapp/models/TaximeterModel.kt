package octii.app.taxiapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import octii.app.taxiapp.constants.OrderActions
import octii.app.taxiapp.models.driver.Prices
import octii.app.taxiapp.models.orders.OrdersModel
import java.util.*

@Entity
data class TaximeterModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = -1,
    @ColumnInfo(name = "timestamp")
    var timeStamp : String? = Date().toString(),
    @ColumnInfo(name = "action")
    var action : String = OrderActions.ACTION_NO,
    @ColumnInfo(name = "coordinates_id")
    var coordinatesId : Long? = (-1).toLong(),
    @ColumnInfo(name = "prices_id")
    var pricesId : Long? = (-1).toLong(),
    @ColumnInfo(name = "order_id")
    var orderId : Long? = (-1).toLong(),
    var coordinates : CoordinatesModel = CoordinatesModel(),
    var prices : Prices = Prices(),
    var orderModel: OrdersModel = OrdersModel()
)
