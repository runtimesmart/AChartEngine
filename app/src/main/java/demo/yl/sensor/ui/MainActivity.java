package demo.yl.sensor.ui;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;


import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.tools.ZoomEvent;
import org.achartengine.tools.ZoomListener;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import demo.yl.sensor.BlueToothLeService.BluetoothLeService;
import demo.yl.sensor.MyApplication;
import demo.yl.sensor.R;
import demo.yl.sensor.Utils.Constants;
import demo.yl.sensor.Utils.GattAttributes;
import demo.yl.sensor.Utils.Utils;
import demo.yl.sensor.Utils.yLog;
import rx.Subscription;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    View.OnClickListener bluetoothListener;
    MyApplication myApplication;

    private BluetoothGattCharacteristic notifyCharacteristic;
    private BluetoothGattCharacteristic readCharacteristic;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic indicateCharacteristic;
//    private boolean nofityEnable;
    private boolean isDebugMode;

    private Runnable timerRunnable= new Runnable() {
        @Override
        public void run() {
            emptyReply++;
            if(emptyReply>10){

                Log.e("test","发送notify到蓝牙");
                prepareBroadcastDataNotify(notifyCharacteristic);emptyReply = 0;
            }if(emptyReply>5){

                Log.e("test","发送notify到蓝牙");
                stopBroadcastDataNotify(notifyCharacteristic);
                prepareBroadcastDataNotify(notifyCharacteristic);emptyReply = 0;
            }
            Log.e("test","发送1到蓝牙");
            writeOption("1");
            if(BluetoothLeService.getConnectionState() != BluetoothLeService.STATE_DISCONNECTED) {
                handler.sendEmptyMessageDelayed(0,1000);

            }
        }
    };
    private int emptyReply = 15;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0){
                timerRunnable.run();
            }
            super.handleMessage(msg);
        }
    };
    private String title1 = " ";
    private String title2 = " ";
    private String title3 = " ";


    private XYSeries seriesHimdity; //湿度数据集
    private XYSeries seriesConsistance; //阻抗数据集
    private XYSeries seriesTempearture; //气温数据集
    private XYMultipleSeriesDataset mDataset;
    private GraphicalView chart;
    private XYMultipleSeriesRenderer renderer;
    private Context context;
    private double addX = -1, addY;
    private Subscription subscription;


    //    private final static int MAX = 1 << 22;
//    double[] xv1 = new double[MAX];
//    double[] yv1 = new double[MAX];
//
//    double[] xv2 = new double[MAX];
//    double[] yv2 = new double[MAX];
    private Button  clearBtn, humidityBtn, impedanceBtn,temperatureBtn,deviceBtn;

    private boolean isResistance = true;
    private double conLowValue = 0;
    private double conHighValue = 0;

    private double resisLowValue = 0;
    private double resisHighValue = 0;
    private double A = 0;
    private double B = 0;
    private ScrollView svPanel;
    Intent i;
    private ArrayList<Float> data = null;
    private String receivedMsg = "";


    private static final int TYPE_HUMIDITY = 0;//湿度
    private static final int TYPE_IMPEDANCE = 1;//阻抗
    private static final int TYPE_TEMP = 2;     //温度

    private int SHOWTYPE = TYPE_HUMIDITY;  //显示类型
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myApplication = (MyApplication) getApplication();

        humidityBtn = (Button) findViewById(R.id.shidu);
        impedanceBtn = (Button) findViewById(R.id.zukang);
        temperatureBtn = (Button) findViewById(R.id.qiwen);
        clearBtn = (Button) findViewById(R.id.clear);
//        highValueBtn.setOnClickListener(this);
        clearBtn.setOnClickListener(this);
        temperatureBtn.setOnClickListener(this);
        impedanceBtn.setOnClickListener(this);
        humidityBtn.setOnClickListener(this);
        layout = (LinearLayout) findViewById(R.id.linearLayout1);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        findViewById(R.id.connect_device).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(MainActivity.this, BluetoothListActivity.class);
                startActivityForResult(i,999);
            }
        });
