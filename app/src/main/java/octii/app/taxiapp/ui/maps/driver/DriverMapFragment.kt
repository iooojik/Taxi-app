package octii.app.taxiapp.ui.maps.driver

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController

import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import octii.app.taxiapp.R
import octii.app.taxiapp.databinding.FragmentDriverMapBinding
import octii.app.taxiapp.models.OrdersModel
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.web.SocketHelper
import java.util.*

class DriverMapFragment : Fragment(), View.OnClickListener {

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
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ).toTypedArray(),
                101
            )
            setMap()
        } else {
            googleMap.isMyLocationEnabled = true

        }
    }

    private lateinit var binding: FragmentDriverMapBinding
    private lateinit var ordersUpdate : OrdersUpdate
    private lateinit var mTimer: Timer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDriverMapBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMap()
        setTimer()
        binding.fabSettings.setOnClickListener(this)
        view.findViewById<ImageView>(R.id.call_to_customer).setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        view?.findViewById<ConstraintLayout>(R.id.driver_order_info_layout)?.visibility = View.GONE
    }

    private fun setTimer() {
        ordersUpdate = OrdersUpdate(requireView(), requireContext(), requireActivity())
        mTimer = Timer()
        mTimer.schedule(ordersUpdate, 0, 5000)
    }

    private fun setMap(){
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.fab_settings -> findNavController().navigate(R.id.driverSettingsFragment)
            R.id.finish_order -> finishOrder()
            R.id.call_to_customer -> callToCustomer()
        }
    }

    private fun callToCustomer() {
        val dial = "tel:${OrdersModel.mCustomer.phone}"
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(dial)))
    }

    private fun finishOrder(){
        OrdersModel.isAccepted = false
        SocketHelper.finishOrder(OrdersModel())
        view?.findViewById<ConstraintLayout>(R.id.driver_order_info_layout)?.visibility = View.GONE
    }

    inner class OrdersUpdate(private val view: View, private val context : Context,
                       private val activity : Activity) : TimerTask() {

        override fun run() {
            activity.runOnUiThread {
                logInfo("running")
                if (OrdersModel.isOrdered){
                    logInfo("isOrdered")
                    val bottomSheet = DriverAcceptOrderBottomSheet(context, activity, OrdersModel())
                    bottomSheet.show()
                } else if (OrdersModel.isAccepted) {
                    logInfo("isAccepted")
                    view.findViewById<TextView>(R.id.customer_name).text = OrdersModel.mCustomer.userName
                    view.findViewById<TextView>(R.id.customer_phone).text = OrdersModel.mCustomer.phone
                    view.findViewById<Button>(R.id.finish_order).setOnClickListener(this@DriverMapFragment)
                    view.findViewById<ConstraintLayout>(R.id.driver_order_info_layout).visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mTimer.cancel()
        mTimer.purge()
    }
}