package octii.app.taxiapp.ui.settings.driver

import octii.app.taxiapp.BaseActivity
import octii.app.taxiapp.R

class DriverSettingsActivity : BaseActivity() {
    override fun getFragment(id: Int?): Int {
        return R.id.driverSettingsFragment
    }

    override fun onBackPressed() {}

}