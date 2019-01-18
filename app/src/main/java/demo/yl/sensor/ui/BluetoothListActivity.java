package demo.yl.sensor.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import demo.yl.sensor.BlueToothLeService.BluetoothLeService;
import demo.yl.sensor.MyApplication;
import demo.yl.sensor.R;
import demo.yl.sensor.Utils.GattAttributes;
import demo.yl.sensor.Utils.Utils;
import demo.yl.sensor.adapter.DevicesAdapter;
import demo.yl.sensor.bean.MDevice;
import demo.yl.sensor.bean.MService;

/**
 * Created by lixun on 17/6/3.
 */

public class BluetoothListActivity extends Activity{
    public static final int STATE_NOT_STARTED = 0;
    public static final int STATE_FILL_STARTED = 1;
    public static final int STATE_FINISHED = 2;

    public static final int STATE_END_STARTED = 3;


    private boolean scaning;

    private Handler hander;
    TextView textview_search;
    RecyclerView recyclerView;
    View.OnClickListener searchListener;
    ProgressBar img_search_animation;

    // Blue tooth adapter for BLE device scan
    private static BluetoothAdapter mBluetoothAdapter;

    private Runnable stopScanRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBluetoothAdapter != null)
               stopScan();
        }
    };

    private final List<MDevice> list = new ArrayList<>();
    private DevicesAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetoothlist);
        hander = new Handler();
        initView();
        initData();
        initListener();
        initBluetoothService();



    }

    private void initBluetoothService() {

        checkBleSupportAndInitialize();

        //注册广播接收者，接收消息
        registerReceiver(mGattUpdateReceiver, Utils.makeGattUpdateIntentFilter());

        Intent gattServiceIntent = new Intent(getApplicationContext(),
                BluetoothLeService.class);
        startService(gattServiceIntent);

    }

    private void initData() {



        adapter = new DevicesAdapter(list, BluetoothListActivity.this);
        adapter.setOnItemClickListener(new DevicesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {

                if (scaning){

                    hander.post(stopScanRunnable);
                }
                ((MyApplication)getApplication()).connectDevice(list.get(position).getDevice());
            }
        });

        recyclerView.setAdapter(adapter);




        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                adapter.setDelayStartAnimation(false);
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mGattUpdateReceiver);
        super.onDestroy();
    }

    private void initListener() {
        searchListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if (!scaning) {
                    scaning = true;
                    //如果有连接先关闭连接
                    ((MyApplication)getApplication()).disconnectDevice();
                    searchAnimate();
                    onRefresh();
                }else {

                }


            }
        };


        textview_search.setOnClickListener(searchListener);

    }

    private void initView() {
        textview_search = (TextView) findViewById(R.id.textview_search);
        recyclerView = (RecyclerView) findViewById(R.id.recycleview);
        img_search_animation = (ProgressBar) findViewById(R.id.img_search_animation);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        img_search_animation.setVisibility(View.GONE);
    }


    private void searchAnimate() {
        img_search_animation.setVisibility(View.VISIBLE);
    }



    public void onRefresh() {
        // Prepare list view and initiate scanning
        if (adapter != null) {
            adapter.clear();
            adapter.notifyDataSetChanged();
        }
        startScan();
    }

    private void startScan() {
        ((MyApplication)getApplication()).disconnectDevice();
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        scanPrevious21Version();
//        } else {
//            scanAfter21Version();
//        }
    }



    /**
     * Getting the GATT Services
     * 获得服务
     *
     * @param gattServices
     */
    private void prepareGattServices(List<BluetoothGattService> gattServices) {
        prepareData(gattServices);

        Log.e("test","gattServices -----"+gattServices.size());
//        Intent intent = new Intent(this, ServicesActivity.class);
//        intent.putExtra("dev_name",currentDevName);
//        intent.putExtra("dev_mac",currentDevAddress);
//        startActivity(intent);
//        overridePendingTransition(0, 0);

        if (gattServices==null||gattServices.size()==0){
            return;
        }
        for (int i=0;i<gattServices.size();i++) {
            BluetoothGattService g = gattServices.get(i);
            String uuid = g.getUuid().toString();
            if (uuid.equals(GattAttributes.USR_SERVICE)) {

                Log.e("test","来到来选中到 -----");

                ((MyApplication)getApplication()).setCharacteristics(g.getCharacteristics());
                //这里为了方便暂时直接用Application serviceType 来标记当前的服务，应该是和上面的代码合并
                MyApplication.serviceType = MyApplication.SERVICE_TYPE.TYPE_USR_DEBUG;
                BluetoothGattCharacteristic usrVirtualCharacteristic =
                        new BluetoothGattCharacteristic(UUID.fromString(GattAttributes.USR_SERVICE), -1, -1);


                ((MyApplication) getApplication()).setCharacteristic(usrVirtualCharacteristic);
//        }


                Intent intent = new Intent();
                intent.putExtra("bluetooth_conneted", true);
                this.setResult(RESULT_OK, intent);
                finish();
                break;
            }else if (i == gattServices.size()-1){

                Toast.makeText(getApplication(),"本app不支持该设备",Toast.LENGTH_SHORT).show();
                ((MyApplication)getApplication()).disconnectDevice();
            }
        }
    }


    /**
     * Prepare GATTServices data.
     *
     * @param gattServices
     */
    private void prepareData(List<BluetoothGattService> gattServices) {

        if (gattServices == null)
            return;

        List<MService> list = new ArrayList<>();

        for (BluetoothGattService gattService : gattServices) {
            String uuid = gattService.getUuid().toString();
            if (uuid.equals(GattAttributes.GENERIC_ACCESS_SERVICE) || uuid.equals(GattAttributes.GENERIC_ATTRIBUTE_SERVICE))
                continue;
            String name = GattAttributes.lookup(gattService.getUuid().toString(), "UnkonwService");
            MService mService = new MService(name, gattService);
            list.add(mService);
        }

        ((MyApplication) getApplication()).setServices(list);
    }

    /**
     * BroadcastReceiver for receiving the GATT communication status
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            // Status received when connected to GATT Server
            //连接成功
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                System.out.println("--------------------->连接成功");

                //搜索服务
                BluetoothLeService.discoverServices();
            }
            // Services Discovered from GATT Server
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
//                progressDialog.dismiss();
                prepareGattServices(BluetoothLeService.getSupportedGattServices());
            } else if (action.equals(BluetoothLeService.ACTION_GATT_DISCONNECTED)) {
//                progressDialog.dismiss();
                //connect break (连接断开)
                Toast.makeText(BluetoothListActivity.this,"与蓝牙设备连接断开",
                        Toast.LENGTH_SHORT).show();
            }

        }
    };

    /**
     * 获得蓝牙适配器
     */
    private void checkBleSupportAndInitialize() {
        // Use this check to determine whether BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this,"该蓝牙设备不支持",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // Initializes a Blue tooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            // Device does not support Blue tooth
            Toast.makeText(this,
                    "该蓝牙设备不支持", Toast.LENGTH_SHORT)
                    .show();
            return;
        }


        //打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }


    }


    /**
     * 版本号21之前的调用该方法搜索
     */
    private void scanPrevious21Version() {
        //10秒后停止扫描
        hander.postDelayed(stopScanRunnable,10000);
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }


    private void stopScan(){
        img_search_animation.setVisibility(View.GONE);
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        hander.removeCallbacks(stopScanRunnable);
        scaning = false;
    }


    /**
     * Call back for BLE Scan
     * This call back is called when a BLE device is found near by.
     * 发现设备时回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             byte[] scanRecord) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MDevice mDev = new MDevice(device, rssi);
                    if (list.contains(mDev))
                        return;
                    list.add(mDev);

                    if (adapter != null) {
                        adapter.setList(list);
                        if (list!=null&&list.size()>0){

                            adapter.setOnItemClickListener(new DevicesAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(View itemView, int position) {

                                    if (scaning){

                                        hander.post(stopScanRunnable);
                                    }
                                    ((MyApplication)getApplication()).connectDevice(list.get(position).getDevice());
                                }
                            });

                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };




    @Override
    protected void onResume() {
        super.onResume();
        //如果有连接先关闭连接
        //((MyApplication)getApplication()).disconnectDevice();
    }

}
