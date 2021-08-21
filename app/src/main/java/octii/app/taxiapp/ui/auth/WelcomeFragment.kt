package octii.app.taxiapp.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.databinding.FragmentWelcomeBinding
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.scripts.showSnackbar
import octii.app.taxiapp.ui.Permissions

class WelcomeFragment : Fragment(), AuthUtils {
	
	private lateinit var binding: FragmentWelcomeBinding
	private lateinit var permissions: Permissions
	private var hasMessengers = false
	
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		logInfo("onCreateView ${this.javaClass.name}")
		binding = FragmentWelcomeBinding.inflate(layoutInflater)
		setListeners()
		return binding.root
	}
	
	override fun onResume() {
		super.onResume()
		logInfo("onResume ${this.javaClass.name}")
		permissions = Permissions(requireContext(), requireActivity())
		permissions.requestPermissions()
		//проверка на наличие мессенджеров
		checkMessengers()
	}
	
	private fun checkMessengers() {
		if (isInstalled(Static.VIBER_PACKAGE_NAME, requireActivity().packageManager)) {
			hasMessengers = true
			binding.checkViewViber.setImageResource(R.drawable.outline_check_circle_outline_24)
		} else {
			binding.checkViewViber.setImageResource(R.drawable.outline_cancel_24)
		}
		logInfo("viber is installed: ${
			isInstalled(Static.VIBER_PACKAGE_NAME,
				requireActivity().packageManager)
		}")
	}
	
	private fun setListeners() {
		//слушатели на кнопки
		binding.nextButton.setOnClickListener(this)
		binding.viber.setOnClickListener(this)
		binding.changeLangage.setOnClickListener(this)
	}
	
	override fun onClick(v: View?) {
		when (v!!.id) {
			R.id.next_button -> {
				if (permissions.permissionsGranted && isInstalled(Static.VIBER_PACKAGE_NAME,
						requireActivity().packageManager)
				)
					requireView().findNavController().navigate(R.id.authorizationFragment)
				else showSnackbar(requireContext(),
					resources.getString(R.string.not_installed_viber))
				
				if (!permissions.permissionsGranted)
					permissions.requestPermissions()
			}
			R.id.viber -> {
				logInfo("go to viber google page")
				val browserIntent = Intent(Intent.ACTION_VIEW,
					Uri.parse("https://play.google.com/store/apps/details?id=com.viber.voip"))
				startActivity(browserIntent)
			}
			R.id.change_langage -> selectLang(requireActivity(), requireContext())
		}
	}
	
	override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {}
	
}