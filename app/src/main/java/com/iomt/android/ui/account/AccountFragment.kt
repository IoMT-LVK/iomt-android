package com.iomt.android.ui.account

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.iomt.android.*
import com.iomt.android.entities.DeviceInfo

import com.iomt.android.entities.UserData

import java.util.*

/**
 * Fragment responsible for Account
 */
class AccountFragment : Fragment(), DeviceInfoAdapter.OnClickListener {
    private var uiHandler: Handler? = null
    private var personalInfo: LinearLayout? = null
    private var experience: LinearLayout? = null
    private var review: LinearLayout? = null
    private var btnAdd: LinearLayout? = null
    private var personalinfobtn: TextView? = null
    private var experiencebtn: TextView? = null
    private var textName: TextView? = null
    private var textWeight: TextView? = null
    private var textHeight: TextView? = null
    private var textBirthday: TextView? = null
    private var textPhone: TextView? = null
    private var textEmail: TextView? = null
    private var editPersonal: TextView? = null
    private var editContact: TextView? = null
    private var editWeight: EditText? = null
    private var editHeight: EditText? = null
    private var editBirthday: EditText? = null
    private var editPhone: EditText? = null
    private var editEmail: EditText? = null
    private var isEditingContact = false
    private var isEditingPersonal = false
    private var year = 0
    private var month = 0
    private var day = 0
    private var weight = 0
    private var height = 0
    private var phone: String? = null
    private var email: String? = null
    private var birthdate: String? = null
    private var name: String? = null
    private var surname: String? = null
    private var patr: String? = null
    private var jwt: String? = null
    private var userId: String? = null
    private val deviceInfoCells: MutableList<DeviceInfoCell> = ArrayList()
    private var deviceInfoAdapter: DeviceInfoAdapter? = null
    private var httpRequests: Requests? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uiHandler = Handler(Looper.getMainLooper())
    }

    @Suppress("TOO_MANY_LINES_IN_LAMBDA")
    private val getDataRequestCallback: (UserData) -> Unit = { userData ->
        uiHandler?.post {
            weight = userData.weight
            height = userData.height
            birthdate = userData.birthdate
            phone = userData.phoneNumber
            email = userData.email
            name = userData.name
            surname = userData.surname
            patr = userData.patronymic
            requireActivity().getSharedPreferences(requireActivity().applicationContext.getString(R.string.ACC_DATA), Context.MODE_PRIVATE).edit()
                .apply {
                    putInt("height", height)
                    putInt("weight", weight)
                    putString("phone", phone)
                    putString("email", email)
                    putString("birthdate", birthdate)
                }.apply()
            val nameLbl = "$name $surname"
            textName?.text = nameLbl
            textWeight?.text = weight.toString()
            textHeight?.text = height.toString()
            textPhone?.text = phone
            textEmail?.text = email
            textBirthday?.text = birthdate
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        setHasOptionsMenu(true)
        textName = view.findViewById(R.id.text_name)
        editPersonal = view.findViewById(R.id.edit_personal)
        editContact = view.findViewById(R.id.edit_contact)
        textWeight = view.findViewById(R.id.text_weight)
        editWeight = view.findViewById(R.id.edit_text_weight)
        textHeight = view.findViewById(R.id.text_height)
        editHeight = view.findViewById(R.id.edit_text_height)
        textPhone = view.findViewById(R.id.text_phone)
        editPhone = view.findViewById(R.id.edit_text_phone)
        textEmail = view.findViewById(R.id.text_email)
        editEmail = view.findViewById(R.id.edit_text_email)
        editWeight?.transformationMethod = null
        editHeight?.transformationMethod = null
        textBirthday = view.findViewById(R.id.text_birthdate)
        editBirthday = view.findViewById(R.id.edit_text_birthdate)
        personalInfo = view.findViewById(R.id.personalinfo)
        experience = view.findViewById(R.id.experience)
        review = view.findViewById(R.id.review)
        personalinfobtn = view.findViewById(R.id.personalinfobtn)
        experiencebtn = view.findViewById(R.id.experiencebtn)
        btnAdd = view.findViewById(R.id.button_add)
        personalInfo?.visibility = View.VISIBLE
        experience?.visibility = View.GONE
        review?.visibility = View.GONE
        val prefs = requireActivity().getSharedPreferences(
            requireContext().getString(R.string.ACC_DATA),
            Context.MODE_PRIVATE
        )
        jwt = prefs.getString("JWT", "")
        userId = prefs.getString("UserId", "")
        httpRequests = Requests(jwt, userId)
        httpRequests?.getData(getDataRequestCallback)
        update()
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler)
        recyclerView.setHasFixedSize(true)
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        deviceInfoAdapter = DeviceInfoAdapter(inflater, deviceInfoCells, this)
        recyclerView.adapter = deviceInfoAdapter
        textName?.text = "$name $surname"
        editWeight?.visibility = View.GONE
        editHeight?.visibility = View.GONE
        editBirthday?.visibility = View.GONE
        editPhone?.visibility = View.GONE
        editEmail?.visibility = View.GONE
        editBirthday?.setOnClickListener { callDatePicker() }
        editPersonal?.setOnClickListener {
            if (isEditingPersonal) {
                val ws = editWeight?.text.toString()
                weight = try {
                    ws.toInt()
                } catch (e: NumberFormatException) {
                    editWeight?.error = "Неправильный ввод"
                    -1
                }
                textWeight?.text = ws
                val hs = editHeight?.text.toString()
                height = try {
                    hs.toInt()
                } catch (e: NumberFormatException) {
                    editHeight?.error = "Неправильный ввод"
                    -1
                }
                textHeight?.text = hs
                birthdate = textBirthday?.text.toString()
                textBirthday?.text = editBirthday?.text
                editPersonal?.setText(R.string.edit)
                editWeight?.visibility = View.GONE
                editHeight?.visibility = View.GONE
                editBirthday?.visibility = View.GONE
                textWeight?.visibility = View.VISIBLE
                textHeight?.visibility = View.VISIBLE
                textBirthday?.visibility = View.VISIBLE
                isEditingPersonal = false
                view.clearFocus()
                httpRequests?.sendData(
                    name,
                    surname,
                    patr,
                    birthdate,
                    email,
                    phone,
                    weight,
                    height
                )
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(requireActivity().window.decorView.windowToken, 0)
            } else {
                editPersonal?.text = R.string.save.toString()
                editWeight?.visibility = View.VISIBLE
                editHeight?.visibility = View.VISIBLE
                editBirthday?.visibility = View.VISIBLE
                textWeight?.visibility = View.GONE
                textHeight?.visibility = View.GONE
                textBirthday?.visibility = View.GONE
                editWeight?.setText(textWeight?.text)
                editHeight?.setText(textHeight?.text)
                editBirthday?.setText(textBirthday?.text)
                isEditingPersonal = true
            }
        }
        editContact?.setOnClickListener {
            if (isEditingContact) {
                val ps = editPhone?.text.toString()
                textPhone?.text = ps
                val es = editEmail?.text.toString()
                textEmail?.text = es
                editContact?.text = R.string.edit.toString()
                editEmail?.visibility = View.GONE
                editPhone?.visibility = View.GONE
                textEmail?.visibility = View.VISIBLE
                textPhone?.visibility = View.VISIBLE
                isEditingContact = false
                view.clearFocus()
                phone = textPhone?.text.toString()
                email = textEmail?.text.toString()
                httpRequests?.sendData(
                    name,
                    surname,
                    patr,
                    birthdate,
                    email,
                    phone,
                    weight,
                    height
                )
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(requireActivity().window.decorView.windowToken, 0)
            } else {
                editContact?.setText(R.string.save)
                editEmail?.visibility = View.VISIBLE
                editPhone?.visibility = View.VISIBLE
                textEmail?.visibility = View.GONE
                textPhone?.visibility = View.GONE
                editPhone?.setText(textPhone?.text)
                editEmail?.setText(textEmail?.text)
                isEditingContact = true
            }
        }
        personalinfobtn?.setOnClickListener {
            personalInfo?.visibility = View.VISIBLE
            experience?.visibility = View.GONE
            review?.visibility = View.GONE
            personalinfobtn?.setTextColor(resources.getColor(R.color.colorPrimaryDark))
            experiencebtn?.setTextColor(resources.getColor(R.color.grey))
            update()
        }
        btnAdd?.setOnClickListener {
            val intent = Intent(context, BleSearcher::class.java)
            intent.putExtra("JWT", jwt)
            intent.putExtra("UserId", userId)
            startActivity(intent)
            update()
        }
        experiencebtn?.setOnClickListener {
            personalInfo?.visibility = View.GONE
            experience?.visibility = View.VISIBLE
            review?.visibility = View.GONE
            personalinfobtn?.setTextColor(resources.getColor(R.color.grey))
            experiencebtn?.setTextColor(resources.getColor(R.color.colorPrimaryDark))
            update()
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        update()
    }

    override fun onClickItem(deviceInfoCellCell: DeviceInfoCell?, device: DeviceInfo?) {
        // todo
    }

    private fun callDatePicker() {
        val cal = Calendar.getInstance()
        if (this.year == 0) {
            this.year = cal[Calendar.YEAR]
            this.month = cal[Calendar.MONTH]
            this.day = cal[Calendar.DAY_OF_MONTH]
        }
        val datePickerDialog = DatePickerDialog(requireContext(),
            R.style.MySpinnerDatePickerStyle,
            { _: DatePicker?,
                year: Int,
                monthOfYear: Int,
                dayOfMonth: Int ->
                val zm = if (monthOfYear + 1 >= 10) "" else "0"
                val zd = if (dayOfMonth >= 10) "" else "0"
                val editTextDateParam = zd + dayOfMonth + "." + zm + (monthOfYear + 1) + "." + year
                editBirthday?.setText(editTextDateParam)
                textBirthday?.text = editTextDateParam
                this.year = year
                this.month = monthOfYear
                this.day = dayOfMonth
            }, this.year, this.month, this.day
        )
        datePickerDialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun update() {
        httpRequests?.getData(getDataRequestCallback)
        httpRequests?.getDevices { devices ->
            uiHandler?.let {
                deviceInfoCells.clear()
                devices.forEach { deviceInfo -> deviceInfoCells.add(DeviceInfoCell(deviceInfo)) }
                deviceInfoAdapter?.notifyDataSetChanged()
            }
        }
    }
}
