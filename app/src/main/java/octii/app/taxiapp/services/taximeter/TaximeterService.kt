package octii.app.taxiapp.services.taximeter

import android.app.Service
import android.content.Intent
import android.os.IBinder
import octii.app.taxiapp.constants.StaticOrders
import octii.app.taxiapp.constants.StaticTaximeter
import octii.app.taxiapp.constants.sockets.TaximeterStatus
import octii.app.taxiapp.models.TaximeterUpdate
import octii.app.taxiapp.models.coordinates.CoordinatesModel
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.MyPreferences
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.services.location.MyLocationListener
import octii.app.taxiapp.web.SocketHelper
import java.util.*
import kotlin.math.log

class TaximeterService : Service() {

    private lateinit var taximeterTimer: Timer
    private lateinit var timer: Timer
    private val taximeterUpdate = TaximeterUpdateTimer()
    private val intent = Intent(StaticTaximeter.TAXIMETER_INTENT_FILTER)
    private var orderTime = OrderTime()
    private var action = TaximeterStatus.ACTION_NO

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startTaximeter()
        startTimer()
    }

    private fun startTaximeter(){
        taximeterTimer = Timer()
        taximeterTimer.schedule(taximeterUpdate, 0, 1000)
    }

    private fun startTimer(){
        timer = Timer()
        orderTime = OrderTime()
        timer.scheduleAtFixedRate(orderTime, 0, 1000)
    }

    private fun getOrderTime() : Long =
        if (MyPreferences.taximeterPreferences?.getLong(StaticOrders.SHARED_PREFERENCES_ORDER_TIME, 0L) == null) 0L
        else MyPreferences.taximeterPreferences?.getLong(StaticOrders.SHARED_PREFERENCES_ORDER_TIME, 0L)!!

    private fun getWaitingTime() : Long =
        if (MyPreferences.taximeterPreferences?.getLong(StaticOrders.SHARED_PREFERENCES_ORDER_WAITING_TIME, 0L) == null) 0L
        else MyPreferences.taximeterPreferences?.getLong(StaticOrders.SHARED_PREFERENCES_ORDER_WAITING_TIME, 0L)!!

    private fun isWainting() : Boolean =
        if (MyPreferences.taximeterPreferences?.getBoolean(StaticOrders.SHARED_PREFERENCES_ORDER_IS_WAITING, false) == null) false
        else MyPreferences.taximeterPreferences?.getBoolean(StaticOrders.SHARED_PREFERENCES_ORDER_IS_WAITING, false)!!

    private fun isRunning() : Boolean =
        if (MyPreferences.taximeterPreferences?.getBoolean(StaticTaximeter.SHARED_PREFERENCES_TIMER_STATUS, false) == null) false
        else MyPreferences.taximeterPreferences?.getBoolean(StaticTaximeter.SHARED_PREFERENCES_TIMER_STATUS, false)!!

    private fun isUpdatingCoordinates() : Boolean =
        if (MyPreferences.taximeterPreferences?.
            getBoolean(StaticTaximeter.SHARED_PREFERENCES_UPDATING_COORDINATES, false) == null) false
        else MyPreferences.taximeterPreferences?.
            getBoolean(StaticTaximeter.SHARED_PREFERENCES_UPDATING_COORDINATES, false)!!

    inner class TaximeterUpdateTimer : TimerTask() {
        override fun run() {
            if (isUpdatingCoordinates()) {
                val recipientUUID =
                    if (OrdersModel.mDriver.uuid == UserModel.mUuid) OrdersModel.mCustomer.uuid
                    else OrdersModel.mDriver.uuid
                SocketHelper.taximeterUpdateCoordinates(
                    TaximeterUpdate(
                        CoordinatesModel(-1, MyLocationListener.latitude, MyLocationListener.longitude),
                        recipientUUID, OrdersModel.mUuid)
                    )
            }
        }
    }

    inner class OrderTime : TimerTask() {
        override fun run() {
            var time = getOrderTime()
            var waitingTime = getWaitingTime()
            if (isRunning()) {
                if (!isWainting()) {
                    time = time.plus(1)
                    MyPreferences.taximeterPreferences?.let {
                        MyPreferences.saveToPreferences(it,
                            StaticOrders.SHARED_PREFERENCES_ORDER_TIME,
                            time)
                    }
                } else {
                    waitingTime = waitingTime.plus(1)
                    MyPreferences.taximeterPreferences?.let {
                        MyPreferences.saveToPreferences(it,
                            StaticOrders.SHARED_PREFERENCES_ORDER_WAITING_TIME,
                            waitingTime)
                    }
                }
            }
            intent.putExtra(StaticTaximeter.TAXIMETER_BUNDLE_TIME, time)
            intent.putExtra(StaticTaximeter.TAXIMETER_BUNDLE_WAINTING_TIME, waitingTime)
            sendBroadcast(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        logError("destroy")
    }
}