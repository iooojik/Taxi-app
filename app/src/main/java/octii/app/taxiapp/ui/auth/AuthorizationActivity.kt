package octii.app.taxiapp.ui.auth

import octii.app.taxiapp.BaseActivity

class AuthorizationActivity : BaseActivity() {

    override fun getFragment(id: Int?): Int {
        return getStartLocationId()
    }

}