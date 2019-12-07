package application.minseong.capstone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

//import android.support.v7.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    String sql;
    Cursor cursor;

    @BindView(R.id.input_name)
    EditText _nameText;
    @BindView(R.id.input_email)
    EditText _emailText;
    @BindView(R.id.input_password)
    EditText _passwordText;
    @BindView(R.id.input_password2)
    EditText _passwordTextConfirm;
    @BindView(R.id.btn_signup)
    Button _signupButton;
    @BindView(R.id.link_login)
    TextView _loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signup() {
        Log.d(TAG, "SignUp");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("계정 생성 중입니다. 잠시만 기다려주세요");
        progressDialog.show();

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        DBHelper helper = new DBHelper(this, DBContract.contract.TABLE_NAME,null,1);
        SQLiteDatabase db = helper.getWritableDatabase();

        sql = "SELECT email FROM " + DBContract.contract.TABLE_NAME  + " WHERE email = '" + email + "'";

        //boolean isRegistered = helper.selectUser(db,email);

        cursor = db.rawQuery(sql, null);


        if(cursor.getCount() != 0) {
            Toast toast = Toast.makeText(this, "입력하신 Email은 이미 존재합니다", Toast.LENGTH_SHORT);
            // Todo : 비밀 번호 찾기 기능 구현 해야함.
            // Todo : backpress 로 바로 뒤로가게 하는게 아니라, 계정 검증절차(데이터베이스 select) 거쳐서 먼저 말해주는 것이 좋을듯.
            toast.show();
            onBackPressed();
        }else{
            Boolean insert_TF= helper.insertUser(db,email,name,password);
            if (insert_TF){
                Toast toast = Toast.makeText(getApplicationContext(),"가입이 완료되었습니다",Toast.LENGTH_SHORT);
                toast.show();
                setResult(RESULT_OK,null);
                //finish();
            } else{
                Toast toast = Toast.makeText(this,"회원가입이 실패하였습니다. 다시 시도해주세요",Toast.LENGTH_SHORT);
                toast.show();
                finish();
            }
        }
        db.close();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        onSignupSuccess();
                        // onSignupFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "입력사항을 다시 확인해주세요", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String password_confirm = _passwordTextConfirm.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if(!password.equals(password_confirm)){
            _passwordTextConfirm.setError("입력하신 비밀번호가 일치하지 않습니다");
            valid = false;
        } else{
            _passwordTextConfirm.setError(null);
        }

        return valid;
    }
}