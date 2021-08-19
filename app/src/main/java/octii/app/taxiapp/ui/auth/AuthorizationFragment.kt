package octii.app.taxiapp.ui.auth

import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.databinding.FragmentAuthorizationBinding
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.MyPreferences
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.services.Services
import octii.app.taxiapp.services.location.MyLocationListener
import octii.app.taxiapp.ui.FragmentHelper
import octii.app.taxiapp.web.requests.Requests


class AuthorizationFragment : Fragment(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener, FragmentHelper {

    lateinit var binding: FragmentAuthorizationBinding
    private lateinit var requests: Requests

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        logInfo("onCreateView AuthorizationFragment")
        binding = FragmentAuthorizationBinding.inflate(layoutInflater)
        setListeners()
        MyPreferences.clearAll()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requests = Requests(requireView(), requireActivity())
        MyLocationListener.setUpLocationListener(requireContext())
    }

    private fun setListeners() {
        binding.loginButton.setOnClickListener(this)
        binding.iAmInWhatsapp.setOnCheckedChangeListener(this)
        binding.iAmInViber.setOnCheckedChangeListener(this)
        binding.iAmDriver.setOnCheckedChangeListener(this)
        binding.mainAuth.setOnClickListener(this)
        binding.fabBack.setOnClickListener(this)
        val filter =
            InputFilter { source: CharSequence, _: Int, _: Int, _: Spanned?, _: Int, _: Int ->
                source.toString().trim { it <= ' ' }
                    .replace("[\\W\\d]|_".toRegex(), "")
            }
        binding.nameLayout.editText?.filters = arrayOf(filter)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.login_button -> {
                val phoneNumber = binding.phoneNumberLayout.editText?.text.toString()
                val userName = binding.nameLayout.editText?.text.toString()

                if (!binding.iAmInViber.isChecked) {
                    Snackbar.make(binding.root,
                        resources.getString(R.string.viber_not_chosen),
                        Snackbar.LENGTH_SHORT).show()
                } else if (phoneNumber.isEmpty()) {
                    Snackbar.make(binding.root,
                        resources.getString(R.string.no_phone), Snackbar.LENGTH_SHORT).show()
                } else if (userName.isEmpty() || (phoneNumber.length > 15 || phoneNumber.length < 5)) {
                    Snackbar.make(binding.root,
                        resources.getString(R.string.no_name),
                        Snackbar.LENGTH_SHORT).show()
                } else {
                    logInfo(LatLng(MyLocationListener.latitude, MyLocationListener.longitude))

                    requests.userRequests.login("+$phoneNumber",
                        userName,
                        LatLng(MyLocationListener.latitude, MyLocationListener.longitude),
                        binding.progressBar) {
                        Services(requireActivity(), Static.MAIN_SERVICES).start()
                        if (UserModel.uType == Static.DRIVER_TYPE) findNavController().navigate(R.id.driverMapActivity)
                        else findNavController().navigate(R.id.clientMapActivity)
                    }
                }
            }
            R.id.main_auth -> {
                if (activity != null) hideKeyBoard(requireActivity(), binding.root)
                binding.phoneNumberLayout.clearFocus()
                binding.nameLayout.clearFocus()
            }
            R.id.fab_back -> findNavController().navigateUp()
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView!!.id) {
            /*
            R.id.i_am_in_whatsapp -> {
                if (UserModel.uType == Static.DRIVER_TYPE && binding.iAmInViber.isChecked && !isChecked) {
                    Snackbar.make(requireView(),
                        resources.getString(R.string.to_become_driver),
                        Snackbar.LENGTH_LONG).show()
                    binding.iAmInWhatsapp.isChecked = true
                }
                //if (!isInstalled(Static.WHATSAPP_PACKAGE_NAME, requireActivity().packageManager)) {
                //    showSnackbar(requireContext(),
                //        resources.getString(R.string.not_installed_whatsapp))
                //     binding.iAmInWhatsapp.isChecked = false
                // }
                UserModel.uIsWhatsapp = isChecked

            }*/
            R.id.i_am_in_viber -> {
                /*
                if (UserModel.uType == Static.DRIVER_TYPE && binding.iAmInWhatsapp.isChecked && !isChecked) {
                    Snackbar.make(requireView(),
                        resources.getString(R.string.to_become_driver),
                        Snackbar.LENGTH_LONG).show()
                    binding.iAmInViber.isChecked = true
                }
                //if (!isInstalled(Static.VIBER_PACKAGE_NAME, requireActivity().packageManager)){
                //    showSnackbar(requireContext(), resources.getString(R.string.not_installed_viber))
                //    binding.iAmInViber.isChecked = false
                //}*/
                UserModel.uIsViber = isChecked
            }
            R.id.i_am_driver -> {
                if (isChecked) {
                    if (!binding.iAmInViber.isChecked) {
                        Snackbar.make(requireView(),
                            resources.getString(R.string.to_become_driver),
                            Snackbar.LENGTH_LONG).show()
                        binding.iAmDriver.isChecked = false
                    } else UserModel.uType = Static.DRIVER_TYPE
                } else UserModel.uType = Static.CLIENT_TYPE
            }
        }
    }
}