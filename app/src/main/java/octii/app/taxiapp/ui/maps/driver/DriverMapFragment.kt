package octii.app.taxiapp.ui.maps.driver

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import octii.app.taxiapp.FragmentHelper
import octii.app.taxiapp.R
import octii.app.taxiapp.Static
import octii.app.taxiapp.databinding.FragmentDriverMapBinding
import octii.app.taxiapp.databinding.TaximeterBinding
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.driverAvailable.DriverAvailable
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.services.Services
import octii.app.taxiapp.services.location.MyLocationListener
import octii.app.taxiapp.ui.Permissions
import octii.app.taxiapp.web.SocketHelper
import java.util.*

class DriverMapFragment : Fragment(), View.OnClickListener, View.OnLongClickListener, FragmentHelper {

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
        /*
        try {
            val md = GMapV2Direction()

            val doc: Document =
                md.getDocument(LatLng(OrdersModel.mDriver.coordinates.latitude, OrdersModel.mDriver.coordinates.longitude),
                    LatLng(OrdersModel.mCustomer.coordinates.latitude, OrdersModel.mCustomer.coordinates.longitude),
                    GMapV2Direction.MODE_DRIVING)

            val directionPoint = md.getDirection(doc)
            logError(directionPoint)
            val rectLine = PolylineOptions().width(3f).color(Color.RED)

            for (i in 0 until directionPoint.size) {
                rectLine.add(directionPoint[i])
            }

            googleMap.addPolyline(rectLine)
        } catch (e : Exception){
            e.printStackTrace()
            Log.e(TAG, "err: $e" )
        }*/

        googleMap.isMyLocationEnabled = true


    }

    private lateinit var binding: FragmentDriverMapBinding
    private lateinit var ordersUpdate : OrdersUpdate
    private lateinit var mTimer: Timer
    private lateinit var permissions: Permissions
    private lateinit var services: Services
    private var orderTime : Long = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDriverMapBinding.inflate(layoutInflater)
        setListeners()
        checkUserType()
        setServices()
        setUiInfo()
        blockGoBack(requireActivity(), this)
        return binding.root
    }

    private fun setUiInfo() {
        binding.taximeter.price.text = resources.getString(R.string.taximeter_price,
            "0", "0")
        binding.taximeter.time.text = formatDuration(orderTime)
        binding.taximeter.distance.text = resources.getString(R.string.taximeter_distance, MyLocationListener.distance.toString()  )
    }

    override fun onResume() {
        super.onResume()
        view?.findViewById<ConstraintLayout>(R.id.driver_order_info_layout)?.visibility = View.GONE
        checkPermissions()
        setTimer()
        try {
            setMap()
        } catch (e : java.lang.Exception){
            e.printStackTrace()
            Snackbar.make(requireView(), resources.getString(R.string.check_permissions), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun checkUserType(){
        if (UserModel.uType == Static.CLIENT_TYPE) findNavController().navigate(R.id.clientMapFragment)
    }

    private fun setListeners(){
        binding.fabSettings.setOnClickListener(this)
        binding.driverOrderInfoLayout.customerPhone.setOnLongClickListener(this)
        binding.driverOrderInfoLayout.callToCustomer.setOnClickListener(this)
    }

    private fun setTimer() {
        ordersUpdate = OrdersUpdate(binding.root, requireContext())
        mTimer = Timer()
        mTimer.schedule(ordersUpdate, 0, 1000)
    }

    private fun setServices(){
        services = Services(requireActivity(), Static.MAIN_SERVICES)
        services.start()
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
            R.id.finish_order -> finishOrder()
            R.id.call_to_customer -> callToCustomer()
        }
    }

    private fun callToCustomer() {
        if (OrdersModel.mCustomer.phone.isNotEmpty()) {
            val dial = "tel:${OrdersModel.mCustomer.phone}"
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(dial)))
        }
    }

    private fun finishOrder(){
        OrdersModel.isAccepted = false
        SocketHelper.finishOrder(OrdersModel())
        view?.findViewById<ConstraintLayout>(R.id.driver_order_info_layout)?.visibility = View.GONE
        MyLocationListener.distance = 0f
    }

    inner class OrdersUpdate(private val view: View, private val context : Context) : TimerTask() {

        override fun run() {
            if (activity != null) {
                activity!!.runOnUiThread {
                    if (OrdersModel.isOrdered) {
                        val bottomSheet =
                            DriverAcceptOrderBottomSheet(context, activity!!, OrdersModel())
                        bottomSheet.show()
                    } else if (OrdersModel.isAccepted) {
                        orderTime = orderTime.plus(1)
                        taximeterUpdate(binding.taximeter)
                        binding.driverOrderInfoLayout.customerName.text =
                            OrdersModel.mCustomer.userName
                        binding.driverOrderInfoLayout.customerPhone.text =
                            OrdersModel.mCustomer.phone
                        binding.driverOrderInfoLayout.finishOrder.setOnClickListener(this@DriverMapFragment)
                        view.findViewById<ConstraintLayout>(R.id.driver_order_info_layout).visibility =
                            View.VISIBLE

                        if (OrdersModel.mCustomer.isWhatsapp)
                            binding.driverOrderInfoLayout.messengersInfo.text =
                                activity!!.resources.getString(R.string.user_available_in_whatsapp)
                        else if (OrdersModel.mCustomer.isViber)
                            binding.driverOrderInfoLayout.messengersInfo.text =
                                activity!!.resources.getString(R.string.user_available_in_viber)
                        else if (OrdersModel.mCustomer.isViber && OrdersModel.mCustomer.isWhatsapp)
                            binding.driverOrderInfoLayout.messengersInfo.text =
                                activity!!.resources.getString(R.string.user_available_in_viber_and_whatsapp)

                        if (OrdersModel.mCustomer.avatarURL.trim().isNotEmpty()) {
                            Picasso.with(requireContext())
                                .load(OrdersModel.mCustomer.avatarURL)
                                .transform(RoundedCornersTransformation(40, 5))
                                .resize(160, 160)
                                .centerCrop()
                                .into(binding.driverOrderInfoLayout.customerAvatar)
                        } else {
                            binding.driverOrderInfoLayout.customerAvatar.setImageResource(R.drawable.outline_account_circle_24)
                        }
                    } else {
                        orderTime = 0
                        taximeterUpdate(binding.taximeter)
                    }

                }
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        when(v!!.id){
            R.id.customer_phone -> {
                val clipboard: ClipboardManager =
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("", binding.driverOrderInfoLayout.customerPhone.text.toString())
                clipboard.setPrimaryClip(clip)
                Snackbar.make(binding.root, resources.getString(R.string.copied), Snackbar.LENGTH_SHORT).show()
            }
        }
        return true
    }

    private fun formatDuration(seconds: Long): String = DateUtils.formatElapsedTime(seconds)

    private fun taximeterUpdate(taximeterBinding: TaximeterBinding){
        taximeterBinding.time.text = formatDuration(seconds = orderTime)
        taximeterBinding.distance.text = MyLocationListener.distance.toString()
        taximeterBinding.price.text = resources.getString(
            R.string.taximeter_price,
            (DriverAvailable.mPricePerKm * MyLocationListener.distance).toString(),
            if(orderTime/60 < 1) DriverAvailable.mPricePerMinute.toString()
            else (DriverAvailable.mPricePerMinute * (orderTime/60))
        )
    }
}