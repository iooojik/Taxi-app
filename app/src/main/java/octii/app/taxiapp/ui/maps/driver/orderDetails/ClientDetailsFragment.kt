/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 17:24                       *
 ******************************************************************************/

package octii.app.taxiapp.ui.maps.driver.orderDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import octii.app.taxiapp.R
import octii.app.taxiapp.databinding.FragmentClientDetailsBinding
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.ui.utils.RequestOrderUtils


class ClientDetailsFragment : Fragment(), RequestOrderUtils {
	
	lateinit var binding: FragmentClientDetailsBinding
	
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentClientDetailsBinding.inflate(layoutInflater)
		return binding.root
	}
	
	override fun onResume() {
		super.onResume()
		//показываем информацию о клиенте
		setInformation()
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