package octii.app.taxiapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import octii.app.taxiapp.databinding.ActivityMainBinding
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.sockets.SocketService
import octii.app.taxiapp.web.HttpHelper
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    private lateinit var intentService : Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        HttpHelper.doRetrofit()
        MyPreferences.userPreferences = getSharedPreferences(Static.SHARED_PREFERENCES_USER, Context.MODE_PRIVATE)
        MyPreferences.applicationPreferences = getSharedPreferences(Static.SHARED_PREFERENCES_APPLICATION, Context.MODE_PRIVATE)
        setNavigation()
    }

    private fun setNavigation(){
        //val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        val drawer : DrawerLayout = findViewById(R.id.drawer)
        //val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        AppBarConfiguration.Builder(R.id.authMessengersFragment).setOpenableLayout(drawer).build()
        //NavigationUI.setupWithNavController(bottomNavigationView, navController)
        //val token = MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TOKEN, "")
        //if (token != null && token.isNotEmpty())
            //findNavController(R.id.nav_host_fragment).navigate(R.id.chatRoomsFragment)
        checkAuth()
    }

    private fun checkAuth() {
        val token = if (MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TOKEN, "").isNullOrEmpty()) ""
        else MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TOKEN, "")

        if (token.isNullOrEmpty()) runOnUiThread { findNavController(R.id.nav_host_fragment).navigate(R.id.authMessengersFragment) }
        else {
            runOnUiThread {
                findNavController(R.id.nav_host_fragment).navigate(R.id.clientMapFragment)
            }
        }

        thread {
            val respModel = HttpHelper.USER_API.loginWithToken(UserModel(token = token!!)).execute()
            if (respModel.isSuccessful){
                val model = respModel.body()
                if (model != null && model.token.isNotEmpty()){
                    UserModel.uIsViber = model.isViber
                    UserModel.uIsWhatsapp = model.isWhatsapp
                    UserModel.uType = model.type
                    UserModel.uPhoneNumber = model.phone
                    UserModel.uToken = model.token
                    UserModel.nUserName = model.userName
                    UserModel.mUuid = model.uuid
                    MyPreferences.userPreferences?.let {
                        MyPreferences.saveToPreferences(
                            it, Static.SHARED_PREFERENCES_USER_TOKEN, model.token)
                    }
                    startSocketService()
                }
            } else HttpHelper.errorProcessing(binding.root, respModel.errorBody())
        }


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
        /*
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
         */
        return SocketService.serviceRunning
    }

}