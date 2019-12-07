package application.minseong.capstone;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

//import android.support.annotation.NonNull;
//import android.support.design.widget.Snackbar;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;


public class GpsActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback,PlacesListener
{
    private GoogleMap mGoogleMap = null;
    private Marker currentMarker = null;

    private static final String TAG = "googlemap";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 10000;  // 5초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 10000; // 2.5초
    private int PROXIMITY_RADIUS = 5000;

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0;
    private static final int PERMISSION_REQUEST_CODE = 1;

    final String permissionToCall = Manifest.permission.CALL_PHONE;
    private static Intent phoneCallIntent;


    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;


    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소


    Location mCurrentLocatiion;
    LatLng currentPosition;


    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    List<Marker> previous_marker = null;




    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_gps);

        mLayout = findViewById(R.id.layout_gps);

        Log.d(TAG, "onCreate");

        previous_marker = new ArrayList<Marker>();

        Button findBtn = (Button)findViewById(R.id.findBtn);
        findBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlaceInformation(currentPosition); // 이놈때문에 자꾸 돌아가는듯. 원래위치로.
            }
        }); // find button 주변의 경찰서 찾아줌





        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);


        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final String permissionToCall = Manifest.permission.CALL_PHONE;
//Assume that you have a phone icon.
        (findViewById(R.id.policeBtn)).setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                phoneCallIntent = new Intent(Intent.ACTION_CALL);
                phoneCallIntent.setData(Uri.parse(getString(R.string.callNumber))); //Uri.parse("tel:your number")
                if (ActivityCompat.checkSelfPermission(GpsActivity.this, permissionToCall) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(GpsActivity.this, new String[]{permissionToCall}, 1);
                    return;
                }
                startActivity(phoneCallIntent);
            }
        });


    }




    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());


                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());
                //로그로 찍어내기
                //Toast.makeText(GpsActivity.this,markerSnippet,Toast.LENGTH_SHORT).show();
                // Log.d(TAG, "onLocationResult : " + markerSnippet);
//                tv_latitude.setText(String.valueOf(location.getLatitude()));
//                tv_longitude.setText(String.valueOf(location.getLongitude()));


                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);

                mCurrentLocatiion = location;
            }


        }

    };



    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);



            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ) {

                Log.d(TAG, "startLocationUpdates : Do not have permission");
                return;
            }


            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mGoogleMap.setMyLocationEnabled(true);

        }

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(TAG, "onMapReady :");

        mGoogleMap = googleMap;


        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();



        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);



        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            startLocationUpdates(); // 3. 위치 업데이트 시작


        }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "Location access is required to enable this feature.",
                        Snackbar.LENGTH_INDEFINITE).setAction("confirm", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions( GpsActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }



        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setTrafficEnabled(true);

        Button btnPolice = (Button)findViewById(R.id.policeBtn);
        btnPolice.setOnClickListener(new View.OnClickListener() {
            String Police = "police"; // Or fire_station
            @Override
            public void onClick(View v) {
                mGoogleMap.clear();
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String url = getUrl(latitude,longitude,Police);
                Object[] DataTransfer = new Object[2];
                DataTransfer[0] = mGoogleMap;
                DataTransfer[1] = url;
                GetNearbyPlace getNearbyPlacesData = new GetNearbyPlace();
                getNearbyPlacesData.execute(DataTransfer);
                Toast.makeText(GpsActivity.this,"SEND A MESSAGE TO 112", Toast.LENGTH_LONG).show();
                Toast.makeText(GpsActivity.this,"Your location is "+ currentMarker.getTitle(),Toast.LENGTH_LONG).show();
                //Log.e("tag",currentMarker.getTitle()); // 현재 위치를 보낼 수 있음.
                String sms = "I'M IN TROUBLE RIGHT NOW, PLEASE HELP ME!" + currentMarker.getTitle();
                try{
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage("01022265901",null,sms,null,null);
                    Toast.makeText(GpsActivity.this, "success", Toast.LENGTH_LONG).show();

                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                if (ActivityCompat.checkSelfPermission(GpsActivity.this, permissionToCall) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(GpsActivity.this, new String[]{permissionToCall}, 1);
                    return;
                }

//                if (ActivityCompat.checkSelfPermission(GpsActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
//                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:112"));
//                    startActivity(intent);
//                } else {
//                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"));
//                    startActivity(intent);
//                }
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                intent.setData(Uri.parse("tel:" + "112"));
                startActivity(intent);
            }
        });



        Button btnFireStation = (Button)findViewById(R.id.firestationBtn);
        btnFireStation.setOnClickListener(new View.OnClickListener() {
            String FireStation = "fire_station";
            @Override
            public void onClick(View v) {
                mGoogleMap.clear();
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String url = getUrl(latitude,longitude,FireStation);
                Object[] DataTransfer = new Object[2];
                DataTransfer[0] = mGoogleMap;
                DataTransfer[1] = url;
                GetNearbyPlace getNearbyPlacesData = new GetNearbyPlace();
                getNearbyPlacesData.execute(DataTransfer);
                Toast.makeText(GpsActivity.this,"SEND A MESSAGE TO 119", Toast.LENGTH_LONG).show();
                Toast.makeText(GpsActivity.this,"Your location is "+ currentMarker.getTitle(),Toast.LENGTH_LONG).show();
                //Log.e("tag",currentMarker.getTitle()); // 현재 위치를 보낼 수 있음
//                sendSMSMessage();
                String sms = "I'M IN TROUBLE RIGHT NOW, PLEASE HELP ME!" + currentMarker.getTitle();
                try{
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage("01022265901",null,sms,null,null);
                    Toast.makeText(GpsActivity.this, "success", Toast.LENGTH_LONG).show();

                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

                if (ActivityCompat.checkSelfPermission(GpsActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:119"));
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:119"));
                    startActivity(intent);
                }
            }
        });
    }


//    protected void sendSMSMessage() {
//
//        if (ContextCompat.checkSelfPermission(GpsActivity.this,
//                Manifest.permission.SEND_SMS)
//                != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(GpsActivity.this,
//                    Manifest.permission.SEND_SMS)) {
//            } else {
//                ActivityCompat.requestPermissions(GpsActivity.this,
//                        new String[]{Manifest.permission.SEND_SMS},
//                        MY_PERMISSIONS_REQUEST_SEND_SMS);
//            }
//        }
//    }





    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        if (checkPermission()) {

            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mGoogleMap!=null)
                mGoogleMap.setMyLocationEnabled(true);

        }


    }


    @Override
    protected void onStop() {

        super.onStop();

        if (mFusedLocationClient != null) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }




    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "네트워크 문제로 서비스 되지 않습니다.", Toast.LENGTH_LONG).show();
