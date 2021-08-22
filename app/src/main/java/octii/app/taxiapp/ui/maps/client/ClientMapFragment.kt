package octii.app.taxiapp.ui.maps.client

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
import octii.app.taxiapp.databinding.FragmentClientMapBinding
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.down
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.scripts.up
import octii.app.taxiapp.ui.maps.client.recievers.ClientCoordinatesReciever
import octii.app.taxiapp.ui.maps.client.recievers.ClientOrderReciever
import octii.app.taxiapp.ui.utils.MapUtils
import octii.app.taxiapp.web.SocketHelper
import octii.app.taxiapp.web.requests.Requests


class ClientMapFragment : Fragment(), View.OnClickListener, MapUtils {
	
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
			driverMapFragment = null,
			clientMapFragment = this)
		logInfo("google map ready callback")
	}
	private lateinit var binding: FragmentClientMapBinding
	private lateinit var clientOrderReciever: ClientOrderReciever
	private lateinit var clientCoordinatesReciever: ClientCoordinatesReciever
	var googleMap: GoogleMap? = null
	var cameraisMoved = false
	var marker: Marker? = null
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		logInfo("onCreateView ${this.javaClass.name}")
		binding = FragmentClientMapBinding.inflate(layoutInflater)
		//слушатели на кнопки
		setListeners()
		//блокируем кнопку "назад"
		blockGoBack(requireActivity(), this)
		return binding.root
	}
	
	private fun checkViber() {
		if (!checkViber(requireContext(), requireActivity())) {
			binding.callTaxi.setOnClickListener {
				checkViber()
			}
		} else {
			binding.callTaxi.setOnClickListener(this)
		}
	}
	
	override fun onResume() {
		super.onResume()
		logInfo("onResume ${this.javaClass.name}")
		//проверяем, установлен ли viber
		checkViber()
		//убираем клавиатуру
		hideKeyBoard(requireActivity(), requireView())
		//проверка на наличие заказов
		Requests().orderRequests.orderCheck(UserModel())
		//проверяем соответствует ли тип аккаунта типу карты
		checkClientType(requireActivity())
		//BroadcastReceiver-ы для прослушивания состояния заказа
		clientOrderReciever =
			ClientOrderReciever(binding, requireActivity(), this, requireContext())
		clientCoordinatesReciever =
			ClientCoordinatesReciever(requireActivity(), this, requireContext())
		//регистрируем слушатели сообщений заказа и координат
		requireActivity().registerReceiver(clientOrderReciever,
			IntentFilter(StaticOrders.ORDER_STATUS_INTENT_FILTER))
		requireActivity().registerReceiver(clientCoordinatesReciever,
			IntentFilter(StaticOrders.ORDER_STATUS_COORDINATES_STATUS))
		//проверяем разрешения
		checkPermissions(activity = requireActivity(), context = requireContext())
		//проверяем действующие заказы
		setOrderDetails()
		//пробуем показать карту
		try {
			setMap()
		} catch (e: Exception) {
			e.printStackTrace()
			Snackbar.make(requireView(),
				resources.getString(R.string.check_permissions),
				Snackbar.LENGTH_SHORT).show()
		}
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		logInfo("onDestroy ${this.javaClass.name}")
		//убираем слушатели сообщений заказа
		try {
			requireActivity().unregisterReceiver(clientOrderReciever)
			requireActivity().unregisterReceiver(clientCoordinatesReciever)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
	
	override fun setOrderDetails() {
		//проверяем действующие заказы
		logInfo("order id: ${OrdersModel.mId}")
		//если заказ принят, но не закончен, то показываем меню с таксиметром
		if (binding.root.isAttachedToWindow) {
			if (OrdersModel.mIsAccepted && OrdersModel.mId > 0 && !OrdersModel.mIsFinished) {
				binding.fabShowOrderDetails.setOnClickListener(this)
				showClientFabOrderDetails(binding = binding, activity = requireActivity())
				binding.fabShowOrderDetails.setImageResource(R.drawable.outline_expand_more_24)
				binding.fabShowOrderDetails.tag = Static.EXPAND_MORE_FAB
				binding.orderDetails.up(requireActivity())
			} else {
				logError("button call taxi shown")
				binding.callTaxi.show()
			}
		}
	}
	
	override fun setListeners() {
		binding.callTaxi.setOnClickListener(this)
		binding.fabSettings.setOnClickListener(this)
	}
	
	override fun setMap() {
		val mapFragment =
			childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
		mapFragment?.getMapAsync(callback)
	}
	
	override fun onClick(v: View?) {
		when (v!!.id) {
			R.id.call_taxi -> {
				//проверяем, установлен ли viber
				if (checkViber(requireContext(), requireActivity())) {
					//вызов такси
					logInfo("calling taxi ${this.javaClass.name}")
					//SocketHelper.connect()
					SocketHelper.makeOrder()
					OrdersModel.isOrdered = true
					binding.callTaxi.hide()
					binding.fabSettings.hide()
					binding.clientMapprogressBar.visibility = View.VISIBLE
				}
			}
			R.id.fab_settings -> {
				logInfo("go to settings from ${this.javaClass.name}")
				findNavController().navigate(R.id.clientSettingsFragment)
			}
			R.id.fab_show_order_details -> {
				//скрыть меню заказа
				if (v.tag == Static.EXPAND_MORE_FAB) {
					hideClientFabOrderDetails(binding = binding, activity = requireActivity())
					binding.orderDetails.down(requireActivity())
					binding.fabShowOrderDetails.setImageResource(R.drawable.outline_expand_less_24)
					binding.fabShowOrderDetails.tag = Static.EXPAND_LESS_FAB
				} else {
					//показать меню заказа
					binding.fabShowOrderDetails.setImageResource(R.drawable.outline_expand_more_24)
					binding.fabShowOrderDetails.tag = Static.EXPAND_MORE_FAB
					binding.orderDetails.up(requireActivity())
					showClientFabOrderDetails(binding = binding, activity = requireActivity())
				}
			}
		}
	}
	
}