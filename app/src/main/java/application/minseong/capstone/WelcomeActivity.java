package application.minseong.capstone;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

//import android.support.v7.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity{


    DBHelper helper;
    String sql;
    Cursor cursor;
    SQLiteDatabase database;
    int version = 1;

    LinearLayout l1,l2;
    Button btnStart;
    TextView countRegisteredUser;
    Animation uptodown;
    int userCount;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        helper = new DBHelper(WelcomeActivity.this, DBContract.contract.TABLE_NAME, null, version);
        database = helper.getReadableDatabase();
        userCount = helper.CountUser(database);


        btnStart = (Button)findViewById(R.id.startNowbtn);
        l1 = (LinearLayout)findViewById(R.id.welcome1);
       //l2 = (LinearLayout)findViewById(R.id.welcome2);
        countRegisteredUser = (TextView)findViewById(R.id.registedUser);

        uptodown = AnimationUtils.loadAnimation(WelcomeActivity.this, R.anim.uptodown);
        l1.setAnimation(uptodown);

        countRegisteredUser.setText(Integer.toString(userCount));

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, SliderActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
