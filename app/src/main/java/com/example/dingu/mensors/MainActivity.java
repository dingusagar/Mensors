package com.example.dingu.mensors;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity implements SensorEventListener,SeekBar.OnSeekBarChangeListener{

    private SensorManager sensorManager;
    private Sensor accelerometer;
    static final String TAG = "SensorMonitor";
    TextView sensorText,thresholdText;
    Toast mToast;

    SeekBar seekBar;
    ToggleButton toggleButton;

    double prev_acc_x , prev_acc_y,prev_acc_z;
    double threshold = 0.02;

    boolean canAlert = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorText = findViewById(R.id.sensor_text);
        thresholdText = findViewById(R.id.threshold_text);
        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        toggleButton = findViewById(R.id.toggle_start_stop_service);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    startService(new Intent(getBaseContext(),SensorService.class));
                }else{
                    stopService(new Intent(getBaseContext(),SensorService.class));
                }
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG,"onCreate : Sensor listener registered");
        Toast.makeText(getApplicationContext(),"Sensor Registered",Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG,"Sensor listener registered");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sensorText.setText(String.format("Sensor Values -> x : %.2f   y : %.2f   z: %.2f",event.values[0],event.values[1],event.values[2]));
        double acc_x = event.values[0];
        double acc_y = event.values[1];
        double acc_z = event.values[2];
        boolean movement = false;
        if(Math.abs(prev_acc_x - acc_x) > threshold)
            movement = true;

        if(Math.abs(prev_acc_y - acc_y) > threshold)
            movement = true;

        if(Math.abs(prev_acc_z - acc_z) > threshold)
            movement = true;

        if(movement && canAlert) {
            canAlert = false;
            Toast.makeText(getApplicationContext(), "Phone Moved !!", Toast.LENGTH_SHORT).show();
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.alert);
            mediaPlayer.start();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }finally {
                        canAlert = true;
                    }
                }
            });
            thread.start();
        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


//    Interface -> SeekBar.OnSeekBarChangeListener
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        double min = 0.02;
        double max = 2.0;
        double value = min + ((max - min )/100 )*progress;
        thresholdText.setText(String.format("%.2f",value));
        threshold = value;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
