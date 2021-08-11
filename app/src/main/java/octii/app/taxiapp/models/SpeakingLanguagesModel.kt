package octii.app.taxiapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import octii.app.taxiapp.locale.LocaleUtils
import octii.app.taxiapp.models.user.UserModel

@Entity
class SpeakingLanguagesModel(
    @ColumnInfo(name = "language")
    var language : String = LocaleUtils.SERBIAN,
    @ColumnInfo(name = "language")
    var userId : Long = UserModel.uID
)
