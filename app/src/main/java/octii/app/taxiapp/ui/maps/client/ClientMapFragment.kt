package octii.app.taxiapp.ui.maps.client

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import octii.app.taxiapp.FragmentHelper
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.databinding.FragmentClientMapBinding
import octii.app.taxiapp.models.coordinates.RemoteCoordinates
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.services.Services
import octii.app.taxiapp.services.location.MyLocationListener
import octii.app.taxiapp.ui.Permissions
import octii.app.taxiapp.web.SocketHelper
import java.util.*





class ClientMapFragment : Fragment(), View.OnClickListener, View.OnLongClickListener, FragmentHelper {

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
    private lateinit var mTimer : Timer
    private lateinit var orderUpdate : OrderUpdate
    private lateinit var permissions: Permissions
    private lateinit var services: Services
    private lateinit var googleMap : GoogleMap
    private var isMoved = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentClientMapBinding.inflate(layoutInflater)
        setTimer()
        setListeners()
        checkUserType()
        setServices()
        blockGoBack(requireActivity(), this)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        view?.findViewById<ConstraintLayout>(R.id.client_order_info_layout)?.visibility = View.GONE
        checkPermissions()
        try {
            setMap()
        } catch (e : Exception){
            e.printStackTrace()
            Snackbar.make(requireView(), resources.getString(R.string.check_permissions), Snackbar.LENGTH_SHORT).show()
        }

    }

    private fun setListeners(){
        binding.callTaxi.setOnClickListener(this)
        binding.fabSettings.setOnClickListener(this)
        binding.clientOrderInfoLayout.callToDriver.setOnClickListener(this)
        binding.clientOrderInfoLayout.driverPhone.setOnClickListener(this)
    }

    private fun checkUserType(){
        if (UserModel.uType == Static.DRIVER_TYPE) findNavController().navigate(R.id.driverMapFragment)
    }

    private fun setServices(){
        services = Services(requireActivity(), Static.MAIN_SERVICES)
        services.start()
    }

    private fun checkPermissions(){
        permissions = Permissions(requireContext(), requireActivity())
        permissions.requestPermissions()
    }

    private fun setTimer() {
        orderUpdate = OrderUpdate(binding.root, requireActivity())
        mTimer = Timer()
        mTimer.schedule(orderUpdate, 0, 1000)
    }

    private fun setMap(){
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

    }



    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.call_taxi -> {
                SocketHelper.makeOrder()
                OrdersModel.isOrdered = true
                binding.callTaxi.hide()
                binding.clientMapprogressBar.visibility = View.VISIBLE
            }
            R.id.fab_settings -> findNavController().navigate(R.id.clientSettingsFragment)
            R.id.call_to_driver -> callToDriver()
        }
    }

    private fun callToDriver() {
        if (OrdersModel.mDriver.phone.isNotEmpty()) {
            val dial = "tel:${OrdersModel.mDriver.phone}"
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(dial)))
        }
    }

    inner class OrderUpdate(
        private val view: View, private val activity: Activity,
    ) : TimerTask() {

        override fun run() {
            activity.runOnUiThread {

                if (OrdersModel.isAccepted && OrdersModel.mDriverID > 0) {
                    binding.callTaxi.hide()
                    binding.clientMapprogressBar.visibility = View.INVISIBLE
                    binding.clientOrderInfoLayout.driverName.text = OrdersModel.mDriver.userName
                    binding.clientOrderInfoLayout.driverPhone.text = OrdersModel.mDriver.phone

                    if (OrdersModel.mUuid.trim().isNotEmpty()){
                        if(RemoteCoordinates.remoteLat != 0.0 && RemoteCoordinates.remoteLon != 0.0) {
                            val latLng =
                                LatLng(RemoteCoordinates.remoteLat, RemoteCoordinates.remoteLon)
                            googleMap.addMarker(MarkerOptions()
                                .position(latLng).title(resources.getString(R.string.driver))
                                .icon(bitmapFromVector(requireContext(), R.drawable.car)))
                            if (!isMoved) {
                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                                isMoved = true
                            }
                        }
                    }

                    if (OrdersModel.mDriver.avatarURL.isNotEmpty()){
                        Picasso.with(context)
                            .load(OrdersModel.mDriver.avatarURL)
                            .transform(RoundedCornersTransformation(40, 5))
                            .resize(160, 160)
                            .centerCrop()
                            .into(binding.clientOrderInfoLayout.driverAvatar)
                    } else {
                        binding.clientOrderInfoLayout.driverAvatar.setImageResource(R.drawable.outline_account_circle_24)
                    }
                    view.findViewById<ConstraintLayout>(R.id.client_order_info_layout).visibility = View.VISIBLE
                } else {
                    view.findViewById<ConstraintLayout>(R.id.client_order_info_layout)?.visibility = View.GONE
                    if (!binding.callTaxi.isVisible && !OrdersModel.isOrdered){
                        binding.callTaxi.show()
                        binding.clientMapprogressBar.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        when(v!!.id){
            R.id.driver_phone -> {
                val clipboard: ClipboardManager =
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("", binding.clientOrderInfoLayout.driverPhone.text.toString())
                clipboard.setPrimaryClip(clip)
                Snackbar.make(binding.root, resources.getString(R.string.copied), Snackbar.LENGTH_SHORT).show()
            }
        }
        return true
    }



}