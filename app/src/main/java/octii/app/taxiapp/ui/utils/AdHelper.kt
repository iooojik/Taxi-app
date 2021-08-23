/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 16:42                       *
 ******************************************************************************/

package octii.app.taxiapp.ui.utils

import android.app.Activity
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class AdHelper(activity: Activity, banner: AdView) {
	init {
		MobileAds.initialize(activity) {}
		banner.visibility = View.VISIBLE
		banner.loadAd(AdRequest.Builder().build())
	}
}