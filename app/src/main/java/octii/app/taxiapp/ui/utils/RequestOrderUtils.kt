package octii.app.taxiapp.ui.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import octii.app.taxiapp.R
import octii.app.taxiapp.constants.StaticOrders
import octii.app.taxiapp.models.orders.OrdersModel
import octii.app.taxiapp.ui.maps.driver.DriverMapFragment
import octii.app.taxiapp.ui.maps.driver.DriverOrderBottomSheet
import octii.app.taxiapp.web.SocketHelper

interface RequestOrderUtils : View.OnClickListener, FragmentHelper, CallHelper {
	
	fun setInformation()
	
	fun setAvatar(url: String, context: Context, imagePlaceHolder: ImageView) {
		if (url.trim().isNotEmpty()) {
			Picasso.with(context)
				.load(url)
				.transform(RoundedCornersTransformation(40, 5))
				.resize(160, 160)
				.centerCrop()
				.into(imagePlaceHolder)
		} else {
			imagePlaceHolder.setImageResource(R.drawable.outline_account_circle_24)
		}
	}
	
	fun acceptOrder(activity: Activity, order: OrdersModel, bottomSheet: BottomSheetDialog) {
		//принятие заказа
		activity.sendBroadcast(Intent(StaticOrders.ORDER_STATUS_INTENT_FILTER)
			.putExtra(StaticOrders.ORDER_STATUS, StaticOrders.ORDER_STATUS_ACCEPTED))
		OrdersModel.mIsAccepted = true
		SocketHelper.acceptOrder(order)
		bottomSheet.hide()
	}
	
	fun rejectOrder(order: OrdersModel, bottomSheet: BottomSheetDialog) {
		if (bottomSheet is DriverOrderBottomSheet) {
			try {
				DriverMapFragment.ordered = true
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
		SocketHelper.rejectOrder(order)
		bottomSheet.hide()
	}
	
	
}