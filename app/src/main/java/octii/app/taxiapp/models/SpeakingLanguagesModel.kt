package octii.app.taxiapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import octii.app.taxiapp.locale.LocaleUtils

@Entity
class SpeakingLanguagesModel(
    @ColumnInfo(name = "language")
    var language : String = LocaleUtils.SERBIAN
)
