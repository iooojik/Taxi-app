package octii.app.taxiapp.ui.maps.client

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import octii.app.taxiapp.LocaleUtils
import octii.app.taxiapp.MyPreferences
import octii.app.taxiapp.R
import octii.app.taxiapp.Static
import octii.app.taxiapp.databinding.FragmentClientMapBinding
import octii.app.taxiapp.models.OrdersModel
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.sockets.location.LocationService
import octii.app.taxiapp.ui.settings.CircularTransformation
import octii.app.taxiapp.web.SocketHelper
import java.util.*


class ClientMapFragment : Fragment(), View.OnClickListener, View.OnLongClickListener {

    lateinit var binding: FragmentClientMapBinding
    private lateinit var mTimer : Timer
    private lateinit var orderUpdate : OrderUpdate


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


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentClientMapBinding.inflate(layoutInflater)
        binding.callTaxi.setOnClickListener(this)
        binding.fabSettings.setOnClickListener(this)
        setTimer()
        view?.findViewById<ImageView>(R.id.call_to_driver)?.setOnClickListener(this)
        view?.findViewById<TextView>(R.id.driver_phone)?.setOnLongClickListener(this)
        MyPreferences.userPreferences?.let {
            MyPreferences.saveToPreferences(
                it, Static.SHARED_PREFERENCES_USER_TYPE, Static.CLIENT_TYPE)
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
    }

    override fun onResume() {
        super.onResume()
        view?.findViewById<ConstraintLayout>(R.id.client_order_info_layout)?.visibility = View.GONE
    }

    private fun setTimer() {
        orderUpdate = OrderUpdate(binding.root, requireActivity())
        mTimer = Timer()
        mTimer.schedule(orderUpdate, 0, 1000)
    }

    private fun setMap(){
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.call_taxi -> {
                SocketHelper.makeOrder()
                OrdersModel.isOrdered = true
                binding.callTaxi.hide()
            }
            R.id.fab_settings -> findNavController().navigate(R.id.clientSettingsFragment)
            R.id.call_to_driver -> callToDriver()
        }
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

    private fun callToDriver() {
        val dial = "tel:${OrdersModel.mDriver.phone}"
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(dial)))
    }

    inner class OrderUpdate(
        private val view: View, private val activity: Activity,
    ) : TimerTask() {

        override fun run() {
            activity.runOnUiThread {
                val savedUserType = if (MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TYPE, Static.CLIENT_TYPE).isNullOrEmpty()) Static.CLIENT_TYPE
                else MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TYPE, Static.CLIENT_TYPE)!!

                if (savedUserType != Static.CLIENT_TYPE) findNavController().navigate(R.id.driverMapFragment)

                if (OrdersModel.isAccepted && OrdersModel.mDriverID > 0) {
                    binding.callTaxi.hide()
                    view.findViewById<TextView>(R.id.driver_name).text = OrdersModel.mDriver.userName
                    view.findViewById<TextView>(R.id.driver_phone).text = OrdersModel.mDriver.phone
                    val avatarView = view.findViewById<ImageView>(R.id.driver_avatar)
                    if (OrdersModel.mDriver.avatarURL.isNotEmpty()){
                        Picasso.with(context)
                            .load(OrdersModel.mDriver.avatarURL)
                            .transform(CircularTransformation(0f))
                            .into(avatarView)
                    } else {
                        avatarView.setImageResource(R.drawable.outline_account_circle_24)
                    }
                    view.findViewById<ConstraintLayout>(R.id.client_order_info_layout).visibility = View.VISIBLE
                } else {
                    view.findViewById<ConstraintLayout>(R.id.client_order_info_layout)?.visibility = View.GONE
                    logError(!binding.callTaxi.isVisible && !OrdersModel.isOrdered)
                    logError(!binding.callTaxi.isVisible)
                    logError(!OrdersModel.isOrdered)
                    if (!binding.callTaxi.isVisible && !OrdersModel.isOrdered) binding.callTaxi.show()
                }
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        when(v!!.id){
            R.id.driver_phone -> {
                val clipboard: ClipboardManager =
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("", view?.findViewById<TextView>(R.id.driver_phone)?.text.toString())
                clipboard.setPrimaryClip(clip)
                Snackbar.make(binding.root, resources.getString(R.string.copied), Snackbar.LENGTH_SHORT).show()
            }
        }
        return true
    }

}