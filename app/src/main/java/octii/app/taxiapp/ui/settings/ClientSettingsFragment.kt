package octii.app.taxiapp.ui.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import octii.app.taxiapp.LocaleUtils
import octii.app.taxiapp.R
import octii.app.taxiapp.SettingsFragment
import octii.app.taxiapp.Static
import octii.app.taxiapp.databinding.FragmentClientSettingsBinding
import octii.app.taxiapp.models.driverAvailable.DriverAvailable
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.web.requests.Requests
import kotlin.concurrent.thread


class ClientSettingsFragment : Fragment(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener, SettingsFragment {

    private lateinit var binding : FragmentClientSettingsBinding
    private lateinit var requests : Requests

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

    private fun updateClient(){
        thread {
            val user = requests.userRequests.updateUser()
            if (user.type == Static.DRIVER_TYPE)
                findNavController().navigate(R.id.driverSettingsFragment)
        }
    }

    override fun setLanguageSelector() {
        when(LocaleUtils.getSelectedLanguageId()){
            LocaleUtils.RUSSIAN -> binding.languageSelectors.russianLanguage.isChecked = true
            LocaleUtils.ENGLISH -> binding.languageSelectors.englishLanguage.isChecked = true
            LocaleUtils.SERBIAN -> binding.languageSelectors.serbianLanguage.isChecked = true
        }

        if (UserModel.mLanguages.isNotEmpty()){
            for (speakingLang in UserModel.mLanguages){
                when(speakingLang.language){
                    LocaleUtils.RUSSIAN -> binding.languageSelectors.buttonRussianLanguage.isChecked = true
                    LocaleUtils.SERBIAN -> binding.languageSelectors.buttonSerbianLanguage.isChecked = true
                    LocaleUtils.ENGLISH -> binding.languageSelectors.buttonEnglishLanguage.isChecked = true
                }
            }
        }
    }

    override fun getSettingsInformation() {
        if (UserModel.uID > 0) updateUiInfo()
        /*
        thread {
            val driverModel = requests.driverAvailableRequests.getDriverAvailableModel()
            //проверяем, вернулась ли модель или заглушка, так как у заглушки id = -1
            requireActivity().runOnUiThread { if (driverModel.driverID > 0) updateUiInfo() }
        }
         */
    }

    override fun updateUiInfo() {
        if (!UserModel.mIsOnlyClient) binding.becomeDriver.visibility = View.VISIBLE

        binding.clientName.text = UserModel.nUserName
        binding.clientPhone.text = UserModel.uPhoneNumber

        binding.iAmInViber.isChecked = UserModel.uIsViber
        binding.iAmInWhatsapp.isChecked = UserModel.uIsWhatsapp

        if (UserModel.mAvatarURL.isNotEmpty()){
            Picasso.with(requireContext())
                .load(UserModel.mAvatarURL)
                .transform(CircularTransformation(0f))
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
        when(v!!.id){
            R.id.become_driver -> {
                if (UserModel.uIsViber && UserModel.uIsWhatsapp) {
                    UserModel.uType = Static.DRIVER_TYPE
                    updateClient()
                } else {
                    Snackbar.make(requireView(),
                        resources.getString(R.string.to_become_driver), Snackbar.LENGTH_SHORT).show()
                }
            }
            R.id.fab_back -> {
                findNavController().navigate(R.id.clientMapFragment)
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView!!.id){
            R.id.i_am_in_viber -> {
                UserModel.uIsViber = isChecked
                updateClient()
            }
            R.id.i_am_in_whatsapp -> {
                UserModel.uIsWhatsapp = isChecked
                updateClient()
            }
            R.id.working -> {
                DriverAvailable.mIsWorking = isChecked
                updateClient()
            }
            R.id.russian_language -> {
                if (isChecked)
                    setLanguage(LocaleUtils.RUSSIAN, requireActivity())
            }
            R.id.english_language -> {
                if (isChecked)
                    setLanguage(LocaleUtils.ENGLISH, requireActivity())
            }
            R.id.serbian_language -> {
                if (isChecked)
                    setLanguage(LocaleUtils.SERBIAN, requireActivity())
            }
            R.id.button_russian_language ->{
                changeSpeakingLanguage(LocaleUtils.RUSSIAN, isChecked)
                updateClient()
            }
            R.id.button_serbian_language ->{
                changeSpeakingLanguage(LocaleUtils.SERBIAN, isChecked)
                updateClient()
            }
            R.id.button_english_language ->{
                changeSpeakingLanguage(LocaleUtils.ENGLISH, isChecked)
                updateClient()
            }
        }
    }

}