package octii.app.taxiapp.ui.maps.client.recievers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.View
import com.google.android.gms.maps.GoogleMap
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.constants.StaticOrders
import octii.app.taxiapp.databinding.FragmentClientMapBinding
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.scripts.down
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.scripts.showSnackbar
import octii.app.taxiapp.scripts.up
import octii.app.taxiapp.ui.maps.client.ClientMapFragment
import octii.app.taxiapp.ui.maps.client.ClientOrderBottomSheet

class ClientOrderReciever(
	private val binding: FragmentClientMapBinding,
	private val activity: Activity,
	private val googleMap: GoogleMap?,
	private val clientMapFragment: ClientMapFragment,
	private val fragmentContext: Context,
) : BroadcastReceiver() {
	
	
	private lateinit var clientOrderBottomSheet: ClientOrderBottomSheet
	
	
	override fun onReceive(context: Context?, intent: Intent?) {
		if (intent != null) {
			when (intent.getStringExtra(StaticOrders.ORDER_STATUS)) {
				StaticOrders.ORDER_STATUS_REQUEST -> {
					logInfo("order status ${StaticOrders.ORDER_STATUS_REQUEST}")
					if (context != null) {
						showDialog()
					}
				}
				StaticOrders.ORDER_STATUS_ACCEPTED -> {
					clientMapFragment.setOrderDetails()
					if (!OrdersModel.mIsAccepted && !OrdersModel.isOrdered && OrdersModel.mId > 0) {
						showDialog()
					} else hideDialog()
					binding.clientMapprogressBar.visibility = View.INVISIBLE
				}
				StaticOrders.ORDER_STATUS_FINISHED -> {
					binding.fabSettings.show()
					binding.clientMapprogressBar.visibility = View.INVISIBLE
					binding.fabShowOrderDetails.setOnClickListener {
						if (it.tag == Static.EXPAND_MORE_FAB) {
							synchronized(this) {
								binding.orderDetails.down(activity)
								clientMapFragment.hideClientFabOrderDetails(true,
									activity = activity,
									binding = binding)
							}
							binding.callTaxi.show()
						} else {
							binding.fabShowOrderDetails.setImageResource(R.drawable.outline_expand_more_24)
							binding.fabShowOrderDetails.tag = Static.EXPAND_MORE_FAB
							binding.orderDetails.up(activity)
						}
					}
					googleMap?.clear()
				}
				StaticOrders.ORDER_STATUS_NO_ORDERS -> {
					binding.fabSettings.show()
					binding.callTaxi.show()
					//binding.orderDetails.down(requireActivity())
					binding.clientMapprogressBar.visibility = View.INVISIBLE
					googleMap?.clear()
					showSnackbar(fragmentContext,
						activity.resources.getString(R.string.all_drivers_are_busy))
				}
				StaticOrders.ORDER_STATUS_REJECTED -> {
					hideDialog()
				}
			}
		}
	}
	
	private fun showDialog() {
		try {
			clientOrderBottomSheet =
				ClientOrderBottomSheet(fragmentContext, activity, OrdersModel())
			clientOrderBottomSheet.show()
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
	
	private fun hideDialog() {
		try {
			clientOrderBottomSheet.dismiss()
			
		} catch (e: java.lang.Exception) {
			e.printStackTrace()
		}
	}
}