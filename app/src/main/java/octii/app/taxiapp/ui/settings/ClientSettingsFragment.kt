package octii.app.taxiapp.ui.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import octii.app.taxiapp.R
import octii.app.taxiapp.Static
import octii.app.taxiapp.databinding.FragmentClientSettingsBinding
import octii.app.taxiapp.models.driverAvailable.DriverAvailable
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.web.HttpHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ClientSettingsFragment : Fragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    lateinit var binding : FragmentClientSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentClientSettingsBinding.inflate(layoutInflater)
        if (!UserModel.mIsOnlyClient) {
            binding.becomeDriver.visibility = View.VISIBLE
            binding.becomeDriver.setOnClickListener(this)
            binding.fabBack.setOnClickListener(this)
        }
        binding.clientName.text = UserModel.nUserName
        binding.clientPhone.text = UserModel.uPhoneNumber
        binding.iAmInViber.setOnCheckedChangeListener(this)
        binding.iAmInWhatsapp.setOnCheckedChangeListener(this)
        binding.iAmInViber.isChecked = UserModel.uIsViber
        binding.iAmInWhatsapp.isChecked = UserModel.uIsWhatsapp

        if (UserModel.mAvatarURL.isNotEmpty()){
            Picasso.with(requireContext())
                .load(UserModel.mAvatarURL)
                .transform(CircularTransformation(0f))
                .into(binding.driverAvatar)
        } else {
            binding.driverAvatar.setImageResource(R.drawable.outline_account_circle_24)
        }

        return binding.root
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.become_driver -> {
                if (UserModel.uIsViber && UserModel.uIsWhatsapp) {
                    UserModel.uType = Static.DRIVER_TYPE
                    updateClient()
                } else {
                    Snackbar.make(requireView(),
                        resources.getString(R.string.to_become_driver), Snackbar.LENGTH_SHORT).show()
                }
            }
            R.id.fab_back -> {
                findNavController().navigate(R.id.clientMapFragment)
            }
        }
    }

    private fun updateClient(){
        HttpHelper.USER_API.update(UserModel()).enqueue(object : Callback<UserModel> {
            override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                if (response.isSuccessful){
                    if (response.body() != null){
                        if (response.body()!!.type == Static.DRIVER_TYPE){
                            findNavController().navigate(R.id.driverSettingsFragment)
                        }
                    }

                } else {
                    HttpHelper.errorProcessing(binding.root, response.errorBody())
                }
            }

            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                HttpHelper.onFailure(t)
            }
        })
    }



    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView!!.id){
            R.id.i_am_in_viber -> {
                UserModel.uIsViber = isChecked
                updateClient()
            }
            R.id.i_am_in_whatsapp -> {
                UserModel.uIsWhatsapp = isChecked
                updateClient()
            }
        }
    }

}