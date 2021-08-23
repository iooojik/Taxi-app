/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 23.08.2021, 12:33                       *
 ******************************************************************************/

package octii.app.taxiapp.ui.maps.driver

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.constants.StaticOrders
import octii.app.taxiapp.databinding.FragmentDriverMapBinding
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.down
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.scripts.up
import octii.app.taxiapp.ui.maps.driver.recivers.DriverCoordinatesReciever
import octii.app.taxiapp.ui.maps.driver.recivers.DriverOrderReciever
import octii.app.taxiapp.ui.utils.MapUtils
import octii.app.taxiapp.web.requests.Requests


class DriverMapFragment : Fragment(), MapUtils {
	
	companion object {
		@JvmStatic
		var ordered = true
	}
	
	@SuppressLint("MissingPermission")
	private val callback = OnMapReadyCallback { googleMap ->
		/**
		 * Manipulates the map once available.
		 * This callback is triggered when the map is ready to be used.
		 * This is where we can add markers or lines, add listeners or move the camera.
		 * In this case, we just add a marker near Sydney, Australia.
		 * If Google Play services is not installed on the device, the user will be prompted to
		 * install it inside the SupportMapFragment. This method will only be triggered once the
		 * user has installed Google Play services and returned to the app.
		 */
		googleMap.isMyLocationEnabled = true
		this.googleMap = googleMap
		//перемещаем камеру на пользователя
		locateMe(activity = requireActivity(),
			context = requireContext(),
			driverMapFragment = this,
			clientMapFragment = null)
		logInfo("google map ready callback")
	}
	
	private lateinit var binding: FragmentDriverMapBinding
	private lateinit var driverOrderReciever: DriverOrderReciever
	private lateinit var driverCoordinatesReciever: DriverCoordinatesReciever
	var googleMap: GoogleMap? = null
	var cameraisMoved = false
	var marker: Marker? = null
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		logInfo("onCreateView ${this.javaClass.name}")
		binding = FragmentDriverMapBinding.inflate(layoutInflater)
		//слушатели на кнопки
		setListeners()
		//блокируем кнопку "назад"
		blockGoBack(requireActivity(), this)
		return binding.root
	}
	
	override fun onResume() {
		super.onResume()
		logInfo("onResume ${this.javaClass.name}")
		//проверяем, установлен ли viber
		checkViber(requireContext(), requireActivity())
		//убираем клавиатуру
		hideKeyBoard(requireActivity(), requireView())
		//проверка на наличие заказов
		Requests().orderRequests.orderCheck(UserModel())
		//проверяем соответствует ли тип аккаунта типу карты
		checkDriverType(requireActivity())
		//BroadcastReceiver-ы для прослушивания состояния заказа
		driverOrderReciever =
			DriverOrderReciever(binding, requireActivity(), this, requireContext())
		driverCoordinatesReciever = DriverCoordinatesReciever(requireActivity(),
			this,
			requireContext())
		//проверяем действующие заказы
		setOrderDetails()
		//проверяем разрешения
		checkPermissions(context = requireContext(), activity = requireActivity())
		//регистрируем слушатели сообщений заказа и координат
		requireActivity().registerReceiver(driverOrderReciever,
			IntentFilter(StaticOrders.ORDER_STATUS_INTENT_FILTER))
		requireActivity().registerReceiver(driverCoordinatesReciever,
			IntentFilter(StaticOrders.ORDER_STATUS_COORDINATES_STATUS))
		//пробуем показать карту
		try {
			setMap()
		} catch (e: java.lang.Exception) {
			e.printStackTrace()
			Snackbar.make(requireView(),
				resources.getString(R.string.check_permissions),
				Snackbar.LENGTH_SHORT).show()
		}
	}
	
	override fun onDestroy() {
		super.onDestroy()
		logInfo("onDestroy ${this.javaClass.name}")
		//убираем слушатели сообщений заказа
		try {
			requireActivity().unregisterReceiver(driverOrderReciever)
			requireActivity().unregisterReceiver(driverCoordinatesReciever)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
	
	override fun setOrderDetails() {
		//проверяем действующие заказы
		logInfo("order id: ${OrdersModel.mId}")
		//если заказ принят, но не закончен, то показываем меню с таксиметром
		if (OrdersModel.mIsAccepted && OrdersModel.mId > 0 && !OrdersModel.mIsFinished) {
			binding.fabShowOrderDetails.setOnClickListener(this)
			binding.fabShowOrderDetails.setImageResource(R.drawable.outline_expand_more_24)
			binding.fabShowOrderDetails.tag = Static.EXPAND_MORE_FAB
			showDriverFabOrderDetails(binding, requireActivity())
			binding.orderDetails.up(requireActivity())
		}
	}
	
	override fun setListeners() {
		//слушатели на кнопки
		binding.fabSettings.setOnClickListener(this)
	}
	
	override fun setMap() {
		//пробуем показать карту
		val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
		mapFragment?.getMapAsync(callback)
	}
	
	override fun onClick(v: View?) {
		when (v!!.id) {
			R.id.fab_settings -> {
				logInfo("go to settings from ${this.javaClass.name}")
				findNavController().navigate(R.id.action_driverMapFragment_to_driverSettingsFragment)
			}
			R.id.fab_show_order_details -> {
				//скрыть меню заказа
				if (v.tag == Static.EXPAND_MORE_FAB) {
					binding.orderDetails.down(requireActivity())
					binding.fabShowOrderDetails.setImageResource(R.drawable.outline_expand_less_24)
					binding.fabShowOrderDetails.tag = Static.EXPAND_LESS_FAB
					hideDriverFabOrderDetails(binding = binding, activity = requireActivity())
				} else {
					//показать меню заказа
					binding.fabShowOrderDetails.setImageResource(R.drawable.outline_expand_more_24)
					binding.fabShowOrderDetails.tag = Static.EXPAND_MORE_FAB
					binding.orderDetails.up(requireActivity())
					showDriverFabOrderDetails(binding = binding, activity = requireActivity())
				}
			}
		}
	}
	
	
}