package octii.app.taxiapp.ui.maps.driver.recivers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.maps.GoogleMap
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.constants.StaticOrders
import octii.app.taxiapp.databinding.FragmentDriverMapBinding
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.scripts.down
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.scripts.up
import octii.app.taxiapp.ui.maps.driver.DriverMapFragment
import octii.app.taxiapp.ui.maps.driver.DriverOrderBottomSheet
import octii.app.taxiapp.ui.utils.FragmentHelper

class DriverOrderReciever(
    private val binding: FragmentDriverMapBinding,
    private val activity: Activity,
    private val googleMap: GoogleMap?,
    private val driverMapFragment: DriverMapFragment,
    private val fragmentContext: Context,
) : BroadcastReceiver(), FragmentHelper {
	
	private lateinit var driverOrderBottomSheet: DriverOrderBottomSheet
	
	override fun onReceive(context: Context?, intent: Intent?) {
		if (intent != null) {
			when (intent.getStringExtra(StaticOrders.ORDER_STATUS)) {
				StaticOrders.ORDER_STATUS_REQUEST -> {
					logInfo("order status ${StaticOrders.ORDER_STATUS_REQUEST} \n ordered: ${DriverMapFragment.ordered}")
					if (DriverMapFragment.ordered) {
						DriverMapFragment.ordered = false
						driverOrderBottomSheet =
							DriverOrderBottomSheet(fragmentContext, activity, OrdersModel())
						showBottomSheet()
					}
				}
				StaticOrders.ORDER_STATUS_ACCEPTED -> {
					logInfo("order status ${StaticOrders.ORDER_STATUS_ACCEPTED}")
					logInfo("${OrdersModel.mIsAccepted} ${OrdersModel.isOrdered} ${OrdersModel.mId > 0}")
					logInfo(OrdersModel.mId)
					
					if (!OrdersModel.mIsAccepted && !OrdersModel.isOrdered && OrdersModel.mId > 0) {
						//проверка, когда пользователь вышел из окна принятия заказа
						driverOrderBottomSheet =
							DriverOrderBottomSheet(fragmentContext, activity, OrdersModel())
						showBottomSheet()
					} else {
						hideBottomSheet()
						driverMapFragment.setOrderDetails()
					}
				}
				StaticOrders.ORDER_STATUS_FINISHED -> {
					logInfo("order status ${StaticOrders.ORDER_STATUS_FINISHED}")
					DriverMapFragment.ordered = true
					binding.fabSettings.show()
					binding.fabShowOrderDetails.setOnClickListener {
						if (it.tag == Static.EXPAND_MORE_FAB) {
							synchronized(this) {
								binding.orderDetails.down(activity)
								driverMapFragment.hideDriverFabOrderDetails(true,
									activity = activity,
									binding = binding)
							}
						} else {
							binding.fabShowOrderDetails.setImageResource(R.drawable.outline_expand_more_24)
							binding.fabShowOrderDetails.tag = Static.EXPAND_MORE_FAB
							binding.orderDetails.up(activity)
						}
					}
					googleMap?.clear()
				}
				
				StaticOrders.ORDER_STATUS_REJECTED -> {
					DriverMapFragment.ordered = true
					logInfo("order status ${StaticOrders.ORDER_STATUS_REJECTED}")
					hideBottomSheet()
				}
			}
		}
	}
	
	private fun hideBottomSheet(){
		try {
			driverOrderBottomSheet.hide()
		} catch (e : Exception){
			logInfo(e.stackTrace)
		}
	}
	
	private fun showBottomSheet(){
		try {
			driverOrderBottomSheet.show()
		} catch (e : Exception){
			logInfo(e.stackTrace)
		}
	}
}