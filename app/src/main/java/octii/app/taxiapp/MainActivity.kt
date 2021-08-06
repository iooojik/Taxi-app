package octii.app.taxiapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.databinding.ActivityMainBinding
import octii.app.taxiapp.locale.Application
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.web.requests.Requests
import java.security.Permissions


class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    private lateinit var intentService : Intent
    private lateinit var requests: Requests

    override fun attachBaseContext(newBase: Context?) {
        setLanguage(context = newBase)
        super.attachBaseContext(newBase)
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
    }

    private fun setLanguage(context : Context?){
        if (context != null) Application.getInstance().initAppLanguage(context)
    }

    private fun checkAuth() {
        requests = Requests(activity = this)
        val token = getToken()
        if (token != null && octii.app.taxiapp.ui.Permissions(this, this).checkPermissions()) {
            if (token.isEmpty()) {
                navigateToStartPage()
            } else {
                requests.userRequests.loginWithToken(token)
                getStartLocation()
            }
        } else navigateToStartPage()
    }

    private fun getToken() : String?{
        return if (MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TOKEN, "").isNullOrEmpty()) ""
        else MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TOKEN, "")
    }

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
}