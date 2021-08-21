package octii.app.taxiapp.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import octii.app.taxiapp.R
import octii.app.taxiapp.databinding.FragmentSplashBinding
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.ui.Permissions
import octii.app.taxiapp.ui.utils.SharedPrefsUtil
import octii.app.taxiapp.web.requests.Requests
import octii.app.taxiapp.web.requests.RequestsResult


class SplashFragment : Fragment(), SharedPrefsUtil {
	
	private lateinit var splashBinding: FragmentSplashBinding
	
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		splashBinding = FragmentSplashBinding.inflate(layoutInflater)
		return splashBinding.root
	}
	
	override fun onResume() {
		super.onResume()
		checkAuth()
	}
	
	private fun checkAuth() {
		logInfo("checking authorization")
		val token = getToken()
		logInfo("token : ${getToken()}")
		if (token != null && Permissions(requireContext(),
				requireActivity()).checkPermissions() && getUserUUID().trim().isNotEmpty()
		) {
			logInfo("permissions granted")
			if (token.isNotEmpty()) {
				logInfo("token is not empty")
				Requests(activity = requireActivity()).userRequests.loginWithToken(token,
					RequestsResult(false, requireActivity(), getSavedUserType(), getToken()))
			}
		} else {
			requireActivity().findNavController(R.id.nav_host_fragment)
				.navigate(R.id.welcomeFragment)
		}
	}
}