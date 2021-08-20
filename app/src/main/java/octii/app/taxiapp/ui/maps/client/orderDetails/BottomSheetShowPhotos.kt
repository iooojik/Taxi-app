package octii.app.taxiapp.ui.maps.client.orderDetails

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.databinding.BottomSheetShowPhotosBinding
import octii.app.taxiapp.databinding.EditPhotoItemBinding
import octii.app.taxiapp.models.files.FileModel
import octii.app.taxiapp.ui.utils.FragmentHelper

class BottomSheetShowPhotos(context: Context, val activity: Activity, images: List<FileModel>) :
    BottomSheetDialog(context), View.OnClickListener, FragmentHelper {

    val binding: BottomSheetShowPhotosBinding =
        BottomSheetShowPhotosBinding.inflate(activity.layoutInflater)
    private val editPhotoItemViews = arrayListOf<EditPhotoItemBinding>()


    init {
        setContentView(binding.root)
        setViews()

        images.forEach {
            editPhotoItemViews.forEach { b ->
                if (b.root.tag == it.type) {
                    setImage(it.url, b.photoPlaceholder, b.uploadingProgress)
                }
            }
        }
    }

    private fun setImage(
        url: String, photoPlaceholder: ImageView,
        progressBar: ProgressBar? = null, progressLayout: LinearLayout? = null,
    ) {
        progressBar?.visibility = View.VISIBLE

        Picasso.with(context)
            .load(url)
            .into(photoPlaceholder, object : com.squareup.picasso.Callback {
                override fun onSuccess() {
                    progressBar?.visibility = View.GONE
                    photoPlaceholder.visibility = View.VISIBLE
                    progressLayout?.visibility = View.GONE
                }

                override fun onError() {
                    //do smth when there is picture loading error
                }
            })
    }

    private fun setViews() {
        if (Static.PHOTO_TYPES.size == 4) {
            Static.PHOTO_TYPES.forEach {
                val editPhotosBinding = EditPhotoItemBinding.inflate(layoutInflater)
                when (it) {
                    Static.PHOTO_TYPES[0] -> {
                        //avatar
                        setPhotoView(editPhotosBinding,
                            it,
                            activity.resources.getString(R.string.avatar_type))
                    }
                    Static.PHOTO_TYPES[1] -> {
                        //car
                        setPhotoView(editPhotosBinding,
                            it,
                            activity.resources.getString(R.string.car_type))
                    }
                    Static.PHOTO_TYPES[2] -> {
                        //car_number
                        setPhotoView(editPhotosBinding,
                            it,
                            activity.resources.getString(R.string.car_number_type))
                    }
                    Static.PHOTO_TYPES[3] -> {
                        //license
                        setPhotoView(editPhotosBinding,
                            it,
                            activity.resources.getString(R.string.license_type))
                    }
                }
            }
        }
    }

    private fun setPhotoView(editPhotosBinding: EditPhotoItemBinding, tag: String, type: String) {
        val view = editPhotosBinding.root
        view.tag = tag
        editPhotosBinding.photoType.text = type
        binding.photosLayout.addView(view)
        editPhotoItemViews.add(editPhotosBinding)
        editPhotosBinding.newPhoto.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when (v!!.id) {

        }
    }
}