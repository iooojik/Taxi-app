/******************************************************************************
 * Copyright (c) 2021. Created by iooojik.                                    *
 * Telegram: @iooojik                                                         *
 * Email: sbobrov760@gmail.com                                                *
 * All rights reserved. Last modified 21.08.2021, 16:50                       *
 ******************************************************************************/

package octii.app.taxiapp.models.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import octii.app.taxiapp.locale.LocaleUtils
import octii.app.taxiapp.models.SpeakingLanguagesModel
import octii.app.taxiapp.models.coordinates.CoordinatesModel
import octii.app.taxiapp.models.driver.DriverModel
import octii.app.taxiapp.models.files.FileModel

@Entity
class UserModel(
	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "_id")
	var id: Long = uID,
	
	@ColumnInfo(name = "user_name")
	var userName: String? = nUserName,
	
	@ColumnInfo(name = "user_phone")
	var phone: String = uPhoneNumber,
	
	@ColumnInfo(name = "token")
	var token: String = uToken,
	
	@ColumnInfo(name = "type")
	var type: String = uType,
	
	@ColumnInfo(name = "is_viber")
	var isViber: Boolean = uIsViber,
	
	@ColumnInfo(name = "uuid")
	var uuid: String = mUuid,
	
	@ColumnInfo(name = "is_only_client")
	var isOnlyClient: Boolean = mIsOnlyClient,
	
	@ColumnInfo(name = "avatar_url")
	var avatarURL: String = mAvatarURL,
	
	var languages: List<SpeakingLanguagesModel> = mLanguages,
	
	var coordinates: CoordinatesModel? = mCoordinates,
	
	var driver: DriverModel = mDriver,
	
	var files: List<FileModel> = mFiles,
) {
	companion object {
		@JvmStatic
		var uID: Long = (-1).toLong()
		
		@JvmStatic
		var uToken: String = ""
		
		@JvmStatic
		var nUserName: String = ""
		
		@JvmStatic
		var uPhoneNumber: String = ""
		
		@JvmStatic
		var uType: String = "client"
		
		@JvmStatic
		var uIsViber: Boolean = false
		
		@JvmStatic
		var mUuid: String = ""
		
		@JvmStatic
		var mIsOnlyClient: Boolean = true
		
		@JvmStatic
		var mAvatarURL: String = ""
		
		@JvmStatic
		var mLanguages: List<SpeakingLanguagesModel> =
			listOf(SpeakingLanguagesModel(language = LocaleUtils.SERBIAN))
		
		@JvmStatic
		var mCoordinates: CoordinatesModel? = CoordinatesModel()
		
		@JvmStatic
		var mDriver: DriverModel = DriverModel()
		
		@JvmStatic
		var mFiles: List<FileModel> = listOf()
	}
}
