package octii.app.taxiapp.ui.maps

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import octii.app.taxiapp.R
import octii.app.taxiapp.databinding.BottomSheetOpenMessengerBinding
import octii.app.taxiapp.ui.FragmentHelper


class OpenMessengerBottomSheet (context: Context, val activity: Activity) :
    BottomSheetDialog(context), View.OnClickListener, FragmentHelper {

    val binding : BottomSheetOpenMessengerBinding = BottomSheetOpenMessengerBinding.inflate(activity.layoutInflater)

    init {
        setContentView(binding.root)
        binding.whatsapp.setOnClickListener(this)
        binding.viber.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.whatsapp -> {
                goToApplication("com.whatsapp", activity)
                hide()
            }
            R.id.viber -> {
                goToApplication("com.viber.voip", activity)
                hide()
            }
        }
    }
}