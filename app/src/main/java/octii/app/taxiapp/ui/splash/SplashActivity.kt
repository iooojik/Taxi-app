package octii.app.taxiapp.ui.splash

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import octii.app.taxiapp.BaseActivity
import octii.app.taxiapp.R
import octii.app.taxiapp.scripts.logError

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<ConstraintLayout>(R.id.splash_layout).visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        //val adRequest = AdRequest.Builder().build()
        //findViewById<AdView>(R.id.adView).loadAd(adRequest)
        logError("on create SplashActivity")
        checkAuth(this)
    }

    override fun getFragment(id: Int?): Int? {
        return null
    }
}