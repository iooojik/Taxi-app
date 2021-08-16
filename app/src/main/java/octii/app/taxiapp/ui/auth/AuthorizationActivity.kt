package octii.app.taxiapp.ui.auth

import octii.app.taxiapp.BaseActivity
import octii.app.taxiapp.R

class AuthorizationActivity : BaseActivity() {

    override fun getFragment(id: Int?): Int {
        //return getStartLocationId()
        return R.id.welcomeFragment
    }

}