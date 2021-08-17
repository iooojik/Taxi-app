package octii.app.taxiapp.ui

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class AdHelper(private val context : Context, private val activity : Activity, val banner : AdView) {
    init {
        MobileAds.initialize(activity){}
        banner.visibility = View.VISIBLE
        banner.loadAd(AdRequest.Builder().build())
    }

    fun hideBanner(){
        banner.visibility = View.GONE
    }

    fun showBanner(){
        banner.visibility = View.VISIBLE
    }
}