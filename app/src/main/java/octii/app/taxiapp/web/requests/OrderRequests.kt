package octii.app.taxiapp.web.requests

import android.app.Activity
import android.view.View
import com.google.gson.Gson
import octii.app.taxiapp.models.driver.DriverModel
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.web.HttpHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderRequests(private val view : View? = null, private val activity: Activity? = null) {

    private val gson = Gson()

    fun getOrderModel(order : OrdersModel, isOrdered : Boolean = false, isAccepted : Boolean = false) : OrdersModel {
        logInfo("order $order")
        OrdersModel.mId = order.id
        OrdersModel.mDriverID = order.driverID
        OrdersModel.mCustomerID = order.customerID
        OrdersModel.mUuid = order.uuid
        OrdersModel.mIsFinished = order.isFinished

        if (order.driver != null){

            OrdersModel.mDriver = order.driver!!
            if (order.driver?.driver?.prices != null) {
                DriverModel.mPrices.pricePerKm = order.driver?.driver?.prices!!.pricePerKm
                DriverModel.mPrices.pricePerMinute = order.driver?.driver?.prices!!.pricePerMinute
                DriverModel.mPrices.priceWaitingMin = order.driver?.driver?.prices!!.priceWaitingMin
            }
        }
        if (order.customer != null)
            OrdersModel.mCustomer = order.customer!!

        OrdersModel.isOrdered = isOrdered

        OrdersModel.isAccepted = isAccepted

        return OrdersModel()
    }

    fun orderCheck(model : UserModel){
        HttpHelper.ORDERS_API.ordersCheck(model).enqueue(object : Callback<OrdersModel>{
            override fun onResponse(call: Call<OrdersModel>, response: Response<OrdersModel>) {
                if (response.isSuccessful){
                    getOrderModel(response.body()!!, false,
                        response.body()!!.driverID > 0 && !response.body()!!.isFinished)
                }
            }

            override fun onFailure(call: Call<OrdersModel>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

}