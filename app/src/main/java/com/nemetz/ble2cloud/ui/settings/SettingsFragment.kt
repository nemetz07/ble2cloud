package com.nemetz.ble2cloud.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.nemetz.ble2cloud.BLE2CloudApplication
import com.nemetz.ble2cloud.MainActivity
import com.nemetz.ble2cloud.R
import com.nemetz.ble2cloud.service.DataCollectionService
import kotlinx.android.synthetic.main.settings_fragment.*

class SettingsFragment : Fragment() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private lateinit var viewModel: SettingsViewModel
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)

        init()
    }

    private fun init() {

        firebaseAuth = FirebaseAuth.getInstance()

        settingsSignoutButton.setOnClickListener {
            context?.let { it1 ->
                AuthUI.getInstance().signOut(it1).addOnCompleteListener {
                    Log.d("SETTINGS", "Signed out")
                    if ((context!!.applicationContext as BLE2CloudApplication).isServiceRunning.value == true) {
                        Intent(context, DataCollectionService::class.java).apply { action = "STOP" }
                            .also { context!!.startService(it) }
                    }
                    val mainActivityIntent = Intent(context, MainActivity::class.java)
                    startActivity(Intent.makeRestartActivityTask(mainActivityIntent.component))
                }
            }
        }

        settingsUserNameTV.text = firebaseAuth.currentUser?.displayName

        settingsEmailTV.text = firebaseAuth.currentUser?.email

        context?.let { Glide.with(it) }?.load(firebaseAuth.currentUser?.photoUrl)
            ?.into(settingsProfileImageView)
    }

}
