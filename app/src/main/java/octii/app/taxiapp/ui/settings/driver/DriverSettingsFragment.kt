package octii.app.taxiapp.ui.settings.driver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.databinding.FragmentDriverSettingsBinding
import octii.app.taxiapp.locale.LocaleUtils
import octii.app.taxiapp.models.driver.DriverModel
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.sendLogs
import octii.app.taxiapp.scripts.showSnackbar
import octii.app.taxiapp.ui.settings.SettingsHelper
import octii.app.taxiapp.ui.utils.FragmentHelper
import octii.app.taxiapp.web.requests.Requests


class DriverSettingsFragment : Fragment(), View.OnClickListener,
	CompoundButton.OnCheckedChangeListener, SettingsHelper, FragmentHelper {
	
	private lateinit var binding: FragmentDriverSettingsBinding
	private lateinit var requests: Requests
	
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentDriverSettingsBinding.inflate(inflater)
		//блокируем кнопку "назад", так как при смене типа аккаунта,
		// пользователь сможет вернуться в предыдущий без проверки на установленный Viber
		blockGoBack(requireActivity(), this)
		return binding.root
	}
	
	override fun onResume() {
		super.onResume()
		requests = Requests(view = requireView(), activity = requireActivity())
		//выбор языка
		setLanguageSelector(binding.languageSelectors)
		//слушатели на кнопки
		setListeners()
		//обновление информации
		getSettingsInformation()
	}
	
	override fun setListeners() {
		if (binding.root == requireView()) {
			//buttons
			binding.becomeClient.setOnClickListener(this)
			binding.fabBack.setOnClickListener(this)
			binding.working.setOnCheckedChangeListener(this)
			binding.addPhotos.setOnClickListener(this)
			binding.sendLogs.setOnClickListener(this)
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
	
	override fun getSettingsInformation() {
		updateUiInfo()
	}
	
	override fun updateUiInfo() {
		//цена за км
		binding.pricePerKm.editText?.setText(DriverModel.mPrices.pricePerKm.toString())
		//цена за минуту
		binding.pricePerMin.editText?.setText(DriverModel.mPrices.pricePerMinute.toString())
		//цена ожидания
		binding.priceWaiting.editText?.setText(DriverModel.mPrices.priceWaitingMin.toString())
		//максимальная дистанция поиска клиента
		binding.maxDistance.editText?.setText(DriverModel.mRideDistance.toString())
		//статус: работаю или нет
		binding.working.isChecked = DriverModel.mIsWorking
		//имя водителя
		binding.driverName.text = UserModel.nUserName
		//телефон водителя
		binding.driverPhone.text = UserModel.uPhoneNumber
		
		//убираем или показываем кнопку для изменения типа аккаунта
		if (OrdersModel.mIsAccepted) {
			binding.becomeClient.isEnabled = false
			binding.priceWaiting.isEnabled = false
			binding.pricePerMin.isEnabled = false
			binding.pricePerKm.isEnabled = false
			binding.maxDistance.isEnabled = false
			binding.addPhotos.isEnabled = false
			binding.becomeClient.setBackgroundColor(ContextCompat.getColor(requireContext(),
				R.color.colorGrey))
			binding.becomeClient.setOnClickListener {
				showSnackbar(requireContext(), resources.getString(R.string.you_cannot_change_type))
			}
		} else {
			binding.becomeClient.isEnabled = true
			binding.becomeClient.setBackgroundColor(ContextCompat.getColor(requireContext(),
				R.color.yellow))
			binding.becomeClient.setOnClickListener(this)
		}
		//loading avatar image
		if (UserModel.mAvatarURL.isNotEmpty()) {
			setAvatar(UserModel.mAvatarURL, requireContext(), binding.driverAvatar)
		} else {
			binding.driverAvatar.setImageResource(R.drawable.outline_account_circle_24)
		}
	}
	
	override fun onClick(v: View?) {
		when (v!!.id) {
			R.id.become_client -> {
				UserModel.uType = Static.CLIENT_TYPE
				updateDriver {
					findNavController().navigate(R.id.clientSettingsFragment)
				}
			}
			R.id.fab_back -> {
				findNavController().navigate(R.id.driverMapFragment)
			}
			R.id.add_photos -> {
				findNavController().navigate(R.id.editPhotoListFragment)
			}
			R.id.send_logs -> {
				sendLogs(requireContext())
			}
		}
	}
	
	override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
		when (buttonView!!.id) {
			R.id.working -> {
				//проверка на установленный вибер
				if (!isInstalled(Static.VIBER_PACKAGE_NAME, requireActivity().packageManager)) {
					showSnackbar(requireContext(),
						resources.getString(R.string.not_installed_viber))
					buttonView.isChecked = false
					UserModel.mDriver.isWorking = false
					return
				}
				//проверка на загрузку всех фотографий
				if (UserModel.mFiles.size != Static.PHOTO_TYPES.size) {
					showSnackbar(requireContext(), resources.getString(R.string.check_photos))
					buttonView.isChecked = false
					UserModel.mDriver.isWorking = false
					return
				}
				UserModel.mDriver.isWorking = isChecked
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
	
	private fun updateDriver(runnable: Runnable? = null) {
		val prices = listOf(
			binding.pricePerKm.editText,
			binding.pricePerMin.editText,
			binding.priceWaiting.editText,
			binding.maxDistance.editText
		)
		
		for (p in prices) if (p != null) if (p.text.isEmpty()) requireActivity().runOnUiThread {
			p.setText("0.0")
		}
		
		
		UserModel.mDriver.prices.pricePerKm = prices[0]?.text.toString().toFloat()
		UserModel.mDriver.prices.pricePerMinute = prices[1]?.text.toString().toFloat()
		UserModel.mDriver.prices.priceWaitingMin = prices[2]?.text.toString().toFloat()
		UserModel.mDriver.rideDistance = prices[3]?.text.toString().toFloat()
		
		Requests(activity = requireActivity(), view = requireView()).userRequests.update {
			runnable?.run()
		}
	}
	
	override fun onPause() {
		updateDriver()
		super.onPause()
	}
	
}