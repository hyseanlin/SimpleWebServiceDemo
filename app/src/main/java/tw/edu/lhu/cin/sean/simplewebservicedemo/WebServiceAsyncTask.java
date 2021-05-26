package tw.edu.lhu.cin.sean.simplewebservicedemo;

import android.app.ProgressDialog;
import android.content.Context;
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
    private String LOG_ACTIVITY = "WEB_SERVICE_DEMO";
    private ProgressDialog progressDialog;
    private Context mActivity;
    private Class mClass;

    public WebServiceAsyncTask(Context context, Class cls)
    {
        mActivity = context;
        mClass = cls;
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
        Log.e("a:::::::::::::::", result.toString());
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
        String bearerToken = (strings.length == 4)?strings[3]:null;
        // 進行 http 的協定處理並回傳結果
        String webRespone = "連接失敗";
        URL url = null;
        HttpURLConnection conn = null;
        try {
            url = new URL(requestURL);
            conn = (HttpURLConnection) url.openConnection();
            publishProgress(20);

            //設置從主機讀取超時
            conn.setReadTimeout(15000);
            //設置連線超時
            conn.setConnectTimeout(10000);

            if (requestMethod == "GET") {
                conn.setRequestMethod("GET");

            } else if (requestMethod == "POST")
                conn.setRequestMethod("POST");
            else if (requestMethod == "PUT" || requestMethod == "PATCH")
            {
                conn.setRequestMethod("POST");
                conn.setRequestProperty("X-HTTP-Method-Override", requestMethod);
            }
            if (bearerToken != null)
            {
                if (requestMethod == "GET") {
                    conn.setRequestProperty("Content-length", "0");
                } else
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
            }


            //允許從服務器獲得響應，能夠使用getInputStream
            conn.setDoInput(true);
            //允許可調用getOutPutStream()從服務獲得字節輸出流
            if (requestMethod != "GET")
                conn.setDoOutput(true);
            conn.setUseCaches(false);
            publishProgress(30);

            if (requestMethod != "GET") {
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
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //獲取伺服器返回並進行讀取
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                webRespone = br.readLine();
                br.close();
            } else {
                Log.e(LOG_ACTIVITY, "Response code: " + responseCode);
                webRespone = String.valueOf(responseCode);
            }
            publishProgress(75);
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (conn != null)
            {
                conn.disconnect();
            }
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
            if (status == 1)
            {   // success
                if (mClass != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("WEB_DATA", responseJSON.getString("data"));

                    Intent it = new Intent(mActivity, mClass);
                    it.putExtras(bundle);
                    mActivity.startActivity(it);
                }
                if (responseJSON.has("data"))
                {
                    Log.e(LOG_ACTIVITY, "Data: " + responseJSON.getString("data"));
                }
            } else {
                // failed
                JSONObject errorJSON = responseJSON.getJSONObject("error");
                int errorCode = errorJSON.getInt("code");
                String msg = errorJSON.getString("message");
                Toast.makeText(mActivity, "Error message: " + msg, Toast.LENGTH_LONG).show();
                Log.e(LOG_ACTIVITY, "Error message: " + msg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}