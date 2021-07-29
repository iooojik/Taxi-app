package octii.app.taxiapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.AppBarConfiguration


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun setNavigation(){
        //val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        val drawer : DrawerLayout = findViewById(R.id.drawer)
        //val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        AppBarConfiguration.Builder(R.id.authProcessFragment).setOpenableLayout(drawer).build()
        //NavigationUI.setupWithNavController(bottomNavigationView, navController)
        //val token = MyPreferences.userPreferences?.getString(Static.SHARED_PREFERENCES_USER_TOKEN, "")
        //if (token != null && token.isNotEmpty())
            //findNavController(R.id.nav_host_fragment).navigate(R.id.chatRoomsFragment)
    }
}