package octii.app.taxiapp.ui.maps.driver

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import octii.app.taxiapp.R
import octii.app.taxiapp.databinding.BottomSheetDriverOrderBinding
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.web.SocketHelper

class DriverOrderBottomSheet (context: Context, activity: Activity, private val order : OrdersModel) :
    BottomSheetDialog(context), View.OnClickListener {

    private val binding : BottomSheetDriverOrderBinding = BottomSheetDriverOrderBinding.inflate(activity.layoutInflater)

    init{
        this.setContentView(binding.root)
        binding.customerName.text = order.customer?.userName
        binding.customerPhone.text = order.customer?.phone
        if (order.customer != null){
            if (order.customer!!.isWhatsapp)
                binding.messengersInfo.text =
                    activity.resources.getString(R.string.user_available_in_whatsapp)
            else if (order.customer!!.isViber)
                binding.messengersInfo.text =
                    activity.resources.getString(R.string.user_available_in_viber)
            else if (order.customer!!.isViber && order.customer!!.isWhatsapp)
                binding.messengersInfo.text =
                    activity.resources.getString(R.string.user_available_in_viber_and_whatsapp)
        }
        binding.finishOrder.setOnClickListener(this)
    }

    private fun finishOrder(){
        OrdersModel.isAccepted = false
        SocketHelper.finishOrder(order)
        this.hide()
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.finish_order -> finishOrder()
        }
    }

}