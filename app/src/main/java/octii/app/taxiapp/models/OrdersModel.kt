package octii.app.taxiapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import octii.app.taxiapp.models.user.UserModel
import java.util.*

@Entity
class OrdersModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = mId,
    @ColumnInfo(name = "driver_id")
    var driverID : Long = mDriverID,
    @ColumnInfo(name = "customer_id")
    var customerID : Long = mCustomerID,
    @ColumnInfo(name = "uuid")
    var uuid : String = mUuid,
    @ColumnInfo(name = "is_finished")
    var isFinished : Boolean = mIsFinished,
    var driver : UserModel = mDriver,
    var customer : UserModel = mCustomer
) {
    companion object{
        @JvmStatic
        var mId: Long = UserModel.uID
        @JvmStatic
        var mDriverID : Long = UserModel.uID
        @JvmStatic
        var mCustomerID : Long = UserModel.uID
        @JvmStatic
        var mUuid : String = UUID.randomUUID().toString()
        @JvmStatic
        var mIsFinished : Boolean = false
        @JvmStatic
        var mDriver : UserModel = UserModel()
        @JvmStatic
        var mCustomer : UserModel = UserModel()
    }
}
