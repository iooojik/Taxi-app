/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 17:24                       *
 ******************************************************************************/

package octii.app.taxiapp.ui.maps.driver.orderDetails

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.StaticOrders
import octii.app.taxiapp.databinding.FragmentClientDetailsBinding
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.ui.utils.RequestOrderUtils


class ClientDetailsFragment : Fragment(), RequestOrderUtils {
	
	lateinit var binding: FragmentClientDetailsBinding
	private var orderStatusReciever = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (intent != null) {
				when (intent.getStringExtra(StaticOrders.ORDER_STATUS)) {
					StaticOrders.ORDER_STATUS_ACCEPTED -> {
						setInformation()
					}
				}
			}
		}
	}
	
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentClientDetailsBinding.inflate(layoutInflater)
		return binding.root
	}
	
	override fun onResume() {
		super.onResume()
		requireActivity().registerReceiver(orderStatusReciever, IntentFilter(StaticOrders.ORDER_STATUS_INTENT_FILTER))
		//показываем информацию о клиенте
		setInformation()
	}
	
	override fun onDestroy() {
		super.onDestroy()
		try {
			requireActivity().unregisterReceiver(orderStatusReciever)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
	
	override fun setInformation() {
		//имя клиента
		binding.customerName.text =
			OrdersModel.mCustomer.userName
		//телефон клиента
		binding.customerPhone.text =
			OrdersModel.mCustomer.phone
		//слушатель
		binding.callToCustomer.setOnClickListener(this)
		//показываем аватар клиента
		setAvatar(url = OrdersModel.mCustomer.avatarURL, context = requireContext(),
			imagePlaceHolder = binding.customerAvatar)
	}
	
	override fun onClick(v: View?) {
		when (v!!.id) {
			R.id.call_to_customer -> {
				val phone = OrdersModel.mCustomer.phone
				copyToClipBoard(phone, requireContext())
				if (UserModel.uIsViber) callViber(phone, requireContext())
				else callToCustomer(binding.customerPhone.text.toString(), requireActivity())
			}
		}
	}
	
}