/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 16:56                       *
 ******************************************************************************/

package octii.app.taxiapp.ui.settings.driver

import android.annotation.SuppressLint
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
import octii.app.taxiapp.models.files.CountingFileRequestBody
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.scripts.showSnackbar
import octii.app.taxiapp.ui.Permissions
import octii.app.taxiapp.ui.utils.FragmentHelper
import octii.app.taxiapp.web.HttpHelper
import octii.app.taxiapp.web.requests.Requests
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


class EditPhotoListFragment : Fragment(), FragmentHelper, View.OnClickListener {
	
	private lateinit var binding: FragmentEditPhotoListBinding
	private val editPhotoItemViews = arrayListOf<PhotoItem>()
	private var selectedImageItem: PhotoItem? = null
	
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentEditPhotoListBinding.inflate(layoutInflater)
		binding.fabBack.setOnClickListener(this)
		setViews()
		setInformation {}
		return binding.root
	}
	
	private fun setInformation(runnable: Runnable) {
		//???????????????????? ?????????????????????? ?????????????????????????? ???????????????????? ?? ?????????????????? ?? ???????????? ????????-????????????
		editPhotoItemViews.forEach { b ->
			if (UserModel.mFiles.isNotEmpty()) {
				UserModel.mFiles.forEach {
					if (b.binding.root.tag == it.type) {
						setImage(it.url, b)
					}
				}
			} else setImage("z", b)
		}
		runnable.run()
	}
	
	private fun setImage(
		url: String, photoItem: PhotoItem,
	) {
		val binding = photoItem.binding
		binding.progress.visibility = View.VISIBLE
		binding.uploadingProgress.visibility = View.VISIBLE
		
		Picasso.with(context)
			.load(url)
			.into(binding.photoPlaceholder, object : com.squareup.picasso.Callback {
				
				override fun onSuccess() {
					//???????? ???????????????? ?????????????? ??????????????????????
					binding.progress.visibility = View.GONE
					binding.uploadingProgress.visibility = View.GONE
					binding.photoType.visibility = View.VISIBLE
					binding.photoPlaceholder.visibility = View.VISIBLE
					binding.noPhoto.visibility = View.GONE
					photoItem.status = true
				}
				
				override fun onError() {
					binding.photoPlaceholder.setImageResource(R.drawable.outline_image_not_supported_24)
					binding.noPhoto.visibility = View.VISIBLE
					binding.photoType.visibility = View.GONE
					binding.uploadingProgress.visibility = View.GONE
					binding.progress.visibility = View.GONE
					photoItem.status = false
				}
			})
	}
	
	private fun setViews() {
		//???????????????????? ????????-???????????????? ?? ui
		if (Static.PHOTO_TYPES.size == 4) {
			Static.PHOTO_TYPES.forEach {
				val editPhotosBinding = EditPhotoItemBinding.inflate(layoutInflater)
				when (it) {
					Static.PHOTO_TYPES[0] -> {
						//avatar
						setPhotoView(editPhotosBinding,
							it,
							resources.getString(R.string.avatar_type))
					}
					Static.PHOTO_TYPES[1] -> {
						//car
						setPhotoView(editPhotosBinding, it, resources.getString(R.string.car_type))
					}
					Static.PHOTO_TYPES[2] -> {
						//car_number
						setPhotoView(editPhotosBinding,
							it,
							resources.getString(R.string.car_number_type))
					}
					Static.PHOTO_TYPES[3] -> {
						//license
						setPhotoView(editPhotosBinding,
							it,
							resources.getString(R.string.license_type))
					}
				}
			}
		}
	}
	
	private fun setPhotoView(editPhotosBinding: EditPhotoItemBinding, tag: String, type: String) {
		//??????????????????/???????????????????? ????????????????????
		val view = editPhotosBinding.root
		view.tag = tag
		editPhotosBinding.photoType.text = type
		binding.photosLayout.addView(view)
		val i = PhotoItem(editPhotosBinding, false, tag)
		editPhotoItemViews.add(i)
		editPhotosBinding.newPhoto.setOnClickListener {
			if (Permissions(requireContext(), requireActivity()).checkPermissions()) {
				selectedImageItem = i
				uploadFile()
			} else Permissions(requireContext(), requireActivity()).requestPermissions()
		}
	}
	
	private fun uploadFile() {
		val intent = Intent()
		intent.type = "image/*"
		intent.action = Intent.ACTION_PICK
		if (intent.resolveActivity(requireActivity().packageManager) != null) {
			startActivityForResult(Intent.createChooser(intent, "Select Picture"),
				Static.PICK_IMAGE_AVATAR)
		}
	}
	
	@SuppressLint("SetTextI18n")
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		when (requestCode) {
			Static.PICK_IMAGE_AVATAR -> {
				logError("picked")
				if (data != null && data.data != null) {
					logError("data != null")
					
					val selectedBitmap =
						MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,
							data.data)
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
						
						logError(selectedImageItem?.type.toString())
						logError("progress bar is ${selectedImageItem?.binding?.progressBar != null}")
						if (selectedImageItem?.binding?.progressBar != null
							&& selectedImageItem?.binding?.progressText != null && selectedImageItem?.binding?.progress != null
						) {
							selectedImageItem?.binding?.progress?.visibility = View.VISIBLE
						}
						
						if (selectedImageItem != null)
							selectedImageItem?.binding?.photoPlaceholder?.visibility = View.GONE
						
						val requestBody1 = CountingFileRequestBody(body.body, "file") { _, num ->
							Log.d("FinishAdapter", "Perecentae is :$num")
							requireActivity().runOnUiThread {
								if (selectedImageItem?.binding?.progressBar != null
									&& selectedImageItem?.binding?.progressText != null
									&& selectedImageItem?.binding?.progress != null
								) {
									selectedImageItem?.binding?.progressBar?.progress = num
									selectedImageItem?.binding?.progressText?.text = "$num%"
								}
							}
						}
						
						uploadImageProcess(MultipartBody.Part.createFormData("file",
							image.name,
							requestBody1),
							selectedImageItem!!.type, HttpHelper.FILE_API) {
							Requests(activity = requireActivity(),
								view = requireView()).userRequests.update {
								requireActivity().runOnUiThread {
									if (selectedImageItem != null) {
										UserModel.mFiles.forEach {
											if (it.type == selectedImageItem!!.type) {
												setImage(it.url, selectedImageItem!!)
											}
										}
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
		when (v!!.id) {
			R.id.fab_back -> findNavController().navigateUp()
		}
	}
	
	inner class PhotoItem(val binding: EditPhotoItemBinding, var status: Boolean, var type: String)
	
}