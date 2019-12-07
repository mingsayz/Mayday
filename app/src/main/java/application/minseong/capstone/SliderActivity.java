package application.minseong.capstone;

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class SliderActivity extends AppCompatActivity {

        ViewPager viewPager;
        Adapter adapter;
        List<Model> models;
        Integer[] colors = null;
        ArgbEvaluator argbEvaluator = new ArgbEvaluator();



        @Override
        protected void onCreate(Bundle saveInstanceState){
            super.onCreate(saveInstanceState);
            setContentView(R.layout.activity_slide);

            models = new ArrayList<>();
            models.add(new Model(R.drawable.emergencycall,"긴급통화","터치 한번으로 등록된 친구의 정보로 긴급통화를 이용할 수 있습니다."));
            models.add(new Model(R.drawable.gps,"GPS","현재위치에서 가장 가까운 경찰서의 위치를 알려줍니다."));
            models.add(new Model(R.drawable.youtube1,"YOUTUBE","화상시 응급처치, 심폐소생술, 소화기사용법을 알려줍니다."));
            models.add(new Model(R.drawable.detection,"사물인식","주변 환경을 인식하여 전달함으로써 신속한 구조를 돕습니다."));
            models.add(new Model(R.drawable.alarm_no,"경고음","경고음 버튼을 누름과 동시에 경고음이 울려 주위에 위험을 알립니다."));
            models.add(new Model(R.drawable.qrcode,"QR코드","QR코드를 인식하여 현재위치를 정확히 인식하여 빠른 신고를 돕습니다."));
            models.add(new Model(R.drawable.addfriend,"친구추가","긴급통화를 위한 친구를 추가할 수 있습니다."));
            models.add(new Model(R.drawable.toggle,"위험감지","딥러닝을 이용하여 위험을 감지하여 주위에 경고음으로 알려줍니다."));

            adapter = new Adapter(models, this);

                    viewPager = findViewById(R.id.viewpager);
            viewPager.setAdapter(adapter);
            viewPager.setPadding(130,0,130,0);

            Integer[] colors_temp = {
                    getResources().getColor(R.color.color1),
                    getResources().getColor(R.color.color2),
                    getResources().getColor(R.color.color3),
                    getResources().getColor(R.color.color4),
                    getResources().getColor(R.color.color5),
                    getResources().getColor(R.color.color6),
                    getResources().getColor(R.color.color7),
                    getResources().getColor(R.color.color8),
            };

            colors = colors_temp;

            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    if(position < (adapter.getCount() -1) && position < (colors.length-1)){
                        viewPager.setBackgroundColor(
                                (Integer)argbEvaluator.evaluate(
                                        positionOffset,
                                        colors[position],
                                        colors[position+1])
                        );
                    }

                    else {
                        viewPager.setBackgroundColor(colors[colors.length - 1]);
                    }
                }

                @Override
                public void onPageSelected(int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            findViewById(R.id.btnOrder).setOnClickListener(new View.OnClickListener() {
             @Override
                public void onClick(View v) {
                Intent intent = new Intent(SliderActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        }
       @Override
       public void onBackPressed() {
           super.onBackPressed();
          Intent i = new Intent(SliderActivity.this, LoginActivity.class);
          i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          startActivity(i);
            finish();
    }

}

