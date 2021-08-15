package octii.app.taxiapp

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.constants.StaticTaximeter
import octii.app.taxiapp.databinding.ActivityMainBinding
import octii.app.taxiapp.locale.Application
import octii.app.taxiapp.scripts.MyPreferences
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.services.Services
import octii.app.taxiapp.ui.Permissions
import octii.app.taxiapp.web.requests.Requests


abstract class BaseActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    private lateinit var requests: Requests

    var snackbarsReceiver = object : BroadcastReceiver(){
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        registerReceiver(snackbarsReceiver, IntentFilter(Static.SNACKBAR_INTENT_FILTER))
        getSharedPrefernces()
        MobileAds.initialize(this) {}

        setLanguage(this)
        setContentView(binding.root)

        if (getFragment() != null) {
            findNavController(R.id.nav_host_fragment).navigate(getFragment()!!)
        }
        logError("on create BaseActivity")
    }

    abstract fun getFragment(id : Int? = R.id.welcomeFragment) : Int?

    fun getSharedPrefernces(){
        MyPreferences.userPreferences = getSharedPreferences(Static.SHARED_PREFERENCES_USER, Context.MODE_PRIVATE)
        MyPreferences.applicationPreferences = getSharedPreferences(Static.SHARED_PREFERENCES_APPLICATION, Context.MODE_PRIVATE)
        MyPreferences.taximeterPreferences = getSharedPreferences(StaticTaximeter.SHARED_PREFERENCES_TAXIMETER, Context.MODE_PRIVATE)
    }

    fun setLanguage(context : Context?){
        if (context != null) Application.getInstance().initAppLanguage(context)
    }

    fun checkAuth(callingActivity : Activity) {
        requests = Requests(activity = this)
        val token = getToken()
        logError("token : ${getToken()}")

        if (token != null && Permissions(this, this).checkPermissions() && getUserUUID().trim().isNotEmpty()) {
            logError("passed")
            if (token.isNotEmpty()) {
                requests.userRequests.loginWithToken(token) {
                    getStartLocation(callingActivity)
                    Services(this, Static.MAIN_SERVICES).start()
                }
            }
        } else {
            findNavController(R.id.nav_host_fragment).navigate(R.id.authorizationActivity)
            callingActivity.finish()
        }
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

    private fun getStartLocation(callingActivity : Activity){
        val uType = getSavedUserType()
        logError("token : ${getToken()}")
        if (getToken() != null) {
            if (uType == Static.DRIVER_TYPE) findNavController(R.id.nav_host_fragment).navigate(R.id.driverMapActivity)
            else findNavController(R.id.nav_host_fragment).navigate(R.id.clientMapActivity)
            callingActivity.finish()
        } else findNavController(R.id.nav_host_fragment).navigate(R.id.authorizationActivity)
    }

    fun getStartLocationId() : Int{
        getSharedPrefernces()
        val uType = getSavedUserType()
        return if (!getToken().isNullOrEmpty()) {
            if (uType == Static.DRIVER_TYPE) R.id.driverMapActivity
            else R.id.clientMapActivity
        } else R.id.welcomeFragment
    }

    private fun navigateToStartPage(){
        runOnUiThread { findNavController(R.id.nav_host_fragment).navigate(R.id.welcomeFragment) }
    }

    override fun onDestroy() {
        //UserModel.mDriver.isWorking = false todo
        //requests.userRequests.update{}
        //unregisterReceiver(snackbarsReceiver)
        super.onDestroy()
    }
}