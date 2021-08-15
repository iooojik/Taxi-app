package octii.app.taxiapp.ui.maps.driver.orderDetails

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import octii.app.taxiapp.constants.StaticOrders
import octii.app.taxiapp.databinding.FragmentDriverOrderDetailsBinding
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.ui.FragmentHelper


class OrderDetails : Fragment(), FragmentHelper, View.OnClickListener {

    private lateinit var binding : FragmentDriverOrderDetailsBinding
    private var orderStatusReciever = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null){
                when(intent.getStringExtra(StaticOrders.ORDER_STATUS)){
                    StaticOrders.ORDER_STATUS_ACCEPTED -> {
                        binding.pages.currentItem = 2
                        binding.pages.isEnabled = false
                    }
                    StaticOrders.ORDER_STATUS_FINISHED -> {
                        binding.pages.currentItem = 1
                        binding.pages.isEnabled = false
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDriverOrderDetailsBinding.inflate(layoutInflater)
        binding.pages.adapter = FragmentsAdapter(requireActivity().supportFragmentManager, lifecycle)
        TabLayoutMediator(binding.indicator, binding.pages){ _, _ ->}.attach()
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        requireActivity().registerReceiver(orderStatusReciever, IntentFilter(StaticOrders.ORDER_STATUS_INTENT_FILTER))
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            requireActivity().unregisterReceiver(orderStatusReciever)
        } catch (e : Exception){
            e.printStackTrace()
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id){

        }
    }

    inner class FragmentsAdapter internal constructor(fm: FragmentManager, lifecycle: Lifecycle)
        : FragmentStateAdapter(fm, lifecycle) {

        private val fragments = listOf<Fragment>(ClientDetailsFragment(), TaximeterDetailsFragment(), StartStopWaitFragment())

        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun getItemViewType(position: Int): Int {
            var pos = position
            if (position == 2 && !OrdersModel.isAccepted){
                pos-=1
            }
            return super.getItemViewType(pos)
        }

        override fun createFragment(position: Int): Fragment {
            return when(position){
                0 -> fragments[position]
                1 -> fragments[position]
                2 -> fragments[position]
                else -> fragments[position]
            }
        }
    }

}