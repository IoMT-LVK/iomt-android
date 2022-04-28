package com.iomt.android.ui.account

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
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.iomt.android.*
import java.util.*

class AccountFragment : Fragment(), DeviceInfoAdapter.OnClickListener {
    private var uiHandler: Handler? = null
    var personalinfo: LinearLayout? = null
    var experience: LinearLayout? = null
    var review: LinearLayout? = null
    var btn_add: LinearLayout? = null
    var personalinfobtn: TextView? = null
    var experiencebtn: TextView? = null
    var text_name: TextView? = null
    var text_weight: TextView? = null
    var text_height: TextView? = null
    var text_birthday: TextView? = null
    var text_phone: TextView? = null
    var text_email: TextView? = null
    var edit_personal: TextView? = null
    var edit_contact: TextView? = null
    var edit_weight: EditText? = null
    var edit_height: EditText? = null
    var edit_birthday: EditText? = null
    var edit_phone: EditText? = null
    var edit_email: EditText? = null
    var isEditing_contact = false
    var isEditing_personal = false
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var weight = 0
    private var height = 0
    private var phone: String? = null
    private var email: String? = null
    private var birthdate: String? = null
    private var name: String? = null
    private var surname: String? = null
    private var patr: String? = null
    private var JWT: String? = null
    private var UserId: String? = null
    private val deviceInfoCells: MutableList<DeviceInfoCell> = ArrayList()
    private var deviceInfoAdapter: DeviceInfoAdapter? = null
    private val builder = GsonBuilder()
    private val gson = builder.create()
    private var action: Action? = null
    private var httpRequests: HTTPRequests? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uiHandler = Handler(Looper.getMainLooper())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        setHasOptionsMenu(true)
        text_name = view.findViewById(R.id.text_name)
        edit_personal = view.findViewById(R.id.edit_personal)
        edit_contact = view.findViewById(R.id.edit_contact)
        text_weight = view.findViewById(R.id.text_weight)
        edit_weight = view.findViewById(R.id.edit_text_weight)
        text_height = view.findViewById(R.id.text_height)
        edit_height = view.findViewById(R.id.edit_text_height)
        text_phone = view.findViewById(R.id.text_phone)
        edit_phone = view.findViewById(R.id.edit_text_phone)
        text_email = view.findViewById(R.id.text_email)
        edit_email = view.findViewById(R.id.edit_text_email)
        edit_weight?.transformationMethod = null
        edit_height?.transformationMethod = null
        text_birthday = view.findViewById(R.id.text_birthdate)
        edit_birthday = view.findViewById(R.id.edit_text_birthdate)
        personalinfo = view.findViewById(R.id.personalinfo)
        experience = view.findViewById(R.id.experience)
        review = view.findViewById(R.id.review)
        personalinfobtn = view.findViewById(R.id.personalinfobtn)
        experiencebtn = view.findViewById(R.id.experiencebtn)
        btn_add = view.findViewById(R.id.button_add)
        personalinfo?.visibility = View.VISIBLE
        experience?.visibility = View.GONE
        review?.visibility = View.GONE
        val prefs = requireActivity().getSharedPreferences(
            requireContext().getString(R.string.ACC_DATA),
            Context.MODE_PRIVATE
        )
        JWT = prefs.getString("JWT", "")
        UserId = prefs.getString("UserId", "")
        action = Action { args: Array<String?>? ->
            uiHandler?.post {
                val editor = requireActivity().getSharedPreferences(
                    requireActivity().applicationContext.getString(R.string.ACC_DATA),
                    Context.MODE_PRIVATE
                ).edit()
                weight = args!![0]!!.toInt()
                height = args[1]!!.toInt()
                birthdate = args[2]
                phone = args[3]
                email = args[4]
                name = args[5]
                surname = args[6]
                patr = args[7]
                editor.putInt("height", height)
                editor.putInt("weight", weight)
                editor.putString("phone", phone)
                editor.putString("email", email)
                editor.putString("birthdate", birthdate)
                editor.apply()
                val name_lbl = "$name $surname"
                text_name?.text = name_lbl
                text_weight?.text = weight.toString()
                text_height?.text = height.toString()
                text_phone?.text = phone
                text_email?.text = email
                text_birthday?.text = birthdate
            }
        }
        httpRequests = HTTPRequests(requireContext(), JWT, UserId)
        httpRequests!!.getData(action!!)
        update()
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler)
        recyclerView.setHasFixedSize(true)
        val _layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recyclerView.layoutManager = _layoutManager
        deviceInfoAdapter = DeviceInfoAdapter(inflater, deviceInfoCells, this)
        recyclerView.adapter = deviceInfoAdapter
        val textName = "$name $surname"
        text_name?.text = textName
        edit_weight?.visibility = View.GONE
        edit_height?.visibility = View.GONE
        edit_birthday?.visibility = View.GONE
        edit_phone?.visibility = View.GONE
        edit_email?.visibility = View.GONE
        edit_birthday?.setOnClickListener { v: View? -> callDatePicker() }
        edit_personal?.setOnClickListener { v: View? ->
            if (isEditing_personal) {
                val ws = edit_weight?.text.toString()
                weight = try {
                    ws.toInt()
                } catch (e: NumberFormatException) {
                    edit_weight?.error = "Неправильный ввод"
                    -1
                }
                text_weight?.text = ws
                val hs = edit_height?.text.toString()
                height = try {
                    hs.toInt()
                } catch (e: NumberFormatException) {
                    edit_height?.error = "Неправильный ввод"
                    -1
                }
                text_height?.text = hs
                birthdate = text_birthday?.text.toString()
                text_birthday?.text = edit_birthday?.text
                edit_personal?.setText(R.string.edit)
                edit_weight?.visibility = View.GONE
                edit_height?.visibility = View.GONE
                edit_birthday?.visibility = View.GONE
                text_weight?.visibility = View.VISIBLE
                text_height?.visibility = View.VISIBLE
                text_birthday?.visibility = View.VISIBLE
                isEditing_personal = false
                view.clearFocus()
                httpRequests!!.sendData(
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
                edit_personal?.text = R.string.save.toString()
                edit_weight?.visibility = View.VISIBLE
                edit_height?.visibility = View.VISIBLE
                edit_birthday?.visibility = View.VISIBLE
                text_weight?.visibility = View.GONE
                text_height?.visibility = View.GONE
                text_birthday?.visibility = View.GONE
                edit_weight?.setText(text_weight?.text)
                edit_height?.setText(text_height?.text)
                edit_birthday?.setText(text_birthday?.text)
                isEditing_personal = true
            }
        }
        edit_contact?.setOnClickListener { v: View? ->
            if (isEditing_contact) {
                val ps = edit_phone?.text.toString()
                text_phone?.text = ps
                val es = edit_email?.text.toString()
                text_email?.text = es
                edit_contact?.text = R.string.edit.toString()
                edit_email?.visibility = View.GONE
                edit_phone?.visibility = View.GONE
                text_email?.visibility = View.VISIBLE
                text_phone?.visibility = View.VISIBLE
                isEditing_contact = false
                view.clearFocus()
                phone = text_phone?.text.toString()
                email = text_email?.text.toString()
                httpRequests!!.sendData(
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
                edit_contact?.setText(R.string.save)
                edit_email?.visibility = View.VISIBLE
                edit_phone?.visibility = View.VISIBLE
                text_email?.visibility = View.GONE
                text_phone?.visibility = View.GONE
                edit_phone?.setText(text_phone?.text)
                edit_email?.setText(text_email?.text)
                isEditing_contact = true
            }
        }
        personalinfobtn?.setOnClickListener {
            personalinfo?.visibility = View.VISIBLE
            experience?.visibility = View.GONE
            review?.visibility = View.GONE
            personalinfobtn?.setTextColor(resources.getColor(R.color.colorPrimaryDark))
            experiencebtn?.setTextColor(resources.getColor(R.color.grey))
            update()
        }
        btn_add?.setOnClickListener { v: View? ->
            val intent = Intent(context, BLESearcher::class.java)
            intent.putExtra("JWT", JWT)
            intent.putExtra("UserId", UserId)
            startActivity(intent)
            update()
        }
        experiencebtn?.setOnClickListener { v: View? ->
            personalinfo?.visibility = View.GONE
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
        //todo
    }

    private fun callDatePicker() {
        val cal = Calendar.getInstance()
        if (mYear == 0) {
            mYear = cal[Calendar.YEAR]
            mMonth = cal[Calendar.MONTH]
            mDay = cal[Calendar.DAY_OF_MONTH]
        }
        val datePickerDialog = DatePickerDialog(requireContext(),
            R.style.MySpinnerDatePickerStyle,
            { _: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val zm = if (monthOfYear + 1 >= 10) "" else "0"
                val zd = if (dayOfMonth >= 10) "" else "0"
                val editTextDateParam = zd + dayOfMonth + "." + zm + (monthOfYear + 1) + "." + year
                edit_birthday!!.setText(editTextDateParam)
                text_birthday!!.text = editTextDateParam
                mYear = year
                mMonth = monthOfYear
                mDay = dayOfMonth
            }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
    }

    private fun update() {
        httpRequests!!.getData(action!!)
        val actionCells = Action { args: Array<String?>? ->
            uiHandler?.post {
                val listOfDevices = object : TypeToken<ArrayList<DeviceInfo?>?>() {}.type
                val devs = gson.fromJson<List<DeviceInfo>>(
                    args!![0], listOfDevices
                )
                deviceInfoCells.clear()
                for (dev in devs) {
                    deviceInfoCells.add(DeviceInfoCell(dev))
                }
                deviceInfoAdapter!!.notifyDataSetChanged()
            }
        }
        httpRequests!!.getDevices(actionCells)
    }
}