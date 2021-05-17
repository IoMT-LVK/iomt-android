package com.iomt.android;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class HTTPRequests {
    private static final String TAG = "HTTPRequests";
    private String JWT;
    private String UserId;
    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.create();
    private Context context;

    public HTTPRequests(Context ctx, String jwt, String userId) {
        context = ctx;
        JWT = jwt;
        UserId = userId;
    }

    public HTTPRequests(Context ctx) {
        context = ctx;
    }

    public void send_dev(DeviceInfo dev){
        String postUrl = context.getString(R.string.base_url) + "/devices/register/?token=" + JWT + "&user_id=" + UserId;
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JSONObject postData = new JSONObject();
        try {
            postData.put("device_id", dev.getAddress());
            postData.put("device_name", dev.getName());
            postData.put("device_type", dev.getDevice_type());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, postUrl, postData, response -> {}, error -> {
            Log.d(TAG, "HTTP Error: " + error);
            error.printStackTrace();
        });
        Log.d(TAG, "Sent");
        requestQueue.add(jsonObjectRequest);
    }

    public void del_dev(DeviceInfo dev){
        String getUrl = context.getString(R.string.base_url) + "/devices/delete/?token=" + JWT + "&user_id=" + UserId + "&id=" + dev.getAddress();
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getUrl, null, response -> {}, error -> {
            Log.d(TAG, "HTTP Error: " + error);
            error.printStackTrace();
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void getDevices(Action action){
        String getUrl = context.getString(R.string.base_url) + "/devices/get/?token=" + JWT + "&user_id=" + UserId;
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getUrl, null, response -> {
            try {
                String[] args = new String[1];
                args[0] = response.getString("devices");
                Log.d(TAG, args[0]);
                action.run(args);
            } catch (JSONException e) {
                Log.d(TAG, "JSON Error: " + Objects.requireNonNull(e.getMessage()));
                e.printStackTrace();
            }

        }, error -> {
            Log.d(TAG, "HTTP Error: " + error);
            error.printStackTrace();
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void getDeviceTypes(Action action){
        String getUrl = context.getString(R.string.base_url) + "/devices/types/?token=" + JWT + "&user_id=" + UserId;
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getUrl, null, response -> {
            try {
                String[] args = new String[1];
                args[0] = response.getString("devices");
                Log.d(TAG, "DevTypes: " + args[0]);
                action.run(args);
            } catch (JSONException e) {
                Log.d(TAG, "JSON Error: " + Objects.requireNonNull(e.getMessage()));
                e.printStackTrace();
            }

        }, error -> {
            Log.d(TAG, "HTTP Error: " + error);
            error.printStackTrace();
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void send_login(String login, String password, SuccessAction success, ErrorAction error){
        String postUrl = context.getString(R.string.base_url) + "/auth/";
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JSONObject postData = new JSONObject();
        try {
            postData.put("login", login);
            postData.put("password", password);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, postUrl, postData, response -> {
            try {
                String[] args = new String[2];
                args[0] = response.getString("jwt");
                args[1] = response.getString("user_id");
                success.run(args, response.getBoolean("confirmed"));
            } catch (JSONException e) {
                error.run();
                Log.d(TAG, "JSON Error: " + Objects.requireNonNull(e.getMessage()));
                e.printStackTrace();
            }
        }, err -> {
            error.run();
            Log.d(TAG, "HTTP Error: " + err);
            err.printStackTrace();
        });
        Log.d(TAG, "Sent");
        requestQueue.add(jsonObjectRequest);
    }

    public void send_reg(
            String name,
            String surname,
            String patronymic,
            String birthdate,
            String email,
            String mobile,
            String login,
            String password,
            Action successAction,
            ErrorAction errorAction){
        String postUrl = context.getString(R.string.base_url) + "/users/register/";
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JSONObject postData = new JSONObject();
        try {
            postData.put("name", name);
            postData.put("surname", surname);
            postData.put("patronymic", patronymic);
            postData.put("birthdate", birthdate);
            postData.put("email", email);
            postData.put("phone_number", mobile);
            postData.put("login", login);
            postData.put("password", password);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, postUrl, postData, response -> {
            String err = null;
            try {
                err = response.getString("error");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String[] args = new String[1];
            args[0] = err;
            successAction.run(args);
        }, error -> {
            errorAction.run();
            Log.d(TAG, "HTTP Error: " + error);
            error.printStackTrace();
        });
        Log.d(TAG, "Sent");
        requestQueue.add(jsonObjectRequest);
    }

    public void getData(Action action){
        String getUrl = context.getString(R.string.base_url) + "/users/info/?token=" + JWT + "&user_id=" + UserId;
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getUrl, null, response -> {
            try {
                String[] args = new String[8];
                args[0] = String.valueOf(response.getInt("weight"));
                args[1] = String.valueOf(response.getInt("height"));
                args[2] = (String) response.get("birthdate");
                args[3] = (String) response.get("phone_number");
                args[4] = (String) response.get("email");
                args[5] = (String) response.get("name");
                args[6] = (String) response.get("surname");
                args[7] = (String) response.get("patronymic");
                action.run(args);
            } catch (JSONException e) {
                Log.d(TAG, "JSON Error: " + Objects.requireNonNull(e.getMessage()));
                e.printStackTrace();
            }

        }, error -> {
            Log.d(TAG, "HTTP Error: " + error);
            error.printStackTrace();
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void send_data(String name,
                          String surname,
                          String patr,
                          String birthdate,
                          String email,
                          String phone,
                          int weight,
                          int height){
        String postUrl =  context.getString(R.string.base_url) + "/users/info/?token=" + JWT + "&user_id=" + UserId;
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject postData = new JSONObject();
        try {
            postData.put("name", name);
            postData.put("surname", surname);
            postData.put("birthdate", birthdate);
            postData.put("height", height);
            postData.put("weight", weight);
            postData.put("email", email);
            postData.put("phone_number", phone);
            postData.put("patronymic", patr);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, postUrl, postData, response -> {}, error -> {
            Log.d(TAG, "HTTP Error: " + error);
            error.printStackTrace();
        });
        Log.d(TAG, "Sent");
        requestQueue.add(jsonObjectRequest);
    }
}