//        int it = Integer.valueOf("80192304", 16);
        context = getApplicationContext();
        initChart(500, 0, false);
        chart.postInvalidate();
        chart.addZoomListener(new ZoomListener() {
            @Override
            public void zoomApplied(ZoomEvent zoomEvent) {
//                svPanel.setEnabled(false);
                yLog.e(renderer.getYAxisMin() + "----缩放");
//                if(renderer.getYAxisMin()<=0){
//                    renderer.setYLabels(0);
//                }
            }

            @Override
            public void zoomReset() {
                yLog.e(renderer.getYAxisMin() + "----释放");
//                svPanel.setEnabled(true);

            }
        }, true, true);
//        subscription = RxBus.getDefault().take(String.class).subscribe(new Action1<String>() {
//            @Override
//            public void call(String s) {
//                setMessage(s);
//            }
//        });
//        subscription = RxBus.getDefault().take(Integer.class).subscribe(new Action1<Integer>() {
//            @Override
//            public void call(Integer integer) {
//                if (integer == 0x1) {
//                    MyApplication.getInstance().setisThreadStart(true);
////                    if(MyApplication.getInstance().isThreadAlerdyStart()){
////                    }else{
////                      MyApplication.getInstance().bluethoothThread= new BluethoothThread
////                              (MyApplication.getInstance().bluetoothSocket, MainActivity.this);
////                        MyApplication.getInstance().executor.execute(MyApplication.getInstance().bluethoothThread);
////                        MyApplication.getInstance().setThreadAlerdyStart(true);
////                    }
//                    i = new Intent(MyApplication.getInstance(), BluetoothService.class);
//                    MyApplication.getInstance().startService(i);
//                    if (renderer.getYTitle().contains("R")) {
//                        renderer.setChartTitle(ySpConfig.readStrFromSp("device") + " R data");
//                    } else {
//                        renderer.setChartTitle(ySpConfig.readStrFromSp("device") + " C data");
//                    }
//                }
//            }
//        });

        chart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int ev = event.getAction();
                switch (ev) {
//                    case MotionEvent.ACTION_POINTER_DOWN:
//                        if((MotionEvent.ACTION_POINTER_DOWN | 0x0200)>=3){
//                            yToast.show("当前版本:v"+ BuildConfig.VERSION_NAME+":"+BuildConfig.VERSION_CODE,true);
//                        }
//                        break;
                    case MotionEvent.ACTION_DOWN:
                        long c = System.currentTimeMillis();
                        if (c - f < 1000l) {
                            onResume();
                        } else {
                            f = c;
                        }
                        break;
                    case MotionEvent.ACTION_HOVER_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        });




        registerReceiver(mGattUpdateReceiver, Utils.makeGattUpdateIntentFilter());
