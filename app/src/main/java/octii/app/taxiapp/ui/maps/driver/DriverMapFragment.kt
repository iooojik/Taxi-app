package octii.app.taxiapp.ui.maps.driver

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
import octii.app.taxiapp.databinding.FragmentDriverMapBinding
import octii.app.taxiapp.models.coordinates.RemoteCoordinates
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.scripts.MyPreferences
import octii.app.taxiapp.scripts.down
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.scripts.up
import octii.app.taxiapp.services.Services
import octii.app.taxiapp.services.location.MyLocationListener
import octii.app.taxiapp.services.taximeter.TaximeterService
import octii.app.taxiapp.ui.FragmentHelper
import octii.app.taxiapp.ui.Permissions
import kotlin.concurrent.thread


class DriverMapFragment : Fragment(), View.OnClickListener,
    FragmentHelper {

    companion object{
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
    }

    private lateinit var binding: FragmentDriverMapBinding
    private lateinit var permissions: Permissions
    private var googleMap : GoogleMap? = null
    private var isMoved = false
    private var marker : Marker?  = null
    private var cameraMoved = false
    private val EXPAND_MORE_FAB = "expand more"
    private val EXPAND_LESS_FAB = "expand less"

    private var orderStatusReciever : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when(intent.getStringExtra(StaticOrders.ORDER_STATUS)){

                    StaticOrders.ORDER_STATUS_REQUEST -> {
                        logError(ordered)
                        if (ordered) {
                            ordered = false
                            DriverAcceptOrderBottomSheet(requireContext(), requireActivity(), OrdersModel()).show()
                        }
                    }
                    StaticOrders.ORDER_STATUS_ACCEPTED -> {
                        logError("accepted")
                        setOrderDetails()
                    }
                    StaticOrders.ORDER_STATUS_FINISHED -> {
                        ordered = true
                        binding.fabSettings.show()
                        binding.fabShowOrderDetails.setOnClickListener {
                            if (it.tag == EXPAND_MORE_FAB){
                                synchronized(this){
                                    binding.orderDetails.down(requireActivity())
                                    hideFabOrderDetails(true)
                                }
                            } else {
                                binding.fabShowOrderDetails.setImageResource(R.drawable.outline_expand_more_24)
                                binding.fabShowOrderDetails.tag = EXPAND_MORE_FAB
                                binding.orderDetails.up(requireActivity())
                            }
                        }
                        googleMap?.clear()
                    }
                }
            }
        }
    }

    private var coordinatesStatusReciever : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when(intent.getStringExtra(StaticCoordinates.COORDINATES_STATUS_UPDATE)){
                    StaticCoordinates.COORDINATES_STATUS_UPDATE_ -> {
                        logError("${RemoteCoordinates.remoteLat} ${RemoteCoordinates.remoteLon}")
                        if(RemoteCoordinates.remoteLat != 0.0 && RemoteCoordinates.remoteLon != 0.0){
                            if (googleMap != null) {
                                val latLng =
                                    LatLng(RemoteCoordinates.remoteLat, RemoteCoordinates.remoteLon)
                                if (marker != null) marker!!.remove()
                                marker = googleMap!!.addMarker(MarkerOptions()
                                    .position(latLng).title(resources.getString(R.string.customer))
                                    .icon(bitmapFromVector(requireContext(), R.drawable.user)))

                                if (!isMoved) {
                                    googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                                    isMoved = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDriverMapBinding.inflate(layoutInflater)
        setListeners()
        checkUserType()
        setUi()
        blockGoBack(requireActivity(), this)
        return binding.root
    }

    private fun setUi() {
        thread {
            while (!cameraMoved){
                if (!cameraMoved && googleMap != null && MyLocationListener.latitude != 0.0 && MyLocationListener.longitude != 0.0){
                    val lt = LatLng(MyLocationListener.latitude, MyLocationListener.longitude)
                    activity?.runOnUiThread {
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLng(lt))

                        if (googleMap != null && OrdersModel.isOrdered) {
                            val latLng =
                                LatLng(OrdersModel.mCustomer.coordinates!!.latitude, OrdersModel.mCustomer.coordinates!!.longitude)
                            if (marker != null) marker!!.remove()
                            marker = googleMap!!.addMarker(MarkerOptions()
                                .position(latLng).title(resources.getString(R.string.customer))
                                .icon(bitmapFromVector(requireContext(), R.drawable.user)))

                            if (!isMoved) {
                                googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                                isMoved = true
                            }
                        }
                        googleMap?.animateCamera(CameraUpdateFactory.zoomTo(12f))
                    }

                    cameraMoved = true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
        requireActivity().registerReceiver(orderStatusReciever, IntentFilter(StaticOrders.ORDER_STATUS_INTENT_FILTER))
        requireActivity().registerReceiver(coordinatesStatusReciever, IntentFilter(StaticOrders.ORDER_STATUS_COORDINATES_STATUS))

        if (OrdersModel.isAccepted && OrdersModel.mId > 0) {
            binding.fabSettings.hide()
        }
        try {
            setMap()
        } catch (e : java.lang.Exception){
            e.printStackTrace()
            Snackbar.make(requireView(), resources.getString(R.string.check_permissions), Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(orderStatusReciever)
        requireActivity().unregisterReceiver(coordinatesStatusReciever)
    }

    private fun checkUserType(){
        if (getSavedUserType() == Static.CLIENT_TYPE) findNavController().navigate(R.id.clientMapFragment)
    }

    private fun getSavedUserType() : String{
        return if (MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TYPE, "").isNullOrEmpty()) ""
        else MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TYPE, "")!!
    }

    private fun setOrderDetails(){
        logError("${OrdersModel.isAccepted && OrdersModel.mId > 0} ${OrdersModel.isAccepted} ${OrdersModel.mId > 0}")
        if (OrdersModel.isAccepted && OrdersModel.mId > 0) {
            binding.fabShowOrderDetails.setOnClickListener(this)
            showFabOrderDetails()
            binding.fabShowOrderDetails.setImageResource(R.drawable.outline_expand_more_24)
            binding.fabShowOrderDetails.tag = EXPAND_MORE_FAB
            binding.orderDetails.up(requireActivity())
        } else {
            binding.fabSettings.show()
        }
    }

    private fun showFabOrderDetails(){
        synchronized(this){
            binding.fabSettings.hide()
            binding.fabShowOrderDetails.show()
            binding.fabShowOrderDetails.up(requireActivity(), binding.orderDetails)
        }
    }

    private fun hideFabOrderDetails(fullHide : Boolean = false){
        synchronized(this){
            binding.fabShowOrderDetails.down(requireActivity(), false, binding.orderDetails)
            if (fullHide)
                binding.fabShowOrderDetails.hide()
        }
    }

    private fun setListeners(){
        binding.fabSettings.setOnClickListener(this)
    }

    private fun setMap(){
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun checkPermissions(){
        permissions = Permissions(requireContext(), requireActivity())
        permissions.requestPermissions()
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.fab_settings -> findNavController().navigate(R.id.driverSettingsFragment)
            R.id.fab_show_order_details -> {
                if (v.tag == EXPAND_MORE_FAB){
                    hideFabOrderDetails()
                    binding.orderDetails.down(requireActivity())
                    binding.fabShowOrderDetails.setImageResource(R.drawable.outline_expand_less_24)
                    binding.fabShowOrderDetails.tag = EXPAND_LESS_FAB
                } else {
                    binding.fabShowOrderDetails.setImageResource(R.drawable.outline_expand_more_24)
                    binding.fabShowOrderDetails.tag = EXPAND_MORE_FAB
                    binding.orderDetails.up(requireActivity())
                    showFabOrderDetails()
                }
            }
        }
    }

}