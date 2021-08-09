package octii.app.taxiapp.ui.maps.client.orderDetails

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import octii.app.taxiapp.constants.StaticOrders
import octii.app.taxiapp.constants.StaticTaximeter
import octii.app.taxiapp.databinding.FragmentClientTaximeterDetailsBinding
import octii.app.taxiapp.models.driver.DriverModel
import octii.app.taxiapp.scripts.MyPreferences
import octii.app.taxiapp.services.location.MyLocationListener


class TaximeterDetailsFragment : Fragment(), View.OnClickListener {

    lateinit var binding : FragmentClientTaximeterDetailsBinding

    private val taximeterReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val time = intent.getLongExtra(StaticTaximeter.TAXIMETER_BUNDLE_TIME, 0L)
            val waitingTime = intent.getLongExtra(StaticTaximeter.TAXIMETER_BUNDLE_WAINTING_TIME, 0L)
            setTaximeter(time, waitingTime)
        }
    }

    private fun number2digits(number : Float) : String = String.format("%.2f", number)

    private fun formatDuration(seconds: Long): String = DateUtils.formatElapsedTime(seconds)

    private fun setTaximeter(time : Long, waitingTime : Long){
        val distance = MyLocationListener.distance
        val totalPricePerKm = DriverModel.mPrices.pricePerKm * MyLocationListener.distance
        val totalPricePerMin = if(time/60 < 1) DriverModel.mPrices.pricePerMinute
        else DriverModel.mPrices.pricePerMinute * (time / 60)

        val totalPriceWaiting = DriverModel.mPrices.priceWaitingMin * waitingTime/60

        binding.taximeter.distance.text = distance.toString()
        binding.taximeter.pricePerKm.text = number2digits(totalPricePerKm)
        binding.taximeter.pricePerMin.text = number2digits(totalPricePerMin)
        binding.taximeter.priceWaiting.text = number2digits(totalPriceWaiting)
        val totalPriceKm = totalPricePerKm + totalPriceWaiting
        val totalPriceMin = totalPricePerMin + totalPriceWaiting
        binding.taximeter.priceTotal.text = "${number2digits(totalPriceKm)}/${number2digits(totalPriceMin)}"

        binding.taximeter.time.text = formatDuration(time)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentClientTaximeterDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requireActivity().registerReceiver(taximeterReceiver, IntentFilter(StaticTaximeter.TAXIMETER_INTENT_FILTER))
        setUiInfo()
        setTaximeter(getOrderTime(), getWaitingTime())
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(taximeterReceiver)
    }

    private fun getOrderTime() : Long =
        if (MyPreferences.taximeterPreferences?.getLong(StaticOrders.SHARED_PREFERENCES_ORDER_TIME, 0L) == null) 0L
        else MyPreferences.taximeterPreferences?.getLong(StaticOrders.SHARED_PREFERENCES_ORDER_TIME, 0L)!!

    private fun getWaitingTime() : Long =
        if (MyPreferences.taximeterPreferences?.getLong(StaticOrders.SHARED_PREFERENCES_ORDER_WAITING_TIME, 0L) == null) 0L
        else MyPreferences.taximeterPreferences?.getLong(StaticOrders.SHARED_PREFERENCES_ORDER_WAITING_TIME, 0L)!!

    private fun setUiInfo(){
        val dealPrice = MyPreferences.taximeterPreferences?.getInt(StaticOrders.SHARED_PREFERENCES_DEAL_PRICE, -1)
    }

    override fun onClick(v: View?) {
        when(v!!.id){}
    }

}