//        没用
        initOthers();


    }


    private void initTestButton() {

        View.OnClickListener myClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.btn_1:
                        Log.e("test","发送notify到蓝牙");
//                        notifyOption();
                        break;
                    case R.id.btn_2:
                        Log.e("test","发送1到蓝牙");
                        writeOption("1");
                        break;
                    case R.id.btn_3:
                        Log.e("test","发送2到蓝牙");
                        writeOption("2");
                        break;
                }
            }
        };
        findViewById(R.id.btn_1).setOnClickListener(myClickListener);
        findViewById(R.id.btn_2).setOnClickListener(myClickListener);
        findViewById(R.id.btn_3).setOnClickListener(myClickListener);
    }

    long f = 0;

    LinearLayout layout;

    private void initChart(int yMax, int yMin, boolean isSwitch) {
        //这里获得main界面上的布局，下面会把图表画在这个布局里面
//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//        ll.height = (int) (dm.density * 350);
//        layout.setLayoutParams(ll);
        //这个类用来放置曲线上的所有点，是一个点的集合，根据这些点画出曲线
        seriesHimdity = new XYSeries(title1);
        seriesConsistance = new XYSeries(title2);
        seriesTempearture=new XYSeries(title3);

        //创建一个数据集的实例，这个数据集将被用来创建图表
        mDataset = new XYMultipleSeriesDataset();

        //将点集添加到这个数据集中
        mDataset.addSeries(seriesHimdity);
        mDataset.addSeries(seriesConsistance);
        mDataset.addSeries(seriesTempearture);

        //以下都是曲线的样式和属性等等的设置，renderer相当于一个用来给图表做渲染的句柄
        int color1 = Color.GREEN;
        int color2 = Color.TRANSPARENT;

        PointStyle style = PointStyle.CIRCLE;
        renderer = buildRenderer(color1, color2, style, true);

        //设置好图表的样式
        setChartSettings(renderer, "X", "Y", 0, 60, yMin, yMax, Color.WHITE, Color.WHITE);

        //生成图表
         chart = ChartFactory.getLineChartView(context, mDataset, renderer);

        //将图表添加到布局中去
        layout.addView(chart, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (isSwitch) {
            myApplication.setisThreadStart(true);
        }

    }

    /***
     * --需要修改
     *
     * */
    private void switchStatus(int type) {
        SHOWTYPE=type;
        if (type==TYPE_HUMIDITY) {
//            mode = true;
            r3.setColor(Color.TRANSPARENT);
            r2.setColor(Color.TRANSPARENT);
            r1.setColor(Color.GREEN);
//            renderer.setChartTitle("Humidity data");
            renderer.setChartTitle(" ");
            renderer.setYTitle("Humidity(RH%)");
            renderer.setYAxisMax(maxShiValue);

        } else if(type==TYPE_IMPEDANCE) {
//            mode = false;

            r1.setColor(Color.TRANSPARENT);
            r2.setColor(Color.GREEN);
            r3.setColor(Color.TRANSPARENT);
//            renderer.setYTitle("Impedance data");
            renderer.setChartTitle(" ");
            renderer.setYTitle("Impedance(kΩ)");
//            renderer.setYAxisMax(maxZuValue*3);
            renderer.setYAxisMax(maxZuValue);
        }else if(type==TYPE_TEMP){
            r1.setColor(Color.TRANSPARENT);
            r2.setColor(Color.TRANSPARENT);
            r3.setColor(Color.GREEN);
            renderer.setYTitle("Temperature(°C)");

            renderer.setChartTitle("");
            renderer.setYAxisMax(maxWenValue);
        }
        justfyY();
    }


    View view1;
    String conL, conH,resisL,resisH;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.shidu) {
            switchStatus(TYPE_HUMIDITY);
        } else if (id == R.id.zukang) {
            switchStatus(TYPE_IMPEDANCE);
        }else if (id == R.id.qiwen) {
            switchStatus(TYPE_TEMP);
        }
        else if (id == R.id.clear) {
            cleardata();
        }

    }
    XYSeriesRenderer r1, r2,r3;

    protected XYMultipleSeriesRenderer buildRenderer(int color1, int color2, PointStyle style, boolean fill) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        //设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
        r1 = new XYSeriesRenderer();
        r1.setColor(color1);
        r1.setPointStyle(style);
        r1.setFillPoints(fill);
        r1.setLineWidth(3);
        renderer.addSeriesRenderer(r1);

        r2 = new XYSeriesRenderer();
        r2.setColor(color2);
        r2.setPointStyle(style);
        r2.setFillPoints(fill);
        r2.setLineWidth(3);
        renderer.addSeriesRenderer(r2);


        r3 = new XYSeriesRenderer();
        r3.setColor(color2);
        r3.setPointStyle(style);
        r3.setFillPoints(fill);
        r3.setLineWidth(3);
        renderer.addSeriesRenderer(r3);
        return renderer;
    }

    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String xTitle, String yTitle,
                                    double xMin, double xMax, double yMin, double yMax, int axesColor, int labelsColor) {
        //有关对图表的渲染可参看api文档
        renderer.setChartTitle(title1);
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);
        renderer.setShowGrid(true);
        renderer.setGridColor(Color.GRAY);
        renderer.setBackgroundColor(Color.BLACK);
        renderer.setApplyBackgroundColor(true);
        renderer.setMargins(new int[]{40, 125, 40, 20});//上、左、下、右
        renderer.setDisplayValues(true);
        renderer.setXLabels(10); //设置X轴平均分割
        renderer.setYLabels(10); //设置Y轴平均分割
        renderer.setXTitle("Time(s)");
        renderer.setYTitle("Humidity(RH%)");
        renderer.setChartTitleTextSize(40);
        renderer.setLabelsTextSize(25);  //改x y 轴 数字大小
        renderer.setAxisTitleTextSize(32); //改侧边标题字体大小
