package octii.app.taxiapp.ui.maps.client.orderDetails

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import octii.app.taxiapp.databinding.BottomSheetShowPhotosBinding
import octii.app.taxiapp.databinding.EditPhotoItemBinding
import octii.app.taxiapp.models.files.FileModel
import octii.app.taxiapp.ui.FragmentHelper

class BottomSheetShowPhotos (context: Context, val activity: Activity, images : List<FileModel>) :
    BottomSheetDialog(context), View.OnClickListener, FragmentHelper {

    val binding : BottomSheetShowPhotosBinding = BottomSheetShowPhotosBinding.inflate(activity.layoutInflater)

    init {
        setContentView(binding.root)
        images.forEach {
            val editPhotosBinding = EditPhotoItemBinding.inflate(layoutInflater)
            Picasso.with(context)
                .load(it.url)
                .into(editPhotosBinding.photoPlaceholder)
            editPhotosBinding.newPhoto.visibility = View.GONE
            binding.photosLayout.addView(editPhotosBinding.root)
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id){

        }
    }
}