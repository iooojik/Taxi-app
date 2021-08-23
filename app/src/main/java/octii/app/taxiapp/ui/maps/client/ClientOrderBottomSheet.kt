/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 22:50                       *
 ******************************************************************************/

package octii.app.taxiapp.ui.maps.client

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import octii.app.taxiapp.R
import octii.app.taxiapp.databinding.BottomSheetAcceptOrderClientBinding
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.scripts.MyPreferences
import octii.app.taxiapp.ui.maps.client.orderDetails.BottomSheetShowPhotos
import octii.app.taxiapp.ui.utils.RequestOrderUtils

class ClientOrderBottomSheet(
	context: Context,
	private val activity: Activity,
	private val order: OrdersModel,
) :
	BottomSheetDialog(context), RequestOrderUtils {
	
	val binding: BottomSheetAcceptOrderClientBinding =
		BottomSheetAcceptOrderClientBinding.inflate(layoutInflater)
	
	init {
		setContentView(binding.root)
		setInformation()
		setCancelable(false)
	}
	
	
	override fun setInformation() {
		MyPreferences.clearTaximeter()
		binding.clientInfo.driverName.text = OrdersModel.mDriver.userName
		binding.clientInfo.driverPhone.text = OrdersModel.mDriver.phone
		binding.clientInfo.callToDriver.setOnClickListener(this)
		binding.acceptOrder.setOnClickListener(this)
		binding.rejectOrder.setOnClickListener(this)
		binding.clientInfo.showPhotos.setOnClickListener(this)
		//показываем аватар
		setAvatar(url = OrdersModel.mDriver.avatarURL, context = context,
			imagePlaceHolder = binding.clientInfo.driverAvatar)
	}
	
	override fun onClick(v: View?) {
		when (v!!.id) {
			R.id.call_to_driver -> {
				val phoneNum = order.driver?.phone!!
				copyToClipBoard(phoneNum, context)
				if (OrdersModel.mDriver.isViber) callViber(phoneNum, context)
				else callToCustomer(phoneNum, activity)
			}
			R.id.accept_order -> {
				acceptOrder(activity, order, this)
			}
			R.id.reject_order -> {
				rejectOrder(order, this)
			}
			R.id.show_photos -> {
				BottomSheetShowPhotos(context, activity, OrdersModel.mDriver.files).show()
			}
		}
	}
}