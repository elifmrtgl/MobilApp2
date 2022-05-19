package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    
    TextView textViewId, xValue, yValue, zValue;
    SensorManager sensorManager;
    Sensor lightSensor, accelerometer;
    private float[] mGravity;
    private double mAccel;
    private double mAccelCurrent;
    private double mAccelLast;
    private int hitCounter = 0;
    private double hitSummation = 0;
    private double hitRes = 0;
    private final int SAMPLE_SIZE = 50;
    private final double THRESHOLD = 0.2;
    private boolean lightsOn=false;
    private boolean isMoving=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewId = findViewById(R.id.tvId);
        xValue = findViewById(R.id.xValue);
        yValue = findViewById(R.id.yValue);
        zValue = findViewById(R.id.zValue);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Intent intent = new Intent("com.example.application.BroadcastSensor");
        if(sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT){
            if(sensorEvent.values[0] > 0){
                lightsOn=true;
            }else{
                lightsOn=false;
            }
        }
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){

            mGravity = sensorEvent.values.clone();
            double x = mGravity[0];
            double y = mGravity[1];
            double z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = Math.sqrt(x * x + y * y + z * z);
            double delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            if (hitCounter <= SAMPLE_SIZE) {
                hitCounter++;
                hitSummation += Math.abs(mAccel);
            } else {
                hitRes = hitSummation / SAMPLE_SIZE;

                Log.d(TAG, String.valueOf(hitRes));

                if (hitRes > THRESHOLD) {
                    isMoving=true;

                    xValue.setText("xValue: " + sensorEvent.values[0]);
                    yValue.setText("yValue: " + sensorEvent.values[1]);
                    zValue.setText("zValue: " + sensorEvent.values[2]);
                } else {
                    isMoving=false;
                }
                hitCounter = 0;
                hitSummation = 0;
                hitRes = 0;
            }
        }


            if(isMoving==true){
                textViewId.setText("Telefon cepte.");
                if(lightsOn==false){
                    intent.putExtra("situation", "on");
                }
            }else{
                if(lightsOn==true){
                    textViewId.setText("Telefon masada ve yüz üstü.");
                    intent.putExtra("situation", "on");
                }else{
                    textViewId.setText("Telefon masada ve sırt üstü.");
                    intent.putExtra("situation", "off");

                }
            }

        sendBroadcast(intent);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}