package com.iomt.android.ui.account;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.iomt.android.Action;
import com.iomt.android.BLESearcher;
import com.iomt.android.DeviceInfo;
import com.iomt.android.DeviceInfoAdapter;
import com.iomt.android.DeviceInfoCell;
import com.iomt.android.HTTPRequests;
import com.iomt.android.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class AccountFragment extends Fragment implements DeviceInfoAdapter.OnClickListener {

    LinearLayout personalinfo, experience, review, btn_add;
    TextView personalinfobtn, experiencebtn, text_name, text_weight,
            text_height, text_birthday, text_phone, text_email, edit_personal, edit_contact;
    EditText edit_weight, edit_height, edit_birthday, edit_phone, edit_email;
    boolean isEditing_contact = false, isEditing_personal = false;
    private int mYear = 0, mMonth, mDay;
    private int weight, height;
    private String phone, email, birthdate, name, surname, patr, JWT, UserId;

    private List<DeviceInfoCell> deviceInfoCells = new ArrayList<>();
    private DeviceInfoAdapter deviceInfoAdapter;
    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.create();
    private Action action;
    private HTTPRequests httpRequests;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);
        setHasOptionsMenu(true);

        text_name = view.findViewById(R.id.text_name);
        edit_personal = view.findViewById(R.id.edit_personal);
        edit_contact = view.findViewById(R.id.edit_contact);

        text_weight = view.findViewById(R.id.text_weight);
        edit_weight = view.findViewById(R.id.edit_text_weight);
        text_height = view.findViewById(R.id.text_height);
        edit_height = view.findViewById(R.id.edit_text_height);
        text_phone = view.findViewById(R.id.text_phone);
        edit_phone = view.findViewById(R.id.edit_text_phone);
        text_email = view.findViewById(R.id.text_email);
        edit_email = view.findViewById(R.id.edit_text_email);
        edit_weight.setTransformationMethod(null);
        edit_height.setTransformationMethod(null);

        text_birthday = view.findViewById(R.id.text_birthdate);
        edit_birthday = view.findViewById(R.id.edit_text_birthdate);

        personalinfo = view.findViewById(R.id.personalinfo);
        experience = view.findViewById(R.id.experience);
        review = view.findViewById(R.id.review);
        personalinfobtn = view.findViewById(R.id.personalinfobtn);
        experiencebtn = view.findViewById(R.id.experiencebtn);
        btn_add = view.findViewById(R.id.button_add);
        personalinfo.setVisibility(View.VISIBLE);
        experience.setVisibility(View.GONE);
        review.setVisibility(View.GONE);


        SharedPreferences prefs = requireActivity().getSharedPreferences(requireContext().getString(R.string.ACC_DATA), MODE_PRIVATE);
        JWT = prefs.getString("JWT", "");
        UserId = prefs.getString("UserId", "");

        action  = (String[] args) -> {
            SharedPreferences.Editor editor = requireActivity().getSharedPreferences(requireActivity().getApplicationContext().getString(R.string.ACC_DATA), MODE_PRIVATE).edit();
            weight = Integer.parseInt(args[0]);
            height = Integer.parseInt(args[1]);
            birthdate = args[2];
            phone = args[3];
            email = args[4];
            name = args[5];
            surname = args[6];
            patr = args[7];

            editor.putInt("height", height);
            editor.putInt("weight", weight);
            editor.putString("phone", phone);
            editor.putString("email", email);
            editor.putString("birthdate", birthdate);
            editor.apply();

            String name_lbl = name + " " + surname;
            text_name.setText(name_lbl);
            text_weight.setText(String.valueOf(weight));
            text_height.setText(String.valueOf(height));
            text_phone.setText(phone);
            text_email.setText(email);
            text_birthday.setText(birthdate);
        };

        httpRequests = new HTTPRequests(requireContext(), JWT, UserId);
        httpRequests.getData(action);
        update();

        RecyclerView recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager _layoutManager = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(_layoutManager);
        deviceInfoAdapter = new DeviceInfoAdapter(inflater, deviceInfoCells, this);
        recyclerView.setAdapter(deviceInfoAdapter);

        String textName = name + " " + surname;
        text_name.setText(textName);
        edit_weight.setVisibility(View.GONE);
        edit_height.setVisibility(View.GONE);
        edit_birthday.setVisibility(View.GONE);
        edit_phone.setVisibility(View.GONE);
        edit_email.setVisibility(View.GONE);


        edit_birthday.setOnClickListener(v -> callDatePicker());

        edit_personal.setOnClickListener(v -> {
            if (isEditing_personal) {
                String ws = edit_weight.getText().toString();
                try {
                    weight = Integer.parseInt(ws);
                } catch (NumberFormatException e) {
                    edit_weight.setError("Неправильный ввод");
                    return;
                }
                text_weight.setText(ws);

                String hs = edit_height.getText().toString();
                try {
                    height = Integer.parseInt(hs);
                } catch (NumberFormatException e) {
                    edit_height.setError("Неправильный ввод");
                    return;
                }
                text_height.setText(hs);

                birthdate = text_birthday.getText().toString();
                text_birthday.setText(edit_birthday.getText());

                edit_personal.setText(R.string.edit);
                edit_weight.setVisibility(View.GONE);
                edit_height.setVisibility(View.GONE);
                edit_birthday.setVisibility(View.GONE);
                text_weight.setVisibility(View.VISIBLE);
                text_height.setVisibility(View.VISIBLE);
                text_birthday.setVisibility(View.VISIBLE);
                isEditing_personal = false;
                view.clearFocus();
                httpRequests.send_data(name, surname, patr, birthdate, email, phone, weight, height);
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(requireActivity().getWindow().getDecorView().getWindowToken(), 0);
            } else {
                edit_personal.setText(R.string.save);
                edit_weight.setVisibility(View.VISIBLE);
                edit_height.setVisibility(View.VISIBLE);
                edit_birthday.setVisibility(View.VISIBLE);
                text_weight.setVisibility(View.GONE);
                text_height.setVisibility(View.GONE);
                text_birthday.setVisibility(View.GONE);
                edit_weight.setText(text_weight.getText());
                edit_height.setText(text_height.getText());
                edit_birthday.setText(text_birthday.getText());
                isEditing_personal = true;

            }
        });

        edit_contact.setOnClickListener(v -> {
            if (isEditing_contact) {
                String ps = edit_phone.getText().toString();
                text_phone.setText(ps);

                String es = edit_email.getText().toString();
                text_email.setText(es);

                edit_contact.setText(R.string.edit);
                edit_email.setVisibility(View.GONE);
                edit_phone.setVisibility(View.GONE);
                text_email.setVisibility(View.VISIBLE);
                text_phone.setVisibility(View.VISIBLE);
                isEditing_contact = false;
                view.clearFocus();
                phone = text_phone.getText().toString();
                email = text_email.getText().toString();
                httpRequests.send_data(name, surname, patr, birthdate, email, phone, weight, height);
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(requireActivity().getWindow().getDecorView().getWindowToken(), 0);
            } else {
                edit_contact.setText(R.string.save);
                edit_email.setVisibility(View.VISIBLE);
                edit_phone.setVisibility(View.VISIBLE);
                text_email.setVisibility(View.GONE);
                text_phone.setVisibility(View.GONE);
                edit_phone.setText(text_phone.getText());
                edit_email.setText(text_email.getText());
                isEditing_contact = true;
            }
        });


        personalinfobtn.setOnClickListener(v -> {
            personalinfo.setVisibility(View.VISIBLE);
            experience.setVisibility(View.GONE);
            review.setVisibility(View.GONE);
            personalinfobtn.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            experiencebtn.setTextColor(getResources().getColor(R.color.grey));
            update();
        });

        btn_add.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), BLESearcher.class);
            intent.putExtra("JWT", JWT);
            intent.putExtra("UserId", UserId);
            startActivity(intent);
            update();
        });

        experiencebtn.setOnClickListener(v -> {
            personalinfo.setVisibility(View.GONE);
            experience.setVisibility(View.VISIBLE);
            review.setVisibility(View.GONE);
            personalinfobtn.setTextColor(getResources().getColor(R.color.grey));
            experiencebtn.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            update();
        });
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    @Override
    public void onClickItem(DeviceInfoCell deviceInfoCellCell, DeviceInfo device) {
        //todo
    }

    private void callDatePicker() {
        final Calendar cal = Calendar.getInstance();
        if (mYear == 0) {
            mYear = cal.get(Calendar.YEAR);
            mMonth = cal.get(Calendar.MONTH);
            mDay = cal.get(Calendar.DAY_OF_MONTH);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                R.style.MySpinnerDatePickerStyle,
                (view, year, monthOfYear, dayOfMonth) -> {
                    String zm = monthOfYear + 1 >= 10 ? "":"0";
                    String zd = dayOfMonth >= 10 ? "":"0";
                    String editTextDateParam = zd + dayOfMonth + "." + zm + (monthOfYear + 1) + "." + year;
                    edit_birthday.setText(editTextDateParam);
                    text_birthday.setText(editTextDateParam);
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }
    public void update(){
        httpRequests.getData(action);
        Action action_cells = (String[] args) -> {
            Type listOfDevices= new TypeToken<ArrayList<DeviceInfo>>(){}.getType();
            List<DeviceInfo> devs = gson.fromJson(args[0], listOfDevices);
            deviceInfoCells.clear();
            for (DeviceInfo dev: devs) {
                deviceInfoCells.add(new DeviceInfoCell(dev));
            }
            deviceInfoAdapter.notifyDataSetChanged();
        };
        httpRequests.getDevices(action_cells);
    }
}