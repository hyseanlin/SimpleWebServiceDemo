package tw.edu.lhu.cin.sean.simplewebservicedemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/* AsyncTask 是一個泛型類別
    可參考 AsynTask 的介紹如下：
    http://aiur3908.blogspot.com/2015/06/android-asynctask.html
 * */
public class WebServiceAsyncTask extends AsyncTask<String, Integer, String> {
    public static final String METHOD_POST = "POST";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_PATCH = "PATCH";
    public static final String METHOD_DELETE = "DELETE";

    private String WEB_SERVICE_TASK = "WEB_SERVICE_TASK";

    private ProgressDialog progressDialog;
    private Activity mActivity;
    private Class mClass;
    private String mBearerToken;

    // Declare a delegate with type of protocol declared in this task
    // c.f.: https://stackoverflow.com/questions/9458258/return-a-value-from-asynctask-in-android
    private TaskDelegate mDelegate;

    // Here is the task protocol to can delegate on other object
    public interface TaskDelegate {
        // Define you method headers to override
        void onTaskFinishGettingData(String result);
        void onTaskEndWithResult(int status);
    }

    public void setDelegate(TaskDelegate delegate) {
        mDelegate = delegate;
    }

    public WebServiceAsyncTask(Activity act, Class cls)
    {
        mActivity = act;
        mClass = cls;
        mBearerToken = null;
        mDelegate = null;
    }

    public WebServiceAsyncTask(Activity act, Class cls, String token)
    {
        mActivity = act;
        mClass = cls;
        mBearerToken = token;
        mDelegate = null;
    }

    static public String getPostDataString(HashMap<String, String> params)
    {
        //與String差別在於內存的消耗
        //他不能+但能使用append 及remove
        //在操作時不需重新實例化，節省內存
        //https://zhidao.baidu.com/question/56752235.html
        StringBuilder result = new StringBuilder();
        boolean first = true;
        //entrySet()取得所有資料
        //每個entry 是每個key與對應value的重新包裝
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (first)
                    first = false;
                else
                    //數據間用&隔開
                    result.append("&");

                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //初始化進度條並設定樣式及顯示的資訊。
        progressDialog = new ProgressDialog(mActivity);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... strings) {
        String requestURL = strings[0];
        String queryString = strings[1];
        String requestMethod = strings[2];
        // 進行 http 的協定處理並回傳結果
        String webRespone = "連接失敗";
        URL url = null;
        HttpURLConnection conn = null;
        try {
            url = new URL(requestURL);
            conn = (HttpURLConnection) url.openConnection();
            publishProgress(10);

            //設置從主機讀取超時
            conn.setReadTimeout(15000);
            //設置連線超時
            conn.setConnectTimeout(10000);

            switch (requestMethod)
            {
                default:
                case METHOD_GET:
                    conn.setRequestMethod(METHOD_GET);
                    conn.setRequestProperty("Content-length", "0");
                    break;
                case METHOD_POST:
                    conn.setRequestMethod(METHOD_POST);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    break;
                case METHOD_DELETE:
                case METHOD_PATCH:
                case METHOD_PUT:
                    conn.setRequestMethod(METHOD_POST);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("X-HTTP-Method-Override", requestMethod);
                    break;
            }

            if (mBearerToken != null)
                conn.setRequestProperty("Authorization", "Bearer " + mBearerToken);

            //允許從服務器獲得響應，能夠使用getInputStream
            conn.setDoInput(true);
            conn.setUseCaches(false);
            publishProgress(20);

            if (requestMethod != METHOD_GET) {
                //允許可調用getOutPutStream()從服務獲得字節輸出流
                conn.setDoOutput(true);
                //資料的目的與來源，銜接兩者為串流物件
                //若要將資料從來源取出，使用輸入串流
                //若將資料傳入目的地則使用輸出串流
                //連接 向伺服器進行輸出
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(queryString);
                //刷新寫入的outputstream
                writer.flush();
                //關閉
                writer.close();
                os.close();
            }
            publishProgress(60);

            //拿到請求結果
            int responseCode = conn.getResponseCode();
            Log.e(WEB_SERVICE_TASK, "Method: " + requestMethod +
                    "; URL: " + requestURL +
                    ", Reponse Code: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //獲取伺服器返回並進行讀取
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                webRespone = br.readLine();
                br.close();
                if (mDelegate != null)
                    mDelegate.onTaskFinishGettingData(webRespone);
            } else {
                webRespone = String.valueOf(responseCode);
            }
            publishProgress(75);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null)
                conn.disconnect();
        }
        publishProgress(100);
        return webRespone;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        //執行中 可以在這邊告知使用者進度
        super.onProgressUpdate(values);
        //取得更新的進度
        progressDialog.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(String webResponse) {
        //執行後 完成背景任務
        super.onPostExecute(webResponse);
        progressDialog.dismiss();
         /*
             JSONObject 的使用方法可參閱 https://jc7003.pixnet.net/blog/post/293480218-android-%E8%A7%A3%E6%9E%90json%E7%94%A8%E6%B3%95
         * */
        JSONObject responseJSON;
        try {
            responseJSON = new JSONObject(webResponse);
            int status = responseJSON.getInt("status");
            Log.e(WEB_SERVICE_TASK, "status: " + status);
            if (status == 1)
            {   // success
                if (mClass != null) {
                    // Prepare bundle data to pass through intent
                    Bundle bundle = new Bundle();
                    bundle.putString("WEB_DATA", responseJSON.getString("data"));
                    // Launch another activity using intent
                    Intent it = new Intent(mActivity, mClass);
                    it.putExtras(bundle);
                    mActivity.startActivity(it);
                }
                if (responseJSON.has("data"))
                {
                    Log.e(WEB_SERVICE_TASK, "data: " + responseJSON.getString("data"));
                }
            } else {
                // failed
                JSONObject errorJSON = responseJSON.getJSONObject("error");
                int errorCode = errorJSON.getInt("code");
                String msg = errorJSON.getString("message");
                Toast.makeText(mActivity, "Error message: " + msg, Toast.LENGTH_LONG).show();
                Log.e(WEB_SERVICE_TASK, "Error message: " + msg);
            }
            if (mDelegate != null)
                mDelegate.onTaskEndWithResult(status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}