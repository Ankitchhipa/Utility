package com.cam.scanner.scantopdf.android.receiver;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

import com.cam.scanner.scantopdf.android.BuildConfig;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.EncryptedParams;
import com.cam.scanner.scantopdf.android.util.PrefManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

/**
 * Created by anis on 16-01-2018.
 */

public class IPFetcher extends IntentService {

    public static final String TAG = "IPFetcher";

    private HttpURLConnection urlConnection;
    private static final int RETRY_COUNT = 3;
    private String utm_source;
    private String utm_medium;
    private String utm_term;
    private String utm_content;
    private String utm_campaign;
    private static final String SEPERATOR = "|";

    private PrefManager prefManager;

    private Context context;
    private String ip;
    private String country;
    private int counter = 0;
    String encryptedData;
    private int failed;
    private int passed;
    private int time_out = 15000;

    public IPFetcher(String name) {
        super(name);
    }

    public IPFetcher() {
        super("IPFetcher");
    }

//    private final static String TAG = "IPFetcher";

    private final int TOTAL_RETRIES = 2;
    private int retryCount = 0;
    private int retryCountIp = 0;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        try {
            Log.d(TAG + " 1", "innn");
            context = getBaseContext();
            prefManager = new PrefManager(context);

            ip = downloadUrl(Constants.DOWNLOAD_URL); //1.2.0 (tracking)

            if (ip == null)
                ip = "";

        } catch (IOException e) {
            e.printStackTrace();
        }

