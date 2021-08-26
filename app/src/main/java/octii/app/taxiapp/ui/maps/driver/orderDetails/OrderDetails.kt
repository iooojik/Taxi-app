/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 23.08.2021, 11:03                       *
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
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.StaticOrders
import octii.app.taxiapp.databinding.FragmentDriverOrderDetailsBinding
import octii.app.taxiapp.databinding.FragmentStartStopBinding
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.ui.utils.RequestOrderUtils


class OrderDetails : BottomSheetDialogFragment(), RequestOrderUtils {
	
	private lateinit var binding: FragmentDriverOrderDetailsBinding
	private val fragments = listOf<Fragment>(ClientDetailsFragment(),
		TaximeterDetailsFragment(),
		StartStopWaitFragment())
	private var orderStatusReciever = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (intent != null) {
				when (intent.getStringExtra(StaticOrders.ORDER_STATUS)) {
					StaticOrders.ORDER_STATUS_FINISHED -> {
						binding.pages.currentItem = 1
						//binding.pages.isEnabled = false
					}
				}
			}
		}
	}
	
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentDriverOrderDetailsBinding.inflate(layoutInflater)
		setInformation()
		setListeners()
		return binding.root
	}
	
	private fun setListeners() {
		binding.goBack.setOnClickListener(this)
		binding.goNext.setOnClickListener(this)
	}
	
	override fun onResume() {
		super.onResume()
		requireActivity().registerReceiver(orderStatusReciever, IntentFilter(StaticOrders.ORDER_STATUS_INTENT_FILTER))
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
		binding.pages.adapter =
			FragmentsAdapter(requireActivity().supportFragmentManager, lifecycle)
		TabLayoutMediator(binding.indicator, binding.pages) { _, _ -> }.attach()
	}
	
	override fun onClick(v: View?) {
		when(v!!.id){
			R.id.go_back -> {
				if (binding.pages.currentItem == 0)
					binding.pages.currentItem = (fragments.size - 1)
				else binding.pages.currentItem = binding.pages.currentItem - 1
			}
			R.id.go_next -> {
				if (binding.pages.currentItem == (fragments.size-1))
					binding.pages.currentItem = 0
				else binding.pages.currentItem = binding.pages.currentItem + 1
			}
		}
	}
	
	inner class FragmentsAdapter internal constructor(fm: FragmentManager, lifecycle: Lifecycle) :
		FragmentStateAdapter(fm, lifecycle) {
		
		override fun getItemCount(): Int {
			return fragments.size
		}
		
		override fun createFragment(position: Int): Fragment {
			return when (position) {
				0 -> fragments[position]
				1 -> fragments[position]
				2 -> fragments[position]
				else -> fragments[position]
			}
		}
		
	}
	
}