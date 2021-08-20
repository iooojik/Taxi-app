package octii.app.taxiapp.ui.maps.client.recievers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.StaticCoordinates
import octii.app.taxiapp.databinding.FragmentClientMapBinding
import octii.app.taxiapp.models.coordinates.RemoteCoordinates
import octii.app.taxiapp.ui.maps.client.ClientMapFragment
import octii.app.taxiapp.ui.utils.FragmentHelper

class ClientCoordinatesReciever(
    private val activity: Activity,
    private val googleMap: GoogleMap?,
    private val clientMapFragment: ClientMapFragment,
    private val context: Context,
    private var marker: Marker?
) : BroadcastReceiver(), FragmentHelper {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            when (intent.getStringExtra(StaticCoordinates.COORDINATES_STATUS_UPDATE)) {
                StaticCoordinates.COORDINATES_STATUS_UPDATE_ -> {
                    if (RemoteCoordinates.remoteLat != 0.0 && RemoteCoordinates.remoteLon != 0.0) {
                        if (googleMap != null) {
                            val latLng =
                                LatLng(RemoteCoordinates.remoteLat, RemoteCoordinates.remoteLon)
                            if (marker != null) marker!!.remove()
                            marker = googleMap.addMarker(MarkerOptions()
                                .position(latLng).title(activity.resources.getString(R.string.driver))
                                .icon(bitmapFromVector(this.context, R.drawable.car)))

                            if (!clientMapFragment.isMoved) {
                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                                clientMapFragment.isMoved = true
                            }
                        }
                    }
                }
            }
        }
    }
}