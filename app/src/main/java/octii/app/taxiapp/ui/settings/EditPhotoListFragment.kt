package octii.app.taxiapp.ui.settings

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.Static
import octii.app.taxiapp.databinding.EditPhotoItemBinding
import octii.app.taxiapp.databinding.FragmentEditPhotoListBinding
import octii.app.taxiapp.models.files.FileModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logError
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


class EditPhotoListFragment : Fragment(), FragmentHelper, View.OnClickListener {

    private lateinit var binding : FragmentEditPhotoListBinding
    private val editPhotoItemViews = arrayListOf<EditPhotoItemBinding>()
    private var selectedType = ""

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
                        Picasso.with(context)
                            .load(it.url)
                            .into(b.photoPlaceholder)
                        //b.newPhoto.visibility = View.GONE
                    }
                }
            }
        }
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
            Permissions(requireContext(), requireActivity()).requestPermissions()
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
                        HttpHelper.FILE_API.uploadImage(body, selectedType, UserModel.mUuid)
                            .enqueue(object :
                                Callback<FileModel> {
                                override fun onResponse(
                                    call: Call<FileModel>,
                                    response: Response<FileModel>
                                ) {
                                    Requests().userRequests.update {
                                        setInformation()
                                    }
                                }

                                override fun onFailure(call: Call<FileModel>, t: Throwable) {
                                    HttpHelper.onFailure(t)
                                }
                            })
                    }
                    /*
                    val selectedImage = data.data!!
                    //call the standard crop action intent (the user device may not support it)
                    val cropIntent = Intent("com.android.camera.action.CROP")
                    //indicate image type and Uri
                    cropIntent.setDataAndType(selectedImage, "image/*")
                    //set crop properties
                    cropIntent.putExtra("crop", "true")
                    //indicate aspect of desired crop
                    cropIntent.putExtra("aspectX", 1)
                    cropIntent.putExtra("aspectY", 1)
                    //indicate output X and Y
                    cropIntent.putExtra("outputX", 256)
                    cropIntent.putExtra("outputY", 256)
                    //retrieve data on return
                    cropIntent.putExtra("return-data", true)
                    //start the activity - we handle returning in onActivityResult
                    startActivityForResult(cropIntent, Static.PICK_CROP)*/*/
                }
            } Static.PICK_CROP -> {
                Log.e("ttt", "crop")
                if (data != null && data.extras != null) {
                    val extras: Bundle = data.extras!!
                    val selectedBitmap = extras.getParcelable<Bitmap>("data")

                    if (selectedBitmap != null) {
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
                        HttpHelper.FILE_API.uploadImage(body, selectedType, UserModel.mUuid)
                            .enqueue(object :
                                Callback<FileModel> {
                                override fun onResponse(
                                    call: Call<FileModel>,
                                    response: Response<FileModel>
                                ) {
                                    Requests().userRequests.update {
                                        setInformation()
                                    }
                                }

                                override fun onFailure(call: Call<FileModel>, t: Throwable) {
                                    HttpHelper.onFailure(t)
                                }
                            })
                    }
                }
        }
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.fab_back -> findNavController().navigateUp()
        }
    }

}