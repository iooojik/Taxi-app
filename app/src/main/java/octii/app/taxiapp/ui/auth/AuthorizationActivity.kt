package octii.app.taxiapp.ui.auth

import android.os.Bundle
import octii.app.taxiapp.BaseActivity
import octii.app.taxiapp.R

class AuthorizationActivity : BaseActivity() {

    override fun getFragment(id: Int?): Int {
        //return getStartLocationId()
        return R.id.welcomeFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adHelper.hideBanner()
    }
}