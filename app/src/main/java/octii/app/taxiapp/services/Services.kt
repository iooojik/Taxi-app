package octii.app.taxiapp.services

import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.scripts.logError
import kotlin.reflect.KClass

class Services(private val activity: Activity?,
               private val servicesList: List<KClass<out Service>> = Static.MAIN_SERVICES) {

    fun start(){
        if (servicesList.isNotEmpty()) {
            servicesList.forEach {
                //activity.startService(Intent(activity, it.java))
                startService(it)
            }
        }
    }

    fun startNewService(serviceName: KClass<out Service>){
        startService(serviceName)
    }

    fun stopService(serviceName: KClass<out Service>){
        activity?.stopService(Intent(activity, serviceName.java))
    }

    private fun startService(serviceName : KClass<out Service>){
        //создание намерения, которое будет запущено
        val intentService = Intent(activity, serviceName.java)
        //запуск сервиса. Если метод возвращает true, то сервис был запущен,
        // если сервис был остановлен, то false
        logError("${touchService(intentService, serviceName)} $serviceName")
    }

    private fun touchService(intentService : Intent, serviceName : KClass<out Service>) : Boolean {
        return if (!isMyServiceRunning(serviceName)) {activity?.startService(intentService); true}
        else {activity?.stopService(intentService); false}
        //else false
    }

    private fun isMyServiceRunning(serviceName : KClass<out Service>): Boolean {
        val manager = activity?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceName.java.name == service.service.className) {
                return true
            }
        }
        return false

    }

}