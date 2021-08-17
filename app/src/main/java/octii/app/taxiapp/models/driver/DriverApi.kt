package octii.app.taxiapp.models.driver

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface DriverApi {

    //@POST("/drivers.available/driver.get")
    @POST("/taxi/drivers.available/driver.get")
    fun getDriver(@Body driver: DriverModel): Call<DriverModel>

    //@POST("/drivers.available/driver.update")
    @POST("/taxi/drivers.available/driver.update")
    fun update(@Body driver: DriverModel): Call<DriverModel>

}