package demo.yl.sensor;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;

import demo.yl.sensor.BlueToothLeService.BluetoothLeService;
import demo.yl.sensor.Utils.yApp;
import demo.yl.sensor.bean.MService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by USR_LJQ on 2015-11-17.
 */
public class MyApplication extends Application {

    public enum SERVICE_TYPE{
        TYPE_USR_DEBUG,TYPE_NUMBER,TYPE_STR,TYPE_OTHER;
    }
    public boolean isThreadAlerdyStart() {
        return isThreadAlerdyStart;
    }

    public void setThreadAlerdyStart(boolean threadAlerdyStart) {
        isThreadAlerdyStart = threadAlerdyStart;
    }
    public boolean mLogOpen = true;

    private boolean isThreadAlerdyStart=false;
    private  boolean isThreadStart=false;
    public boolean getIsThreadStart()
    {
        return isThreadStart;
    } public void setisThreadStart(boolean isThreadStart)
    {
        this.isThreadStart=isThreadStart;
    }
    protected void initParam()
    {
        this.mLogOpen= yApp.getDebugMode();
    }

    private static MyApplication instance;
    public MyApplication()
    {
        instance=this;
    }

    public static synchronized Application getInstance()
    {
        return instance;
    }
    private final List<MService> services = new ArrayList<>();
    private final List<BluetoothGattCharacteristic> characteristics = new ArrayList<>();

    private BluetoothGattCharacteristic characteristic;

    public List<MService> getServices() {
        return services;
    }

    public static SERVICE_TYPE serviceType ;

    public void setServices(List<MService> services) {
        this.services.clear();
        this.services.addAll(services);
    }


    public List<BluetoothGattCharacteristic> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(List<BluetoothGattCharacteristic> characteristics) {
        this.characteristics.clear();
        this.characteristics.addAll(characteristics);
    }


    public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }





    public static String currentDevAddress = null;
    public static String currentDevName  = null;

    public void connectDevice(BluetoothDevice device) {
        currentDevAddress = device.getAddress();
        currentDevName = device.getName();
        //如果是连接状态，断开，重新连接
        if (BluetoothLeService.getConnectionState() != BluetoothLeService.STATE_DISCONNECTED)
            BluetoothLeService.disconnect();

        BluetoothLeService.connect(currentDevAddress, currentDevName, this);
    }

    public void disconnectDevice() {
        BluetoothLeService.disconnect();

        currentDevAddress = null;
        currentDevName  = null;
    }
}
