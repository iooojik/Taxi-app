package octii.app.taxiapp.web

import android.annotation.SuppressLint
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.models.coordinates.CoordinatesModel
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.scripts.logInfo
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import io.reactivex.CompletableTransformer
import octii.app.taxiapp.models.TaximeterUpdate


class SocketHelper {
    companion object{

        val mStompClient: StompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, Static.WEB_SOCKET_URL)
        private val gson = Gson()
        @JvmStatic
        var compositeDisposable : CompositeDisposable = CompositeDisposable()


        fun connect(){
            mStompClient.withClientHeartbeat(1000).withServerHeartbeat(1000)
            resetSubscriptions()

            val disposableLifecycle: Disposable? =
                mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ lifecycleEvent ->
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
                    mStompClient.connect()
                }, { throwable ->
                logError("ttt :$throwable")
                throwable.printStackTrace()
            })

            compositeDisposable.add(disposableLifecycle!!)
        }

        fun resetSubscriptions() {
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
        }

        @SuppressLint("CheckResult")
        fun makeOrder(){
            mStompClient.send("/requests/order.make.${UserModel.mUuid}", toJSON(UserModel())).compose(
                applySchedulers()).subscribe({
                logInfo("success")
            }, { throwable ->
                logError("ttt :$throwable")
                throwable.printStackTrace()
            })

        }

        @SuppressLint("CheckResult")
        fun acceptOrder(order : OrdersModel){
            mStompClient.send("/requests/order.accept.${UserModel.mUuid}", toJSON(order))
                .compose(applySchedulers()).subscribe({
                    logInfo("success")
                }, { throwable ->
                    logError("ttt :$throwable")
                    throwable.printStackTrace()
                })
        }

        @SuppressLint("CheckResult")
        fun rejectOrder(order : OrdersModel){
            mStompClient.send("/requests/order.reject.${UserModel.mUuid}", toJSON(order))
                .compose(applySchedulers()).subscribe({
                logInfo("success")
            }, { throwable ->
                logError("ttt :$throwable")
                throwable.printStackTrace()
            })
        }

        @SuppressLint("CheckResult")
        fun finishOrder(order : OrdersModel){
            mStompClient.send("/requests/order.finish.${UserModel.mUuid}", toJSON(order))
                .compose(applySchedulers()).subscribe({
                logInfo("success")
            }, { throwable ->
                logError("ttt :$throwable")
                throwable.printStackTrace()
            })
        }

        @SuppressLint("CheckResult")
        fun updateCoordinates(coordinatesModel: CoordinatesModel){
            mStompClient.send("/requests/navigation.coordinates.update.${UserModel.mUuid}",
                toJSON(coordinatesModel)).compose(applySchedulers()).subscribe({
                logInfo("success")
            }, { throwable ->
                logError("ttt :$throwable")
                throwable.printStackTrace()
            })
        }

        @SuppressLint("CheckResult")
        fun taximeterUpdate(taximeterUpdate: TaximeterUpdate){
            mStompClient.send("/requests/taximeter.update.${UserModel.mUuid}",
                toJSON(taximeterUpdate)).compose(applySchedulers()).subscribe({
            }, { throwable ->
                logError("ttt :$throwable")
                throwable.printStackTrace()
            })
        }

        private fun toJSON(model : Any) : String{
            return gson.toJson(model)
        }

        fun disconnectStomp() {
            mStompClient.disconnect()
        }

        fun applySchedulers(): CompletableTransformer {
            return CompletableTransformer { upstream: Completable ->
                upstream
                    .unsubscribeOn(Schedulers.newThread())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }
        }
    }

}