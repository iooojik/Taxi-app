package octii.app.taxiapp.ui.maps.driver

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import octii.app.taxiapp.MyPreferences
import octii.app.taxiapp.R
import octii.app.taxiapp.Static
import octii.app.taxiapp.databinding.FragmentDriverMapBinding
import octii.app.taxiapp.models.OrdersModel
import octii.app.taxiapp.scripts.logDebug
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.sockets.location.LocationService
import octii.app.taxiapp.sockets.location.MyLocationListener
import octii.app.taxiapp.ui.maps.GMapV2Direction
import octii.app.taxiapp.ui.settings.CircularTransformation
import octii.app.taxiapp.web.SocketHelper
import org.w3c.dom.Document
import java.util.*

class DriverMapFragment : Fragment(), View.OnClickListener, View.OnLongClickListener {

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
        logError(OrdersModel.mCustomer.coordinates != null)
        try {
            if (OrdersModel.mCustomer.coordinates != null) {
                val md = GMapV2Direction()

                val doc: Document =
                    md.getDocument(LatLng(MyLocationListener.latitude, MyLocationListener.longitude),
                        LatLng(OrdersModel.mCustomer.coordinates!!.latitude, OrdersModel.mCustomer.coordinates!!.longitude),
                        GMapV2Direction.MODE_DRIVING)

                val directionPoint = md.getDirection(doc)
                logError(directionPoint)
                val rectLine = PolylineOptions().width(3f).color(Color.RED)

                for (i in 0 until directionPoint.size) {
                    rectLine.add(directionPoint[i])
                }

                googleMap.addPolyline(rectLine)
            }
        } catch (e : Exception){
            e.printStackTrace()
        }

        googleMap.isMyLocationEnabled = true


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
        MyPreferences.userPreferences?.let {
            MyPreferences.saveToPreferences(
                it, Static.SHARED_PREFERENCES_USER_TYPE, Static.DRIVER_TYPE)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            requestPermissions(true)
        } catch (e : Exception){
            e.printStackTrace()
        }
        setTimer()
        binding.fabSettings.setOnClickListener(this)
        view.findViewById<TextView>(R.id.customer_phone)?.setOnLongClickListener(this)
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

    private fun requestPermissions(startLocationService : Boolean){
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
            requestPermissions(startLocationService)
        } else {
            if (startLocationService){
                val intentService = Intent(requireContext(), LocationService::class.java)
                requireActivity().startService(intentService)
            }
            setMap()
        }
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
                val savedUserType = if (MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TYPE, Static.CLIENT_TYPE).isNullOrEmpty()) Static.CLIENT_TYPE
                else MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TYPE, Static.CLIENT_TYPE)!!

                if (savedUserType != Static.DRIVER_TYPE) findNavController().navigate(R.id.clientMapFragment)

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
                    val avatarView = view.findViewById<ImageView>(R.id.customer_avatar)
                    if (OrdersModel.mCustomer.avatarURL.isNotEmpty()){
                        Picasso.with(requireContext())
                            .load(OrdersModel.mCustomer.avatarURL)
                            .transform(CircularTransformation(0f))
                            .into(avatarView)
                    } else {
                        avatarView.setImageResource(R.drawable.outline_account_circle_24)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mTimer.cancel()
        mTimer.purge()
    }

    override fun onLongClick(v: View?): Boolean {
        when(v!!.id){
            R.id.customer_phone -> {
                val clipboard: ClipboardManager =
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("", view?.findViewById<TextView>(R.id.customer_phone)?.text.toString())
                clipboard.setPrimaryClip(clip)
                Snackbar.make(binding.root, resources.getString(R.string.copied), Snackbar.LENGTH_SHORT).show()
            }
        }
        return true
    }
}