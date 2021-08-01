package octii.app.taxiapp.models.driverAvailable

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface DriverAvailableApi {

    @POST("/drivers.available/driver.get")
    fun getDriver(@Body driver : DriverAvailable) : Call<DriverAvailable>

    @POST("/drivers.available/driver.update")
    fun update(@Body driver : DriverAvailable) : Call<DriverAvailable>

}