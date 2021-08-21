package octii.app.taxiapp.ui.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import octii.app.taxiapp.R
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.scripts.showSnackbar


interface CallHelper {
	
	fun getUriFromPhoneNumber(phoneNumber: String, context: Context): Uri? {
		var uri: Uri? = null
		val contactId: String? = getContactIdByPhoneNumber(phoneNumber, context)
		if (!TextUtils.isEmpty(contactId) && !contactId.isNullOrEmpty()) {
			val cursor: Cursor? = context.contentResolver.query(
				ContactsContract.Data.CONTENT_URI,
				arrayOf(
					ContactsContract.Data._ID
				),
				ContactsContract.Data.MIMETYPE + "=? AND " + ContactsContract.Data.CONTACT_ID + " = ?",
				arrayOf(
					"vnd.android.cursor.item/vnd.com.whatsapp.voip.call",
					contactId
				),
				null)
			if (cursor != null) {
				Log.wtf(TAG, cursor.toString())
				while (cursor.moveToNext()) {
					val id: String =
						cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data._ID))
					if (!TextUtils.isEmpty(id)) {
						uri = Uri.parse(ContactsContract.Data.CONTENT_URI.toString() + "/" + id)
						Log.d(TAG, "URI: $uri")
						break
					}
				}
				cursor.close()
			}
		}
		return uri
	}
	
	fun getContactIdByPhoneNumber(phoneNumber: String, context: Context): String? {
		val contentResolver = context.contentResolver
		var contactId: String? = null
		val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
			Uri.encode(phoneNumber))
		val projection = arrayOf(ContactsContract.PhoneLookup._ID)
		val cursor = contentResolver.query(uri, projection, null, null, null)
		if (cursor != null) {
			while (cursor.moveToNext()) {
				contactId =
					cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID))
			}
			cursor.close()
		}
		Log.wtf(TAG, "ContactID: $contactId")
		return contactId
	}
	
	fun copyToClipBoard(text: String, context: Context) {
		val clipboard: ClipboardManager =
			context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
		val clip = ClipData.newPlainText("", text)
		clipboard.setPrimaryClip(clip)
		showSnackbar(context, context.resources.getString(R.string.copied))
	}
	
	
	fun callToCustomer(phone: String, activity: Activity) {
		if (OrdersModel.mCustomer.phone.isNotEmpty()) {
			val dial = "tel:$phone"
			activity.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(dial)))
		}
	}
}