package octii.app.taxiapp

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.constants.StaticTaximeter
import octii.app.taxiapp.databinding.ActivityMainBinding
import octii.app.taxiapp.databinding.DialogConnectionLostBinding
import octii.app.taxiapp.locale.Application
import octii.app.taxiapp.scripts.MyPreferences
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.scripts.logInfo
import octii.app.taxiapp.ui.utils.AdHelper
import octii.app.taxiapp.ui.Permissions
import octii.app.taxiapp.web.SocketHelper
import octii.app.taxiapp.web.requests.Requests
import octii.app.taxiapp.web.requests.RequestsResult


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var requests: Requests
    lateinit var adHelper: AdHelper
    lateinit var dialog: MaterialAlertDialogBuilder
    private var dialogShown = false
    var alertDialog: AlertDialog? = null


    private var snackbarsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                val message = intent.getStringExtra(Static.SNACKBAR_MESSAGE)
                val length = intent.getIntExtra(Static.SNACKBAR_MESSAGE_LENGTH, -1)
                if (!message?.trim().isNullOrEmpty())
                    Snackbar.make(findViewById(R.id.drawer), message!!, length).show()
            }
        }
    }

    private var connectionLostReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.getStringExtra(Static.CONNECTION_STATUS)) {
                    Static.CONNECTION_LOST -> {
                        if (!dialogShown) alertDialog = dialog.show()
                        dialogShown = true
                    }
                    Static.CONNECTION_EST -> {
                        dialogShown = false
                        alertDialog?.dismiss()
                        alertDialog = null
                    }
                }
            }
        }
    }

    private fun connectionLostDialog(): MaterialAlertDialogBuilder {
        val dialog = MaterialAlertDialogBuilder(this)
        //dialog.setMessage(resources.getString(R.string.connection_lost))
        dialog.setCancelable(false)
        val dialogBinding = DialogConnectionLostBinding.inflate(layoutInflater)
        dialog.setView(dialogBinding.root)
        dialogBinding.button.setOnClickListener {
            SocketHelper.connect()
            SocketHelper.mStompClient.connect()
        }
        /*
        dialog.setPositiveButton(resources.getString(R.string.try_to_connect)) { _, _ ->
            SocketHelper.connect()
        }*/
        return dialog
    }

    override fun attachBaseContext(newBase: Context?) {
        setLanguage(context = newBase)
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        adHelper = AdHelper(this, this, binding.appBarMain.include.adView)
        registerReceiver(snackbarsReceiver, IntentFilter(Static.SNACKBAR_INTENT_FILTER))
        registerReceiver(connectionLostReceiver, IntentFilter(Static.CONNECTION_INTENT_FILTER))
        getSharedPrefernces()
        dialog = connectionLostDialog()
        SocketHelper.activity = this
        setLanguage(this)
        setContentView(binding.root)
        findNavController(R.id.nav_host_fragment).navigate(R.id.splashFragment)
        logInfo("on create BaseActivity")
    }

    private fun getSharedPrefernces() {
        MyPreferences.userPreferences =
            getSharedPreferences(Static.SHARED_PREFERENCES_USER, Context.MODE_PRIVATE)
        MyPreferences.applicationPreferences =
            getSharedPreferences(Static.SHARED_PREFERENCES_APPLICATION, Context.MODE_PRIVATE)
        MyPreferences.taximeterPreferences =
            getSharedPreferences(StaticTaximeter.SHARED_PREFERENCES_TAXIMETER, Context.MODE_PRIVATE)
    }

    private fun setLanguage(context: Context?) {
        if (context != null) Application.getInstance().initAppLanguage(context)
    }



    /*private fun getStartLocation(callingActivity: Activity) {
        val uType = getSavedUserType()
        logError("token : ${getToken()}")
        if (getToken() != null) {
            if (uType == Static.DRIVER_TYPE) findNavController(R.id.nav_host_fragment).navigate(R.id.driverMapActivity)
            else findNavController(R.id.nav_host_fragment).navigate(R.id.clientMapActivity)
            callingActivity.finish()
        } else findNavController(R.id.nav_host_fragment).navigate(R.id.authorizationActivity)
    }

    fun getStartLocationId(): Int {
        return if (!Permissions(this, this).checkPermissions()) {
            logInfo("permissions not granted")
            R.id.authorizationActivity
        } else {
            getSharedPrefernces()
            val uType = getSavedUserType()
            if (!getToken().isNullOrEmpty()) {
                if (uType == Static.DRIVER_TYPE) R.id.driverMapActivity
                else R.id.clientMapActivity
            } else R.id.welcomeFragment
        }
    }*/

    override fun onDestroy() {
        try {
            unregisterReceiver(snackbarsReceiver)
            unregisterReceiver(connectionLostReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }
}