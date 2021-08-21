package octii.app.taxiapp.ui.auth

import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.databinding.FragmentAuthorizationBinding
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.MyPreferences
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.scripts.showSnackbar
import octii.app.taxiapp.services.Services
import octii.app.taxiapp.services.location.MyLocationListener
import octii.app.taxiapp.web.requests.Requests


class AuthorizationFragment : Fragment(), AuthUtils {
	
	lateinit var binding: FragmentAuthorizationBinding
	private lateinit var requests: Requests
	
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		logInfo("onCreateView ${this.javaClass.name}")
		binding = FragmentAuthorizationBinding.inflate(layoutInflater)
		return binding.root
	}
	
	override fun onResume() {
		super.onResume()
		logInfo("onResume ${this.javaClass.name}")
		requests = Requests(requireView(), requireActivity())
		//получаем координаты пользователя, чтобы при авторизации координаты не были равны 0
		MyLocationListener.setUpLocationListener(requireContext())
		//слушатели на кнопки
		setListeners()
		//очищаем настройки
		MyPreferences.clearAll()
		setPhoneNumberListener()
		setNameLayoutListener()
	}
	
	private fun setPhoneNumberListener() {
		binding.phoneNumberLayout.editText?.doOnTextChanged { text, _, _, _ ->
			if (text != null) {
				if (text.trim().isEmpty() || (text.length > 15 || text.length < 5)) {
					binding.phoneNumberLayout.isErrorEnabled = true
					binding.phoneNumberLayout.error = resources.getString(R.string.no_phone)
				} else binding.phoneNumberLayout.isErrorEnabled = false
			}
		}
	}
	
	private fun setNameLayoutListener() {
		binding.nameLayout.editText?.doOnTextChanged { text, _, _, _ ->
			if (text != null) {
				if (text.trim().isEmpty()) {
					binding.nameLayout.isErrorEnabled = true
					binding.nameLayout.error = resources.getString(R.string.no_name)
				} else binding.nameLayout.isErrorEnabled = false
			}
		}
	}
	
	private fun setListeners() {
		binding.loginButton.setOnClickListener(this)
		binding.iAmDriver.setOnCheckedChangeListener(this)
		if (!isInstalled(Static.VIBER_PACKAGE_NAME,
				requireActivity().packageManager) && binding.iAmDriver.isChecked
		) {
			binding.iAmDriver.isChecked = false
			showSnackbar(requireContext(), resources.getString(R.string.viber_not_chosen))
		}
		binding.mainAuth.setOnClickListener(this)
		binding.fabBack.setOnClickListener(this)
		//фильтр для имени(можно вводить только буквы и нижнее подчёркивание)
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
				//проверка на установленный viber
				if (!isInstalled(Static.VIBER_PACKAGE_NAME, requireActivity().packageManager)) {
					showSnackbar(requireContext(), resources.getString(R.string.viber_not_chosen))
					findNavController().popBackStack()
				} else if (phoneNumber.isEmpty() || (phoneNumber.length > 15 || phoneNumber.length < 5)) {
					//проверка на наличие номера телефона
					binding.phoneNumberLayout.isErrorEnabled = true
					binding.phoneNumberLayout.error = resources.getString(R.string.no_phone)
					showSnackbar(requireContext(), resources.getString(R.string.no_phone))
				} else if (userName.isEmpty()) {
					//проверка на наличие имени
					binding.nameLayout.isErrorEnabled = true
					binding.nameLayout.error = resources.getString(R.string.no_name)
					showSnackbar(requireContext(), resources.getString(R.string.no_name))
				} else {
					//авторизация
					logInfo("user coordinates: lat:${MyLocationListener.latitude} lon: ${MyLocationListener.longitude}")
					hideKeyBoard(requireActivity(), requireView())
					requests.userRequests.login("+$phoneNumber",
						userName,
						LatLng(MyLocationListener.latitude, MyLocationListener.longitude),
						binding.progressBar) {
						Services(requireActivity(), Static.MAIN_SERVICES).start()
						if (UserModel.uType == Static.DRIVER_TYPE) findNavController().navigate(R.id.driverMapFragment)
						else findNavController().navigate(R.id.clientMapFragment)
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
			R.id.i_am_driver -> {
				if (!isInstalled(Static.VIBER_PACKAGE_NAME, requireActivity().packageManager)) {
					showSnackbar(requireContext(), resources.getString(R.string.to_become_driver))
					UserModel.uType = Static.CLIENT_TYPE
				} else UserModel.uType = Static.DRIVER_TYPE
			}
		}
	}
}