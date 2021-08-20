package octii.app.taxiapp.ui.maps.client

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.constants.StaticCoordinates
import octii.app.taxiapp.constants.StaticOrders
import octii.app.taxiapp.databinding.FragmentClientMapBinding
import octii.app.taxiapp.models.coordinates.RemoteCoordinates
import octii.app.taxiapp.models.driver.DriverModel
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.scripts.*
import octii.app.taxiapp.services.location.MyLocationListener
import octii.app.taxiapp.ui.utils.FragmentHelper
import octii.app.taxiapp.ui.Permissions
import octii.app.taxiapp.ui.maps.client.recievers.ClientCoordinatesReciever
import octii.app.taxiapp.ui.maps.client.recievers.ClientOrderReciever
import octii.app.taxiapp.web.SocketHelper
import kotlin.concurrent.thread


class ClientMapFragment : Fragment(), View.OnClickListener, FragmentHelper {

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


    }

    private lateinit var binding: FragmentClientMapBinding
    private lateinit var permissions: Permissions
    private var googleMap: GoogleMap? = null
    var isMoved = false
    private var marker: Marker? = null
    private var cameraMoved = false
    private lateinit var clientOrderReciever: ClientOrderReciever
    private lateinit var clientCoordinatesReciever: ClientCoordinatesReciever

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentClientMapBinding.inflate(layoutInflater)
        setListeners()
        checkUserType()
        blockGoBack(requireActivity(), this)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        clientOrderReciever = ClientOrderReciever(binding, requireActivity(), googleMap, this, requireContext())
        clientCoordinatesReciever = ClientCoordinatesReciever(requireActivity(), googleMap, this, requireContext(), marker)
        requireActivity().registerReceiver(clientOrderReciever, IntentFilter(StaticOrders.ORDER_STATUS_INTENT_FILTER))
        requireActivity().registerReceiver(clientCoordinatesReciever, IntentFilter(StaticOrders.ORDER_STATUS_COORDINATES_STATUS))
        checkPermissions()
        moveGoogleCameraToMe()
        setOrderDetails()
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
        try {
            requireActivity().unregisterReceiver(clientOrderReciever)
            requireActivity().unregisterReceiver(clientCoordinatesReciever)
        } catch (e : Exception){
            e.printStackTrace()
        }
    }

    fun setOrderDetails() {
        if (OrdersModel.isAccepted && OrdersModel.mId > 0) {
            binding.fabShowOrderDetails.setOnClickListener(this)
            binding.callTaxi.hide()
            showFabOrderDetails()
            binding.fabShowOrderDetails.setImageResource(R.drawable.outline_expand_more_24)
            binding.fabShowOrderDetails.tag = Static.EXPAND_MORE_FAB
            binding.orderDetails.up(requireActivity())
        } else {
            logError("call taxi shown")
            binding.callTaxi.show()
            binding.fabSettings.show()
        }
    }

    private fun showFabOrderDetails() {
        synchronized(this) {
            binding.fabShowOrderDetails.show()
            binding.callTaxi.hide()
            binding.fabShowOrderDetails.up(requireActivity(), binding.orderDetails)
        }
    }

    fun hideFabOrderDetails(fullHide: Boolean = false) {
        synchronized(this) {
            binding.fabShowOrderDetails.down(requireActivity(), false, binding.orderDetails)
            if (fullHide)
                binding.fabShowOrderDetails.hide()
        }
    }

    private fun moveGoogleCameraToMe() {
        //переводит камеру
        thread {
            while (!cameraMoved) {
                if (!cameraMoved && googleMap != null && MyLocationListener.latitude != 0.0 && MyLocationListener.longitude != 0.0) {
                    val lt = LatLng(MyLocationListener.latitude, MyLocationListener.longitude)
                    activity?.runOnUiThread {
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLng(lt))
                        logInfo("zoom level: ${getZoomLevel(DriverModel.mRideDistance)}")
                        logInfo("rideDistance: ${DriverModel.mRideDistance}")
                        googleMap?.animateCamera(CameraUpdateFactory.zoomTo(getZoomLevel(DriverModel.mRideDistance)))
                        if (OrdersModel.isAccepted && OrdersModel.mId > 0) {
                            binding.callTaxi.hide()
                        } else {
                            binding.callTaxi.show()
                            binding.fabSettings.show()
                        }
                    }
                    cameraMoved = true
                }
            }
        }
    }

    private fun setListeners() {
        binding.callTaxi.setOnClickListener(this)
        binding.fabSettings.setOnClickListener(this)
    }

    private fun checkUserType() {
        if (getSavedUserType() == Static.DRIVER_TYPE) findNavController().navigate(R.id.driverMapFragment)
    }

    private fun getSavedUserType(): String =
        if (MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TYPE, "")
                .isNullOrEmpty()
        ) ""
        else MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TYPE, "")!!

    private fun checkPermissions() {
        permissions = Permissions(requireContext(), requireActivity())
        permissions.requestPermissions()
    }

    private fun setMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.call_taxi -> {
                SocketHelper.makeOrder()
                OrdersModel.isOrdered = true
                binding.callTaxi.hide()
                binding.clientMapprogressBar.visibility = View.VISIBLE
            }
            R.id.fab_settings -> {
                findNavController().navigate(R.id.clientSettingsFragment)
            }
            R.id.fab_show_order_details -> {
                if (v.tag == Static.EXPAND_MORE_FAB) {
                    hideFabOrderDetails()
                    binding.orderDetails.down(requireActivity())
                    binding.fabShowOrderDetails.setImageResource(R.drawable.outline_expand_less_24)
                    binding.fabShowOrderDetails.tag = Static.EXPAND_LESS_FAB
                } else {
                    binding.fabShowOrderDetails.setImageResource(R.drawable.outline_expand_more_24)
                    binding.fabShowOrderDetails.tag = Static.EXPAND_MORE_FAB
                    binding.orderDetails.up(requireActivity())
                    showFabOrderDetails()
                }
            }
        }
    }

}