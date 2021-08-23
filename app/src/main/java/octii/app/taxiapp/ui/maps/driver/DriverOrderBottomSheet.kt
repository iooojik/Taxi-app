/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 17:24                       *
 ******************************************************************************/

package octii.app.taxiapp.ui.maps.driver

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import octii.app.taxiapp.R
import octii.app.taxiapp.databinding.BottomSheetAcceptOrderDriverBinding
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.ui.utils.RequestOrderUtils

class DriverOrderBottomSheet(
	context: Context,
	private val activity: Activity,
	private val order: OrdersModel,
) :
	BottomSheetDialog(context), RequestOrderUtils {
	
	val binding: BottomSheetAcceptOrderDriverBinding =
		BottomSheetAcceptOrderDriverBinding.inflate(layoutInflater)
	
	init {
		//отмена закрытия
		setCancelable(false)
		//показываем информацию о клиенте
		setInformation()
		setContentView(binding.root)
	}
	
	override fun setInformation() {
		//имя клиента
		binding.clientInfo.customerName.text = OrdersModel.mCustomer.userName
		//номер телефона клиента
		binding.clientInfo.customerPhone.text = OrdersModel.mCustomer.phone
		//слушатели
		binding.clientInfo.callToCustomer.setOnClickListener(this)
		binding.acceptOrder.setOnClickListener(this)
		binding.rejectOrder.setOnClickListener(this)
		//показываем аватар
		setAvatar(url = OrdersModel.mCustomer.avatarURL, context = context,
			imagePlaceHolder = binding.clientInfo.customerAvatar)
	}
	
	override fun onClick(v: View?) {
		when (v!!.id) {
			R.id.call_to_customer -> {
				val phoneNum = order.customer?.phone!!
				copyToClipBoard(phoneNum, context)
				if (OrdersModel.mCustomer.isViber) callViber(phoneNum, context)
				else callToCustomer(phoneNum, activity)
			}
			R.id.accept_order -> {
				acceptOrder(activity, order, this)
			}
			R.id.reject_order -> {
				rejectOrder(order, this)
			}
		}
	}
}