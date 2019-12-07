package application.minseong.capstone;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;

//import android.support.annotation.Nullable;
//import android.support.v4.content.FileProvider;


public class UploadUserProfile extends AppCompatActivity {

    @BindView(R.id.btn_UploadPicture)
    Button btn_UploadPhoto;

    private static final String TAG = "mayday";

    private Boolean isPermission = true;

    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;

    private File tempFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        tedPermission();

        btn_UploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: ");
                // 권한 허용에 동의하지 않았을 경우 토스트를 띄웁니다.
                if(isPermission) takePhoto();
                else Toast.makeText(view.getContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != AppCompatActivity.RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();

            if (tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.e(TAG, tempFile.getAbsolutePath() + " 삭제 성공");
                        tempFile = null;
                    }
                }
            }

            return;
        }

        if (requestCode == PICK_FROM_ALBUM) {

            Uri photoUri = data.getData();
            Log.d(TAG, "PICK_FROM_ALBUM photoUri : " + photoUri);

            Cursor cursor = null;

            try {

                /*
                 *  Uri 스키마를
                 *  content:/// 에서 file:/// 로  변경한다.
                 */
                String[] proj = {MediaStore.Images.Media.DATA};

                assert photoUri != null;
                cursor = getContentResolver().query(photoUri, proj, null, null, null);

                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                cursor.moveToFirst();

                tempFile = new File(cursor.getString(column_index));

                Log.d(TAG, "tempFile Uri : " + Uri.fromFile(tempFile));

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            setImage();

        } else if (requestCode == PICK_FROM_CAMERA) {

            setImage();

        }
    }

    /**
     *  앨범에서 이미지 가져오기
     */
    private void goToAlbum() {
        Log.d(TAG,"gotoablum");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }


    /**
     *  카메라에서 이미지 가져오기
     */
    private void takePhoto() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            tempFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        if (tempFile != null) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                Uri photoUri = FileProvider.getUriForFile(this,
                        "{package name}.provider", tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);

            } else {

                Uri photoUri = Uri.fromFile(tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);

            }
        }
    }

    /**
     *  폴더 및 파일 만들기
     */
    private File createImageFile() throws IOException {

        // 이미지 파일 이름 ( blackJin_{시간}_ )
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "blackJin_" + timeStamp + "_";

        // 이미지가 저장될 폴더 이름 ( blackJin )
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/blackJin/");
        if (!storageDir.exists()) storageDir.mkdirs();

        // 파일 생성
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.d(TAG, "createImageFile : " + image.getAbsolutePath());

        return image;
    }

    /**
     *  tempFile 을 bitmap 으로 변환 후 ImageView 에 설정한다.
     */
    private void setImage() {

        ImageView imageView = findViewById(R.id.user_image);

        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);
        Log.d(TAG, "setImage : " + tempFile.getAbsolutePath());

        imageView.setImageBitmap(originalBm);

        /**
         *  tempFile 사용 후 null 처리를 해줘야 합니다.
         *  (resultCode != RESULT_OK) 일 때 tempFile 을 삭제하기 때문에
         *  기존에 데이터가 남아 있게 되면 원치 않은 삭제가 이뤄집니다.
         */
        tempFile = null;

    }

    /**
     *  권한 설정
     */
    private void tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
                isPermission = true;

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
                isPermission = false;

            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,Manifest.permission.READ_CONTACTS,Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();

    }




}
//
//    private static final int PICK_FROM_CAMERA = 0;
//    private static final int PICK_FROM_ALBUM = 1;
//    private static final int CROP_FROM_IMAGE = 2;
//
//    private Uri mImageCaptureUri;
//    private ImageView iv_UserPhoto;
//    private int id_view;
//    private String absolutePath;
//
//    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}; //권한 설정 변수
//    private static final int MULTIPLE_PERMISSIONS = 101; //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수
//
//    @Override
//    public void onCreate(Bundle savedInstanceState){
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.user_profile);
//
//        iv_UserPhoto = (ImageView)this.findViewById(R.id.user_image);
//        Button btn_agreeJoin = (Button)this.findViewById(R.id.btn_UploadPicture);
//
//        checkPermissions();
//
//        btn_agreeJoin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                UploadPhoto(v);
//            }
//        });
//
//
//    }
//
//    private boolean checkPermissions() {
//        int result;
//        List<String> permissionList = new ArrayList<>();
//        for (String pm : permissions) {
//            result = ContextCompat.checkSelfPermission(this, pm);
//            if (result != PackageManager.PERMISSION_GRANTED) { //사용자가 해당 권한을 가지고 있지 않을 경우 리스트에 해당 권한명 추가
//                permissionList.add(pm);
//            }
//        }
//        if (!permissionList.isEmpty()) { //권한이 추가되었으면 해당 리스트가 empty가 아니므로 request 즉 권한을 요청합니다.
//            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
//            return false;
//        }
//        return true;
//    }
//
//
//    //아래는 권한 요청 Callback 함수입니다. PERMISSION_GRANTED로 권한을 획득했는지 확인할 수 있습니다. 아래에서는 !=를 사용했기에
////권한 사용에 동의를 안했을 경우를 if문으로 코딩되었습니다.
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MULTIPLE_PERMISSIONS: {
//                if (grantResults.length > 0) {
//                    for (int i = 0; i < permissions.length; i++) {
//                        if (permissions[i].equals(this.permissions[0])) {
//                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                                showNoPermissionToastAndFinish();
//                            }
//                        } else if (permissions[i].equals(this.permissions[1])) {
//                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                                showNoPermissionToastAndFinish();
//
//                            }
//                        } else if (permissions[i].equals(this.permissions[2])) {
//                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                                showNoPermissionToastAndFinish();
//
//                            }
//                        }
//                    }
//                } else {
//                    showNoPermissionToastAndFinish();
//                }
//                return;
//            }
//        }
//    }
//    //권한 획득에 동의를 하지 않았을 경우 아래 Toast 메세지를 띄우며 해당 Activity를 종료시킵니다.
//    private void showNoPermissionToastAndFinish() {
//        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
//        finish();
//    }
////
//
//
//
//    public void doTakePhotoAction(){ // 카메라 촬영후 이미지 가져오기
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//        String url = "temp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
//        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),url));
//
//        intent.putExtra(MediaStore.EXTRA_OUTPUT,mImageCaptureUri);
//        startActivityForResult(intent,PICK_FROM_CAMERA);
//    }
//
//
//    public void doTakeAlbumAction(){
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
//        startActivityForResult(intent,PICK_FROM_ALBUM);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode,int resultCode,Intent data){
//        super.onActivityResult(requestCode,resultCode,data);
//
//        if(resultCode != RESULT_OK)
//            return;
//
//        switch (requestCode)
//        {
//            case PICK_FROM_ALBUM:{
//                mImageCaptureUri = data.getData();
//                Log.d("tag",mImageCaptureUri.getPath().toString()) ;
//            }
//            case PICK_FROM_CAMERA:{
//                Intent intent = new Intent("com.android.camera.action.CROP");
//                intent.setDataAndType(mImageCaptureUri,"image/*");
//
//                intent.putExtra("outputX",120);
//                intent.putExtra("outputY",120);
//                intent.putExtra("aspectX",1);
//                intent.putExtra("aspectY",1);
//                intent.putExtra("scale",true);
//                intent.putExtra("return-data",true);
//                startActivityForResult(intent,CROP_FROM_IMAGE);
//                break;
//            }
//            case CROP_FROM_IMAGE:{
//                if(resultCode != RESULT_OK){
//                    return;
//                }
//
//                final Bundle extras = data.getExtras();
//
//                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mayday/" + System.currentTimeMillis() + ".jpg";
//
//                if(extras != null){
//                    Bitmap photo = extras.getParcelable("data");
//                    iv_UserPhoto.setImageBitmap(photo);
//
//                    storeCropImage(photo,filePath);
//                    absolutePath = filePath;
//                    break;
//                }
//
//                File f = new File(mImageCaptureUri.getPath());
//                if(f.exists()){
//                    f.delete();
//                }
//
//            }
//        }
//    }
//
//    public void UploadPhoto(View v){
//        Log.e("tag","UploadPhoto");
//        id_view = v.getId();
//        if(v.getId() == R.id.btn_UploadPicture){
//            DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    doTakePhotoAction();
//                }
//            };
//            DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    doTakeAlbumAction();
//                }
//            };
//
//            DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            };
//
//            new AlertDialog.Builder(this)
//                    .setTitle("select a photo to upload")
//                    .setPositiveButton("take a photo",cameraListener)
//                    .setNeutralButton("album",albumListener)
//                    .setNegativeButton("cancel",cancelListener)
//                    .show();
//        }
//    }
//
//    // Bitmap 저장
//
//    private void storeCropImage(Bitmap bitmap,String filePath){
//        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mayday";
//        File directory_Mayday = new File(dirPath);
//
//        if(!directory_Mayday.exists())
//            directory_Mayday.mkdir();
//
//        File copyFile = new File(filePath);
//        BufferedOutputStream out = null;
//
//        try{
//            copyFile.createNewFile();
//            out = new BufferedOutputStream(new FileOutputStream(copyFile));
//            bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
//            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(copyFile)));
//
//            out.flush();
//            out.close();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//    private static final int PICK_FROM_CAMERA = 1; //카메라 촬영으로 사진 가져오기
//    private static final int PICK_FROM_ALBUM = 2; //앨범에서 사진 가져오기
//    private static final int CROP_FROM_CAMERA = 3; //가져온 사진을 자르기 위한 변수
//
//    Uri photoUri;
//
//    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}; //권한 설정 변수
//    private static final int MULTIPLE_PERMISSIONS = 101; //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.user_profile);
//        checkPermissions(); //권한 묻기
////        mImageView = (ImageView)findViewById(R.id.user_image);
////        btn_UploadPhoto = (Button)findViewById(R.id.btn_UploadPicture);
//        ButterKnife.bind(this);
//
//        btn_UploadPhoto.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                goToAlbum();
//            }
//        });
//    }
//
//    private boolean checkPermissions() {
//        int result;
//        List<String> permissionList = new ArrayList<>();
//        for (String pm : permissions) {
//            result = ContextCompat.checkSelfPermission(this, pm);
//            if (result != PackageManager.PERMISSION_GRANTED) { //사용자가 해당 권한을 가지고 있지 않을 경우 리스트에 해당 권한명 추가
//                permissionList.add(pm);
//            }
//        }
//        if (!permissionList.isEmpty()) { //권한이 추가되었으면 해당 리스트가 empty가 아니므로 request 즉 권한을 요청합니다.
//            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
//            return false;
//        }
//        return true;
//    }
//
//
//    //아래는 권한 요청 Callback 함수입니다. PERMISSION_GRANTED로 권한을 획득했는지 확인할 수 있습니다. 아래에서는 !=를 사용했기에
////권한 사용에 동의를 안했을 경우를 if문으로 코딩되었습니다.
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MULTIPLE_PERMISSIONS: {
//                if (grantResults.length > 0) {
//                    for (int i = 0; i < permissions.length; i++) {
//                        if (permissions[i].equals(this.permissions[0])) {
//                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                                showNoPermissionToastAndFinish();
//                            }
//                        } else if (permissions[i].equals(this.permissions[1])) {
//                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                                showNoPermissionToastAndFinish();
//
//                            }
//                        } else if (permissions[i].equals(this.permissions[2])) {
//                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                                showNoPermissionToastAndFinish();
//
//                            }
//                        }
//                    }
//                } else {
//                    showNoPermissionToastAndFinish();
//                }
//                return;
//            }
//        }
//    }
//    //권한 획득에 동의를 하지 않았을 경우 아래 Toast 메세지를 띄우며 해당 Activity를 종료시킵니다.
//    private void showNoPermissionToastAndFinish() {
//        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
//        finish();
//    }
//
//    private void takePhoto() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //사진을 찍기 위하여 설정합니다.
//        File photoFile = null;
//        try {
//            photoFile = createImageFile();
//        } catch (IOException e) {
//            Toast.makeText(UploadUserProfile.this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();              finish();
//        }
//        if (photoFile != null) {
//            photoUri = FileProvider.getUriForFile(UploadUserProfile.this,
//                    "application.minseong.capstone.provider", photoFile); //FileProvider의 경우 이전 포스트를 참고하세요.
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri); //사진을 찍어 해당 Content uri를 photoUri에 적용시키기 위함
//            startActivityForResult(intent, PICK_FROM_CAMERA);
//        }
//    }
//
//    // Android M에서는 Uri.fromFile 함수를 사용하였으나 7.0부터는 이 함수를 사용할 시 FileUriExposedException이
//    // 발생하므로 아래와 같이 함수를 작성합니다. 이전 포스트에 참고한 영문 사이트를 들어가시면 자세한 설명을 볼 수 있습니다.
//    private File createImageFile() throws IOException {
//        // Create an image file name
//        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
//        String imageFileName = "IP" + timeStamp + "_";
//        File storageDir = new File(Environment.getExternalStorageDirectory() + "/capstone/"); //test라는 경로에 이미지를 저장하기 위함
//        if (!storageDir.exists()) {
//            storageDir.mkdirs();
//        }
//        File image = File.createTempFile(
//                imageFileName,
//                ".jpg",
//                storageDir
//        );
//        return image;
//    }
//
//
//    public void goToAlbum() {
//        Log.d("tag", "goToAlbum: ");
//        Intent intent = new Intent(Intent.ACTION_PICK); //ACTION_PICK 즉 사진을 고르겠다!
//        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
//        startActivityForResult(intent, PICK_FROM_ALBUM);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode != RESULT_OK) {
//            Toast.makeText(UploadUserProfile.this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
//        }
//        if (requestCode == PICK_FROM_ALBUM) {
//            if(data==null){
//                return;
//            }
//            photoUri = data.getData();
//            cropImage();
//        } else if (requestCode == PICK_FROM_CAMERA) {
//            cropImage();
//            MediaScannerConnection.scanFile(UploadUserProfile.this, //앨범에 사진을 보여주기 위해 Scan을 합니다.
//                    new String[]{photoUri.getPath()}, null,
//                    new MediaScannerConnection.OnScanCompletedListener() {
//                        public void onScanCompleted(String path, Uri uri) {
//                        }
//                    });
//        } else if (requestCode == CROP_FROM_CAMERA) {
//            try { //저는 bitmap 형태의 이미지로 가져오기 위해 아래와 같이 작업하였으며 Thumbnail을 추출하였습니다.
//
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
//                Bitmap thumbImage = ThumbnailUtils.extractThumbnail(bitmap, 128, 128);
//                ByteArrayOutputStream bs = new ByteArrayOutputStream();
//                thumbImage.compress(Bitmap.CompressFormat.JPEG, 100, bs); //이미지가 클 경우 OutOfMemoryException 발생이 예상되어 압축
//
//
//                //여기서는 ImageView에 setImageBitmap을 활용하여 해당 이미지에 그림을 띄우시면 됩니다.
//
//                mImageView.setImageBitmap(thumbImage);
//            } catch (Exception e) {
//                Log.e("ERROR", e.getMessage().toString());
//            }
//        }
//    }
//
//    public void cropImage() {
//        this.grantUriPermission("com.android.camera", photoUri,
//                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        Intent intent = new Intent("com.android.camera.action.CROP");
//        intent.setDataAndType(photoUri, "image/*");
//
//        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
//        grantUriPermission(list.get(0).activityInfo.packageName, photoUri,
//                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        int size = list.size();
//        if (size == 0) {
//            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
//            return;
//        } else {
//            Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            intent.putExtra("crop", "true");
//            intent.putExtra("aspectX", 4);
//            intent.putExtra("aspectY", 3);
//            intent.putExtra("scale", true);
//            File croppedFileName = null;
//            try {
//                croppedFileName = createImageFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            File folder = new File(Environment.getExternalStorageDirectory() + "/capstone/");
//            File tempFile = new File(folder.toString(), croppedFileName.getName());
//
//            photoUri = FileProvider.getUriForFile(UploadUserProfile.this,
//                    "application.minseong.capstone.provider", tempFile);
//
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//
//
//            intent.putExtra("return-data", false);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
//            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()); //Bitmap 형태로 받기 위해 해당 작업 진행
//
//            Intent i = new Intent(intent);
//            ResolveInfo res = list.get(0);
//            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            grantUriPermission(res.activityInfo.packageName, photoUri,
//                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
//            startActivityForResult(i, CROP_FROM_CAMERA);
//
//
//        }
//
//    }
