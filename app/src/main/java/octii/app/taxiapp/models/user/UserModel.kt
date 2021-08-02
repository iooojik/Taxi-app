package octii.app.taxiapp.models.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import octii.app.taxiapp.models.CoordinatesModel
import octii.app.taxiapp.models.SpeakingLanguagesModel
import java.util.*

@Entity
class UserModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = uID,

    @ColumnInfo(name = "user_name")
    var userName: String? = nUserName,

    @ColumnInfo(name = "user_phone")
    var phone: String? = uPhoneNumber,

    @ColumnInfo(name = "token")
    var token: String = uToken,

    @ColumnInfo(name = "type")
    var type: String = uType,

    @ColumnInfo(name = "is_whatsapp")
    var isWhatsapp : Boolean = uIsWhatsapp,

    @ColumnInfo(name = "is_viber")
    var isViber : Boolean = uIsViber,

    @ColumnInfo(name = "uuid")
    var uuid: String = mUuid,

    @ColumnInfo(name = "is_only_client")
    var isOnlyClient : Boolean = mIsOnlyClient,

    @ColumnInfo(name = "avatar_url")
    var avatarURL : String = mAvatarURL,

    var languages : List<SpeakingLanguagesModel> = mLanguages,

    var coordinates : CoordinatesModel? = mCoordinates
){
    companion object{
        @JvmStatic
        var uID: Long = (-1).toLong()

        @JvmStatic
        var uToken: String = UUID.randomUUID().toString()

        @JvmStatic
        var nUserName: String = ""

        @JvmStatic
        var uPhoneNumber: String = ""

        @JvmStatic
        var uType: String = "client"

        @JvmStatic
        var uIsWhatsapp : Boolean = false

        @JvmStatic
        var uIsViber : Boolean = false

        @JvmStatic
        var mUuid: String = UUID.randomUUID().toString()

        @JvmStatic
        var mIsOnlyClient : Boolean = true

        @JvmStatic
        var mAvatarURL : String = ""

        @JvmStatic
        var mLanguages : List<SpeakingLanguagesModel> = listOf()

        @JvmStatic
        var mCoordinates : CoordinatesModel? = CoordinatesModel()
    }
}
