package octii.app.taxiapp

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import octii.app.taxiapp.databinding.ActivityMainBinding
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.sockets.SocketService
import octii.app.taxiapp.web.requests.Requests
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    private lateinit var intentService : Intent
    private lateinit var requests: Requests

    override fun attachBaseContext(newBase: Context?) {
        Application.getInstance().initAppLanguage(newBase)
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Application.getInstance().initAppLanguage(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        requests = Requests(activity = this)
        setContentView(binding.root)

        MyPreferences.userPreferences = getSharedPreferences(Static.SHARED_PREFERENCES_USER, Context.MODE_PRIVATE)
        MyPreferences.applicationPreferences = getSharedPreferences(Static.SHARED_PREFERENCES_APPLICATION, Context.MODE_PRIVATE)

        checkAuth()
    }

    private fun setNavigation(){
        val drawer : DrawerLayout = findViewById(R.id.drawer)
        AppBarConfiguration.Builder(R.id.authMessengersFragment).setOpenableLayout(drawer).build()
    }

    private fun checkAuth() {
        val token = if (MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TOKEN, "").isNullOrEmpty()) ""
        else MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TOKEN, "")!!

        thread {
            requests.userRequests.loginWithToken(token)
            runOnUiThread {
                startSocketService()
                setNavigation()

                if (UserModel.uToken.isEmpty()) {
                    navigateToStartPage()
                }
                else {
                    if (UserModel.uType == Static.DRIVER_TYPE) navigateToDriverMap()
                    else navigateToClientMap()
                }
            }

        }
    }

    private fun navigateToDriverMap(){
        runOnUiThread { findNavController(R.id.nav_host_fragment).navigate(R.id.driverMapFragment) }
    }

    private fun navigateToClientMap(){
        runOnUiThread { findNavController(R.id.nav_host_fragment).navigate(R.id.clientMapFragment) }
    }

    private fun navigateToStartPage(){
        runOnUiThread { findNavController(R.id.nav_host_fragment).navigate(R.id.authMessengersFragment) }
    }

    private fun startSocketService(){
        //создание намерения, которое будет запущено
        intentService = Intent(this, SocketService::class.java)
        //запуск сервиса. Если метод возвращает true, то сервис был запущен,
        // если сервис был остановлен, то false
        touchService()
    }

    private fun touchService() : Boolean {
        return if (!isMyServiceRunning()) {startService(intentService); true}
        else {stopService(intentService); false}
    }

    private fun isMyServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (SocketService::javaClass.name == service.service.className) {
                return true
            }
        }
        return false

    }

}