package octii.app.taxiapp.web.requests

import android.app.Activity
import android.content.Intent
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.StaticOrders
import octii.app.taxiapp.models.driver.DriverModel
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.scripts.showSnackbar
import octii.app.taxiapp.web.HttpHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderRequests(private val activity: Activity? = null) {
	
	fun getOrderModel(
		order: OrdersModel,
		isOrdered: Boolean? = null,
	): OrdersModel {
		logInfo("order $order")
		OrdersModel.mId = order.id
		OrdersModel.mDriverID = order.driverID
		OrdersModel.mCustomerID = order.customerID
		OrdersModel.mUuid = order.uuid
		OrdersModel.mIsFinished = order.isFinished
		
		if (order.driver != null) {
			
			OrdersModel.mDriver = order.driver!!
			if (order.driver?.driver?.prices != null) {
				DriverModel.mPrices.pricePerKm = order.driver?.driver?.prices!!.pricePerKm
				DriverModel.mPrices.pricePerMinute = order.driver?.driver?.prices!!.pricePerMinute
				DriverModel.mPrices.priceWaitingMin = order.driver?.driver?.prices!!.priceWaitingMin
			}
		}
		if (order.customer != null)
			OrdersModel.mCustomer = order.customer!!
		
		if (isOrdered != null)
			OrdersModel.isOrdered = isOrdered
		
		OrdersModel.mIsAccepted = order.isAccepted
		
		return OrdersModel()
	}
	
	fun currentOrderUpdate(dealPrice : Float){
		HttpHelper.ORDERS_API.orderUpdate(OrdersModel(uuid = OrdersModel.mUuid, dealPrice = dealPrice))
			.enqueue(object : Callback<OrdersModel>{
			override fun onResponse(call: Call<OrdersModel>, response: Response<OrdersModel>) {
				if (response.isSuccessful) OrdersModel.mDealPrice = if (response.body() != null)
					response.body()!!.dealPrice else 0f
				else throw Exception()
			}
			
			override fun onFailure(call: Call<OrdersModel>, t: Throwable) {
				t.printStackTrace()
				if (activity != null)
					showSnackbar(activity.applicationContext, activity.resources.getString(R.string.error))
			}
		})
	}
	
	fun orderCheck(model: UserModel) {
		logInfo("order check")
		HttpHelper.ORDERS_API.ordersCheck(model).enqueue(object : Callback<OrdersModel> {
			override fun onResponse(call: Call<OrdersModel>, response: Response<OrdersModel>) {
				logInfo("${response.raw()}")
				if (response.isSuccessful) {
					logInfo("${response.body()}")
					getOrderModel(response.body()!!, false)
					activity?.sendBroadcast(Intent(StaticOrders.ORDER_STATUS_INTENT_FILTER)
						.putExtra(StaticOrders.ORDER_STATUS, StaticOrders.ORDER_STATUS_ACCEPTED))
				}
			}
			
			override fun onFailure(call: Call<OrdersModel>, t: Throwable) {
				t.printStackTrace()
			}
		})
	}
	
}