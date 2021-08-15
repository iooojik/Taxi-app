package octii.app.taxiapp.ui.maps.client

import octii.app.taxiapp.BaseActivity
import octii.app.taxiapp.R

class ClientMapActivity :BaseActivity() {

    override fun getFragment(id: Int?): Int {
        return R.id.clientMapFragment
    }

    override fun onBackPressed() {}
}