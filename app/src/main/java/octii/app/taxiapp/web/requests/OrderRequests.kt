package octii.app.taxiapp.web.requests

import android.app.Activity
import android.view.View
import com.google.gson.Gson
import octii.app.taxiapp.models.OrdersModel
import octii.app.taxiapp.scripts.logInfo

class OrderRequests(private val view : View? = null, private val activity: Activity? = null) {

    private val gson = Gson()

    fun getOrderModel(json : Any, isOrdered : Boolean = false, isAccepted : Boolean = false) : OrdersModel {
        val order = gson.fromJson(gson.toJson(json), OrdersModel::class.java)
        logInfo("order request $order")

        OrdersModel.mId = order.id
        OrdersModel.mDriverID = order.driverID
        OrdersModel.mCustomerID = order.customerID
        OrdersModel.mUuid = order.uuid
        OrdersModel.mIsFinished = order.isFinished

        if (order.driver != null)
            OrdersModel.mDriver = order.driver!!
        if (order.customer != null)
            OrdersModel.mCustomer = order.customer!!

        OrdersModel.isOrdered = isOrdered

        if (order.isFinished) OrdersModel.isAccepted = false
        else OrdersModel.isAccepted = isAccepted

        return OrdersModel()
    }

}