package octii.app.taxiapp.ui.maps.driver

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import octii.app.taxiapp.R
import octii.app.taxiapp.databinding.BottomSheetAcceptOrderBinding
import octii.app.taxiapp.models.OrdersModel
import octii.app.taxiapp.web.SocketHelper
import java.util.*

class DriverAcceptOrderBottomSheet (context: Context, activity: Activity, private val order : OrdersModel) :
    BottomSheetDialog(context), View.OnClickListener {

    private var binding: BottomSheetAcceptOrderBinding =
        BottomSheetAcceptOrderBinding.inflate(activity.layoutInflater)
    private var timer: Timer

    init {
        this.setContentView(binding.root)
        OrdersModel.isOrdered = false
        binding.rejectOrder.setOnClickListener(this)
        binding.acceptOrder.setOnClickListener(this)
        binding.customerName.text = order.customer?.userName
        binding.customerPhone.text = order.customer?.phone
        if (order.customer != null){
            if (order.customer?.avatarURL?.trim()?.isNotEmpty() == true){
                Picasso.with(context)
                    .load(OrdersModel.mCustomer.avatarURL)
                    .transform(RoundedCornersTransformation(40, 5))
                    .resize(160, 160)
                    .centerCrop()
                    .into(binding.customerAvatar)
            } else binding.customerAvatar.setImageResource(R.drawable.outline_account_circle_24)
        }

        val timerToReject = TimerToReject(binding.timer, activity)
        timer = Timer()
        timer.schedule(timerToReject, 0, 1000)

    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.reject_order -> rejectOrder()
            R.id.accept_order -> acceptOrder()
        }
    }

    private fun acceptOrder(){
        finishTimer(timer)
        SocketHelper.acceptOrder(order)
        this@DriverAcceptOrderBottomSheet.hide()
    }

    private fun rejectOrder(){
        finishTimer(timer)
        SocketHelper.rejectOrder(order)
        this@DriverAcceptOrderBottomSheet.hide()
    }

    private fun finishTimer(timer : Timer){
        timer.cancel()
        timer.purge()
    }

    inner class TimerToReject(private val view: TextView, private val activity: Activity) : TimerTask() {

        private var seconds = 10

        override fun run() {
            activity.runOnUiThread {
                if (seconds - 1 < 0) {
                    rejectOrder()
                } else {
                    seconds--
                    view.text = "$seconds"
                }
            }
        }
    }

}