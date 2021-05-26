package tw.edu.lhu.cin.sean.simplewebservicedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    String mStrToken;
    Button mBtnLogin;
    EditText mEdtTxtEmail, mEdtTxtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEdtTxtEmail = findViewById(R.id.edtTxtEmail);
        mEdtTxtPassword = findViewById(R.id.edtTxtPassword);

        mBtnLogin = findViewById(R.id.btnLogin);
        mBtnLogin.setOnClickListener(mBtnLoginOnClikeckedListener);

        // Get the passed data from intent
        Bundle bundle = getIntent().getExtras();
        String webData = bundle.getString("WEB_DATA");
        // Decode the JSON string
        JSONObject responseJSON;
        try {
            responseJSON = new JSONObject(webData);
            JSONObject dataJSON = responseJSON.getJSONObject("data");
            mStrToken = dataJSON.getString("token");
            Log.e("WEB_DATA", "Token: " + mStrToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener mBtnLoginOnClikeckedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 打包資料
            String strRequestMethod="POST";
            String strQueryString="";
            HashMap<String, String> data = new HashMap<>();
            data.put("email", mEdtTxtEmail.getText().toString());
            data.put("password", mEdtTxtPassword.getText().toString());
            strQueryString = WebServiceAsyncTask.getPostDataString(data);
            String strUrlRegister = "http://10.0.2.2:8000/login";
            // 建構一個 AsyncTask 物件
            WebServiceAsyncTask wsTask = new WebServiceAsyncTask(LoginActivity.this, null);
            // 呼叫其 execute 方法(可夾帶參數)
            wsTask.execute(strUrlRegister, strQueryString, strRequestMethod, mStrToken);
        }
    };
}