//        renderer.setYLabelsAlign(Paint.Align.CENTER);
        renderer.setYLabelsAlign(Paint.Align.RIGHT);

        renderer.setPointSize((float) 2);
        renderer.setShowLegend(false);
        renderer.setPanEnabled(true, true);
//        renderer.setYAxisMin(-10);
//        renderer.setYAxisMin(0);
//        renderer.setPanLimits(new double[]{0, mode?maxNoValue:maxZuValue, , 20});
        renderer.setZoomEnabled(false, true);
//        renderer.setXLabels(0);
//        for(int i=0;i<=600;i++){
//            if((i)%60==0){
//                renderer.addXTextLabel(i,i/60+" min");
//            }else{
//
//            }
//        }
//        renderer.setZoomLimits(new double[] { 20, 20, 20, 20 });
    }

//    private double offset = yApp.toDouble((double) 1/60);

    private double offset = 1;
    private double maxNoValue = 0;
    private double minZuValue = 0;
    private double minNoValue = 0;
    private double maxZuValue = 10000;
    private double maxShiValue = 100;
    private double maxWenValue = 100;
    private boolean isFirst=true;
    StringBuffer sb = new StringBuffer();
    /***
     *
     * 数据调用显示方法  需要修改
     *  impe  阻抗
     * humi  湿度
     *  temp  温度
     * **/
    public void setMessage(float impe,float temp,float humi) {
//        yLog.e(msg);
        addX+=offset;
//        int start = msg.indexOf("fa");
//        int end = msg.lastIndexOf("f5");
//        if (start != 0) return;
//        msg = msg.substring(start + 2, end);
//        sb.append(msg);

//        int impe = Integer.parseInt(zukang, 16); //阻抗
//        int humi = Integer.parseInt(shidu, 16);  //湿度
//        int temp = Integer.parseInt(qiwen, 16);  //温度

        double currentMaxValue = 0;
        //判断Y轴最大值 重置最大值
        if (SHOWTYPE==TYPE_HUMIDITY) {
            if(humi>=maxShiValue){
                maxShiValue = humi;
                currentMaxValue = maxShiValue;
            }
        } else if(SHOWTYPE==TYPE_IMPEDANCE) {

            if (impe >= maxZuValue) {
                maxZuValue = impe;
//                renderer.setYAxisMax(delat*2);
                currentMaxValue = maxZuValue;
            }
        }else if(SHOWTYPE==TYPE_TEMP){
            if(temp>=maxWenValue){
                maxWenValue = temp;
                currentMaxValue = maxWenValue;
            }
        }
        yLog.e("--y轴节点--"+renderer.getYAxisMax()+"--"+renderer.getYAxisMin());
        if(renderer.getYAxisMax()<currentMaxValue){
            justfyY();
        }
        if(isFirst){
            justfyY();
        }
//            yLog.saveLogToFile("resistanceConv4Delta.txt",delat+"-------\n");
        sb.delete(0, sb.length());
//        yLog.e(delat + "--实际计算结果");


//        addY = impe;

        mDataset.removeSeries(seriesHimdity);
        mDataset.removeSeries(seriesConsistance);
        mDataset.removeSeries(seriesTempearture);


//判断当前点集中到底有多少点，因为屏幕总共只能容纳100个，所以当点数超过100时，长度永远是100
//        int length = seriesResistance.getItemCount();

//            yLog.e("---###"+length);
//            yLog.e("---@@@"+renderer.getXAxisMax());

        if (addX>= renderer.getXAxisMax()) {
            renderer.setXAxisMin(renderer.getXAxisMin() + offset * 20);
            renderer.setXAxisMax(renderer.getXAxisMax() + offset * 20);
        }


        /**
         * 添加新的点--需要修改
         * */
        seriesConsistance.add(addX, impe);
        seriesHimdity.add(addX, humi);
        seriesTempearture.add(addX, temp);


        //在数据集中添加新的点集
        mDataset.addSeries(seriesHimdity);
        mDataset.addSeries(seriesConsistance);
        mDataset.addSeries(seriesTempearture);

        //视图更新，没有这一步，曲线不会呈现动态
        //如果在非UI主线程中，需要调用postInvalidate()，具体参考api
        chart.invalidate();

    }

    public void cleardata() {
        addX=-1;
        renderer.setXAxisMin(0);
        renderer.setXAxisMax(60);
//        mode=true;
        mDataset.clear();
        seriesHimdity.clear();
        seriesConsistance.clear();
        seriesTempearture.clear();
//        layout.removeView(chart);
//        initChart(500, 0, false);
        isFirst=true;
    }

    /**
     * 根据输入的高低值，计算A的值
     * Yh=10000ppm
     * Yl=10ppm
     *
     * 默认电阻
     * Xh=10000kΩ
     * Xl=10kΩ
     */



    @Override
    public void onDestroy() {
//        if (!subscription.isUnsubscribed()) {
//            subscription.unsubscribe();
//        }
//        shutdownExector();
        unregisterReceiver(mGattUpdateReceiver);
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    AlertDialog ad;
    //调整Y轴最大最小值
    private void justfyY()
    {
        if (SHOWTYPE==TYPE_IMPEDANCE) {
            renderer.setYAxisMax(maxZuValue);
            renderer.setYAxisMin(1000);
            yLog.e("--调整阻抗Y轴--"+maxZuValue);

        } else if (SHOWTYPE==TYPE_HUMIDITY){
            renderer.setYAxisMax(maxShiValue);
            renderer.setYAxisMin(0);
            yLog.e("--调整湿度Y轴--"+maxShiValue);
        } else if (SHOWTYPE==TYPE_TEMP){
            renderer.setYAxisMax(maxWenValue);
            renderer.setYAxisMin(-10);
            yLog.e("--调整气温Y轴--"+maxWenValue);
        }
        isFirst=false;
    }
    private void initOthers() {

                bluetoothListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,BluetoothListActivity.class);
                startActivityForResult(intent,999);
            }
        };

        findViewById(R.id.btn_bluetoothlist).setOnClickListener(bluetoothListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999&&data!=null){
            boolean connect = data.getBooleanExtra("bluetooth_conneted",false);
            if (connect){
//                Log.e("test","----蓝牙连接成功");
//                init();
//                //initTestButton();
//
//                notifyOption();
//                timer.schedule(timerTask,1000,1000);
            }else{
                Log.e("test","----蓝牙连接失败");
            }
        }
    }


    @Override
    protected void onResume() {


        Log.e("test","---onResume");
        if(BluetoothLeService.getConnectionState() != BluetoothLeService.STATE_DISCONNECTED){
            Log.e("test","----蓝牙连接成功");

            getSupportActionBar().setTitle("Device connected");

            init();
            //initTestButton();
            if (emptyReply > 10) {
                handler.sendEmptyMessageDelayed(0,1000);
            }
        }else {
            getSupportActionBar().setTitle("NO Device");

        }
        super.onResume();
    }

    private void init() {

        initCharacteristics();
    }


    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            //There are four basic operations for moving data in BLE: read, write, notify,
            // and indicate. The BLE protocol specification requires that the maximum data
            // payload size for these operations is 20 bytes, or in the case of read operations,
            // 22 bytes. BLE is built for low power consumption, for infrequent short-burst data transmissions.
            // Sending lots of data is possible, but usually ends up being less efficient than classic Bluetooth
            // when trying to achieve maximum throughput.  从google查找的，解释了为什么android下notify无法解释超过
            //20个字节的数据
            Bundle extras = intent.getExtras();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                emptyReply = 0;
                // Data Received
                if (extras.containsKey(Constants.EXTRA_BYTE_VALUE)) {
                    if (extras.containsKey(Constants.EXTRA_BYTE_UUID_VALUE)) {
                        if (myApplication != null) {
                            BluetoothGattCharacteristic requiredCharacteristic = myApplication.getCharacteristic();
                            String uuidRequired = requiredCharacteristic.getUuid().toString();
                            String receivedUUID = intent.getStringExtra(Constants.EXTRA_BYTE_UUID_VALUE);

                            if (isDebugMode){
                                byte[] array = intent.getByteArrayExtra(Constants.EXTRA_BYTE_VALUE);
//                                Message msg = new Message(Message.MESSAGE_TYPE.RECEIVE,formatMsgContent(array));
//                                notifyAdapter(msg);

                                updateData(array);
//                                Log.e("test","------get msg -----"+msg.getContent().toString());

                            }
                        }
                    }
                }
                if (extras.containsKey(Constants.EXTRA_DESCRIPTOR_BYTE_VALUE)) {
                    if (extras.containsKey(Constants.EXTRA_DESCRIPTOR_BYTE_VALUE_CHARACTERISTIC_UUID)) {
                        BluetoothGattCharacteristic requiredCharacteristic = myApplication.
                                getCharacteristic();
                        String uuidRequired = requiredCharacteristic.getUuid().toString();
                        String receivedUUID = intent.getStringExtra(
                                Constants.EXTRA_DESCRIPTOR_BYTE_VALUE_CHARACTERISTIC_UUID);

                        byte[] array = intent
                                .getByteArrayExtra(Constants.EXTRA_DESCRIPTOR_BYTE_VALUE);

//                        System.out.println("GattDetailActivity---------------------->descriptor:" + Utils.ByteArraytoHex(array));
                        if (isDebugMode){
//                            updateButtonStatus(array);
                        }else if (uuidRequired.equalsIgnoreCase(receivedUUID)) {
//                            updateButtonStatus(array);
                        }

                    }
                }
            }

            if (action.equals(BluetoothLeService.ACTION_GATT_DESCRIPTORWRITE_RESULT)){
                if (extras.containsKey(Constants.EXTRA_DESCRIPTOR_WRITE_RESULT)){
                    int status = extras.getInt(Constants.EXTRA_DESCRIPTOR_WRITE_RESULT);
                    if (status != BluetoothGatt.GATT_SUCCESS){
                        Toast.makeText(MainActivity.this,"Operation failure",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            if (action.equals(BluetoothLeService.ACTION_GATT_CHARACTERISTIC_ERROR)) {
                if (extras.containsKey(Constants.EXTRA_CHARACTERISTIC_ERROR_MESSAGE)) {
                    String errorMessage = extras.
                            getString(Constants.EXTRA_CHARACTERISTIC_ERROR_MESSAGE);
                    System.out.println("GattDetailActivity---------------------->err:" + errorMessage);
                    Toast.makeText(MainActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                }

            }



            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
//                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
//                if (state == BluetoothDevice.BOND_BONDING) {}
//                else if (state == BluetoothDevice.BOND_BONDED) {}
//                else if (state == BluetoothDevice.BOND_NONE) {}
            }

            //connect break (连接断开)
            if (action.equals(BluetoothLeService.ACTION_GATT_DISCONNECTED)){

                getSupportActionBar().setTitle("NO Device");
                    stopBroadcastDataNotify(notifyCharacteristic);

                Toast.makeText(MainActivity.this,"与蓝牙连接断开",Toast.LENGTH_SHORT).show();
            }
        }

    };

    private void updateData(byte[] array) {

        if (data == null){
            data = new ArrayList<>();
        }
        if (receivedMsg == null){
            receivedMsg = "";
        }

        receivedMsg = receivedMsg+formatMsgContent(array);
         yLog.e("长度－－－－"+receivedMsg.length());
        if (receivedMsg.length() >= 48){
            for (int i = 0;i<6;i++){
                String a = receivedMsg.substring(i*8,i*8+8);
                yLog.e("结果－－－－"+a);
                if(i==0||i>3){
                    Float value=0f;
                    try {
                        int b=Integer.valueOf(a.trim(), 16);
                         value = Float.intBitsToFloat(b);
                    }catch(Exception e){
                        yLog.e(e.getLocalizedMessage()+"数据异常");
                    }
                    yLog.e("结果2－－－－"+value);
                    if(i==0){
                        value = value/1000;
                        yLog.e("结果2333－－－－"+value);
                    }
                    data.add(value);
                    yLog.e("转换后结果－－－－"+big(value));
                }
            }

            //更新数据到界面
            updateDataToUI(data);
            //清空数据
            receivedMsg = null;
            data = null;
        }
    }

    // 方法一：NumberFormat
    private static String big(double d) {

        BigDecimal d1 = new BigDecimal(Double.toString(d));
        BigDecimal d2 = new BigDecimal(Integer.toString(1));
        // 四舍五入,保留2位小数
        return d1.divide(d2,2,BigDecimal.ROUND_HALF_UP).toString();
    }
    private synchronized void updateDataToUI(final ArrayList<Float> data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO 更新数据到界面 的实际调用处

                if (data.size() == 3){
                    setMessage(data.get(0),data.get(1),data.get(2));
                }

            }
        });
    }


    private void initCharacteristics(){
        BluetoothGattCharacteristic characteristic = ((MyApplication)getApplication()).getCharacteristic();
        if (characteristic.getUuid().toString().equals(GattAttributes.USR_SERVICE)){
            isDebugMode = true;
            List<BluetoothGattCharacteristic> characteristics = ((MyApplication)getApplication()).getCharacteristics();

            for (BluetoothGattCharacteristic c :characteristics){
                if (Utils.getPorperties(this,c).equals("Notify")){
                    notifyCharacteristic = c;
                    continue;
                }

                if (Utils.getPorperties(this,c).equals("Write")){
                    writeCharacteristic = c;
                    continue;
                }
            }


        }else {

            notifyCharacteristic = characteristic;
            readCharacteristic = characteristic;
            writeCharacteristic = characteristic;
            indicateCharacteristic = characteristic;
        }
    }


