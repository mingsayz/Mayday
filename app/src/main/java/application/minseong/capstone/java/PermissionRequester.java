package application.minseong.capstone.java;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class PermissionRequester {
    public static final int NOT_SUPPORT_VERSION = 2;

    public static final int ALREADY_GRANTED = -1;

    public static final int REQUEST_PERMISSION = 0;

    private Activity context;
    private Builder builder;
    private void setBuilder(Builder builder) {
        this.builder = builder;
    }

    private PermissionRequester(Activity context) {
        this.context = context;
    }
    public int request(final String permission, final int requestCode, final OnClickDenyButtonListener denyAction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                if (context.shouldShowRequestPermissionRationale(permission)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle(builder.getTitle())
                            .setMessage(builder.getMessage())
                            .setPositiveButton(builder.getPositiveButtonName(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        context.requestPermissions(new String[]{permission}, requestCode);
                                    }
                                }
                            })
                            .setNegativeButton(builder.getNegativeButtonName(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    denyAction.onClick(context);
                                }
                            }).create().show();

                    return REQUEST_PERMISSION;
                } else {
                    context.requestPermissions(new String[]{permission}, requestCode);
                    return REQUEST_PERMISSION;
                }
            } else {
                return ALREADY_GRANTED;
            }
        }
        return NOT_SUPPORT_VERSION;
    }
    public static class Builder{
        private PermissionRequester requester;
        public Builder(Activity context){
            requester = new PermissionRequester(context);
        }
        private String title = "권한 요청";
        private String message = "기능의 사용을 위해 권한이 필요합니다.";
        private String positiveButtonName = "네";
        private String negativeButtonName = "아니요";

        public  String getTitle(){
            return title;
        }

        public Builder setTitle(String title){
            this.title = title;
            return this;
        }

        public String getMessage(){
            return message;
        }

        public Builder setMessage(String message){
            this.message = message;
            return this;
        }

        public String getPositiveButtonName(){
            return positiveButtonName;
        }
        public Builder setPositiiveButtonName(String positiveButtonName){
            this.positiveButtonName = positiveButtonName;
            return this;
        }

        public String getNegativeButtonName(){
            return negativeButtonName;
        }

        public Builder setNagetiveButtonName(String negativeButtonName){
            this.negativeButtonName = negativeButtonName;
            return this;
        }
        public PermissionRequester create(){
            this.requester.setBuilder(this);
            return this.requester;
        }

    }
    public interface OnClickDenyButtonListener{
        public void onClick(Activity activity);
    }

}