package octii.app.taxiapp.ui.settings.client

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
import octii.app.taxiapp.databinding.FragmentClientSettingsBinding
import octii.app.taxiapp.locale.LocaleUtils
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.sendLogs
import octii.app.taxiapp.scripts.showSnackbar
import octii.app.taxiapp.ui.settings.SettingsHelper
import octii.app.taxiapp.web.requests.Requests


class ClientSettingsFragment : Fragment(), SettingsHelper {
	
	private lateinit var binding: FragmentClientSettingsBinding
	private lateinit var requests: Requests
	
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentClientSettingsBinding.inflate(layoutInflater)
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
	
	private fun updateClient(runnable: Runnable? = null) {
		//обновлени клиента
		Requests(activity = requireActivity(), view = requireView()).userRequests.update {
			runnable?.run()
		}
	}
	
	override fun getSettingsInformation() {
		if (UserModel.uID > 0) updateUiInfo()
	}
	
	override fun updateUiInfo() {
		if (!UserModel.mIsOnlyClient) binding.becomeDriver.visibility = View.VISIBLE
		
		binding.clientName.text = UserModel.nUserName
		binding.clientPhone.text = UserModel.uPhoneNumber
		//убираем или показываем кнопку для изменения типа аккаунта
		if (OrdersModel.mIsAccepted) {
			binding.becomeDriver.isEnabled = false
			binding.becomeDriver.setBackgroundColor(ContextCompat.getColor(requireContext(),
				R.color.colorGrey))
			binding.becomeDriver.setOnClickListener {
				showSnackbar(requireContext(), resources.getString(R.string.you_cannot_change_type))
			}
		} else {
			binding.becomeDriver.isEnabled = true
			binding.becomeDriver.setBackgroundColor(ContextCompat.getColor(requireContext(),
				R.color.yellow))
			binding.becomeDriver.setOnClickListener(this)
		}
		
		if (UserModel.mAvatarURL.isNotEmpty()) {
			setAvatar(UserModel.mAvatarURL, requireContext(), binding.clientAvatar)
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
		}
	}
	
	override fun onClick(v: View?) {
		when (v!!.id) {
			R.id.become_driver -> {
				if (UserModel.uIsViber && isInstalled(Static.VIBER_PACKAGE_NAME,
						requireActivity().packageManager)
				) {
					UserModel.uType = Static.DRIVER_TYPE
					updateClient {
						findNavController().navigate(R.id.driverSettingsFragment)
					}
				} else {
					showSnackbar(requireContext(), resources.getString(R.string.to_become_driver))
				}
			}
			R.id.fab_back -> {
				findNavController().navigate(R.id.clientMapFragment)
			}
			R.id.send_logs -> {
				sendLogs(requireContext())
			}
		}
	}
	
	override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
		when (buttonView!!.id) {
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