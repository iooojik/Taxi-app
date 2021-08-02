package octii.app.taxiapp.web.requests

import android.app.Activity
import android.view.View
import octii.app.taxiapp.models.OrdersModel
import octii.app.taxiapp.scripts.logDebug
import octii.app.taxiapp.scripts.logInfo

class OrderRequests(private val view : View? = null, private val activity: Activity? = null) {

    fun getOrderModel(order : OrdersModel, isOrdered : Boolean = false, isAccepted : Boolean = false) : OrdersModel {
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
        logDebug(isAccepted)
        logDebug(order.isFinished)

        if (order.isFinished) OrdersModel.isAccepted = false
        else OrdersModel.isAccepted = isAccepted

        return OrdersModel()
    }

}