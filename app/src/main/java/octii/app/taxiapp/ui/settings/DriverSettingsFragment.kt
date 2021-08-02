package octii.app.taxiapp.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import octii.app.taxiapp.LocaleUtils
import octii.app.taxiapp.R
import octii.app.taxiapp.Static
import octii.app.taxiapp.databinding.FragmentDriverSettingsBinding
import octii.app.taxiapp.models.SpeakingLanguagesModel
import octii.app.taxiapp.models.driverAvailable.DriverAvailable
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.web.requests.Requests
import kotlin.concurrent.thread


class DriverSettingsFragment : Fragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private lateinit var binding : FragmentDriverSettingsBinding
    private lateinit var requests: Requests

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDriverSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requests = Requests(view = requireView(), activity = requireActivity())
        setLanguageSelector()
        setListeners()
        getSettingsInformation()
    }

    private fun setListeners(){

        if (binding.root == requireView()) {
            //buttons
            binding.becomeClient.setOnClickListener(this)
            binding.fabBack.setOnClickListener(this)
            binding.working.setOnCheckedChangeListener(this)
            //radio buttons
            binding.russianLanguage.setOnCheckedChangeListener(this)
            binding.englishLanguage.setOnCheckedChangeListener(this)
            binding.serbianLanguage.setOnCheckedChangeListener(this)
            //checkboxes
            binding.buttonRussianLanguage.setOnCheckedChangeListener(this)
            binding.buttonSerbianLanguage.setOnCheckedChangeListener(this)
            binding.buttonEnglishLanguage.setOnCheckedChangeListener(this)
        }
    }

    private fun setLanguageSelector() {
        when(LocaleUtils.getSelectedLanguageId()){
            LocaleUtils.RUSSIAN -> binding.russianLanguage.isChecked = true
            LocaleUtils.ENGLISH -> binding.englishLanguage.isChecked = true
            LocaleUtils.SERBIAN -> binding.serbianLanguage.isChecked = true
        }

        if (UserModel.mLanguages.isNotEmpty()){
            for (speakingLang in UserModel.mLanguages){
                when(speakingLang.language){
                    LocaleUtils.RUSSIAN -> binding.buttonRussianLanguage.isChecked = true
                    LocaleUtils.SERBIAN -> binding.buttonSerbianLanguage.isChecked = true
                    LocaleUtils.ENGLISH -> binding.buttonEnglishLanguage.isChecked = true
                }
            }
        }
    }

    private fun getSettingsInformation(){
        thread {
            val driverModel = requests.driverAvailableRequests.getDriverAvailableModel()
            //проверяем, вернулась ли модель или заглушка, так как у заглушки id = -1
            requireActivity().runOnUiThread { if (driverModel.driverID > 0) updateUiInfo() }
        }
    }

    private fun setLanguage(language : String){
        LocaleUtils.setSelectedLanguageId(language)
        val i: Intent? = requireActivity().packageManager.getLaunchIntentForPackage(requireActivity().packageName)
        startActivity(i)
    }

    private fun updateDriver(){
        thread {
            val user = requests.userRequests.updateUser()
            requireActivity().runOnUiThread {
                if (user.type == Static.CLIENT_TYPE) findNavController().navigate(R.id.clientSettingsFragment)
            }
        }

        val prices = listOf(
            binding.pricePerKm.editText,
            binding.pricePerMin.editText,
            binding.pricePerMin.editText,
            binding.maxDistance.editText
        )

        for (p in prices) if (p != null) if (p.text.isEmpty()) p.setText("0.0")

        val driverAvailable = DriverAvailable(
            pricePerKm = prices[0]?.text.toString().toFloat(),
            pricePerMinute = prices[1]?.text.toString().toFloat(),
            priceWaitingMin = prices[2]?.text.toString().toFloat(),
            rideDistance = prices[3]?.text.toString().toFloat(),
        )

        thread {
            requests.driverAvailableRequests.updateDriverAvailableModel(driverAvailable)
        }

    }

    private fun updateUiInfo() {
        binding.pricePerKm.editText?.setText(DriverAvailable.mPricePerKm.toString())
        binding.pricePerMin.editText?.setText(DriverAvailable.mPricePerMinute.toString())
        binding.priceWaiting.editText?.setText(DriverAvailable.mPriceWaitingMin.toString())
        binding.maxDistance.editText?.setText(DriverAvailable.mRideDistance.toString())
        binding.working.isChecked = DriverAvailable.mIsWorking
        binding.driverName.text = UserModel.nUserName
        binding.driverPhone.text = UserModel.uPhoneNumber

        //loading avatar image
        if (UserModel.mAvatarURL.isNotEmpty()){
            Picasso.with(requireContext())
                .load(UserModel.mAvatarURL)
                .transform(CircularTransformation(0f))
                .into(binding.driverAvatar)
        } else {
            binding.driverAvatar.setImageResource(R.drawable.outline_account_circle_24)
        }
    }

    private fun changeSpeakingLanguage(lang: String, isChecked: Boolean){
        val languages = arrayListOf<SpeakingLanguagesModel>()
        if (isChecked){
            for (spLang in UserModel.mLanguages) languages.add(spLang)
            languages.add(SpeakingLanguagesModel(language = lang, userId = UserModel.uID))
        } else {
            for (spLang in UserModel.mLanguages) {
                if (spLang.language != lang) languages.add(spLang)
            }
        }
        logError(languages)
        logError(UserModel.mLanguages)
        logError(lang)
        UserModel.mLanguages = languages.toList()
        updateDriver()
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.become_client -> {
                UserModel.uType = Static.CLIENT_TYPE
                updateDriver()
            }
            R.id.fab_back -> {
                updateDriver()
                findNavController().navigate(R.id.driverMapFragment)
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView!!.id){
            R.id.working -> {
                DriverAvailable.mIsWorking = isChecked
                updateDriver()
            }
            R.id.russian_language -> {
                if (isChecked)
                    setLanguage(LocaleUtils.RUSSIAN)
            }
            R.id.english_language -> {
                if (isChecked)
                    setLanguage(LocaleUtils.ENGLISH)
            }
            R.id.serbian_language -> {
                if (isChecked)
                    setLanguage(LocaleUtils.SERBIAN)
            }
            R.id.button_russian_language ->{
                changeSpeakingLanguage(LocaleUtils.RUSSIAN, isChecked)
            }
            R.id.button_serbian_language ->{
                changeSpeakingLanguage(LocaleUtils.SERBIAN, isChecked)

            }
            R.id.button_english_language ->{
                changeSpeakingLanguage(LocaleUtils.ENGLISH, isChecked)
            }
        }
    }
}