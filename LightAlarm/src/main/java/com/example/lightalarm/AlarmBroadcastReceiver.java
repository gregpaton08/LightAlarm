package com.example.lightalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Greg on 5/20/14.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {

    public static final String TURN_WIFI_OFF = "turn_wifi_off";

    Context m_context = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        m_context = context;

        SharedPreferences preferences = context.getSharedPreferences(MainActivity.PREFS_NAME, 0);
        String ip = preferences.getString(MainActivity.LIGHT_IP_KEY, MainActivity.PREFS_ERROR);
        if (ip.equals(MainActivity.PREFS_ERROR)) {
            return;
        }

        String url = "http://" + ip + "/light_on";

        String wifiStatus = "";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (false == wifiManager.isWifiEnabled()) {
            wifiStatus = TURN_WIFI_OFF;
            wifiManager.setWifiEnabled(true);
        }

        new HTTPRequest(context).execute(url, wifiStatus);
    }

    public class HTTPRequest extends AsyncTask<String, String, String> {

        private Context m_context;

        public HTTPRequest(Context context) {
            m_context = context;
        }

        private boolean isWifiConnected(Context context) {
            ConnectivityManager connMgr = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if (ConnectivityManager.TYPE_WIFI == networkInfo.getType()) {
                    return true;
                }
            }

            return false;
        }

        @Override
        protected String doInBackground(String... url) {
            HttpClient httpClient= new DefaultHttpClient();
            HttpResponse response;
            String ret = null;
            try {
                int timeout = 0;
                while (false == isWifiConnected(m_context) &&
                       timeout++ < 15) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {

                    }
                }

                URI uri = new URI(url[0]);
                response = httpClient.execute(new HttpGet(uri));
            } catch (ClientProtocolException e) {
                ret = "ClientProtocolException";
            } catch (IOException e) {
                ret = "IOException";
            } catch (URISyntaxException e) {
                ret = "URISyntaxException";
            }

            if (url.length > 1) {
                if (url[1].equals(TURN_WIFI_OFF)) {
                    WifiManager wifiManager = (WifiManager) m_context.getSystemService(Context.WIFI_SERVICE);
                    wifiManager.setWifiEnabled(false);
                }
            }

            return ret;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (null != result) {
                Toast.makeText(m_context, result, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
