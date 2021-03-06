/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 23.08.2021, 12:33                       *
 ******************************************************************************/

package octii.app.taxiapp.ui.maps.client.orderDetails

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
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.StaticOrders
import octii.app.taxiapp.constants.StaticTaximeter
import octii.app.taxiapp.constants.sockets.TaximeterType
import octii.app.taxiapp.databinding.FragmentClientOrderDetailsBinding
import octii.app.taxiapp.scripts.MyPreferences
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.services.location.MyLocationListener
import octii.app.taxiapp.ui.utils.RequestOrderUtils


class OrderDetails : Fragment(), RequestOrderUtils {
	
	private lateinit var binding: FragmentClientOrderDetailsBinding
	private val fragments =
		listOf<Fragment>(DriverDetailsFragment(), TaximeterDetailsFragment())
	private var orderStatusReciever = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (intent != null) {
				when (intent.getStringExtra(StaticOrders.ORDER_STATUS)) {
					StaticOrders.ORDER_STATUS_ACCEPTED -> {
						binding.pages.currentItem = 1
					}
					StaticOrders.ORDER_STATUS_FINISHED -> {
						binding.pages.currentItem = 1
						binding.pages.isEnabled = false
					}
				}
			}
		}
	}
	
	private var taximeterBroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (intent != null) {
				when (intent.getSerializableExtra(StaticTaximeter.TAXIMETER_STATUS)) {
					TaximeterType.TAXIMETER_START -> {
						//?????????????? ???????????? ?? sharedPrefs ?? ?????????????????? ????????????
						MyLocationListener.distance = 0f
						//?????????????????? ????????????
						MyPreferences.taximeterPreferences?.let {
							MyPreferences.saveToPreferences(it,
								StaticOrders.SHARED_PREFERENCES_ORDER_IS_WAITING,
								false)
						}
						MyPreferences.taximeterPreferences?.let {
							MyPreferences.saveToPreferences(it,
								StaticTaximeter.SHARED_PREFERENCES_TIMER_STATUS,
								true)
						}
					}
					TaximeterType.TAXIMETER_STOP -> {
						MyPreferences.taximeterPreferences?.let {
							MyPreferences.saveToPreferences(it,
								StaticTaximeter.SHARED_PREFERENCES_TIMER_STATUS,
								false)
						}
					}
					TaximeterType.TAXIMETER_WAITING -> {
						logError(isWainting())
					}
				}
			}
		}
	}
	
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentClientOrderDetailsBinding.inflate(layoutInflater)
		binding.pages.adapter =
			FragmentsAdapter(requireActivity().supportFragmentManager, lifecycle)
		TabLayoutMediator(binding.indicator, binding.pages) { _, _ -> }.attach()
		setListeners()
		return binding.root
	}
	
	override fun onResume() {
		super.onResume()
		requireActivity().registerReceiver(orderStatusReciever,
			IntentFilter(StaticOrders.ORDER_STATUS_INTENT_FILTER))
		requireActivity().registerReceiver(taximeterBroadcastReceiver,
			IntentFilter(StaticTaximeter.TAXIMETER_STATUS_INTENT_FILTER))
	}
	
	override fun onDestroy() {
		super.onDestroy()
		try {
			requireActivity().unregisterReceiver(orderStatusReciever)
			requireActivity().unregisterReceiver(taximeterBroadcastReceiver)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
	
	private fun setListeners() {
		binding.goBack.setOnClickListener(this)
		binding.goNext.setOnClickListener(this)
	}
	
	override fun setInformation() {}
	
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
	
	private fun isWainting(): Boolean =
		if (MyPreferences.taximeterPreferences?.getBoolean(StaticOrders.SHARED_PREFERENCES_ORDER_IS_WAITING,
				false) == null
		) false
		else MyPreferences.taximeterPreferences?.getBoolean(StaticOrders.SHARED_PREFERENCES_ORDER_IS_WAITING,
			false)!!
	
	inner class FragmentsAdapter internal constructor(fm: FragmentManager, lifecycle: Lifecycle) :
		FragmentStateAdapter(fm, lifecycle) {
		
		
		override fun getItemCount(): Int {
			return fragments.size
		}
		
		override fun createFragment(position: Int): Fragment {
			return when (position) {
				0 -> fragments[position]
				1 -> fragments[position]
				else -> fragments[position]
			}
		}
	}
	
	
}