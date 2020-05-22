package application.minseong.capstone;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import application.minseong.capstone.java.PermissionRequester;
import butterknife.BindView;
import butterknife.ButterKnife;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import android.support.v7.app.AlertDialog;

import android.content.res.AssetFileDescriptor;
//import android.support.v7.app.AppCompatActivity;
import org.tensorflow.lite.Interpreter; // tensorflow lite 모듈
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    //final int DIALOG_TEXT = 1;
    static final int ADD_FRIEND = 5;
    static final int QR_CODE = 10;

    @BindView(R.id.addFriend)
    ImageView _addFriend;

    @BindView(R.id.gps_image)
    ImageView _gpsImg;

    @BindView(R.id.alarmBtn)
    ImageView _alarmBtn;

    @BindView(R.id.emergencycall)
    ImageView _emergencyImg;

    @BindView(R.id.youtube)
    ImageView _youtube;

    @BindView(R.id.detection_image)
    ImageView _detectionTest;

    @BindView(R.id.qrcode)
    ImageView qrcode_image;


    public static MediaPlayer mp2 = null;
    public static MediaPlayer mp3 = null;


    public boolean isPlayingSong = false;
    private BluetoothSPP bt;

    Dialog myDialog;

    Switch aSwitch;

    //Using the Accelometer & Gyroscoper
    SensorManager mSensorManager;

    //Using the Accelometer
    SensorEventListener mAccLis;
    Sensor mAccelometerSensor = null;
    public float[][][] arrayOfAcc;
    public int cur;
    public boolean PlayingforAcc = false;
    CountDownTimer countDownTimer;
    public boolean endCount = false;
    public String[] moveLabel = {"Standing", "Walking", "Jogging", "Jumping", "Stairs up",
            "Stairs down", "Back-sitting-chair", "Front-knees-lying", "Forward-lying", "Sideward-lying"};

    public String getFriendName;
    public Integer getFriendNum;
    public Integer gps_qrcode;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //bt = new BluetoothSPP(this);
        final Context context = this;


//        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
//            Toast.makeText(getApplicationContext()
//                    , "Bluetooth is not available"
//                    , Toast.LENGTH_SHORT).show();
//            finish();
//        }


        _gpsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GpsActivity.class);
                startActivity(intent);
            }
        });



        _alarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlayingSong){
                    isPlayingSong = false;
                    mp2.stop();
                    _alarmBtn.setImageResource(R.drawable.alarm_no);
                }else{
                    AlarmFun();
                    _alarmBtn.setImageResource(R.drawable.alarm_red2);
                }
            }
        });
//
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {
                Log.d("hu",message);
                String gas= message.substring(0,3);
                String temp= message.substring(3,5);
                String hu= message.substring(5,7);
                int gas1=Integer.parseInt(gas);
                int temp1=Integer.parseInt(temp);
                int hu1=Integer.parseInt(hu);
                String Play_sound = "NotPlay";
                String dangas;
                String dantemp;
                String danhu;
                MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.sample);

                TextView GasView = (TextView)findViewById(R.id.gasValue);

                TextView TempView = (TextView)findViewById(R.id.tempValue);

                TextView HuView = (TextView)findViewById(R.id.huValue);


                if(gas1>600){
                    dangas="가스:위험";
                    Play_sound = "PS";
                    Toast.makeText(MainActivity.this, dangas, Toast.LENGTH_SHORT).show();
                    GasView.setTextColor(Color.parseColor("#ff0000"));
                    GasView.setText(gas);
                }
                else{
                    GasView.setTextColor(Color.parseColor("#000000"));
                    GasView.setText(gas);
                }
                if(temp1>50){
                    dantemp="온도:위험";
                    Play_sound = "PS";
                    Toast.makeText(MainActivity.this, dantemp, Toast.LENGTH_SHORT).show();
                    TempView.setTextColor(Color.parseColor("#ff0000"));
                    TempView.setText(temp);
                }
                else{

                    TempView.setTextColor(Color.parseColor("#000000"));
                    TempView.setText(temp);
                }
                if(hu1<30){
                    danhu="습도:위험";
                    Play_sound = "PS";
                    Toast.makeText(MainActivity.this, danhu, Toast.LENGTH_SHORT).show();
                    HuView.setTextColor(Color.parseColor("#ff0000"));
                    HuView.setText(hu);
                }
                else{
                    HuView.setTextColor(Color.parseColor("#000000"));
                    HuView.setText(hu);
                }////////기준치 넘는지

                if (Play_sound.equals("PS")){
                    mediaPlayer.start(); //소리
                }


                //Toast.makeText(MainActivity.this, dangas+"  "+dantemp+"  "+danhu, Toast.LENGTH_SHORT).show();
                //if (Integer.parseInt(message)>0){
                //   Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                //}
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() { //연결해제
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() { //연결실패
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnConnect = findViewById(R.id.btnConnect); //연결시도
        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });



        aSwitch = findViewById(R.id.sw1);
        //sound_pool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
        mp3 = MediaPlayer.create(MainActivity.this, R.raw.siren);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Using the Accelometer 센서 객체 생성
        //assert mSensorManager != null;
        mAccelometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mAccLis = new accListener();
        arrayOfAcc = new float[3][582][1];

