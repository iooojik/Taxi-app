package octii.app.taxiapp.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import octii.app.taxiapp.R
import octii.app.taxiapp.databinding.FragmentWelcomeBinding
import octii.app.taxiapp.locale.LocaleUtils
import octii.app.taxiapp.ui.FragmentHelper
import octii.app.taxiapp.ui.Permissions
import android.content.pm.PackageInfo

import android.content.pm.PackageManager
import android.content.res.Resources
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.scripts.showSnackbar
import java.lang.Exception
import kotlin.Throws

class WelcomeFragment : Fragment(), View.OnClickListener, FragmentHelper {

    private lateinit var binding : FragmentWelcomeBinding
    private lateinit var permissions: Permissions
    private var hasMessengers = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentWelcomeBinding.inflate(layoutInflater)
        setListeners()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        permissions = Permissions(requireContext(), requireActivity())
        permissions.requestPermissions()
        checkMessengers()
    }

    private fun checkMessengers(){
        if (isInstalled(Static.WHATSAPP_PACKAGE_NAME, requireActivity().packageManager)){
            hasMessengers = true
            binding.checkViewWhatsapp.setImageResource(R.drawable.outline_check_circle_outline_24)
        }
        else{
            binding.checkViewWhatsapp.setImageResource(R.drawable.outline_cancel_24)
        }
        if (isInstalled(Static.VIBER_PACKAGE_NAME, requireActivity().packageManager)) {
            hasMessengers = true
            binding.checkViewViber.setImageResource(R.drawable.outline_check_circle_outline_24)
        }
        else{
            binding.checkViewViber.setImageResource(R.drawable.outline_cancel_24)
        }
    }

    private fun setListeners(){
        binding.nextButton.setOnClickListener(this)
        binding.whatsapp.setOnClickListener(this)
        binding.viber.setOnClickListener(this)
        binding.changeLangage.setOnClickListener(this)
    }

    private fun selectLang(){
        val items = arrayOf(
            "${resources.getString(R.string.serbian_language_icon)} ${resources.getString(R.string.serbian_language)}",
            "${resources.getString(R.string.english_language_icon)} ${resources.getString(R.string.english_language)}",
            "${resources.getString(R.string.russian_language_icon)} ${resources.getString(R.string.russian_language)}",
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_language)
            .setItems(items) { _, which ->
                when(which){
                    0 -> {
                        setLanguage(LocaleUtils.SERBIAN, activity)
                    }
                    1 -> {
                        setLanguage(LocaleUtils.ENGLISH, activity)
                    }
                    2 -> {
                        setLanguage(LocaleUtils.RUSSIAN, activity)
                    }
                }
            }
            .show()
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.next_button -> {
                if (permissions.permissionsGranted && hasMessengers)
                    requireView().findNavController().navigate(R.id.authorizationFragment)
                if (!permissions.permissionsGranted)
                    permissions.requestPermissions()
                if (!hasMessengers)
                    showSnackbar(requireContext(), resources.getString(R.string.whatsapp_or_viber_not_chosen))
            }
            R.id.whatsapp -> {
                val browserIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp"))
                startActivity(browserIntent)
            }
            R.id.viber -> {
                val browserIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.viber.voip"))
                startActivity(browserIntent)
            }
            R.id.change_langage -> selectLang()
        }
    }

}