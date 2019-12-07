package application.minseong.capstone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

//import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private ImageView splash_top,splash_below;
    private TextView splash_text,splash_text2;

    Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splash_top = (ImageView)findViewById(R.id.splash_top);
        splash_text = (TextView)findViewById(R.id.splash_text);
        splash_text2 = (TextView)findViewById(R.id.splash_text2);

//        animation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.pushdown);
//        splash_top.setAnimation(animation);
//
//        animation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.pushdown);
//        splash_text.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.blink);
        splash_text2.setAnimation(animation);




        Thread thread = new Thread(){
            @Override
            public void run(){
                try{
                    Activity activity = SplashActivity.this;
                    sleep(4000);
                    activity.startActivity(new Intent(activity, LoginActivity.class));
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

                super.run();
            }
        };

//        itemView.setOnClickListener(
//                view -> {
//                    Activity activity = MainActivity.this;
//                    switch (detectionMode) {
//                        case ODT_LIVE:
//                            activity.startActivity(new Intent(activity, LiveObjectDetectionActivity.class));
//                            break;
//                        case ODT_STATIC:
//                            Utils.openImagePicker(activity);
//                            break;
//                        case BARCODE_LIVE:
//                            activity.startActivity(new Intent(activity, LiveBarcodeScanningActivity.class));
//                            break;
//                    }
//                });

        thread.start();
    }
}
