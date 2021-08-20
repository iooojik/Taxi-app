package octii.app.taxiapp.ui.settings.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.databinding.FragmentClientSettingsBinding
import octii.app.taxiapp.locale.LocaleUtils
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.LogSender
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.scripts.showSnackbar
import octii.app.taxiapp.ui.utils.FragmentHelper
import octii.app.taxiapp.ui.settings.SettingsHelper
import octii.app.taxiapp.web.requests.Requests


class ClientSettingsFragment : Fragment(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener, SettingsHelper, FragmentHelper {

    private lateinit var binding: FragmentClientSettingsBinding
    private lateinit var requests: Requests

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentClientSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requests = Requests(view = requireView(), activity = requireActivity())
        setLanguageSelector()
        setListeners()
        getSettingsInformation()
    }

    private fun updateClient(runnable: Runnable? = null) {
        Requests(activity = requireActivity(), view = requireView()).userRequests.update {
            runnable?.run()
        }
    }

    override fun setLanguageSelector() {
        when (LocaleUtils.getSelectedLanguageId()) {
            LocaleUtils.RUSSIAN -> binding.languageSelectors.russianLanguage.isChecked = true
            LocaleUtils.ENGLISH -> binding.languageSelectors.englishLanguage.isChecked = true
            LocaleUtils.SERBIAN -> binding.languageSelectors.serbianLanguage.isChecked = true
        }

        if (UserModel.mLanguages.isNotEmpty()) {
            for (speakingLang in UserModel.mLanguages) {
                when (speakingLang.language) {
                    LocaleUtils.RUSSIAN -> binding.languageSelectors.buttonRussianLanguage.isChecked =
                        true
                    LocaleUtils.SERBIAN -> binding.languageSelectors.buttonSerbianLanguage.isChecked =
                        true
                    LocaleUtils.ENGLISH -> binding.languageSelectors.buttonEnglishLanguage.isChecked =
                        true
                }
            }
        }
    }

    override fun getSettingsInformation() {
        if (UserModel.uID > 0) updateUiInfo()
    }

    override fun updateUiInfo() {
        if (!UserModel.mIsOnlyClient) binding.becomeDriver.visibility = View.VISIBLE

        binding.clientName.text = UserModel.nUserName
        binding.clientPhone.text = UserModel.uPhoneNumber

        binding.iAmInViber.isChecked = UserModel.uIsViber
        logInfo("viber: ${UserModel.uIsViber}")
        binding.iAmInWhatsapp.isChecked = UserModel.uIsWhatsapp
        logInfo("Whatsapp: ${UserModel.uIsWhatsapp}")

        if (OrdersModel.isAccepted){
            binding.becomeDriver.isEnabled = false
            binding.becomeDriver.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorGrey))
            binding.becomeDriver.setOnClickListener {
                showSnackbar(requireContext(), resources.getString(R.string.you_cannot_change_type))
            }
        } else {
            binding.becomeDriver.isEnabled = true
            binding.becomeDriver.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.yellow))
            binding.becomeDriver.setOnClickListener(this)
        }


        if (UserModel.mAvatarURL.isNotEmpty()) {
            val roundedCornerTransformation = RoundedCornersTransformation(40, 5)
            Picasso.with(requireContext())
                .load(UserModel.mAvatarURL)
                .transform(roundedCornerTransformation)
                .resize(160, 160)
                .centerCrop()
                .into(binding.clientAvatar)
        } else {
            binding.clientAvatar.setImageResource(R.drawable.outline_account_circle_24)
        }
    }

    override fun setListeners() {
        if (binding.root == requireView()) {
            //buttons
            binding.becomeDriver.setOnClickListener(this)
            binding.fabBack.setOnClickListener(this)
            binding.sendLogs.setOnClickListener(this)
            //radio buttons
            binding.languageSelectors.russianLanguage.setOnCheckedChangeListener(this)
            binding.languageSelectors.englishLanguage.setOnCheckedChangeListener(this)
            binding.languageSelectors.serbianLanguage.setOnCheckedChangeListener(this)
            //checkboxes
            binding.languageSelectors.buttonRussianLanguage.setOnCheckedChangeListener(this)
            binding.languageSelectors.buttonSerbianLanguage.setOnCheckedChangeListener(this)
            binding.languageSelectors.buttonEnglishLanguage.setOnCheckedChangeListener(this)
            binding.iAmInViber.setOnCheckedChangeListener(this)
            binding.iAmInWhatsapp.setOnCheckedChangeListener(this)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.become_driver -> {
                if (UserModel.uIsViber && UserModel.uIsWhatsapp) {
                    UserModel.uType = Static.DRIVER_TYPE
                    updateClient{
                        findNavController().navigate(R.id.driverSettingsActivity)
                    }
                } else {
                    showSnackbar(requireContext(), resources.getString(R.string.to_become_driver))
                }
            }
            R.id.fab_back -> {
                findNavController().navigate(R.id.clientMapActivity)
            }
            R.id.send_logs -> {
                LogSender().sendLogs(requireActivity())
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView!!.id) {
            R.id.i_am_in_viber -> {
                //if (!isInstalled(Static.VIBER_PACKAGE_NAME, requireActivity().packageManager) && isChecked) {
                //     showSnackbar(requireContext(),
                //         resources.getString(R.string.not_installed_viber))
                //     binding.iAmInViber.isChecked = false
                //     UserModel.uIsViber = false
                //} else
                UserModel.uIsViber = isChecked
                logInfo("viber checked: $isChecked")
            }
            R.id.i_am_in_whatsapp -> {
                //if (!isInstalled(Static.WHATSAPP_PACKAGE_NAME, requireActivity().packageManager) && isChecked) {
                //    showSnackbar(requireContext(),
                //        resources.getString(R.string.not_installed_whatsapp))
                //    binding.iAmInWhatsapp.isChecked = false
                //}
                UserModel.uIsWhatsapp = isChecked
            }
            R.id.russian_language -> {
                if (isChecked)
                    setLanguage(LocaleUtils.RUSSIAN, activity)
            }
            R.id.english_language -> {
                if (isChecked)
                    setLanguage(LocaleUtils.ENGLISH, activity)
            }
            R.id.serbian_language -> {
                if (isChecked)
                    setLanguage(LocaleUtils.SERBIAN, activity)
            }
            R.id.button_russian_language -> {
                changeSpeakingLanguage(LocaleUtils.RUSSIAN, isChecked)
            }
            R.id.button_serbian_language -> {
                changeSpeakingLanguage(LocaleUtils.SERBIAN, isChecked)
            }
            R.id.button_english_language -> {
                changeSpeakingLanguage(LocaleUtils.ENGLISH, isChecked)
            }
        }
    }

    override fun onPause() {
        updateClient()
        super.onPause()
    }

}