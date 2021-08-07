package octii.app.taxiapp.models.orders

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import octii.app.taxiapp.models.TaximeterModel
import octii.app.taxiapp.models.user.UserModel

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
    @ColumnInfo(name = "is_new")
    var isNew : Boolean = false,
    var driver : UserModel? = mDriver,
    var customer : UserModel? = mCustomer,
    var taximeterModel: TaximeterModel? = null
) {
    companion object{
        @JvmStatic
        var mId: Long = UserModel.uID
        @JvmStatic
        var mDriverID : Long = UserModel.uID
        @JvmStatic
        var mCustomerID : Long = UserModel.uID
        @JvmStatic
        var mUuid : String = ""
        @JvmStatic
        var mIsFinished : Boolean = true
        @JvmStatic
        var mDriver : UserModel = UserModel()
        @JvmStatic
        var mCustomer : UserModel = UserModel()
        @JvmStatic
        var isOrdered = false
        @JvmStatic
        var isAccepted = false
    }
}
