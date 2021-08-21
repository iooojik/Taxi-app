package octii.app.taxiapp.ui.utils

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.navigation.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.databinding.FragmentClientMapBinding
import octii.app.taxiapp.databinding.FragmentDriverMapBinding
import octii.app.taxiapp.models.driver.DriverModel
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.*
import octii.app.taxiapp.services.location.MyLocationListener
import octii.app.taxiapp.ui.Permissions
import octii.app.taxiapp.ui.maps.client.ClientMapFragment
import octii.app.taxiapp.ui.maps.driver.DriverMapFragment
import kotlin.concurrent.thread

interface MapUtils : View.OnClickListener, FragmentHelper {
	
	fun checkViber(context: Context, activity: Activity): Boolean {
		return if (!isInstalled(Static.VIBER_PACKAGE_NAME, activity.packageManager)) {
			showSnackbar(context, activity.resources.getString(R.string.not_installed_viber))
			false
		} else true
	}
	//******************************************************
	//****************DRIVER
	
	fun hideDriverFabOrderDetails(
		fullHide: Boolean = false,
		binding: FragmentDriverMapBinding,
		activity: Activity,
	) {
		synchronized(this) {
			binding.fabShowOrderDetails.down(activity, false, binding.orderDetails)
			if (fullHide) binding.fabShowOrderDetails.hide()
		}
	}
	
	
	fun getSavedUserType(): String {
		return if (MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TYPE, "")
				.isNullOrEmpty()
		) ""
		else MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TYPE, "")!!
	}
	
	fun showDriverFabOrderDetails(binding: FragmentDriverMapBinding, activity: Activity) {
		synchronized(this) {
			binding.fabShowOrderDetails.show()
			binding.fabShowOrderDetails.up(activity, binding.orderDetails)
		}
	}
	
	
	//******************************************************
//****************DRIVER
//****************CLIENT
	fun showClientFabOrderDetails(binding: FragmentClientMapBinding, activity: Activity) {
		synchronized(this) {
			binding.fabShowOrderDetails.show()
			binding.callTaxi.hide()
			binding.fabShowOrderDetails.up(activity, binding.orderDetails)
		}
	}
	
	fun hideClientFabOrderDetails(
		fullHide: Boolean = false,
		binding: FragmentClientMapBinding,
		activity: Activity,
	) {
		synchronized(this) {
			binding.fabShowOrderDetails.down(activity, false, binding.orderDetails)
			if (fullHide)
				binding.fabShowOrderDetails.hide()
		}
	}
	
	fun checkPermissions(context: Context, activity: Activity) {
		//проверка разрешений приложения
		Permissions(context, activity).requestPermissions()
	}
	
	fun setListeners()
	
	fun setMap()
	
	fun checkDriverType(activity: Activity) {
		if (getSavedUserType() == Static.CLIENT_TYPE)
			activity.findNavController(R.id.nav_host_fragment).navigate(R.id.clientMapFragment)
	}
	
	fun checkClientType(activity: Activity) {
		if (getSavedUserType() == Static.DRIVER_TYPE)
			activity.findNavController(R.id.nav_host_fragment).navigate(R.id.driverMapFragment)
	}
	
	fun setOrderDetails()
	
	fun locateMe(
		activity: Activity, context: Context,
		driverMapFragment: DriverMapFragment? = null,
		clientMapFragment: ClientMapFragment? = null,
	) {
		val mapType: String
		val googleMap: GoogleMap?
		var marker: Marker?
		var cameraisMoved: Boolean
		val resources = activity.resources
		when {
			driverMapFragment != null -> {
				mapType = Static.DRIVER_TYPE
				googleMap = driverMapFragment.googleMap
				marker = driverMapFragment.marker
				cameraisMoved = driverMapFragment.cameraisMoved
			}
			clientMapFragment != null -> {
				mapType = Static.CLIENT_TYPE
				googleMap = clientMapFragment.googleMap
				marker = clientMapFragment.marker
				cameraisMoved = clientMapFragment.cameraisMoved
			}
			else -> return
		}
		thread {
			//пока не переместили камеру, проверяем координаты текущего пользователя
			while (!cameraisMoved) {//если камера не перемещена и координаты != 0.0, то пробуем переместить камеру
				if (!cameraisMoved && googleMap != null &&
					MyLocationListener.latitude != 0.0
					&& MyLocationListener.longitude != 0.0
				) {
					//координаты текущего пользователя
					val lt = LatLng(MyLocationListener.latitude, MyLocationListener.longitude)
					logInfo(lt)
					activity.runOnUiThread {
						//перемещаем камеру
						googleMap.moveCamera(CameraUpdateFactory.newLatLng(lt))
						if (UserModel.uType == Static.DRIVER_TYPE)
							googleMap.animateCamera(CameraUpdateFactory.zoomTo(getZoomLevel(
								DriverModel.mRideDistance)))
						else googleMap.animateCamera(CameraUpdateFactory.zoomTo(getZoomLevel(Static.STANDART_ZOOM_LEVEL)))
						//если был сделан заказ, то показываем модель таксиста или клиента на карте
						if (OrdersModel.isOrdered && OrdersModel.mIsAccepted) {
							if (mapType == Static.DRIVER_TYPE) {
								val latLng =
									LatLng(OrdersModel.mCustomer.coordinates!!.latitude,
										OrdersModel.mCustomer.coordinates!!.longitude)
								marker?.remove()
								marker = googleMap.addMarker(MarkerOptions()
									.position(latLng).title(resources.getString(R.string.customer))
									.icon(bitmapFromVector(context, R.drawable.user)))
								
								if (!cameraisMoved) {
									googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
									cameraisMoved = true
								}
							} else if (mapType == Static.CLIENT_TYPE) {
								val latLng =
									LatLng(OrdersModel.mDriver.coordinates!!.latitude,
										OrdersModel.mDriver.coordinates!!.longitude)
								marker?.remove()
								marker = googleMap.addMarker(MarkerOptions()
									.position(latLng).title(resources.getString(R.string.driver))
									.icon(bitmapFromVector(context, R.drawable.car)))
								
								if (!cameraisMoved) {
									googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
									cameraisMoved = true
								}
							}
						}
						cameraisMoved = true
					}
				}
			}
		}
	}
}