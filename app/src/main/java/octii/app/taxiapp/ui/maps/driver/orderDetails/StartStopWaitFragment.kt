package octii.app.taxiapp.ui.maps.driver.orderDetails

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.StaticOrders
import octii.app.taxiapp.constants.StaticTaximeter
import octii.app.taxiapp.databinding.FragmentStartStopBinding
import octii.app.taxiapp.models.TaximeterUpdate
import octii.app.taxiapp.models.coordinates.CoordinatesModel
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.scripts.MyPreferences
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.services.location.MyLocationListener
import octii.app.taxiapp.web.SocketHelper


class StartStopWaitFragment : Fragment(), View.OnClickListener {

    lateinit var binding : FragmentStartStopBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentStartStopBinding.inflate(layoutInflater)
        setListeners()
        return binding.root
    }

    private fun setListeners(){
        binding.finishOrder.setOnClickListener(this)
        binding.waitingOrder.setOnClickListener(this)
        binding.startOrder.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (isWainting()) binding.waitingOrder.setBackgroundColor(Color.parseColor("#99d98c"))
        if (isRunning()) {
            binding.finishOrder.visibility = View.VISIBLE
            binding.startOrderLayout.visibility = View.GONE
        } else{
            binding.finishOrder.visibility = View.GONE
            binding.startOrderLayout.visibility = View.VISIBLE
        }
    }

    private fun isWainting() : Boolean =
        if (MyPreferences.taximeterPreferences?.getBoolean(StaticOrders.SHARED_PREFERENCES_ORDER_IS_WAITING, false) == null) false
        else MyPreferences.taximeterPreferences?.getBoolean(StaticOrders.SHARED_PREFERENCES_ORDER_IS_WAITING, false)!!

    private fun isRunning() : Boolean =
        if (MyPreferences.taximeterPreferences?.getBoolean(StaticTaximeter.SHARED_PREFERENCES_TIMER_STATUS, false) == null) false
        else MyPreferences.taximeterPreferences?.getBoolean(StaticTaximeter.SHARED_PREFERENCES_TIMER_STATUS, false)!!

    private fun finishOrder(){
        activity?.sendBroadcast(Intent(StaticOrders.ORDER_STATUS_INTENT_FILTER)
            .putExtra(StaticOrders.ORDER_STATUS, StaticOrders.ORDER_STATUS_FINISHED))
        binding.finishOrder.isEnabled = false
        binding.waitingOrder.isEnabled = false
        MyPreferences.taximeterPreferences?.let { MyPreferences.saveToPreferences(it, StaticTaximeter.SHARED_PREFERENCES_TIMER_STATUS, false)}
        OrdersModel.isAccepted = false
        SocketHelper.finishOrder(OrdersModel())
        SocketHelper.taximeterStop(TaximeterUpdate(coordinates = null,
            recipientUUID = OrdersModel.mCustomer.uuid, orderUUID = OrdersModel.mUuid))
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.finish_order -> {
                val items = arrayOf(
                    resources.getString(R.string.button_yes),
                    resources.getString(R.string.button_no)
                )

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.finish_order_dialog)
                    .setItems(items) { _, which ->
                        when(which){
                            0 -> {
                                finishOrder()
                            }
                            1 -> {}
                        }
                    }
                    .show()
            }
            R.id.start_order -> {
                MyPreferences.taximeterPreferences?.let {
                    MyPreferences.saveToPreferences(it,
                        StaticTaximeter.SHARED_PREFERENCES_TIMER_STATUS, true)
                }
                SocketHelper.taximeterStart(TaximeterUpdate(coordinates = CoordinatesModel(),
                    recipientUUID = OrdersModel.mCustomer.uuid, orderUUID = OrdersModel.mUuid))

                binding.finishOrder.visibility = View.VISIBLE
                binding.startOrderLayout.visibility = View.GONE
            }
            R.id.waiting_order -> {
                if (isWainting()) {
                    MyPreferences.taximeterPreferences?.let {
                        MyPreferences.saveToPreferences(it,
                            StaticOrders.SHARED_PREFERENCES_ORDER_IS_WAITING,
                            false)
                    }
                    SocketHelper.taximeterWaiting(TaximeterUpdate(coordinates = CoordinatesModel(),
                        recipientUUID = OrdersModel.mCustomer.uuid, orderUUID = OrdersModel.mUuid), false)
                    binding.waitingOrder.setBackgroundColor(Color.parseColor("#ffffff"))
                } else {
                    MyPreferences.taximeterPreferences?.let {
                        MyPreferences.saveToPreferences(it,
                            StaticOrders.SHARED_PREFERENCES_ORDER_IS_WAITING,
                            true)
                    }
                    SocketHelper.taximeterWaiting(TaximeterUpdate(coordinates = CoordinatesModel(),
                        recipientUUID = OrdersModel.mCustomer.uuid, orderUUID = OrdersModel.mUuid), true)
                    binding.waitingOrder.setBackgroundColor(Color.parseColor("#99d98c"))
                }
            }
        }
    }

}