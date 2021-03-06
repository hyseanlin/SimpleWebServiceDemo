package tw.edu.lhu.cin.sean.simplewebservicedemo;

import android.app.DownloadManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

/*
讓 Android 模擬器可以存取本機端的網頁服務
https://stackoverflow.com/questions/6760585/accessing-localhostport-from-android-emulator/56769746#56769746
* */
public class MainActivity extends AppCompatActivity implements WebServiceAsyncTask.TaskDelegate {
    EditText mEdtTxtName, mEdtTxtEmail, mEdtTxtPassword;
    Button mBtnRegister, mBtnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEdtTxtName = findViewById(R.id.edtTxtName);
        mEdtTxtEmail = findViewById(R.id.edtTxtEmail);
        mEdtTxtPassword = findViewById(R.id.edtTxtPassword);

        mBtnRegister = findViewById(R.id.btnRegister);
        mBtnRegister.setOnClickListener(mBtnRegisterOnClickedListener);
        mBtnLogin = findViewById(R.id.btnLogin);
        mBtnLogin.setOnClickListener(mBtnLoginOnClickedListener);
    }

    private View.OnClickListener mBtnRegisterOnClickedListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // 打包資料
            String strQueryString="";
            HashMap<String, String> data = new HashMap<>();
            data.put("email", mEdtTxtEmail.getText().toString());
            data.put("name", mEdtTxtName.getText().toString());
            data.put("password", mEdtTxtPassword.getText().toString());
            strQueryString = WebServiceAsyncTask.getPostDataString(data);

            String strUrlRegister = "http://10.0.2.2:8000/register";
            // 建構一個 AsyncTask 物件
            WebServiceAsyncTask wsTask = new WebServiceAsyncTask(MainActivity.this, QueryActivity.class);
            // 呼叫其 execute 方法(可夾帶參數)
            wsTask.execute(strUrlRegister, strQueryString, WebServiceAsyncTask.METHOD_POST);
        }
    };

    private View.OnClickListener mBtnLoginOnClickedListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // 打包資料
            String strQueryString="";
            HashMap<String, String> data = new HashMap<>();
            data.put("email", mEdtTxtEmail.getText().toString());
            data.put("password", mEdtTxtPassword.getText().toString());
            strQueryString = WebServiceAsyncTask.getPostDataString(data);

            String strUrlRegister = "http://10.0.2.2:8000/login";
            // 建構一個 AsyncTask 物件
            WebServiceAsyncTask wsTask = new WebServiceAsyncTask(MainActivity.this, QueryActivity.class);
            wsTask.setDelegate(MainActivity.this);
            // 呼叫其 execute 方法(可夾帶參數)
            wsTask.execute(strUrlRegister, strQueryString, WebServiceAsyncTask.METHOD_POST);
        }
    };

    @Override
    public void onTaskFinishGettingData(String result) {
        // In this example, do nothing for MainActivity
    }

    @Override
    public void onTaskEndWithResult(int status) {
        // In this example, do nothing for MainActivity
    }
}