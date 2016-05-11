package com.example.murtazachunia.project220v3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 This class is responsible for the walking activity to be detected once
 the user has parked. This class creates to accelerometer sensor object
 and than using the accelerometer sensor data it detects the step acticvity.
 Once the user takes more than 4 steps it triggers a callback method onSensorUpdate
 in the mainactivity which calls the stopBleScan method which stops the scanning of
 the ble device and registers the ble device which has the strongest signal strength.
 This class provides an interface which provides a mechanism to communicate back with the class which created
 this class's object once its task is completed. Class which creates this class's object have to implement
 the interface's method.
 */
public class SensorClass extends AsyncTask<String, Void, String[]> implements SensorEventListener{

    Sensor accelerometer;
    SensorManager sensorManager;
    Sensor magnetometer;
    Context ctx;
    int globalsteps =0;
    boolean stop_detection = false;

    SensorClass(Context ctx){
        this.ctx = ctx;
    }


    public static final int MAX_BUFFER_SIZE = 5;

    private static final int Y_DATA_COUNT = 4;
    private static final double MIN_GRAVITY = 2;
    private static final double MAX_GRAVITY = 1200;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        final float[] values = sensorEvent.values;
        final Sensor sensor = sensorEvent.sensor;

        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            magneticDetector(values, sensorEvent.timestamp / (500 * 10 ^ 6l));
        }
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            accelDetector(values, sensorEvent.timestamp / (500 * 10 ^ 6l));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface stepListener{
        public void onSensorUpdate(String[] obj);
    }

    stepListener listener;

    public void setStepListener(stepListener listener){
        this.listener = listener;
    }


    protected void onPreExecute(){

        super.onPreExecute();
        sensorManager = (SensorManager) ctx.getSystemService(ctx.SENSOR_SERVICE);
        sensorManager = (SensorManager)ctx.getSystemService(ctx.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this,accelerometer,sensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,magnetometer, sensorManager.SENSOR_DELAY_FASTEST);
    }


    protected void onPostExecute(String[] obj){
        if(listener != null) {
            sensorManager.unregisterListener(this);
            listener.onSensorUpdate(obj);
        }
    }

    @Override
    protected String[] doInBackground(String... params) {

        while(!stop_detection)
        {}

        return new String[0];
    }

    private ArrayList<float[]> mAccelDataBuffer = new ArrayList<float[]>();
    private ArrayList<Long> mMagneticFireData = new ArrayList<Long>();
    private Long mLastStepTime = null;
    private ArrayList<Pair> mAccelFireData = new ArrayList<Pair>();

    private void accelDetector(float[] detectedValues, long timeStamp)
    {
        float[] currentValues = new float[3];
        for (int i = 0; i < currentValues.length; ++i)
        {
            currentValues[i] = detectedValues[i];
        }
        mAccelDataBuffer.add(currentValues);
        if (mAccelDataBuffer.size() > MAX_BUFFER_SIZE)
        {
            double avgGravity = 0;
            for (float[] values : mAccelDataBuffer)
            {
                avgGravity += Math.abs(Math.sqrt(
                        values[0] * values[0] + values[1] * values[1] + values[2] * values[2]) -    SensorManager.STANDARD_GRAVITY);
            }
            avgGravity /= mAccelDataBuffer.size();

            if (avgGravity >= MIN_GRAVITY && avgGravity < MAX_GRAVITY)
            {
                mAccelFireData.add(new Pair(timeStamp, true));
            }
            else
            {
                mAccelFireData.add(new Pair(timeStamp, false));
            }

            if (mAccelFireData.size() >= Y_DATA_COUNT)
            {
                checkData(mAccelFireData, timeStamp);

                mAccelFireData.remove(0);
            }

            mAccelDataBuffer.clear();
        }
    }

    private void checkData(ArrayList<Pair> accelFireData, long timeStamp)
    {
        boolean stepAlreadyDetected = false;

        Iterator<Pair> iterator = accelFireData.iterator();
        while (iterator.hasNext() && !stepAlreadyDetected)
        {
            stepAlreadyDetected = iterator.next().first.equals(mLastStepTime);
        }
        if (!stepAlreadyDetected)
        {
            int firstPosition = Collections.binarySearch( mMagneticFireData, accelFireData.get(0).first);
            int secondPosition = Collections
                    .binarySearch(mMagneticFireData, accelFireData.get(accelFireData.size() - 1).first - 1);

            if (firstPosition > 0 || secondPosition > 0 || firstPosition != secondPosition)
            {
                if (firstPosition < 0)
                {
                    firstPosition = -firstPosition - 1;
                }
                if (firstPosition < mMagneticFireData.size() && firstPosition > 0)
                {
                    mMagneticFireData = new ArrayList<Long>(
                            mMagneticFireData.subList(firstPosition - 1, mMagneticFireData.size()));
                }

                iterator = accelFireData.iterator();
                while (iterator.hasNext())
                {
                    if (iterator.next().second)
                    {
                        mLastStepTime = timeStamp;
                        accelFireData.remove(accelFireData.size() - 1);
                        accelFireData.add(new Pair(timeStamp, false));
                        // onStep();
                        globalsteps = globalsteps + 1;
                        if(globalsteps > 3)
                            stop_detection = true;
                        //step.setText("STEP TAKEN : " +globalsteps);

                        break;
                    }
                }
            }
        }
    }

    private float mLastDirections;
    private float mLastValues;
    private float mLastExtremes[] = new float[2];
    private Integer mLastType;
    private ArrayList<Float> mMagneticDataBuffer = new ArrayList<Float>();

    private void magneticDetector(float[] values, long timeStamp)
    {
        mMagneticDataBuffer.add(values[2]);

        if (mMagneticDataBuffer.size() > MAX_BUFFER_SIZE)
        {
            float avg = 0;

            for (int i = 0; i < mMagneticDataBuffer.size(); ++i)
            {
                avg += mMagneticDataBuffer.get(i);
            }

            avg /= mMagneticDataBuffer.size();

            float direction = (avg > mLastValues ? 1 : (avg < mLastValues ? -1 : 0));
            if (direction == -mLastDirections)
            {
                // Direction changed
                int extType = (direction > 0 ? 0 : 1); // minumum or maximum?
                mLastExtremes[extType] = mLastValues;
                float diff = Math.abs(mLastExtremes[extType] - mLastExtremes[1 - extType]);

                if (diff > 8 && (null == mLastType || mLastType != extType))
                {
                    mLastType = extType;

                    mMagneticFireData.add(timeStamp);
                }
            }
            mLastDirections = direction;
            mLastValues = avg;

            mMagneticDataBuffer.clear();
        }
    }



    public static class Pair implements Serializable
    {
        Long first;
        boolean second;

        public Pair(long first, boolean second)
        {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof Pair)
            {
                return first.equals(((Pair) o).first);
            }
            return false;
        }
    }

}