//06-06 06:17:18.291 15459-15459/demo.yl.sensor E/test: ------get msg -----HEX:4B A0 8F 37 40 E0 3B E2 4B 9B C7 BB 4A 9B 87 DE 41 BC 00 00   (ASSCII:K160 143 7@224 ;226 K155 199 187 J155 135 222 A188 0 0 )
//            06-06 06:17:18.381 15459-15459/demo.yl.sensor E/test: ------get msg -----HEX:42 59 33 33   (ASSCII:BY33)

    /**
     * 对字节进行格式化
     * @param data
     * @return
     */
    private String formatMsgContent(byte[] data){
        String sss = Utils.ByteArraytoHex(data);
        String result = sss.replace(" ","");

        return result;
    }




//
//
//    private void notifyOption(){
//        if (nofityEnable){
//            nofityEnable = false;
//            stopBroadcastDataNotify(notifyCharacteristic);
//        }else {
//            nofityEnable = true;
//            prepareBroadcastDataNotify(notifyCharacteristic);
//        }
//    }

    /**
     * Preparing Broadcast receiver to broadcast notify characteristics
     *
     * @param characteristic
     */
    void prepareBroadcastDataNotify(
            BluetoothGattCharacteristic characteristic) {
        final int charaProp = characteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            BluetoothLeService.setCharacteristicNotification(characteristic, true);
        }

    }

    /**
     * Stopping Broadcast receiver to broadcast notify characteristics
     *
     * @param characteristic
     */
    void stopBroadcastDataNotify(
            BluetoothGattCharacteristic characteristic) {
        final int charaProp = characteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            BluetoothLeService.setCharacteristicNotification(characteristic, false);
        }
    }


    private void writeOption(String content){
        String text = content;
        if (TextUtils.isEmpty(text)){
            return;
        }
        boolean isHexSend = false;
        if (isHexSend){
            text = text.replace(" ","");
            if (!Utils.isRightHexStr(text)){
                return;
            }
            byte[] array = Utils.hexStringToByteArray(text);
            writeCharacteristic(writeCharacteristic, array);
        }else {

            if(Utils.isAtCmd(text))
                text = text + "\r\n";
            try {
                byte[] array = text.getBytes("US-ASCII");
                writeCharacteristic(writeCharacteristic,array);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                System.out.println("--------------------->write text exception");
                return;
            }

        }

    }


    private void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] bytes) {
        // Writing the hexValue to the characteristics

        Log.e("test","======执行消息发送");
        try {
            BluetoothLeService.writeCharacteristicGattDb(characteristic,
                    bytes);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
