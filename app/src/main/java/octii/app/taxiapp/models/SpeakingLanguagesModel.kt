package octii.app.taxiapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import octii.app.taxiapp.LocaleUtils

@Entity
class SpeakingLanguagesModel(
    @ColumnInfo(name = "language")
    var language : String = LocaleUtils.SERBIAN
)
