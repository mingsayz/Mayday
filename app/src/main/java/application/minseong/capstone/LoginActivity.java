package application.minseong.capstone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.Login;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.kakao.auth.ApiErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.nhn.android.naverlogin.OAuthLogin.mOAuthLoginHandler;

//import android.support.v7.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.input_email)
    EditText _emailText;
    @BindView(R.id.input_password)
    EditText _passwordText;
    @BindView(R.id.btn_login)
    Button _loginButton;
    @BindView(R.id.link_signup)
    TextView _signupLink;


    private SessionCallback sessionCallback;
    String sql = null;
    Cursor cursor = null;
    DBHelper helper;
    SQLiteDatabase database;
    int version = 1;

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    private Context mContext;



    private LoginButton btn_facebook_login;



    private LoginCallBack mLoginCallback;

//    private CallbackManager mCallbackManager;


    private CallbackManager callbackManager;

    com.kakao.usermgmt.LoginButton kakao_loginButton;
    private ImageView fakeKakao;

    LoginButton facebook_loginButton;
    private ImageView fakeFacebook;

    OAuthLoginButton naver_loginButton;
    private ImageView fakeNaver;



    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
//        mCallbackManager = CallbackManager.Factory.create();
        callbackManager = CallbackManager.Factory.create();
        facebook_loginButton = (LoginButton) findViewById(R.id.btn_facebook_login);
        facebook_loginButton.setReadPermissions("email");

        kakao_loginButton = (com.kakao.usermgmt.LoginButton) findViewById(R.id.btn_kakao_login);

        naver_loginButton = (OAuthLoginButton)findViewById(R.id.btn_naver_login);
        naver_loginButton.setOAuthLoginHandler(mOAuthLoginHandler);

        OAuthLogin mOAuthLoginModule = OAuthLogin.getInstance();
        mOAuthLoginModule.init(
                LoginActivity.this
                ,"HITMnjkBMT4csITjJSAT"
                ,"jhiuNwcvxX"
                ,"Mayday"
        );

        fakeNaver = (ImageView)findViewById(R.id.fake_naver);
        fakeNaver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                naver_loginButton.performClick();
            }
        });



        fakeKakao = (ImageView)findViewById(R.id.fake_kakao);
        fakeKakao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kakao_loginButton.performClick();
            }
        });

        fakeFacebook = (ImageView)findViewById(R.id.fake_facebook);
        fakeFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                facebook_loginButton.performClick();
            }
        });


        // Callback registration
        facebook_loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        mContext = getApplicationContext();

//        mCallbackManager = CallbackManager.Factory.create();

        mLoginCallback = new LoginCallBack();



//        btn_facebook_login = (LoginButton) findViewById(R.id.btn_facebook_login);
//
//        btn_facebook_login.setReadPermissions(Arrays.asList("public_profile", "email"));
//
//        btn_facebook_login.registerCallback(mCallbackManager, mLoginCallback);


        helper = new DBHelper(LoginActivity.this, DBContract.contract.TABLE_NAME, null, version);
        database = helper.getReadableDatabase();
//        database = helper.getWritableDatabase();
        //DB

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);

            }
        });

        sessionCallback = new SessionCallback();
        Session.getCurrentSession().addCallback(sessionCallback);
        Session.getCurrentSession().checkAndImplicitOpen();
    }




    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);
//        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
//        //progressDialog.setIndeterminate(true);
//        //progressDialog.setProgressStyle(progressDialog.STYLE_SPINNER);
//        progressDialog.setMessage("계정 정보를 확인 하는 중입니다");
//        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        // TODO: Implement your own authentication logic here.
        // TODO : please, make simple below things using another class and method.
        boolean isRegistered = helper.selectUser(database,email);

        if(!isRegistered){
            Toast toast = Toast.makeText(LoginActivity.this, "존재하지 않는 아이디입니다.", Toast.LENGTH_SHORT);
            toast.show();
            onLoginFailed();
        }

        boolean isCollectedPwd = helper.getPasswordFromId(database,email,password);

        if(!isCollectedPwd){
            Toast toast = Toast.makeText(LoginActivity.this, "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT);
            toast.show();
            onLoginFailed();
        }else{
            //로그인 성공

            Toast toast = Toast.makeText(LoginActivity.this, "로그인 성공! 반갑습니다", Toast.LENGTH_SHORT);
            toast.show();
            try{
                showProgressDialog();
            }catch (Exception e){
                e.printStackTrace();}
        }

    }

    public void showProgressDialog(){
        final ProgressDialog pDialog =  new ProgressDialog(this);
        pDialog.setMessage("Loading Data...");
        pDialog.setCancelable(false);
        pDialog.show();
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        Intent intent = new Intent(LoginActivity.this, GetImageAcitivity.class);
                        startActivity(intent);
                        // On complete call either onLoginSuccess or onLoginFailed
                        onLoginSuccess();
                        pDialog.dismiss();
                    }
                }, 3000);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
            }
        }
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

//        callbackManager.onActivityResult(requestCode, resultCode, data);
//        super.onActivityResult(requestCode, resultCode, data);
//
//        mCallbackManager.onActivityResult(requestCode, resultCode, data);
//        super.onActivityResult(requestCode,resultCode,data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        onLoginSuccess();
        super.onDestroy();
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "로그인에 실패하였습니다", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

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

        return valid;
    }

    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            // 로그인 세션이 열렸을때
            UserManagement.getInstance().me(new MeV2ResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    //로그인에 실패했을 때. 인터넷 연결이 불안정한 경우도 여기에 해당한다.

                    int result = errorResult.getErrorCode();

                    if (result == ApiErrorCode.CLIENT_ERROR_CODE) {
                        Toast.makeText(LoginActivity.this, "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "로그인 도중 오류가 발생했습니다: " + errorResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    // 로그인 도중 세션이 비정상적으로 닫혔을때
                    Toast.makeText(LoginActivity.this, "세션이 닫혔습니다. 다시 시도해 주세요: " + errorResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(MeV2Response result) {
                    //로그인에 성공했을때
                    Intent intent = new Intent(LoginActivity.this, GetImageAcitivity.class);
//                    intent.putExtra("name", result.getNickname());
//                    intent.putExtra("profile", result.getProfileImagePath());
                    startActivity(intent);
                    finish();
                }
            });
        }

        @Override
        public void onSessionOpenFailed(KakaoException e) {
            //로그인 세션이 정상적으로 열리지 않았을때
            if (e != null) {
                Toast.makeText(LoginActivity.this, "로그인 도중 오류가 발생했습니다. 인터넷 연결을 확인해주세요: " + e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}