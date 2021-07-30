package octii.app.taxiapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import octii.app.taxiapp.databinding.ActivityMainBinding
import octii.app.taxiapp.models.UserModel
import octii.app.taxiapp.web.HttpHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

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
                }
            } else HttpHelper.errorProcessing(binding.root, respModel.errorBody())
        }


    }

}