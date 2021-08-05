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
import octii.app.taxiapp.*
import octii.app.taxiapp.databinding.FragmentWelcomeBinding
import octii.app.taxiapp.locale.LocaleUtils
import octii.app.taxiapp.ui.Permissions


class WelcomeFragment : Fragment(), View.OnClickListener, SettingsFragment, FragmentHelper {

    private lateinit var binding : FragmentWelcomeBinding
    private lateinit var permissions: Permissions

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeBinding.inflate(layoutInflater)
        MyPreferences.clearAll()
        setListeners()
        blockGoBack(requireActivity(), this)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        permissions = Permissions(requireContext(), requireActivity())
        permissions.requestPermissions()
    }

    override fun setLanguageSelector() {}

    override fun getSettingsInformation() {}

    override fun updateUiInfo() {}

    override fun setListeners(){
        binding.nextButton.setOnClickListener(this)
        binding.whatsapp.setOnClickListener(this)
        binding.viber.setOnClickListener(this)
        binding.changeLangage.setOnClickListener(this)
    }

    private fun selectLang(){
        val items = arrayOf(
            "${resources.getString(R.string.serbian_language_icon)} ${resources.getString(R.string.serbian_language)}",
            "${resources.getString(R.string.english_language_icon)} ${resources.getString(R.string.english_language)}",
            "${resources.getString(R.string.russian_language_icon)} ${resources.getString(R.string.russian_language)}",)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_language)
            .setItems(items) { _, which ->
                when(which){
                    0 -> {
                        setLanguage(LocaleUtils.SERBIAN, requireActivity())
                    }
                    1 -> {
                        setLanguage(LocaleUtils.ENGLISH, requireActivity())
                    }
                    2 -> {
                        setLanguage(LocaleUtils.RUSSIAN, requireActivity())
                    }
                }
            }
            .show()
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.next_button -> {
                if (permissions.permissionsGranted)
                    requireView().findNavController().navigate(R.id.authorizationFragment)
                else permissions.requestPermissions()
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