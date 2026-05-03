package com.cam.scanner.scantopdf.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.cam.scanner.scantopdf.android.receiver.IPFetcher;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.PrefManager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class AfterIntsall {

    private static final String TAG = "AfterIntsall";
    private boolean fromAdword = false;
    private Context context;
    private PrefManager prefManager;

    public void getInstallDetails(Context context) {

        Log.e("" + TAG, "getInstallDetails method innn");

        InstallReferrerClient referrerClient;
        this.context = context;
        referrerClient = InstallReferrerClient.newBuilder(context).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {

                Log.e("" + TAG, "responseCode: " + responseCode);

                switch (responseCode) {
                    case InstallReferrerClient.InstallReferrerResponse.OK:
                        // Connection established.
//                        Toast.makeText(context, "okkk", Toast.LENGTH_SHORT).show();
                        ReferrerDetails response = null;
                        try {
                            response = referrerClient.getInstallReferrer();
                            String referrer = response.getInstallReferrer();
                            if (referrer != null) {

                                prefManager = new PrefManager(context);

                                referrer = removeUnwantedParams(referrer, new String[]{"ai", "gclid"});
                                saveParams(referrer);

                                Log.e("" + TAG, "innn");

                                /**
                                 * boolean to check whether tracking already done
                                 * as tracking was coming twice.
                                 */


                                boolean trackingdone = prefManager.isTrackingDone();
                                Log.i(TAG, "trackingdone: " + trackingdone);
                                if (trackingdone) {
                                    return;
                                }

                                referrer = referrer + "&isdebug=" + BuildConfig.DEBUG;

                                Log.i(TAG, "referrer: " + referrer);

                                prefManager.setTrackingUrl("" + referrer);
                                prefManager.setTrackingDone(true);
                                context.startService(new Intent(context, IPFetcher.class));
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        // API not available on the current Play Store app.
//                        Toast.makeText(context, "not supported", Toast.LENGTH_SHORT).show();
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        // Connection couldn't be established.
//                        Toast.makeText(context, " unavialable", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    private String removeUnwantedParams(String referrer, String[] filter) {

        try {
            if (referrer.contains("ai") && referrer.contains("gclid")) {
                fromAdword = true;
            }
        } catch (Exception e) {

        }
        StringBuffer allParams = new StringBuffer();
        try {
            Uri uri = Uri.parse(URLDecoder.decode("http://www.abcxyphoehelener.com/post.html?" + referrer,
                    "UTF-8"));
            List<String> stringList = new ArrayList<String>(Arrays.asList(filter)); //new ArrayList is only needed if you absolutely need an ArrayList

            Set<String> args = uri.getQueryParameterNames();
            for (String s : args) {
                if (!stringList.contains(s))
                    allParams.append(s + "=" + uri.getQueryParameter(s) + "&");
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return referrer;
        }
        return removeAmpersandMark(allParams.toString());
    }

    public void saveParams(String referrer) {
        try {
            Log.e(TAG, "doInBackground");
            String paramsReceived = "" + referrer;
            try {

                String decodedParams = "";
                Log.d("1" + TAG, "" + paramsReceived);
                if (paramsReceived.contains("%2526")) {
                    decodedParams = URLDecoder.decode(paramsReceived, "UTF-8");
                } else {
                    decodedParams = paramsReceived;
                }

                String postfix = "";
                if (!decodedParams.startsWith("utm_source")) {
                    int i = decodedParams.indexOf("utm_source");
                    if (i != -1) {
                        postfix = decodedParams.substring(i);
                    }
                } else {
                    postfix = decodedParams;
                }

                Log.d("2" + TAG, "" + postfix);
                String data = Constants.TRACKING_RECEIVER_DATA + postfix;

                Log.d("3" + TAG, "" + postfix);

                Uri uri;
                uri = Uri.parse(URLDecoder.decode(data, "UTF-8"));

                String utm_source = (uri.getQueryParameter("utm_source") == null) ? "" : uri.getQueryParameter("utm_source");
                String utm_medium = (uri.getQueryParameter("utm_medium") == null) ? "" : uri.getQueryParameter("utm_medium");
                String utm_term = (uri.getQueryParameter("utm_term") == null) ? "" : uri.getQueryParameter("utm_term");
                String utm_content = (uri.getQueryParameter("utm_content") == null) ? "" : uri.getQueryParameter("utm_content");
                String utm_campaign = (uri.getQueryParameter("utm_campaign") == null) ? "" : uri.getQueryParameter("utm_campaign");

                if (fromAdword)
                    utm_source = "adwords";

                /**
                 * to send default pixel in case no pixel received
                 */

                //1.2.0
                /*if (utm_medium == null || (!(utm_medium.toLowerCase().endsWith("_runt"))))*/
                if (TextUtils.isEmpty(utm_medium) || (!utm_medium.toLowerCase().endsWith("_runt")))
                    utm_medium = "" + Constants.DEFAULT_PIXEL;

                /*if (utm_medium == null)
                    utm_medium = "" + Constant.DEFAULT_PIXEL;*/
                ////

                /*if (utm_term == null || utm_term.equalsIgnoreCase("null"))*/
                if (TextUtils.isEmpty(utm_term) || (!utm_term.equalsIgnoreCase("null")))
                    utm_term = "" + Constants.SOURCE_INSTALL;

                /*utm_campaign = "mock_test_compaign";
                utm_source = "mock_test_source";
                utm_medium = "mock_test_source";
                utm_content = "mock_test_content";
                utm_term = "mock_test_term";*/

                prefManager.setTrackingCamp("" + utm_campaign);
                prefManager.setTrackingSource("" + utm_source);
                prefManager.setTrackingPub("" + utm_campaign);
//                utm_medium = "prakash_for_testing_runt";
                prefManager.setTrackingMedium("" + utm_medium);
                prefManager.setTrackingContent("" + utm_content);
                prefManager.setTrackingTerm("" + utm_term);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String removeAmpersandMark(String string) {
        if (string == null)
            return null;
        if (string.endsWith("&"))
            return string.substring(0, string.length() - 1);
        else
            return string;
    }

}
