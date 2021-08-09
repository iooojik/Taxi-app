package octii.app.taxiapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.constants.StaticTaximeter
import octii.app.taxiapp.databinding.ActivityMainBinding
import octii.app.taxiapp.locale.Application
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.MyPreferences
import octii.app.taxiapp.ui.Permissions
import octii.app.taxiapp.web.requests.Requests


class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    private lateinit var requests: Requests
    private var snackbarsReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null){
                val message = intent.getStringExtra(Static.SNACKBAR_MESSAGE)
                val length = intent.getIntExtra(Static.SNACKBAR_MESSAGE_LENGTH, -1)
                if (!message?.trim().isNullOrEmpty())
                    Snackbar.make(findViewById(R.id.drawer), message!!, length).show()
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        setLanguage(context = newBase)
        super.attachBaseContext(newBase)
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(snackbarsReceiver, IntentFilter(Static.SNACKBAR_INTENT_FILTER))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSharedPrefernces()
        setLanguage(this)

        checkAuth()
    }

    private fun getSharedPrefernces(){
        MyPreferences.userPreferences = getSharedPreferences(Static.SHARED_PREFERENCES_USER, Context.MODE_PRIVATE)
        MyPreferences.applicationPreferences = getSharedPreferences(Static.SHARED_PREFERENCES_APPLICATION, Context.MODE_PRIVATE)
        MyPreferences.taximeterPreferences = getSharedPreferences(StaticTaximeter.SHARED_PREFERENCES_TAXIMETER, Context.MODE_PRIVATE)
    }

    private fun setLanguage(context : Context?){
        if (context != null) Application.getInstance().initAppLanguage(context)
    }

    private fun checkAuth() {
        requests = Requests(activity = this)
        val token = getToken()
        if (token != null && Permissions(this, this).checkPermissions() && getUserUUID().trim().isNotEmpty()) {
            if (token.isEmpty()) {
                navigateToStartPage()
            } else {
                getStartLocation()
                requests.userRequests.loginWithToken(token) {
                    getStartLocation()
                }
            }
        } else navigateToStartPage()
    }

    private fun getToken() : String?{
        return if (MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TOKEN, "").isNullOrEmpty()) ""
        else MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TOKEN, "")
    }

    private fun getUserUUID() : String =
        MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_UUID, "")!!

    private fun getSavedUserType() : String{
        return if (MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TYPE, "").isNullOrEmpty()) ""
        else MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TYPE, "")!!
    }

    private fun getStartLocation(){
        val uType = getSavedUserType()
        if (uType == Static.DRIVER_TYPE) findNavController(R.id.nav_host_fragment).navigate(R.id.driverMapFragment)
        else findNavController(R.id.nav_host_fragment).navigate(R.id.clientMapFragment)
    }

    private fun navigateToStartPage(){
        runOnUiThread { findNavController(R.id.nav_host_fragment).navigate(R.id.welcomeFragment) }
    }

    override fun onDestroy() {
        UserModel.mDriver.isWorking = false
        requests.userRequests.update{}
        unregisterReceiver(snackbarsReceiver)
        super.onDestroy()
    }
}