/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 2:09                        *
 ******************************************************************************/

package octii.app.taxiapp.ui.utils

import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.scripts.MyPreferences

interface SharedPrefsUtil {
	fun getToken(): String? {
		return if (MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TOKEN,
				"").isNullOrEmpty()
		) ""
		else MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TOKEN, "")
	}
	
	fun getUserUUID(): String =
		MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_UUID, "")!!
	
	fun getSavedUserType(): String {
		return if (MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TYPE, "")
				.isNullOrEmpty()
		) ""
		else MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TYPE, "")!!
	}
}