//            Giocoder not in service, please figure out your network
            return "iocoder not in service, please figure out your network";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "invalid GPS position", Toast.LENGTH_LONG).show();
            return "invalid GPS position";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소를 찾을 수 없습니다.", Toast.LENGTH_LONG).show();
            return "Failed to find address";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {


        if (currentMarker != null) currentMarker.remove();


        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);


        currentMarker = mGoogleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mGoogleMap.moveCamera(cameraUpdate);

    }


    public void setDefaultLocation() {


        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "cannot bring the information of the location";
        String markerSnippet = "check the gps permission";


        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mGoogleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mGoogleMap.moveCamera(cameraUpdate);

    }


    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);



        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            return true;
        }

        return false;

    }



    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {

                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {


                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "Permission denied. Please re-run the app to allow it. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("confirm", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();

                }else {


                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "Permission denied. Please allow it on the Settings tab.. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("confirm", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }

//        if (permsRequestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
//            if (grandResults.length > 0
//                    && grandResults[0] == PackageManager.PERMISSION_GRANTED) {
//                SmsManager smsManager = SmsManager.getDefault();
//                smsManager.sendTextMessage("01022265901", null, "IM IN TROUBLE ,PLEASE HELP ME", null, null);
//                Toast.makeText(GpsActivity.this, "SMS Sent", Toast.LENGTH_LONG).show();
//                smsManager.sendTextMessage("01022265901",null,currentMarker.getTitle(),null,null);
//                Toast.makeText(GpsActivity.this,"Your location is sent, just wait there!",Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(getApplicationContext(),
//                        "SMS faild, please try again.", Toast.LENGTH_LONG).show();
//                return;
//            }
//        }
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(GpsActivity.this);
        builder.setTitle("Disable Location Services");
        builder.setMessage("Location services must be allowed in order to use this function"
                + "Do you want to modify your permission?");
        builder.setCancelable(true);
        builder.setPositiveButton("Setting", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : GPS Activated");


                        needRequest = true;

                        return;
                    }
                }

                break;
        }
    }

    @Override
    public void onPlacesFailure(PlacesException e) {

    }

    @Override
    public void onPlacesStart() {

    }

    @Override
    public void onPlacesSuccess(final List<Place> places) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (noman.googleplaces.Place place : places) {
                    LatLng latLng = new LatLng(place.getLatitude()
                            , place.getLongitude());

                    String markerSnippet = getCurrentAddress(latLng);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(place.getName());
                    markerOptions.snippet(markerSnippet);
                    Marker item = mGoogleMap.addMarker(markerOptions);
                    previous_marker.add(item);

                }
                //중복 마커 제거

                HashSet<Marker> hashSet = new HashSet<Marker>();
                hashSet.addAll(previous_marker);
                previous_marker.clear();
                previous_marker.addAll(hashSet);
                //ArrayList<Marker> sortListbyDistance(previous_marker,currentPosition);
            }
        });
    }

    @Override
    public void onPlacesFinished() {

    }

    public void showPlaceInformation(LatLng location)
    {
        mGoogleMap.clear();//지도 클리어

        if (previous_marker != null)
            previous_marker.clear();//지역정보 마커 클리어

        new NRPlaces.Builder()
                .listener(GpsActivity.this)
                .key("AIzaSyDnlrm2MoEy5Mai-kuM5TYHtG6TxQSBFOc")
                .latlng(location.latitude, location.longitude)//현재 위치
                .radius(PROXIMITY_RADIUS) //3km 내에서 검색
                .type(PlaceType.POLICE) //police
                .build()
                .execute();
    }

//    public static ArrayList<Marker> sortListbyDistance(ArrayList<Marker> markers, final LatLng location){
//        Collections.sort(markers, new Comparator<Marker>() {
//            @Override
//            public int compare(Marker marker2, Marker marker1) {
//                //
//                if(getDistanceBetweenPoints(marker1.getLocation(),location)>getDistanceBetweenPoints(marker2.getLocation(),location)){
//                    return -1;
//                } else {
//                    return 1;
//                }
//            }
//        });
//        return markers;
//    }
//
//    public static float getDistanceBetweenPoints(double firstLatitude, double firstLongitude, double secondLatitude, double secondLongitude) {
//        float[] results = new float[1];
//        Location.distanceBetween(firstLatitude, firstLongitude, secondLatitude, secondLongitude, results);
//        return results[0];
//    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + 3000);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + getResources().getString(R.string.google_place));
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

}
