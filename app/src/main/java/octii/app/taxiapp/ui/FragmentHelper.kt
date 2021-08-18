package octii.app.taxiapp.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import octii.app.taxiapp.R
import octii.app.taxiapp.locale.LocaleUtils
import octii.app.taxiapp.models.SpeakingLanguagesModel
import octii.app.taxiapp.models.files.FileApi
import octii.app.taxiapp.models.files.FileModel
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.web.HttpHelper
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log


interface FragmentHelper {
    fun blockGoBack(activity: ComponentActivity, fragment: Fragment) {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        }
        activity.onBackPressedDispatcher.addCallback(fragment.viewLifecycleOwner, callback)
    }

    fun getRealPathFromURI(context: Context, contentURI: Uri): String {
        var result: String = ""
        val cursor = context.contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) {
            result = contentURI.path.toString()
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            if (idx >= 0) {
                result = cursor.getString(idx)
            }
            cursor.close()
        }
        return result
    }

    fun hideKeyBoard(activity: Activity, v: View) {
        val imm: InputMethodManager =
            activity.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

    fun bitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        // below line is use to generate a drawable.
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)

        // below line is use to set bounds to our vector drawable.
        vectorDrawable!!.setBounds(0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight)

        // below line is use to create a bitmap for our
        // drawable which we have added.
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888)

        // below line is use to add bitmap in our canvas.
        val canvas = Canvas(bitmap)

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas)

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun isInstalled(packageName: String, pm: PackageManager): Boolean {
        return try {
            val pi: PackageInfo? = pm.getPackageInfo(packageName, 0)
            pi != null
        } catch (e: Exception) {
            false
        }
    }

    fun setLanguage(language: String, activity: Activity?) {
        LocaleUtils.setSelectedLanguageId(language)
        if (activity != null)
            synchronized(activity) {
                activity.finish()
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                activity.startActivity(activity.intent)
            }
    }

    fun changeSpeakingLanguage(lang: String, isChecked: Boolean) {
        val languages = arrayListOf<SpeakingLanguagesModel>()
        if (isChecked) {
            for (spLang in UserModel.mLanguages) languages.add(spLang)
            languages.add(SpeakingLanguagesModel(language = lang))
        } else {
            for (spLang in UserModel.mLanguages) {
                if (spLang.language != lang) languages.add(spLang)
            }
        }
        UserModel.mLanguages = languages.toList()

    }

    fun goToApplication(packageName: String, activity: Activity) {
        var intent = activity.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
        } else {
            intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.data = Uri.parse("market://details?id=$packageName")
            activity.startActivity(intent)
        }
    }

    fun callViber(phone : String, context: Context){
        val uri: Uri = Uri.parse("tel:" + Uri.encode(phone))
        val intent = Intent("android.intent.action.VIEW")
        intent.setClassName("com.viber.voip", "com.viber.voip.WelcomeActivity")
        intent.data = uri
        context.startActivity(intent)
    }

    fun getZoomLevel(km: Float): Float {
        return log(40000f / km * 2f, 2.0f)
    }

    fun uploadImageProcess(
        body: MultipartBody.Part,
        selectedType: String,
        api: FileApi,
        runnable: Runnable,
    ) {
        api.uploadImage(body, selectedType, UserModel.mUuid)
            .enqueue(object :
                Callback<FileModel> {
                override fun onResponse(
                    call: Call<FileModel>,
                    response: Response<FileModel>,
                ) {
                    runnable.run()
                }

                override fun onFailure(call: Call<FileModel>, t: Throwable) {
                    HttpHelper.onFailure(t)
                }

            })
    }
}