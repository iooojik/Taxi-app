package octii.app.taxiapp.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.Switch
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import octii.app.taxiapp.MyPreferences
import octii.app.taxiapp.R
import octii.app.taxiapp.Static
import octii.app.taxiapp.databinding.FragmentUserInfoBinding
import octii.app.taxiapp.models.UserApi
import octii.app.taxiapp.models.UserModel
import octii.app.taxiapp.web.HttpHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class UserInfoFragment : Fragment(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {

    lateinit var binding : FragmentUserInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserInfoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.loginButton.setOnClickListener(this)
        binding.iAmInWhatsapp.setOnCheckedChangeListener(this)
        binding.iAmInViber.setOnCheckedChangeListener(this)
        binding.iAmDriver.setOnCheckedChangeListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.login_button -> {
                val phoneNum = binding.phoneNumberLayout.editText?.text.toString()
                val name = binding.nameLayout.editText?.text.toString()
                if ((binding.iAmInWhatsapp.isChecked || binding.iAmInViber.isChecked) && phoneNum.isNotEmpty() && name.isNotEmpty()) {
                    HttpHelper.USER_API.login(UserModel(phone = phoneNum, userName = name)).enqueue(object : Callback<UserModel> {
                        override fun onResponse(
                            call: Call<UserModel>,
                            response: Response<UserModel>
                        ) {
                            if (response.isSuccessful) {
                                val model = response.body()
                                if (model != null) {
                                    UserModel.uIsViber = model.isViber
                                    UserModel.uIsWhatsapp = model.isWhatsapp
                                    UserModel.uType = model.type
                                    UserModel.uPhoneNumber = model.phone
                                    UserModel.uToken = model.token
                                    UserModel.nUserName = model.userName
                                    MyPreferences.userPreferences?.let {
                                        MyPreferences.saveToPreferences(
                                            it, Static.SHARED_PREFERENCES_USER_TOKEN, model.token
                                        )
                                    }
                                    findNavController().navigate(R.id.clientMapFragment)
                                }
                            } else HttpHelper.errorProcessing(binding.root, response.errorBody())
                        }

                        override fun onFailure(call: Call<UserModel>, t: Throwable) {
                            HttpHelper.onFailure(t)
                        }
                    })
                } else {
                    Snackbar.make(binding.root, resources.getString(R.string.whatsapp_or_viber_not_chosen), Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView!!.id){
            R.id.i_am_in_whatsapp -> {
                UserModel.uIsWhatsapp = isChecked
            }
            R.id.i_am_in_viber -> {
                UserModel.uIsViber = isChecked
            }
            R.id.i_am_driver -> {
                if (isChecked) UserModel.uType = Static.DRIVER_TYPE
                else UserModel.uType = Static.CLIENT_TYPE
            }
        }
    }

}