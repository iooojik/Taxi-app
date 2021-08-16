package octii.app.taxiapp.ui.maps.driver

import android.os.Bundle
import androidx.navigation.findNavController
import octii.app.taxiapp.BaseActivity
import octii.app.taxiapp.R
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.ui.Permissions

class DriverMapActivity : BaseActivity() {

    override fun getFragment(id: Int?): Int {
        return R.id.driverMapFragment
    }

    override fun onBackPressed() {}
}