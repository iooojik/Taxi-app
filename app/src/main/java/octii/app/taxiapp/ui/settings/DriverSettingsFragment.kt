package octii.app.taxiapp.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import octii.app.taxiapp.LocaleUtils
import octii.app.taxiapp.R
import octii.app.taxiapp.SettingsFragment
import octii.app.taxiapp.Static
import octii.app.taxiapp.databinding.FragmentDriverSettingsBinding
import octii.app.taxiapp.models.driverAvailable.DriverAvailable
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.web.requests.Requests
import kotlin.concurrent.thread


class DriverSettingsFragment : Fragment(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener, SettingsFragment {

    private lateinit var binding : FragmentDriverSettingsBinding
    private lateinit var requests: Requests

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDriverSettingsBinding.inflate(inflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requests = Requests(view = requireView(), activity = requireActivity())
        setLanguageSelector()
        setListeners()
        getSettingsInformation()
    }

    override fun setListeners(){
        if (binding.root == requireView()) {
            //buttons
            binding.becomeClient.setOnClickListener(this)
            binding.fabBack.setOnClickListener(this)
            binding.working.setOnCheckedChangeListener(this)
            binding.addPhotos.setOnClickListener(this)
            //radio buttons
            binding.languageSelectors.russianLanguage.setOnCheckedChangeListener(this)
            binding.languageSelectors.englishLanguage.setOnCheckedChangeListener(this)
            binding.languageSelectors.serbianLanguage.setOnCheckedChangeListener(this)
            //checkboxes
            binding.languageSelectors.buttonRussianLanguage.setOnCheckedChangeListener(this)
            binding.languageSelectors.buttonSerbianLanguage.setOnCheckedChangeListener(this)
            binding.languageSelectors.buttonEnglishLanguage.setOnCheckedChangeListener(this)
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

    override fun getSettingsInformation(){
        updateUiInfo()
    }

    override fun updateUiInfo() {
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
                .transform(RoundedCornersTransformation(40, 5))
                .resize(160, 160)
                .centerCrop()
                .into(binding.driverAvatar)
        } else {
            binding.driverAvatar.setImageResource(R.drawable.outline_account_circle_24)
        }
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
            R.id.add_photos -> {
                findNavController().navigate(R.id.editPhotoListFragment)
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView!!.id){
            R.id.working -> {
                UserModel.mDriver.isWorking = isChecked
                logError(isChecked)
                updateDriver()
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
                //updateDriver()
            }
            R.id.button_serbian_language ->{
                changeSpeakingLanguage(LocaleUtils.SERBIAN, isChecked)
                //updateDriver()
            }
            R.id.button_english_language ->{
                changeSpeakingLanguage(LocaleUtils.ENGLISH, isChecked)
                //updateDriver()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        logError(requestCode)
        when(requestCode){
            Static.PICK_IMAGE_AVATAR -> {
                logError("image picked")
            }
        }
    }

    private fun updateDriver(){

        val prices = listOf(
            binding.pricePerKm.editText,
            binding.pricePerMin.editText,
            binding.pricePerMin.editText,
            binding.maxDistance.editText
        )

        for (p in prices) if (p != null) if (p.text.isEmpty()) p.setText("0.0")


        UserModel.mDriver.pricePerKm = prices[0]?.text.toString().toFloat()
        UserModel.mDriver.pricePerMinute = prices[1]?.text.toString().toFloat()
        UserModel.mDriver.priceWaitingMin = prices[2]?.text.toString().toFloat()
        UserModel.mDriver.rideDistance = prices[3]?.text.toString().toFloat()

        /*
        thread {
            val user = requests.userRequests.update()
            requireActivity().runOnUiThread {
                if (user.type == Static.CLIENT_TYPE) findNavController().navigate(R.id.clientSettingsFragment)
            }
        }
         */
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updateDriver()
    }
}