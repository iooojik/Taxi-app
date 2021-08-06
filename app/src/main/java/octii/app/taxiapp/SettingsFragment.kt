package octii.app.taxiapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import octii.app.taxiapp.locale.LocaleUtils
import octii.app.taxiapp.models.SpeakingLanguagesModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logError

interface SettingsFragment {
    fun setLanguage(language : String, activity: Activity?) {
        LocaleUtils.setSelectedLanguageId(language)
        activity?.recreate()
    }

    fun changeSpeakingLanguage(lang: String, isChecked: Boolean){
        val languages = arrayListOf<SpeakingLanguagesModel>()
        if (isChecked){
            for (spLang in UserModel.mLanguages) languages.add(spLang)
            languages.add(SpeakingLanguagesModel(language = lang))
        } else {
            for (spLang in UserModel.mLanguages) {
                if (spLang.language != lang) languages.add(spLang)
            }
        }
        UserModel.mLanguages = languages.toList()

    }

    fun setLanguageSelector()
    fun getSettingsInformation()
    fun updateUiInfo()
    fun setListeners()
}