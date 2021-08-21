package octii.app.taxiapp.ui.maps.client.orderDetails

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.StaticOrders
import octii.app.taxiapp.constants.StaticTaximeter
import octii.app.taxiapp.databinding.FragmentClientTaximeterDetailsBinding
import octii.app.taxiapp.models.TaximeterUpdate
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.scripts.MyPreferences
import octii.app.taxiapp.services.location.MyLocationListener
import octii.app.taxiapp.web.SocketHelper


class TaximeterDetailsFragment : Fragment(), View.OnClickListener {
	
	lateinit var binding: FragmentClientTaximeterDetailsBinding
	
	private val taximeterReceiver: BroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent) {
			if (binding != null && binding.finishOrder.visibility == View.GONE
				&& OrdersModel.mIsAccepted && !OrdersModel.mIsFinished
			)
				binding.finishOrder.visibility = View.VISIBLE
			val time = intent.getLongExtra(StaticTaximeter.TAXIMETER_BUNDLE_TIME, 0L)
			val waitingTime =
				intent.getLongExtra(StaticTaximeter.TAXIMETER_BUNDLE_WAINTING_TIME, 0L)
			setTaximeter(time, waitingTime)
		}
	}
	
	private fun number2digits(number: Float): String = String.format("%.2f", number)
	
	private fun formatDuration(seconds: Long): String = DateUtils.formatElapsedTime(seconds)
	
	@SuppressLint("SetTextI18n")
    private fun setTaximeter(time: Long, waitingTime: Long) {
		val distance = MyLocationListener.distance
		val totalPricePerKm =
			OrdersModel.mDriver.driver.prices.pricePerKm * MyLocationListener.distance
		val totalPricePerMin = if (time / 60 < 1) OrdersModel.mDriver.driver.prices.pricePerMinute
		else OrdersModel.mDriver.driver.prices.pricePerMinute * (time / 60)
		
		val totalPriceWaiting = OrdersModel.mDriver.driver.prices.priceWaitingMin * waitingTime / 60
		
		binding.taximeter.distance.text = distance.toString()
		binding.taximeter.pricePerKm.text = number2digits(totalPricePerKm)
		binding.taximeter.pricePerMin.text = number2digits(totalPricePerMin)
		binding.taximeter.priceWaiting.text = number2digits(totalPriceWaiting)
		val totalPriceKm = totalPricePerKm + totalPriceWaiting
		val totalPriceMin = totalPricePerMin + totalPriceWaiting
		binding.taximeter.priceTotal.text =
			"${number2digits(totalPriceKm)}/${number2digits(totalPriceMin)}"
		
		binding.taximeter.time.text = formatDuration(time)
	}
	
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentClientTaximeterDetailsBinding.inflate(layoutInflater)
		binding.finishOrder.setOnClickListener(this)
		return binding.root
	}
	
	private fun finishOrder() {
		OrdersModel.mIsAccepted = false
		activity?.sendBroadcast(Intent(StaticOrders.ORDER_STATUS_INTENT_FILTER)
			.putExtra(StaticOrders.ORDER_STATUS, StaticOrders.ORDER_STATUS_FINISHED))
		
		MyPreferences.taximeterPreferences?.let {
			MyPreferences.saveToPreferences(it,
				StaticTaximeter.SHARED_PREFERENCES_TIMER_STATUS,
				false)
		}
		MyPreferences.taximeterPreferences?.let {
			MyPreferences.saveToPreferences(it,
				StaticOrders.SHARED_PREFERENCES_ORDER_IS_WAITING,
				false)
		}
		MyPreferences.taximeterPreferences?.let {
			MyPreferences.saveToPreferences(it, StaticOrders.SHARED_PREFERENCES_DEAL_PRICE, -1)
		}
		SocketHelper.finishOrder(OrdersModel())
		SocketHelper.taximeterStop(TaximeterUpdate(coordinates = null,
			recipientUUID = OrdersModel.mCustomer.uuid, orderUUID = OrdersModel.mUuid))
		binding.finishOrder.visibility = View.GONE
	}
	
	override fun onResume() {
		super.onResume()
		requireActivity().registerReceiver(taximeterReceiver,
			IntentFilter(StaticTaximeter.TAXIMETER_INTENT_FILTER))
		setTaximeter(getOrderTime(), getWaitingTime())
	}
	
	override fun onPause() {
		super.onPause()
		try {
			requireActivity().unregisterReceiver(taximeterReceiver)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
	
	private fun getOrderTime(): Long =
		if (MyPreferences.taximeterPreferences?.getLong(StaticOrders.SHARED_PREFERENCES_ORDER_TIME,
				0L) == null
		) 0L
		else MyPreferences.taximeterPreferences?.getLong(StaticOrders.SHARED_PREFERENCES_ORDER_TIME,
			0L)!!
	
	private fun getWaitingTime(): Long =
		if (MyPreferences.taximeterPreferences?.getLong(StaticOrders.SHARED_PREFERENCES_ORDER_WAITING_TIME,
				0L) == null
		) 0L
		else MyPreferences.taximeterPreferences?.getLong(StaticOrders.SHARED_PREFERENCES_ORDER_WAITING_TIME,
			0L)!!
    
    override fun onClick(v: View?) {
		when (v!!.id) {
			R.id.finish_order -> MaterialAlertDialogBuilder(requireContext())
				.setMessage(resources.getString(R.string.finish_order_dialog))
				.setPositiveButton(resources.getString(R.string.button_yes)) { _, _ -> finishOrder() }
				.setNegativeButton(resources.getString(R.string.button_no)) { d, _ -> d.dismiss() }
				.show()
		}
	}
	
}
