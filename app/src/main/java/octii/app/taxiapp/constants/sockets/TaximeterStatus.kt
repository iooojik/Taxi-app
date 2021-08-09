package octii.app.taxiapp.constants.sockets

class TaximeterStatus {
    companion object{
        const val ACTION_WAITING_FOR_CUSTOMER = "waiting for customers"
        const val ACTION_STARTING_ROUTE = "starting the route"
        const val ACTION_FINISHING_ROUTE = "finishing the route"
        const val ACTION_NO = "nothing"
    }
}