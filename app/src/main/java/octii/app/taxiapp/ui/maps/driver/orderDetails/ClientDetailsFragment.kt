package octii.app.taxiapp.ui.maps.driver.orderDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import octii.app.taxiapp.R
import octii.app.taxiapp.databinding.FragmentClientDetailsBinding
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.ui.utils.CallHelper
import octii.app.taxiapp.ui.utils.FragmentHelper
import octii.app.taxiapp.ui.maps.OpenMessengerBottomSheet


class ClientDetailsFragment : Fragment(), FragmentHelper, View.OnClickListener, CallHelper {

    lateinit var binding: FragmentClientDetailsBinding
    private var isWhatsApp = false
    private var isViber = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentClientDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setInformation()
    }

    private fun setInformation() {
        binding.customerName.text =
            OrdersModel.mCustomer.userName
        binding.customerPhone.text =
            OrdersModel.mCustomer.phone
        binding.callToCustomer.setOnClickListener(this)
        setAvatar()
        setMessengersInfo()
    }

    private fun setAvatar() {
        if (OrdersModel.mCustomer.avatarURL.trim().isNotEmpty()) {
            Picasso.with(requireContext())
                .load(OrdersModel.mCustomer.avatarURL)
                .transform(RoundedCornersTransformation(40, 5))
                .resize(160, 160)
                .centerCrop()
                .into(binding.customerAvatar)
        } else {
            binding.customerAvatar.setImageResource(R.drawable.outline_account_circle_24)
        }
    }

    private fun setMessengersInfo() {
        if (OrdersModel.mCustomer.isViber) {
            isViber = true
        }
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.call_to_customer -> {
                copyToClipBoard(binding.customerPhone.text.toString(), requireContext())
                if (isWhatsApp && isViber) {
                    OpenMessengerBottomSheet(requireContext(), requireActivity(), OrdersModel.mCustomer.phone).show()
                } else if (isViber) goToApplication("com.viber.voip", requireActivity())
                //else if (isWhatsApp) goToApplication("com.whatsapp", requireActivity())
                //else if (isWhatsApp) callToWhatsApp(binding.customerPhone.text.toString(), requireContext(), requireActivity())
                else {
                    callToCustomer(binding.customerPhone.text.toString(), requireActivity())
                }
            }
        }
    }

}