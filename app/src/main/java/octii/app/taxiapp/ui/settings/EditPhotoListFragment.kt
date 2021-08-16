package octii.app.taxiapp.ui.settings

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.squareup.picasso.Picasso
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.databinding.EditPhotoItemBinding
import octii.app.taxiapp.databinding.FragmentEditPhotoListBinding
import octii.app.taxiapp.models.files.FileModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.scripts.showSnackbar
import octii.app.taxiapp.ui.FragmentHelper
import octii.app.taxiapp.ui.Permissions
import octii.app.taxiapp.web.HttpHelper
import octii.app.taxiapp.web.requests.Requests
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import octii.app.taxiapp.models.files.CountingFileRequestBody





class EditPhotoListFragment : Fragment(), FragmentHelper, View.OnClickListener {

    private lateinit var binding : FragmentEditPhotoListBinding
    private val editPhotoItemViews = arrayListOf<EditPhotoItemBinding>()
    private var selectedType = ""

    private var progressBar : LinearProgressIndicator? = null
    private var progressText : TextView? = null
    private var progressLayout : LinearLayout? = null
    private var selectedImagePlaceHolder : ImageView? = null
    private var progressUploadBar : ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentEditPhotoListBinding.inflate(layoutInflater)
        binding.fabBack.setOnClickListener(this)
        setViews()
        setInformation()
        return binding.root
    }

    private fun setInformation(){
        if (UserModel.mFiles.isNotEmpty()){
            UserModel.mFiles.forEach {
                editPhotoItemViews.forEach { b ->
                    if (b.root.tag == it.type) {
                        setImage(it.url, b.photoPlaceholder, b.uploadingProgress)
                    }
                }
            }
        }
    }

    private fun setImage(url : String, photoPlaceholder : ImageView,
                         progressBar: ProgressBar? = null, progressLayout : LinearLayout? = null){
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

    private fun setViews(){
        if (Static.PHOTO_TYPES.size == 4){
            Static.PHOTO_TYPES.forEach {
                val editPhotosBinding = EditPhotoItemBinding.inflate(layoutInflater)
                when(it){
                    Static.PHOTO_TYPES[0] -> {
                        //avatar
                        setPhotoView(editPhotosBinding, it, resources.getString(R.string.avatar_type))
                    }
                    Static.PHOTO_TYPES[1] -> {
                        //car
                        setPhotoView(editPhotosBinding, it, resources.getString(R.string.car_type))
                    }
                    Static.PHOTO_TYPES[2] -> {
                        //car_number
                        setPhotoView(editPhotosBinding, it, resources.getString(R.string.car_number_type))
                    }
                    Static.PHOTO_TYPES[3] -> {
                        //license
                        setPhotoView(editPhotosBinding, it, resources.getString(R.string.license_type))
                    }
                }
            }
        }
    }

    private fun setPhotoView(editPhotosBinding: EditPhotoItemBinding, tag : String, type : String){
        val view = editPhotosBinding.root
        view.tag = tag
        editPhotosBinding.photoType.text = type
        binding.photosLayout.addView(view)
        editPhotoItemViews.add(editPhotosBinding)
        editPhotosBinding.newPhoto.setOnClickListener {
            selectedType = tag
            //Permissions(requireContext(), requireActivity()).requestPermissions()
            progressBar = editPhotosBinding.progressBar
            progressText = editPhotosBinding.progressText
            progressLayout = editPhotosBinding.progress
            progressUploadBar = editPhotosBinding.uploadingProgress
            selectedImagePlaceHolder = editPhotosBinding.photoPlaceholder
            uploadFile()
        }
    }

    private fun uploadFile(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_PICK
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), Static.PICK_IMAGE_AVATAR)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
             Static.PICK_IMAGE_AVATAR -> {
                 logError("picked")
                if (data != null && data.data != null) {
                    logError("data != null")

                    val selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, data.data)
                    logError(selectedBitmap.toString())

                    if (selectedBitmap != null) {
                        logError("selectedBitmap != null")

                        val imageName = "_${Date().toString().trim()}.jpg"

                        val bos = ByteArrayOutputStream()
                        selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos)

                        val image = File(requireContext().cacheDir, imageName)
                        image.createNewFile()

                        val fos = FileOutputStream(image)
                        fos.write(bos.toByteArray())
                        fos.flush()
                        fos.close()

                        val requestFile: RequestBody =
                            image.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                        val body: MultipartBody.Part =
                            MultipartBody.Part.createFormData("file", image.name, requestFile)

                        logError(selectedType)
                        logError("progress bar is ${progressBar != null}")
                        if (progressBar != null && progressText != null && progressLayout != null){
                            progressLayout?.visibility = View.VISIBLE
                        }

                        if (selectedImagePlaceHolder != null)
                            selectedImagePlaceHolder?.visibility = View.GONE

                        val requestBody1 = CountingFileRequestBody(body.body, "file") { key, num ->
                            Log.d("FinishAdapter", "Perecentae is :$num")
                            requireActivity().runOnUiThread {
                                if (progressBar != null && progressText != null && progressLayout != null) {
                                    progressBar?.progress = num
                                    progressText?.text = "$num%"
                                }
                                //if (num == 100 && progressBar != null)
                                //    progressBar?.hide()
                            }


                            //update progressbar here
                            //dialog.updateProgress(num)
                            //if (num == 100) {
                            //    dialog.dismiss()
                            //}
                        }

                        uploadImageProcess(MultipartBody.Part.createFormData("file", image.name, requestBody1),
                            selectedType, HttpHelper.FILE_API){
                            Requests().userRequests.update {
                                requireActivity().runOnUiThread {
                                    if (selectedImagePlaceHolder != null) {
                                        UserModel.mFiles.forEach {
                                            if (it.type == selectedType){
                                                setImage(it.url, selectedImagePlaceHolder!!, progressUploadBar!!, progressLayout)
                                            }
                                        }
                                        //setInformation()
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> showSnackbar(requireContext(), resources.getString(R.string.error))
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.fab_back -> findNavController().navigateUp()
        }
    }

}