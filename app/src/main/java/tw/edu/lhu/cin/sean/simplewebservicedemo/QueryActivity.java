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

public class QueryActivity extends AppCompatActivity {
    String mStrToken;
    Button mBtnQuery, mBtnUpdate;
    EditText mEdtTxtName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);

        mEdtTxtName = findViewById(R.id.edtTxtName);
        mBtnQuery = findViewById(R.id.btnQuery);
        mBtnUpdate = findViewById(R.id.btnUpdate);

        mBtnQuery.setOnClickListener(mBtnQueryOnClickedListener);
        mBtnUpdate.setOnClickListener(mBtnUpdateOnClickedListener);

        // Get the data from intent
        Bundle bundle = getIntent().getExtras();
        String webData = bundle.getString("WEB_DATA");
        // Decode the JSON string
        JSONObject responseJSON;
        try {
            responseJSON = new JSONObject(webData);
            mStrToken = responseJSON.getString("token");
            Log.e("WEB_DATA", "Token: " + mStrToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener mBtnQueryOnClickedListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // 打包資料
            String strQueryString="";

            String strUrlRegister = "http://10.0.2.2:8000/me";
            // 建構一個 AsyncTask 物件
            WebServiceAsyncTask wsTask = new WebServiceAsyncTask(QueryActivity.this, null);
            // 呼叫其 execute 方法(可夾帶參數)
            wsTask.execute(strUrlRegister, strQueryString, "GET", mStrToken);
        }
    };

    private View.OnClickListener mBtnUpdateOnClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 打包資料
            String strQueryString="";
            HashMap<String, String> data = new HashMap<>();
            data.put("name", mEdtTxtName.getText().toString());
            strQueryString = WebServiceAsyncTask.getPostDataString(data);

            String strUrlRegister = "http://10.0.2.2:8000/me";
            // 建構一個 AsyncTask 物件
            WebServiceAsyncTask wsTask = new WebServiceAsyncTask(QueryActivity.this, null);
            // 呼叫其 execute 方法(可夾帶參數)
            wsTask.execute(strUrlRegister, strQueryString, "PUT", mStrToken);
        }
    };
}