        encrypt(prefManager.getTrackingUrl());
    }

    private String getCountryFromUrl(String getCountryUrl, String ip) {
        String data = "";

        if (ip != null && !ip.isEmpty()) {
            InputStream iStream = null;
            urlConnection = null;
            try {
                URL url = new URL(getCountryUrl + ip);
                urlConnection = (HttpURLConnection) url.openConnection();
                int timeOutMs = Constants.CONNECTION_TIMEOUT * 1000;
                urlConnection.setConnectTimeout(timeOutMs);
                urlConnection.connect();
                iStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
                StringBuffer sb = new StringBuffer();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
                br.close();
                Log.i(TAG, data);
            } catch (SocketTimeoutException e) {
                Log.e(TAG, e.toString());
                if (retryCount++ < TOTAL_RETRIES) {
                    getCountryFromUrl(Constants.GET_COUNTRY_URL, ip);
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            } finally {
                if (iStream != null) {
                    try {
                        iStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
//        data.replaceAll("^\"|\"$", "");
            if (data != null && data.length() > 0) {
                data = data.substring(1, data.length() - 1);
            }

            if (data == null) {
                data = "";
            }

            prefManager.setCountry(data);

            Log.i(TAG, "country from pref: " + prefManager.getCountry());
        }


        return data;
    }

    public void encrypt(String referrer) {
        try {

            String displayLanguage = "";
            try {

                try {
                    displayLanguage = Locale.getDefault().getDisplayLanguage();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                utm_source = (prefManager.getTrackingSource() == null) ? "" : prefManager.getTrackingSource();
                utm_medium = (prefManager.getTrackingMedium() == null) ? "" : prefManager.getTrackingMedium();
                utm_term = (prefManager.getTrackingTerm() == null) ? "" : prefManager.getTrackingTerm();
                utm_content = (prefManager.getTrackingContent() == null) ? "" : prefManager.getTrackingContent();
                utm_campaign = (prefManager.getTrackingCamp() == null) ? "" : prefManager.getTrackingCamp();

                getAdditionalParams(utm_term);

                final String deviceID = "" + Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

                //1.2.0 (tracking)
                prefManager.setDownloadIp(ip);
//                prefManager.setCountry(country);
                prefManager.setX_At(x_at);
                ////

                Uri builtUri = new Uri.Builder()
                        .appendQueryParameter("utm_source", utm_source + "")
                        .appendQueryParameter("lpid", "" + utm_term)
                        .appendQueryParameter("utm_pubid", "")
                        .appendQueryParameter("x-context", "" + utm_content)
                        .appendQueryParameter("pxl", "" + utm_medium)
                        .appendQueryParameter("utm_campaign", "" + utm_campaign)
                        .appendQueryParameter("referUrl", "" + referrer)
                        .appendQueryParameter("x-uid", "" + deviceID)
                        .appendQueryParameter("x-at", "" + x_at)
                        .appendQueryParameter("os", "" + Build.VERSION.SDK_INT)
                        .appendQueryParameter("x-dvname", "" + Build.BRAND + " " + Build.MODEL)
                        .appendQueryParameter("LANG", "" + displayLanguage)
                        .appendQueryParameter("x-version", "" /*+ BuildConfig.VERSION_CODE*/)
                        .appendQueryParameter("x-appip", "" + ip)
//                        .appendQueryParameter("test", ""+test )
                        .appendQueryParameter("isrooted", "" + getInt(isRooted()))
                        .build();

                /*Uri builtUri = new Uri.Builder()
                        .appendQueryParameter("utm_source", utm_source )
                        .appendQueryParameter("utm_campaign", utm_campaign)
                        .appendQueryParameter("utm_pubid", prefManager.getTrackingPub())
                        .appendQueryParameter("pxl",  utm_medium)
                        .build();*/

                Set<String> args = builtUri.getQueryParameterNames();

                StringBuffer allParams = new StringBuffer();
                for (String s : args) {
                    allParams.append(s + "=" + builtUri.getQueryParameter(s) + SEPERATOR);
                }

                allParams.append(x_at_pipeseprated);

                Uri finalUri = Uri.parse(builtUri.toString()).buildUpon().appendQueryParameter("x-var1", allParams.toString()).build();

                Log.i(TAG, "finalUri: " + finalUri);

                String string = finalUri.toString();

                Log.i(TAG, "finalUri string : " + string);

                String urlPram = removeQuestionMark(string);

                Log.i(TAG, "urlPram : " + urlPram);

                byte[] encryptedStr = EncryptedParams.encrypt(urlPram.getBytes());
                encryptedData = Base64.encodeToString(encryptedStr, Base64.DEFAULT);

                String finalUrl = Constants.TRACKING_URL + "t=" + encryptedData;
                Log.i(TAG + " 2", "url----" + finalUrl);
                sendData();

                if (Constants.LOG_TRACKING_URL) {
                    writeToFile(finalUrl, context);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeToFile(String data, Context context) {
        try {
            File file = new File(context.getExternalCacheDir(), "tracking.txt");
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(data.getBytes());
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void sendData() {

        InputStream inputStream = null;
        Log.i(TAG + " 3", "innn sendData");
        Log.i(TAG, "doInBackground");

        String server_response = null;
        HttpURLConnection urlConnection = null;

        URL url = null;
        try {

            Uri builtUri = Uri.parse("" + Constants.TRACKING_URL)
                    .buildUpon()
                    .appendQueryParameter("t", encryptedData + "")
                    .build();

            url = new URL("" + builtUri.toString());
            Log.i(TAG + " 4", "url----" + url);

            for (int i = 0; i < RETRY_COUNT; i++) {
                try {
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(time_out);

                    int responseCode = urlConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        inputStream = urlConnection.getInputStream();
                        server_response = "" + readStream(inputStream);

                        Log.i(TAG + " 5", "HTTP_OK");
                        break;
                    }

                } catch (Exception e) {
                    time_out = time_out + 15000;
                    e.printStackTrace();
                    Log.i(TAG + " 6", e.getMessage());
                } finally {

                    Log.i(TAG + " 7", "finally");

                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (urlConnection != null) {
                        try {
                            if (urlConnection != null)
                                urlConnection.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (urlConnection != null) {
                try {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                prefManager.setInstallTime(Calendar.getInstance().getTimeInMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int getInt(boolean rooted) {
        if (rooted)
            return 1;
        return 0;
    }

    private String removeQuestionMark(String string) {
        if (string == null)
            return null;
        if (string.startsWith("?"))
            return string.substring(1);
        else
            return string;
    }


    private String x_at = "";
    private String x_at_pipeseprated = "";

    /**
     * get comma seprated params from referral
     *
     * @param referral
     */
    private void getAdditionalParams(String referral) {
        try {

            if (TextUtils.isEmpty(referral))
                return;
            String refer = referral.toLowerCase();

            if (refer.contains("%253d")) {
                x_at = URLDecoder.decode(refer);// Some times referral received doubly encoded so encode it make it single encoded
            } else {
                if (refer.contains("%3d")) {
                    x_at = refer;
                } else {
                    x_at = URLEncoder.encode(refer);
                }
            }

            String[] splitedParams = x_at_pipeseprated.split("%253d");
            if (splitedParams != null) {
                for (int i = 0; i < splitedParams.length; i++) {
                    x_at_pipeseprated = x_at_pipeseprated + SEPERATOR + splitedParams[i];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the device is rooted.
     *
     * @return <code>true</code> if the device is rooted, <code>false</code> otherwise.
     */
    private static boolean isRooted() {
        return findBinary("su");
    }

    public static boolean findBinary(String binaryName) {
        boolean found = false;
        if (!found) {
            String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
                    "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
            for (String where : places) {
                if (new File(where + binaryName).exists()) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }


    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }

//    private void getIgnoredContent(String urlString) {
//        URL url;
//        String server_response = null;
//        HttpURLConnection urlConnection = null;
//
//
//        try {
//            url = new URL(urlString);
//            urlConnection = (HttpURLConnection) url.openConnection();
//            int responseCode = urlConnection.getResponseCode();
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                server_response = readStream(urlConnection.getInputStream());
//
//                if (server_response != null) {
//                    JSONObject jsonObject =  new JSONObject(server_response);
//                    ArrayList<String> deviceList = new ArrayList<>();
//                    ArrayList<String> ids = new ArrayList<>();
//                    int os = 15;
//                    if (jsonObject.has("os"))
//                        os = jsonObject.getInt("os");
//
//                    if (jsonObject.has("devices")){
//                        JSONArray deviceArr =  jsonObject.getJSONArray("devices");
//                        for (int i = 0; i < deviceArr.length(); i++){
//                            deviceList.add(deviceArr.getJSONObject(i).getString("name"));
//                        }
//                    }
//
//                    if (jsonObject.has("lpids")){
//                        JSONArray deviceArr =  jsonObject.getJSONArray("lpids");
//                        for (int i = 0; i < deviceArr.length(); i++){
//                            ids.add(deviceArr.getJSONObject(i).getString("id"));
//                        }
//                    }
//
//                    FreeAndroidCleaner.getInstance().pixelMaker =  new PixelMaker(deviceList, ids, os);
//                }
//            }
//
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        finally{
//            if (urlConnection != null){
//                try {
//                    urlConnection.disconnect();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    private void broadcastIPReceived(String ip) {
        Log.d("IPFetcher", "ip " + ip);
        try {
            Intent intent1 = new Intent("com.pcvark.ip.checker");
            intent1.putExtra("IP", ip);
            getBaseContext().sendBroadcast(intent1);
        } catch (Exception e) {
        }
    }

    private String downloadUrl(String strUrl) throws IOException {

        Log.d(TAG + " 2", "innn downloadUrl");

        String data = "";
        InputStream iStream = null;
        urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            int timeOutMs = Constants.CONNECTION_TIMEOUT * 1000;
            urlConnection.setConnectTimeout(timeOutMs);
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.i(TAG, "ip data " + data);
            br.close();

        } catch (SocketTimeoutException e) {
            Log.e(TAG, "SocketTimeoutException downloadUrl exception: " + e.toString());
            if (retryCountIp++ < TOTAL_RETRIES) {
                downloadUrl(Constants.DOWNLOAD_URL);
            }
        } catch (Exception e){
            Log.e(TAG, "Exception downloadUrl exception: " + e.toString());
        } finally {
            if (iStream != null)
                iStream.close();
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        if (data != null && !data.equalsIgnoreCase("")) {
            getCountryFromUrl(Constants.GET_COUNTRY_URL, data);
            Log.i(TAG, "getCountryFromUrl method called first time with " + data);
        }


        return data;
    }

}
