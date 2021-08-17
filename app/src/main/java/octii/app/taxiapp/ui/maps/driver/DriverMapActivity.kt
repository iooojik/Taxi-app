package octii.app.taxiapp.ui.maps.driver

import android.os.Bundle
import com.google.android.gms.ads.MobileAds
import octii.app.taxiapp.BaseActivity
import octii.app.taxiapp.R

class DriverMapActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this){}

    }

    override fun getFragment(id: Int?): Int {
        return R.id.driverMapFragment
    }

    override fun onBackPressed() {}
}