package octii.app.taxiapp

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.google.gson.Gson
import octii.app.taxiapp.databinding.ActivityMainBinding
import octii.app.taxiapp.models.TokenAuthorization
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.sockets.SocketService
import octii.app.taxiapp.web.HttpHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    private lateinit var intentService : Intent

    override fun attachBaseContext(newBase: Context?) {
        Application.getInstance().initAppLanguage(newBase)
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Application.getInstance().initAppLanguage(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        HttpHelper.doRetrofit()
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
        else MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TOKEN, "")

        HttpHelper.USER_API.loginWithToken(UserModel(token = token!!)).enqueue(object : Callback<TokenAuthorization>{
            override fun onResponse(call: Call<TokenAuthorization>, response: Response<TokenAuthorization>) {
                if (response.isSuccessful){
                    val model = response.body()?.user
                    if (model != null && model.token.isNotEmpty()){
                        UserModel.uID = model.id
                        UserModel.uIsViber = model.isViber
                        UserModel.uIsWhatsapp = model.isWhatsapp
                        UserModel.uType = model.type
                        UserModel.uPhoneNumber = model.phone!!
                        UserModel.uToken = model.token
                        UserModel.nUserName = model.userName!!
                        UserModel.mUuid = model.uuid
                        UserModel.mIsOnlyClient = model.isOnlyClient
                        UserModel.mAvatarURL = model.avatarURL
                        UserModel.mLanguages = model.languages
                        UserModel.mCoordinates = model.coordinates

                        MyPreferences.userPreferences?.let {
                            MyPreferences.saveToPreferences(
                                it, Static.SHARED_PREFERENCES_USER_TOKEN, model.token)
                        }

                        if (response.body()?.order != null){
                            val order = response.body()?.order
                            SocketService.getOrderModel(Gson().toJson(order), false,
                                if (!order?.isFinished!!) !order.isFinished else false)
                        }

                        startSocketService()
                        setNavigation()

                        if (token.isNullOrEmpty()) {
                            navigateToStartPage()
                        }
                        else {
                            if (UserModel.uType == Static.DRIVER_TYPE) navigateToDriverMap()
                            else navigateToClientMap()
                        }
                    } else{
                        setNavigation()
                        navigateToStartPage()
                    }
                } else {
                    setNavigation()
                    HttpHelper.errorProcessing(binding.root, response.errorBody())
                    navigateToStartPage()
                }
            }

            override fun onFailure(call: Call<TokenAuthorization>, t: Throwable) {
                HttpHelper.onFailure(t)
                navigateToStartPage()
            }
        })
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