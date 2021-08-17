package octii.app.taxiapp.ui.maps.client.orderDetails

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import octii.app.taxiapp.R
import octii.app.taxiapp.databinding.FragmentDriverDetailsBinding
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.ui.FragmentHelper
import octii.app.taxiapp.ui.maps.OpenMessengerBottomSheet


class DriverDetailsFragment : Fragment(), FragmentHelper, View.OnClickListener {

    lateinit var binding: FragmentDriverDetailsBinding
    private var isWhatsApp = false
    private var isViber = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDriverDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setInformation()
    }

    private fun setInformation() {
        binding.driverName.text = OrdersModel.mDriver.userName
        binding.driverPhone.text = OrdersModel.mDriver.phone
        binding.callToDriver.setOnClickListener(this)
        binding.showPhotos.setOnClickListener(this)
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
                .into(binding.driverAvatar)
        } else {
            binding.driverAvatar.setImageResource(R.drawable.outline_account_circle_24)
        }
    }

    private fun setMessengersInfo() {
        if (OrdersModel.mCustomer.isViber && OrdersModel.mCustomer.isWhatsapp) {
            binding.messengersInfo.text =
                requireActivity().resources.getString(R.string.user_available_in_viber_and_whatsapp)
            isViber = true
            isWhatsApp = true
        } else if (OrdersModel.mCustomer.isWhatsapp) {
            binding.messengersInfo.text =
                requireActivity().resources.getString(R.string.user_available_in_whatsapp)
            isWhatsApp = true
        } else if (OrdersModel.mCustomer.isViber) {
            binding.messengersInfo.text =
                requireActivity().resources.getString(R.string.user_available_in_viber)
            isViber = true
        }
    }

    private fun copyToClipBoard(text: String) {
        val clipboard: ClipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("", text)
        clipboard.setPrimaryClip(clip)
        Snackbar.make(binding.root, resources.getString(R.string.copied), Snackbar.LENGTH_SHORT)
            .show()
    }

    private fun callToCustomer(phone: String) {
        if (OrdersModel.mCustomer.phone.isNotEmpty()) {
            val dial = "tel:$phone"
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(dial)))
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.call_to_customer -> {
                copyToClipBoard(binding.driverPhone.text.toString())
                if (isWhatsApp && isViber) {
                    OpenMessengerBottomSheet(requireContext(), requireActivity()).show()
                } else if (isViber) goToApplication("com.viber.voip", requireActivity())
                else if (isWhatsApp) goToApplication("com.whatsapp", requireActivity())
                else {
                    callToCustomer(binding.driverPhone.text.toString())
                }
            }
            R.id.show_photos -> {
                BottomSheetShowPhotos(requireContext(),
                    requireActivity(),
                    OrdersModel.mDriver.files).show()
            }
        }
    }

}