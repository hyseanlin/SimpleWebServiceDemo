package tw.edu.lhu.cin.sean.simplewebservicedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class QueryActivity extends AppCompatActivity implements WebServiceAsyncTask.TaskDelegate {
    String mStrToken, mStrName, mStrEmail;
    Button mBtnQuery, mBtnUpdate, mBtnDelete, mBtnLogout;
    EditText mEdtTxtName, mEdtTxtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);

        mEdtTxtName = findViewById(R.id.edtTxtName);
        mEdtTxtEmail = findViewById(R.id.edtTxtEmail);
        mEdtTxtEmail.setEnabled(false);
        mBtnQuery = findViewById(R.id.btnQuery);
        mBtnUpdate = findViewById(R.id.btnUpdate);
        mBtnDelete = findViewById(R.id.btnDelete);
        mBtnLogout = findViewById(R.id.btnLogout);

        mBtnQuery.setOnClickListener(mBtnQueryOnClickedListener);
        mBtnUpdate.setOnClickListener(mBtnUpdateOnClickedListener);
        mBtnDelete.setOnClickListener(mBtnDeleteOnClickedListener);
        mBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        // Get the data from intent
        Bundle bundle = getIntent().getExtras();
        String webData = bundle.getString("WEB_DATA");
        // Decode the JSON string
        JSONObject responseJSON;
        try {
            responseJSON = new JSONObject(webData);
            mStrToken = responseJSON.getString("token");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        queryData();
    }

    private void queryData() {
        // 打包資料
        String strQueryString="";
        String strUrlRegister = "http://10.0.2.2:8000/me";
        // 建構一個 AsyncTask 物件
        WebServiceAsyncTask wsTask = new WebServiceAsyncTask(QueryActivity.this, null, mStrToken);
        wsTask.setDelegate(QueryActivity.this);
        // 呼叫其 execute 方法(可夾帶參數)
        wsTask.execute(strUrlRegister, strQueryString, WebServiceAsyncTask.METHOD_GET);
    }

    private View.OnClickListener mBtnQueryOnClickedListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            queryData();
        }
    };

    private View.OnClickListener mBtnUpdateOnClickedListener = new View.OnClickListener()  {
        @Override
        public void onClick(View v) {
            String name;
            name = mEdtTxtName.getText().toString();
            if (!name.isEmpty())
            {
                mStrName = name;
                // 打包資料
                String strQueryString="";
                HashMap<String, String> data = new HashMap<>();
                data.put("name", mStrName);
                strQueryString = WebServiceAsyncTask.getPostDataString(data);

                String strUrlRegister = "http://10.0.2.2:8000/me";
                // 建構一個 AsyncTask 物件
                WebServiceAsyncTask wsTask = new WebServiceAsyncTask(QueryActivity.this, null, mStrToken);
                wsTask.setDelegate(QueryActivity.this);
                // 呼叫其 execute 方法(可夾帶參數)
                wsTask.execute(strUrlRegister, strQueryString, WebServiceAsyncTask.METHOD_PUT);
            } else {
                Toast.makeText(QueryActivity.this, "The name field must be given!", Toast.LENGTH_LONG).show();
            }
        }
    };

    private View.OnClickListener mBtnDeleteOnClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 打包資料
            String strQueryString="";

            String strUrlRegister = "http://10.0.2.2:8000/me";
            // 建構一個 AsyncTask 物件
            WebServiceAsyncTask wsTask = new WebServiceAsyncTask(QueryActivity.this, null, mStrToken);
            wsTask.setDelegate(QueryActivity.this);
            // 呼叫其 execute 方法(可夾帶參數)
            wsTask.execute(strUrlRegister, strQueryString, WebServiceAsyncTask.METHOD_DELETE);
            finish();
        }
    };

    @Override
    public void onTaskFinishGettingData(String result) {
        /* NOTE: Here, CANNOT do any UI-related things (such as a call to Toast.makeText() ) */
        JSONObject responseJSON;
        try {
            responseJSON = new JSONObject(result);
            if (responseJSON.has("token"))
                mStrToken = responseJSON.getString("token");
            if (responseJSON.has("data"))
            {
                JSONObject dataJSON = responseJSON.getJSONObject("data");
                mStrName = dataJSON.getString("name");
                mStrEmail = dataJSON.getString("email");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskEndWithResult(int status) {
        /* NOTE: Here, FREE to do any UI-related things (such as a call to Toast.makeText() ) */
        if (status==1)
        {
            mEdtTxtEmail.setText(mStrEmail);
            mEdtTxtName.setText(mStrName);
        }
    }
}