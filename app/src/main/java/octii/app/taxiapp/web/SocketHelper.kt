package octii.app.taxiapp.web

import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import octii.app.taxiapp.Static
import octii.app.taxiapp.models.CoordinatesModel
import octii.app.taxiapp.models.OrdersModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.scripts.logInfo
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent


class SocketHelper {
    companion object{

        val mStompClient: StompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, Static.WEB_SOCKET_URL)
        @JvmStatic
        var isConnected = mStompClient.isConnected
        private val gson = Gson()
        @JvmStatic
        var compositeDisposable : CompositeDisposable = CompositeDisposable()


        fun connect(){
            val disposableLifecycle: Disposable? = mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { lifecycleEvent ->
                    logInfo(lifecycleEvent.type)
                    if (lifecycleEvent.type != null) {
                        when (lifecycleEvent.type!!) {
                            LifecycleEvent.Type.OPENED -> logInfo("Stomp connection opened")

                            LifecycleEvent.Type.ERROR -> logError("Stomp connection error ${lifecycleEvent.exception}")

                            LifecycleEvent.Type.CLOSED -> {
                                logInfo("Stomp connection closed")
                                resetSubscriptions()
                            }

                            LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> logError("Stomp failed server heartbeat")

                        }
                    }
                }
            if (disposableLifecycle != null) {
                compositeDisposable.add(disposableLifecycle)
            }
            mStompClient.connect()
            //connectToRoom()
        }

        fun resetSubscriptions() {
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            connect()
        }

        fun makeOrder(){
            mStompClient.send("/requests/order.make.${UserModel.mUuid}", toJSON(UserModel())).subscribe()
        }

        fun acceptOrder(order : OrdersModel){
            mStompClient.send("/requests/order.accept.${UserModel.mUuid}", toJSON(order)).subscribe()
        }

        fun rejectOrder(order : OrdersModel){
            mStompClient.send("/requests/order.reject.${UserModel.mUuid}", toJSON(order)).subscribe()
        }

        fun finishOrder(order : OrdersModel){
            mStompClient.send("/requests/order.finish.${UserModel.mUuid}", toJSON(order)).subscribe()
        }

        fun updateCoordinates(coordinatesModel: CoordinatesModel){
            mStompClient.send("/requests/navigation.coordinates.update.${UserModel.mUuid}",
                toJSON(coordinatesModel)).subscribe()
        }

        fun authorization(){
            //mStompClient.send("/messenger/authorization.${UserModel.mUuid}", gson.toJson(UserModel())).subscribe()
        }

        private fun toJSON(model : Any) : String{
            return gson.toJson(model)
        }
    }

}