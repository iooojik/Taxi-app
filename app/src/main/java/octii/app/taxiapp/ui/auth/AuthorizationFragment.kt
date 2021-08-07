package octii.app.taxiapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.databinding.FragmentAuthorizationBinding
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.ui.FragmentHelper
import octii.app.taxiapp.web.requests.Requests


class AuthorizationFragment : Fragment(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener, FragmentHelper {

    lateinit var binding : FragmentAuthorizationBinding
    private lateinit var requests: Requests

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthorizationBinding.inflate(layoutInflater)
        setListeners()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requests = Requests(requireView(), requireActivity())
    }

    private fun setListeners(){
        binding.loginButton.setOnClickListener(this)
        binding.iAmInWhatsapp.setOnCheckedChangeListener(this)
        binding.iAmInViber.setOnCheckedChangeListener(this)
        binding.iAmDriver.setOnCheckedChangeListener(this)
        binding.mainAuth.setOnClickListener(this)
        binding.fabBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.login_button -> {
                val phoneNumber = binding.phoneNumberLayout.editText?.text.toString()
                val userName = binding.nameLayout.editText?.text.toString()

                if (!(binding.iAmInWhatsapp.isChecked || binding.iAmInViber.isChecked)) {
                    Snackbar.make(binding.root,
                        resources.getString(R.string.whatsapp_or_viber_not_chosen),
                        Snackbar.LENGTH_SHORT).show()
                } else if (phoneNumber.isEmpty()) {
                    Snackbar.make(binding.root,
                        resources.getString(R.string.no_phone), Snackbar.LENGTH_SHORT).show()
                } else if (userName.isEmpty() || (phoneNumber.length > 15 || phoneNumber.length < 5)) {
                    Snackbar.make(binding.root,
                        resources.getString(R.string.no_name),
                        Snackbar.LENGTH_SHORT).show()
                } else {
                    requests.userRequests.login("+$phoneNumber", userName, binding.progressBar){
                        if (UserModel.uType == Static.DRIVER_TYPE) findNavController().navigate(R.id.driverMapFragment)
                        else  findNavController().navigate(R.id.clientMapFragment)
                    }
                }
            }
            R.id.main_auth -> {
                if (activity != null)
                    hideKeyBoard(requireActivity(), binding.root)
                binding.phoneNumberLayout.clearFocus()
                binding.nameLayout.clearFocus()
            }
            R.id.fab_back -> findNavController().navigateUp()
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView!!.id){
            R.id.i_am_in_whatsapp -> {
                UserModel.uIsWhatsapp = isChecked
            }
            R.id.i_am_in_viber -> {
                UserModel.uIsViber = isChecked
            }
            R.id.i_am_driver -> {
                if (isChecked) UserModel.uType = Static.DRIVER_TYPE
                else UserModel.uType = Static.CLIENT_TYPE
            }
        }
    }
}