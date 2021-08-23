/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 16:56                       *
 ******************************************************************************/

package octii.app.taxiapp.ui.settings

import android.content.Context
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import octii.app.taxiapp.databinding.LanguageSelectorsBinding
import octii.app.taxiapp.locale.LocaleUtils
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.ui.utils.FragmentHelper

interface SettingsHelper : View.OnClickListener, CompoundButton.OnCheckedChangeListener,
	FragmentHelper {
	fun setLanguageSelector(languageSelectorsBinding: LanguageSelectorsBinding) {
		when (LocaleUtils.getSelectedLanguageId()) {
			LocaleUtils.RUSSIAN -> languageSelectorsBinding.russianLanguage.isChecked = true
			LocaleUtils.ENGLISH -> languageSelectorsBinding.englishLanguage.isChecked = true
			LocaleUtils.SERBIAN -> languageSelectorsBinding.serbianLanguage.isChecked = true
		}
		
		if (UserModel.mLanguages.isNotEmpty()) {
			for (speakingLang in UserModel.mLanguages) {
				when (speakingLang.language) {
					LocaleUtils.RUSSIAN -> languageSelectorsBinding.buttonRussianLanguage.isChecked =
						true
					LocaleUtils.SERBIAN -> languageSelectorsBinding.buttonSerbianLanguage.isChecked =
						true
					LocaleUtils.ENGLISH -> languageSelectorsBinding.buttonEnglishLanguage.isChecked =
						true
				}
			}
		}
	}
	
	fun setAvatar(url: String, context: Context, imagePlaceHolder: ImageView) {
		val roundedCornerTransformation = RoundedCornersTransformation(40, 5)
		Picasso.with(context)
			.load(UserModel.mAvatarURL)
			.transform(roundedCornerTransformation)
			.resize(160, 160)
			.centerCrop()
			.into(imagePlaceHolder)
	}
	
	fun getSettingsInformation()
	fun updateUiInfo()
	fun setListeners()
}