//        danger = sound_pool.load(this, R.raw.siren,1);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) {
                    Toast.makeText(MainActivity.this, "ON", Toast.LENGTH_SHORT).show();
                    cur = 0;
                    if(!endCount){
                        mSensorManager.registerListener(mAccLis, mAccelometerSensor, 17000);
                    }
                    else{
                        aSwitch.setChecked(false);
                        endCount = false;
                        mSensorManager.unregisterListener(mAccLis);
                    }
                }else{
                    Toast.makeText(MainActivity.this,"OFF",Toast.LENGTH_SHORT).show();
                    mSensorManager.unregisterListener(mAccLis);
                    mp3.stop();
                    PlayingforAcc = false;
                    endCount = false;
                }
            }
        });

        _addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,AddFriend.class);
                startActivityForResult(intent,ADD_FRIEND);

//                AddFriend addFriend = new AddFriend();
//                getFriendName = addFriend.getFriendName();
//                getFriendNum = addFriend.getFriendNum();
//                Log.d("tag",getFriendName);
//                Log.d("tag",getFriendNum);
            }
        });

        _detectionTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = MainActivity.this;
                activity.startActivity(new Intent(activity , application.minseong.capstone.java.LivePreviewActivity.class));

            }
        });


        _emergencyImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int result = new PermissionRequester.Builder(MainActivity.this)
                        .setTitle("권한 요청")
                        .setMessage("권한을 요청합니다.")
                        .setPositiiveButtonName("네")
                        .setNagetiveButtonName("아니요")
                        .create()
                        .request(Manifest.permission.CALL_PHONE, 1000, new PermissionRequester.OnClickDenyButtonListener() {
                            @Override
                            public void onClick(Activity activity) {
                                Log.d("xxx","취소함");

                            }
                        });
                if(result == PermissionRequester.ALREADY_GRANTED) {
                    if (getFriendName == null && getFriendNum == null) {
                        Toast.makeText(MainActivity.this, "친구의 정보를 입력해 주세요!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, AddFriend.class);
                        startActivityForResult(intent, ADD_FRIEND);
                    } else {
                        String tel = "tel:0" + getFriendNum.toString();
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(tel));
                        startActivity(intent);
                        //startActivity(new Intent("android.intent.action.CALL", Uri.parse(tel)));
                    }
                }
            }


        });

        _youtube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            DialogPopup();
            }
        });

        qrcode_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,QRcodeTest.class);
                startActivity(intent);

            }
        });

    }

    @Override
    public void onBackPressed(){

        super.onBackPressed();
        Intent i = new Intent(MainActivity.this, SliderActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    public void DialogPopup() {
        final Dialog myDialog = new Dialog(MainActivity.this);
        myDialog.setContentView(R.layout.activity_popup);
        TextView txtclose;
        ImageView _burn, _cpr, _ext;
        _burn = (ImageView) myDialog.findViewById(R.id.burn);
        _cpr = (ImageView) myDialog.findViewById(R.id.cpr);
        _ext = (ImageView) myDialog.findViewById(R.id.ext);
        txtclose = (TextView) myDialog.findViewById(R.id.txtclose);
        myDialog.show();

        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });

        _burn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://youtu.be/" + "G9YegpvgBe0"));
                startActivity(intent);
            }
        });
        _cpr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://youtu.be/" + "Zbp74ri21YE"));

                startActivity(intent);
            }
        });
        _ext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://youtu.be/" + "rcW7sGDUaJM"));

                startActivity(intent);
            }
        });
    }

    public void AlarmFun(){
        mp2 = MediaPlayer.create(MainActivity.this, R.raw.sample);
        mp2.start();
        isPlayingSong = true;
    }


    public void onDestroy() {
        super.onDestroy();
        //bt.stopService(); //블루투스 중지
    }

    public void onStart() {
        super.onStart();
//        if (!bt.isBluetoothEnabled()) { //
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
//        } else {
//            if (!bt.isServiceAvailable()) {
//                bt.setupService();
//                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리
//                //setup();
//            }
//        }
    }

//    public void setup() {
//        Button btnSend = findViewById(R.id.btnSend); //데이터 전송
//        btnSend.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                bt.send("Text", true);
//            }
//        });
//    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
//            if (resultCode == AppCompatActivity.RESULT_OK)
//                bt.connect(data);
//        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
//            if (resultCode == AppCompatActivity.RESULT_OK) {
//                bt.setupService();
//                bt.startService(BluetoothState.DEVICE_OTHER);
//                //setup();
//            } else {
//                Toast.makeText(MainActivity.this
//                        , "Bluetooth was not enabled."
//                        , Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        }

        if (requestCode == ADD_FRIEND) {
            if (resultCode == RESULT_OK) {
                getFriendName = data.getStringExtra("friend1");
                getFriendNum = data.getIntExtra("friend2", 0);
                if (getFriendName != null) {
                    Log.d("tag1", getFriendName);
                }
            }
        }
