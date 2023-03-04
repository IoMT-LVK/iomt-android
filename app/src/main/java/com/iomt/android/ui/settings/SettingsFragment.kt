package com.iomt.android.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.iomt.android.DatabaseHelper
import com.iomt.android.LoginActivity
import com.iomt.android.R

/**
 * Fragment that is responsible for settings
 */
class SettingsFragment : Fragment(), CompoundButton.OnCheckedChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        root.findViewById<SwitchCompat>(R.id.switch_mobile).setOnCheckedChangeListener(this)
        root.findViewById<LinearLayout>(R.id.leave_acc).setOnClickListener {
            DatabaseHelper(requireContext()).clear()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        (activity as AppCompatActivity).supportActionBar!!.title = "Настройки"
        return root
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        val editor = requireActivity().getSharedPreferences(
            requireActivity().applicationContext.getString(R.string.ACC_DATA),
            Context.MODE_PRIVATE
        ).edit()
        editor.putBoolean("lte", isChecked)
        editor.apply()
    }
}
