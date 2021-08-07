package octii.app.taxiapp.services.taximeter

import android.app.Service
import android.content.Intent
import android.content.res.Resources
import android.os.IBinder
import android.text.format.DateUtils
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.OrderActions
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.models.TaximeterUpdate
import octii.app.taxiapp.models.coordinates.CoordinatesModel
import octii.app.taxiapp.models.driver.DriverModel
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.MyPreferences
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.scripts.logService
import octii.app.taxiapp.services.location.MyLocationListener
import octii.app.taxiapp.web.SocketHelper
import java.util.*

class TaximeterService : Service() {

    companion object{
        private var pricePerKm : Float = DriverModel.mPrices.pricePerKm
        var priceWaiting : Float = DriverModel.mPrices.priceWaitingMin
        var pricePerMin : Float = DriverModel.mPrices.pricePerMinute
        var order = OrdersModel()
        var isRunning = false
        private fun number2digits(number : Float) : String = String.format("%.2f", number)


        fun getTaximeterString(resources : Resources) : String{
            val time = MyPreferences.userPreferences?.getLong(Static.SHARED_PREFERENCES_ORDER_TIME, 0L)
            return if (time != null)
                "${resources.getString(R.string.taximeter_price,
                    number2digits((pricePerKm * MyLocationListener.distance)),
                    if(time /60 < 1) number2digits(pricePerMin)
                    else number2digits((pricePerMin * (time/60))))} \n" +
                        "${formatDuration(seconds = time.toLong())}\n ${number2digits(MyLocationListener.distance)}"
            else ""
        }

        private fun formatDuration(seconds: Long): String = DateUtils.formatElapsedTime(seconds)
    }

    private lateinit var taximeterTimer: Timer
    private lateinit var timer: Timer
    private val taximeterUpdate = TaximeterUpdate()
    private val orderTime = OrderTime()
    private var action = OrderActions.ACTION_NO

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logService("taximeter service is running")
        if ((getTime() != null && getTime()!! == 0L) || !isRunning) setTimer()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun setTimer(){
        isRunning = true
        taximeterTimer = Timer()
        timer = Timer()
        taximeterTimer.schedule(taximeterUpdate, 0, 1000)
        timer.schedule(orderTime, 0, 1000)
    }

    private fun getTime() : Long? = MyPreferences.userPreferences?.getLong(Static.SHARED_PREFERENCES_ORDER_TIME, 0L)

    inner class TaximeterUpdate : TimerTask() {
        override fun run() {
            val recipientUUID =
                if(OrdersModel.mDriver.uuid == UserModel.mUuid) OrdersModel.mCustomer.uuid
                else OrdersModel.mDriver.uuid
            SocketHelper.taximeterUpdate(
                TaximeterUpdate(CoordinatesModel(-1,
                    MyLocationListener.latitude, MyLocationListener.longitude), recipientUUID))
                /*
                TaximeterModel(
                    timeStamp = Date().toString(),
                    action = action,
                    coordinates = CoordinatesModel(
                        latitude = MyLocationListener.latitude,
                        longitude = MyLocationListener.longitude),
                    prices = Prices(),
                    orderModel = order
                )

                 */
        }
    }

    inner class OrderTime : TimerTask(){
        override fun run() {
            var time = MyPreferences.userPreferences?.getLong(Static.SHARED_PREFERENCES_ORDER_TIME, 0L)
            if (time != null) {
                time += 1
                MyPreferences.userPreferences?.let {
                    MyPreferences.saveToPreferences(it, Static.SHARED_PREFERENCES_ORDER_TIME, time)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        logError("destroy")
    }
}