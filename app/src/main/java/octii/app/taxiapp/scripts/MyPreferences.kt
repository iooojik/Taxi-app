package octii.app.taxiapp.scripts

import android.content.SharedPreferences

class MyPreferences {

    companion object{
        @JvmStatic
        var userPreferences : SharedPreferences? = null
        @JvmStatic
        var applicationPreferences : SharedPreferences? = null
        @JvmStatic
        var taximeterPreferences : SharedPreferences? = null

        fun saveToPreferences(preferences: SharedPreferences, key : String, value : String){
            preferences.edit().putString(key, value).apply()
        }

        fun saveToPreferences(preferences: SharedPreferences, key : String, value : Int){
            preferences.edit().putInt(key, value).apply()
        }

        fun saveToPreferences(preferences: SharedPreferences, key : String, value : Float){
            preferences.edit().putFloat(key, value).apply()
        }

        fun saveToPreferences(preferences: SharedPreferences, key : String, value : Boolean){
            preferences.edit().putBoolean(key, value).apply()
        }

        fun saveToPreferences(preferences: SharedPreferences, key : String, value : Long){
            preferences.edit().putLong(key, value).apply()
        }

        fun saveToPreferences(preferences: SharedPreferences, key : String, value : Set<String>){
            preferences.edit().putStringSet(key, value).apply()
        }

        fun clearAll(){
            userPreferences?.edit()?.clear()?.apply()
            applicationPreferences?.edit()?.clear()?.apply()
        }

        fun clearTaximeter(){
            taximeterPreferences?.edit()?.clear()?.apply()
        }
    }

}