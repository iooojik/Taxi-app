package octii.app.taxiapp.ui.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.navigation.fragment.findNavController
import com.google.android.material.shape.RoundedCornerTreatment
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import octii.app.taxiapp.R
import octii.app.taxiapp.Static
import octii.app.taxiapp.databinding.FragmentDriverSettingsBinding
import octii.app.taxiapp.models.driverAvailable.DriverAvailable
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.scripts.logError
import octii.app.taxiapp.web.HttpHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DriverSettingsFragment : Fragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private lateinit var binding : FragmentDriverSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDriverSettingsBinding.inflate(layoutInflater)
        binding.becomeClient.setOnClickListener(this)
        binding.fabBack.setOnClickListener(this)
        binding.working.setOnCheckedChangeListener(this)
        binding.driverName.text = UserModel.nUserName
        binding.driverPhone.text = UserModel.uPhoneNumber
        if (UserModel.mAvatarURL.isNotEmpty()){
            Picasso.with(requireContext())
                .load(UserModel.mAvatarURL)
                .transform(CircularTransformation(0f))
                .into(binding.driverAvatar)
        } else {
            binding.driverAvatar.setImageResource(R.drawable.outline_account_circle_24)
        }
        getDriver()
        return binding.root
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.become_client -> {
                UserModel.uType = Static.CLIENT_TYPE
                updateDriver()
            }
            R.id.button_russian_language -> {

            }
            R.id.button_english_language -> {

            }
            R.id.button_serbian_language -> {

            }
            R.id.fab_back -> {
                updateDriver()
                findNavController().navigate(R.id.driverMapFragment)
            }
        }
    }

    private fun updateDriver(){
        HttpHelper.USER_API.update(UserModel()).enqueue(object : Callback<UserModel>{
            override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                if (response.isSuccessful){
                    val model = response.body()
                    if (model != null){
                        if (model.type == Static.CLIENT_TYPE) findNavController().navigate(R.id.clientSettingsFragment)
                    }
                } else {
                    HttpHelper.errorProcessing(binding.root, response.errorBody())
                }
            }

            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                HttpHelper.onFailure(t)
            }
        })
        HttpHelper.DRIVER_AVAILABLE_API.update(DriverAvailable(
            pricePerKm = binding.pricePerKm.editText?.text.toString().toFloat(),
            pricePerMinute = binding.pricePerMin.editText?.text.toString().toFloat(),
            priceWaitingMin = binding.priceWaiting.editText?.text.toString().toFloat(),
            rideDistance = binding.maxDistance.editText?.text.toString().toFloat(),
        )).enqueue(object : Callback<DriverAvailable>{
            override fun onResponse(
                call: Call<DriverAvailable>,
                response: Response<DriverAvailable>,
            ) {
                if (response.isSuccessful){
                    val model = response.body()
                    if (model != null){
                        logError(model)
                        changeStaticInfo(model)
                    }
                } else {
                    HttpHelper.errorProcessing(binding.root, response.errorBody())
                }
            }

            override fun onFailure(call: Call<DriverAvailable>, t: Throwable) {
                HttpHelper.onFailure(t)
            }

        })
    }

    private fun getDriver(){
        HttpHelper.DRIVER_AVAILABLE_API.getDriver(DriverAvailable(driverID = UserModel.uID)).enqueue(object : Callback<DriverAvailable>{
            override fun onResponse(
                call: Call<DriverAvailable>,
                response: Response<DriverAvailable>,
            ) {
                if (response.isSuccessful){
                    val model = response.body()
                    if (model != null){
                        changeStaticInfo(model)
                        updateUiInfo()
                    }
                } else {
                    HttpHelper.errorProcessing(binding.root, response.errorBody())
                }
            }

            override fun onFailure(call: Call<DriverAvailable>, t: Throwable) {
                HttpHelper.onFailure(t)
            }

        })
    }

    private fun changeStaticInfo(newDriver : DriverAvailable?){
        if (newDriver != null){
            DriverAvailable.mDriver = newDriver.driver
            DriverAvailable.mDriverID = newDriver.driverID
            DriverAvailable.mId = newDriver.id
            DriverAvailable.mIsWorking = newDriver.isWorking
            DriverAvailable.mPricePerKm = newDriver.pricePerKm
            DriverAvailable.mPricePerMinute = newDriver.pricePerMinute
            DriverAvailable.mPriceWaitingMin = newDriver.pricePerMinute
            DriverAvailable.mRideDistance = newDriver.rideDistance
        }
    }

    private fun updateUiInfo() {
        binding.pricePerKm.editText?.setText(DriverAvailable.mPricePerKm.toString())
        binding.pricePerMin.editText?.setText(DriverAvailable.mPricePerMinute.toString())
        binding.priceWaiting.editText?.setText(DriverAvailable.mPriceWaitingMin.toString())
        binding.maxDistance.editText?.setText(DriverAvailable.mRideDistance.toString())
        binding.working.isChecked = DriverAvailable.mIsWorking
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView!!.id){
            R.id.working -> {
                DriverAvailable.mIsWorking = isChecked
                updateDriver()
            }
        }
    }

}