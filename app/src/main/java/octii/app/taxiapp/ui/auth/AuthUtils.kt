package octii.app.taxiapp.ui.auth

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.CompoundButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import octii.app.taxiapp.R
import octii.app.taxiapp.locale.LocaleUtils
import octii.app.taxiapp.ui.utils.FragmentHelper

interface AuthUtils : FragmentHelper, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
	fun selectLang(activity: Activity, context: Context) {
		val resources = activity.resources
		//выбор языка
		val items = arrayOf(
			"${resources.getString(R.string.serbian_language_icon)} ${resources.getString(R.string.serbian_language)}",
			"${resources.getString(R.string.english_language_icon)} ${resources.getString(R.string.english_language)}",
			"${resources.getString(R.string.russian_language_icon)} ${resources.getString(R.string.russian_language)}",
		)
		
		MaterialAlertDialogBuilder(context)
			.setTitle(R.string.select_language)
			.setItems(items) { _, which ->
				when (which) {
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
}