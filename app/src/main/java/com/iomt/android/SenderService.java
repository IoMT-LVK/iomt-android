package com.iomt.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SenderService {

    private String TAG = "SenderService";
    private String url = "tcp://iomt.lvk.cs.msu.su:8883";
    private DatabaseHelper dbhelper;
    private Handler handler;
    private int delay;
    private Context context;


    public SenderService(Context ctx, int mdelay) {
        Log.d(TAG, "START");
        delay = mdelay;
        context = ctx;
        handler = new Handler(Looper.getMainLooper());
    }

    public void start() {
        handler.postDelayed(runnableCode, 5000);
    }

    public void stop() {
        handler.removeCallbacksAndMessages(null);
    }

    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {

            boolean connected = false;
            ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                connected= activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
            }

            if (!connected) {
                return;
            }

            MqttAndroidClient mqttAndroidClient = new MqttAndroidClient(context, url, "");
            mqttAndroidClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d(TAG, "Connection was lost!");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    Log.d(TAG, "Message Arrived!: " + topic + ": " + new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Delivery Complete!");
                }
            });


            dbhelper = new DatabaseHelper(context);
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor cursor = db.query(Note.TABLE_NAME, null, null, null, null, null, Note.COLUMN_TIMESTAMP, null);

            // looping through all rows and adding to list
            List<JSONObject> res = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            int cnt = 0;
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    cnt++;
                    JSONObject result = new JSONObject();
                    try {
                        result.put("HeartRate", cursor.isNull(cursor.getColumnIndex(Note.COLUMN_HEART_RATE)) ? JSONObject.NULL : cursor.getInt(cursor.getColumnIndex(Note.COLUMN_HEART_RATE)));
                        result.put("RespRate", cursor.isNull(cursor.getColumnIndex(Note.COLUMN_RESP_RATE)) ? JSONObject.NULL : cursor.getInt(cursor.getColumnIndex(Note.COLUMN_RESP_RATE)));
                        result.put("Insp", cursor.isNull(cursor.getColumnIndex(Note.COLUMN_INSP)) ? JSONObject.NULL : cursor.getFloat(cursor.getColumnIndex(Note.COLUMN_INSP)));
                        result.put("Exp", cursor.isNull(cursor.getColumnIndex(Note.COLUMN_EXP)) ? JSONObject.NULL : cursor.getFloat(cursor.getColumnIndex(Note.COLUMN_EXP)));
                        result.put("Cadence", cursor.isNull(cursor.getColumnIndex(Note.COLUMN_CADENCE)) ? JSONObject.NULL : cursor.getInt(cursor.getColumnIndex(Note.COLUMN_CADENCE)));
                        result.put("Steps", cursor.isNull(cursor.getColumnIndex(Note.COLUMN_STEP_COUNT)) ? JSONObject.NULL : cursor.getInt(cursor.getColumnIndex(Note.COLUMN_STEP_COUNT)));
                        result.put("Activity", cursor.isNull(cursor.getColumnIndex(Note.COLUMN_ACT)) ? JSONObject.NULL : cursor.getFloat(cursor.getColumnIndex(Note.COLUMN_ACT)));
                        result.put("Clitime", cursor.isNull(cursor.getColumnIndex(Note.COLUMN_CLI)) ? JSONObject.NULL : cursor.getString(cursor.getColumnIndex(Note.COLUMN_CLI)));
                        result.put("Millisec", cursor.isNull(cursor.getColumnIndex(Note.COLUMN_MIL)) ? JSONObject.NULL : cursor.getInt(cursor.getColumnIndex(Note.COLUMN_MIL)));
                        res.add(result);
                        ids.add(cursor.getInt(cursor.getColumnIndex(Note.COLUMN_ID)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }

            send_data(mqttAndroidClient, res, ids);
            Log.d(TAG, String.valueOf(cnt));
            // close db connection
            db.close();
            handler.postDelayed(this, delay);
        }
    };

    public void send_data(final MqttAndroidClient mqttAndroidClient, final List<JSONObject> data, List<Integer> ids) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.ACC_DATA), MODE_PRIVATE);
        String JWT = prefs.getString("JWT", "");
        String UserId = prefs.getString("UserId", "");
        String DeviceId = prefs.getString("DeviceId", "");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("username");
        options.setPassword(JWT.toCharArray());
        try {
            mqttAndroidClient.connect(options, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Connection Success!");

                    try {
                        for (int i = 0; i < data.size(); i++) {
                            String data_str = data.get(i).toString();
                            MqttMessage message = new MqttMessage(data_str.getBytes());
                            Log.d(TAG, "Publishing message" + message);
                            message.setQos(2);
                            message.setRetained(false);
                            mqttAndroidClient.publish("c/" + UserId + "/" + DeviceId + "/data", message);
                            SenderService.this.dbhelper.deleteNote(ids.get(i));
                            Thread.sleep(200);
                        }
                    } catch (MqttException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Connection Failure!");
                    Log.d(TAG, String.valueOf(exception));
                }
            });
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }
}
