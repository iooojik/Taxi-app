package octii.app.taxiapp.ui.maps.driver

import octii.app.taxiapp.BaseActivity
import octii.app.taxiapp.R

class DriverMapActivity : BaseActivity() {

    override fun getFragment(id: Int?): Int {
        return R.id.driverMapFragment
    }

    override fun onBackPressed() {}
}