//\
    }



    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private class accListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {
            if(cur<582){
                arrayOfAcc[0][cur][0] = event.values[0];
                arrayOfAcc[1][cur][0] = event.values[1];
                arrayOfAcc[2][cur][0] = event.values[2];
//                if(cur%100 == 0){
//                    Log.d("tagplz", Integer.toString(cur)+"  "+Float.toString(event.values[0])+"  "+Float.toString(event.values[1])+"  "+Float.toString(event.values[2]));
//                }
                cur++;
            }else {
                //        텐서플로 라이트 실행 구간
                try {
//                    float[][] output = new float[][]{{-1}};  // 0이면 ADL(일상 생활), 1이면 Fall
//                    Log.d("tagplz", Float.toString(output[0][0]));
//                    Interpreter tflite = getTfliteInterpreter("ADL_Fall_model.tflite");
//                    tflite.run(arrayOfAcc, output);
//                    Log.d("tagplz", Float.toString(output[0][0]));
//
//                    if (output[0][0] == 1) {
//                        // Fall 이므로 사이렌
//                        PlayingforAcc = true;
//                        mp3.start();
//                        DialogSimple();
//                        Toast.makeText(MainActivity.this, "Fall", Toast.LENGTH_LONG).show();
//                    } else if (output[0][0] == 0){
//                        Toast.makeText(MainActivity.this, "ADL", Toast.LENGTH_LONG).show(); }

                    int[] output = new int[]{-1};
//                    Log.d("tagplz", Arrays.toString(output));
                    Interpreter tflite = getTfliteInterpreter("Ten_Move_model.tflite");
                    tflite.run(arrayOfAcc, output);
//                    Log.d("tagplz", Arrays.toString(output));

                    if (output[0] >= 6) {
                        // Fall 이므로 사이렌
                        PlayingforAcc = true;
                        mp3.start();
                        DialogSimple();
                        Toast.makeText(MainActivity.this, moveLabel[output[0]], Toast.LENGTH_LONG).show();
                    } else if (output[0] >= 0 && output[0] < 6){
                        Toast.makeText(MainActivity.this, moveLabel[output[0]], Toast.LENGTH_LONG).show(); }
                } catch (Exception e) {
                    e.printStackTrace(); }
                cur = 0;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    }

    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(MainActivity.this, modelPath)); }
        catch (Exception e) {
            e.printStackTrace(); }
        return null;
    }

    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void DialogSimple() {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        alt_bld.setMessage("");
        alt_bld.setCancelable(false).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Action for 'NO' Button
                        dialog.cancel();
                        endCount = true;
                    }
                });
        AlertDialog alert = alt_bld.create();
        alert.setTitle("낙상 감지");
        // Icon for AlertDialog
        alert.setIcon(R.drawable.logo_transparent1);
        alert.show();
        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                alert.setMessage(String.format("낙상이 감지 되었습니다\n %d초 후 전화를 겁니다", millisUntilFinished / 1000));
                if (endCount) {
                    endCount = false;
                    cancel();
                }
            }
            @Override
            public void onFinish() {
                if(getFriendName == null && getFriendNum == null){
                    Toast.makeText(MainActivity.this,"친구의 정보를 입력해 주세요!",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this,AddFriend.class);
                    startActivityForResult(intent,ADD_FRIEND);
                }else{
                    String tel = "tel:0"+getFriendNum.toString();
                    Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse(tel));
                    startActivity(intent);
                    //startActivity(new Intent("android.intent.action.CALL", Uri.parse(tel)));
                }
                alert.dismiss();
            }
        }.start();
    }

    private void DialogYoutube(){
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        alt_bld.setMessage("What do you want to watch?").setCancelable(
                false).setPositiveButton("Extinguisher",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Intent intent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse( "http://youtu.be/" + "rcW7sGDUaJM" ));

                        startActivity( intent );

                    }
                }).setNegativeButton("CPR",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Action for 'NO' Button
                        Intent intent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse( "http://youtu.be/" + "Zbp74ri21YE" ));

                        startActivity( intent );
                    }
                }).setNeutralButton("Burn",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse( "http://youtu.be/" + "G9YegpvgBe0" ));

                        startActivity( intent );

                    }
                });
        AlertDialog alert = alt_bld.create();
        // Title for AlertDialog
        alert.setTitle("YOUTUBE");
        // Icon for AlertDialog
        alert.setIcon(R.drawable.logo_transparent1);
        alert.show();
    }

}

