package octii.app.taxiapp.ui.settings.client

import octii.app.taxiapp.BaseActivity
import octii.app.taxiapp.R

class ClientSettingsActivity : BaseActivity() {
    override fun getFragment(id: Int?): Int {
        return R.id.clientSettingsFragment
    }
}