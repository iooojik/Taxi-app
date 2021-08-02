package octii.app.taxiapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import octii.app.taxiapp.MyPreferences
import octii.app.taxiapp.R
import octii.app.taxiapp.databinding.FragmentAuthMessengersBinding


class AuthMessengersFragment : Fragment(), View.OnClickListener {

    lateinit var binding : FragmentAuthMessengersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthMessengersBinding.inflate(layoutInflater)
        MyPreferences.clearAll()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.nextButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.next_button -> findNavController().navigate(R.id.userInfoFragment)

        }
    }

}