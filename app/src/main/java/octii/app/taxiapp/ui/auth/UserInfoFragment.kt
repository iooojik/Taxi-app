package octii.app.taxiapp.ui.auth

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import octii.app.taxiapp.R
import octii.app.taxiapp.Static
import octii.app.taxiapp.databinding.FragmentUserInfoBinding
import octii.app.taxiapp.models.user.UserModel
import octii.app.taxiapp.sockets.SocketService
import octii.app.taxiapp.web.requests.Requests
import kotlin.concurrent.thread


class UserInfoFragment : Fragment(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {

    lateinit var binding : FragmentUserInfoBinding
    private lateinit var requests: Requests

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserInfoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requests = Requests(requireView(), requireActivity())
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

                if (!(binding.iAmInWhatsapp.isChecked || binding.iAmInViber.isChecked)) {
                    Snackbar.make(binding.root,
                        resources.getString(R.string.whatsapp_or_viber_not_chosen),
                        Snackbar.LENGTH_SHORT).show()
                } else if (phoneNum.isEmpty()) {
                    Snackbar.make(binding.root,
                        resources.getString(R.string.no_phone),
                        Snackbar.LENGTH_SHORT).show()
                } else if (phoneNum.isEmpty()) {
                    Snackbar.make(binding.root,
                        resources.getString(R.string.no_name),
                        Snackbar.LENGTH_SHORT).show()
                } else {
                    thread {
                        val user = requests.userRequests.login(phoneNum, name)
                        if (user.token.isNotEmpty()) {
                            startSocketService()
                            requireActivity().runOnUiThread {
                                if (user.type == Static.DRIVER_TYPE)
                                    findNavController().navigate(R.id.driverMapFragment)
                                else findNavController().navigate(R.id.clientMapFragment)
                            }
                        }
                    }

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

    private fun startSocketService(){
        //создание намерения, которое будет запущено
        val intentService = Intent(requireActivity(), SocketService::class.java)
        //запуск сервиса. Если метод возвращает true, то сервис был запущен,
        // если сервис был остановлен, то false
        touchService(intentService)
    }

    private fun touchService(intentService : Intent) : Boolean {
        return if (!isMyServiceRunning()) {requireActivity().startService(intentService); true}
        else {requireActivity().stopService(intentService); false}
    }

    private fun isMyServiceRunning(): Boolean {
        val manager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (SocketService::javaClass.name == service.service.className) {
                return true
            }
        }
        return false

    }

}