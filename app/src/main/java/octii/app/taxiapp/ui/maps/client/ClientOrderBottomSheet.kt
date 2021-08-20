package octii.app.taxiapp.ui.maps.client

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.StaticOrders
import octii.app.taxiapp.databinding.BottomSheetAcceptOrderClientBinding
import octii.app.taxiapp.databinding.FragmentClientDetailsBinding
import octii.app.taxiapp.databinding.FragmentDriverDetailsBinding
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.ui.maps.OpenMessengerBottomSheet
import octii.app.taxiapp.ui.maps.client.orderDetails.BottomSheetShowPhotos
import octii.app.taxiapp.ui.utils.CallHelper
import octii.app.taxiapp.ui.utils.FragmentHelper
import octii.app.taxiapp.web.SocketHelper
import java.util.*

class ClientOrderBottomSheet(
    context: Context,
    private val activity: Activity,
    private val order: OrdersModel,
) :
    BottomSheetDialog(context), View.OnClickListener, FragmentHelper, CallHelper {

    val binding : BottomSheetAcceptOrderClientBinding = BottomSheetAcceptOrderClientBinding.inflate(layoutInflater)
    private var isWhatsApp = false
    private var isViber = false

    init {
        setContentView(binding.root)
        setInformation()
        setCancelable(false)
    }


    private fun setInformation() {
        binding.clientInfo.driverName.text = OrdersModel.mDriver.userName
        binding.clientInfo.driverPhone.text = OrdersModel.mDriver.phone
        binding.clientInfo.callToDriver.setOnClickListener(this)
        binding.acceptOrder.setOnClickListener(this)
        binding.rejectOrder.setOnClickListener(this)
        setAvatar()
        setMessengersInfo()
    }

    private fun setAvatar() {
        if (OrdersModel.mCustomer.avatarURL.trim().isNotEmpty()) {
            Picasso.with(context)
                .load(OrdersModel.mCustomer.avatarURL)
                .transform(RoundedCornersTransformation(40, 5))
                .resize(160, 160)
                .centerCrop()
                .into(binding.clientInfo.driverAvatar)
        } else {
            binding.clientInfo.driverAvatar.setImageResource(R.drawable.outline_account_circle_24)
        }
    }

    private fun setMessengersInfo() {
        if (OrdersModel.mDriver.isViber) {
            isViber = true
        }
    }

    private fun acceptOrder() {
        activity.sendBroadcast(Intent(StaticOrders.ORDER_STATUS_INTENT_FILTER)
            .putExtra(StaticOrders.ORDER_STATUS, StaticOrders.ORDER_STATUS_ACCEPTED))
        SocketHelper.acceptOrder(order)
        this.hide()
    }

    private fun rejectOrder() {
        SocketHelper.rejectOrder(order)
        this.hide()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.call_to_driver -> {
                val phoneNum = order.driver?.phone!!
                copyToClipBoard(phoneNum, context)
                if (isViber) callViber(phoneNum, context)
                else callToCustomer(phoneNum, activity)
                /*
                if (isWhatsApp && isViber) {
                    OpenMessengerBottomSheet(context, activity, OrdersModel.mCustomer.phone).show()
                } else if (isViber) goToApplication("com.viber.voip", activity)
                //else if (isWhatsApp) goToApplication("com.whatsapp", requireActivity())
                //else if (isWhatsApp) callToWhatsApp(binding.customerPhone.text.toString(), requireContext(), requireActivity())
                else {
                    callToCustomer(binding.customerPhone.text.toString(), activity)
                }*/
            }
            R.id.accept_order -> {
                acceptOrder()
            }
            R.id.reject_order -> {
                rejectOrder()
            }
            R.id.show_photos -> {
                BottomSheetShowPhotos(context, activity, OrdersModel.mDriver.files).show()
            }
        }
    }


/*
    private var binding: BottomSheetAcceptOrderBinding =
        BottomSheetAcceptOrderBinding.inflate(activity.layoutInflater)
    private var timer: Timer

    init {
        this.setContentView(binding.root)
        this.setOnCancelListener { rejectOrder() }
        OrdersModel.isOrdered = false
        binding.rejectOrder.setOnClickListener(this)
        binding.acceptOrder.setOnClickListener(this)
        binding.customerName.text = order.customer?.userName
        binding.customerPhone.text = order.customer?.phone
        if (order.customer != null) {
            if (order.customer?.avatarURL?.trim()?.isNotEmpty() == true) {
                Picasso.with(context)
                    .load(OrdersModel.mCustomer.avatarURL)
                    .transform(RoundedCornersTransformation(40, 5))
                    .resize(160, 160)
                    .centerCrop()
                    .into(binding.customerAvatar)
            } else binding.customerAvatar.setImageResource(R.drawable.outline_account_circle_24)
        }
        try {
            show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val timerToReject = TimerToReject(binding.timer, activity)
        timer = Timer()
        timer.schedule(timerToReject, 0, 1000)

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.reject_order -> rejectOrder()
            R.id.accept_order -> acceptOrder()
        }
    }

    private fun acceptOrder() {
        finishTimer(timer)
        activity.sendBroadcast(Intent(StaticOrders.ORDER_STATUS_INTENT_FILTER)
            .putExtra(StaticOrders.ORDER_STATUS, StaticOrders.ORDER_STATUS_ACCEPTED))
        SocketHelper.acceptOrder(order)
        this@DriverAcceptOrderBottomSheet.hide()
    }

    private fun rejectOrder() {
        finishTimer(timer)
        SocketHelper.rejectOrder(order)
        try {
            DriverMapFragment.ordered = true
        } catch (e : Exception) {
            e.printStackTrace()
        }
        this@DriverAcceptOrderBottomSheet.hide()
    }

    private fun finishTimer(timer: Timer) {
        timer.cancel()
        timer.purge()
    }

    inner class TimerToReject(private val view: TextView, private val activity: Activity) :
        TimerTask() {

        private var seconds = 15

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
*/
}