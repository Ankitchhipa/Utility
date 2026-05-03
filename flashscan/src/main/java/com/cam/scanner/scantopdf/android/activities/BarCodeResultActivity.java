package com.cam.scanner.scantopdf.android.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.cam.scanner.scantopdf.android.BuildConfig;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.barcodereader.BarcodeReaderActivity;
import com.cam.scanner.scantopdf.android.barcodereader.BarcodeType;
import com.cam.scanner.scantopdf.android.barcodereader.model.MultiplePhones;
import com.cam.scanner.scantopdf.android.barcodereader.model.ResultBarCode;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.vision.barcode.Barcode;
import com.itl.commonres.utils.AdsPlacementsEnum;
import com.itl.commonres.utils.CommonMethods;

import java.util.ArrayList;

public class BarCodeResultActivity extends BaseActivity implements View.OnClickListener {

    private static final int BARCODE_READER_ACTIVITY_REQUEST = 101;
    private TextView tv_result_bar_code, tv_date, toolbar_title, tv_copy, tv_share, tv_rescan;
    private String barQrCodeResultTxt;
    private FlashScanUtil util;
    private PrefManager prefManager;
    private Context context;
    private ImageView iv_tick, iv_search, iv_home;
    private Button btn_open_result, btn_open_result_other;
    private static final String TAG = BarCodeResultActivity.class.getSimpleName();
    private Barcode barcode;
    private ResultBarCode resultBarCode;
    //    private FrameLayout nativeLargeAdQRcodeResult;
    private FrameLayout ad_view_banner_container;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_code_result);
        initObjects();
        findViewIds();
        setClickListeners();
        getIntentData();
        checkAds();
    }

    private void setClickListeners() {
        tv_copy.setOnClickListener(this);
        btn_open_result.setOnClickListener(this);
        tv_share.setOnClickListener(this);
        tv_rescan.setOnClickListener(this);
        iv_home.setOnClickListener(this);
        btn_open_result_other.setOnClickListener(this);
    }

    private void initObjects() {
        context = this;
        util = new FlashScanUtil(context);
        prefManager = new PrefManager(context);
    }

    private void getIntentData() {
        ResultBarCode barcode = null;
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.SCANNED_BARCODE)) {
            barcode = getIntent().getParcelableExtra(Constants.PutExtraConstants.SCANNED_BARCODE);
        }
        if (barcode != null) {
            /*setUiData(barcode);*/
            setResultUi(barcode);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        checkAds();
    }

    private void checkAds() {
        if (util.isConnectingToInternet() && !prefManager.isAppAdFree() && com.itl.commonres.utils.Constants.isAdShow && CommonMethods.isAdActive(AdsPlacementsEnum.SH_QR_BAR_CODE_RESULT.getValue())) {
            ad_view_banner_container.setVisibility(View.VISIBLE);
            callAdaptiveAd();
        } else {
            Log.e("Mobibuz : ", "Ad Not Showing");
            ad_view_banner_container.setVisibility(View.GONE);
        }
    }

    private void callAdaptiveAd() {
        AdView adView = new AdView(this);
        //adView.setAdSize(AdSize.BANNER);
        AdSize adSize = getAdSize();
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);
        adView.setAdUnitId(BuildConfig.AD_UNIT_ID_NEW_DOC_IMAGE_EDIT_SCREEN_ADAPTIVE_AD);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                ad_view_banner_container.removeAllViews();
            }

            @Override
            public void onAdLoaded() {
                ad_view_banner_container.removeAllViews();
                ad_view_banner_container.addView(adView);
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private void setResultUi(ResultBarCode barcode) {
        this.resultBarCode = barcode;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tickAnimation();
            }
        }, Constants.TICK_ANIMATION_DELAY);
        if (!TextUtils.isEmpty(barcode.getRawValue())) {
            tv_result_bar_code.setText(barcode.getRawValue());
        }
        int valueFormat = barcode.getValueFormat();
        if (!BarcodeType.isBarcodeTypeExist(valueFormat))
            return;
        BarcodeType barcodeType = BarcodeType.forValue(valueFormat);
        switch (barcodeType) {
            case CalendarEvent:
                Log.e(TAG, "Barcode CALENDAR_EVENT");
                btn_open_result.setText(getString(R.string.add_calendar_event));
                btn_open_result_other.setVisibility(View.GONE);
                break;
            case ContactInfo:
                Log.e(TAG, "Barcode CONTACT_INFO");
                btn_open_result.setText(getString(R.string.open_contact_info));
                btn_open_result_other.setVisibility(View.GONE);
                break;
            case Email:
                Log.e(TAG, "Barcode EMAIL");
                btn_open_result.setText(getString(R.string.open_email));
                btn_open_result_other.setVisibility(View.GONE);
                break;
            case Geo:
                Log.e(TAG, "Barcode GEO");
                btn_open_result.setText(getString(R.string.open_map));
                btn_open_result_other.setVisibility(View.GONE);
                break;
            case Phone:
                Log.e(TAG, "Barcode PHONE");
                btn_open_result.setText(getString(R.string.add_to_contacts));
                btn_open_result_other.setVisibility(View.VISIBLE);
                btn_open_result_other.setText(getString(R.string.call));
                break;
            case Product:
                Log.e(TAG, "Barcode PRODUCT");
                btn_open_result.setText(getString(R.string.web_search));
                btn_open_result_other.setVisibility(View.GONE);
                break;
            case Sms:
                Log.e(TAG, "Barcode SMS");
                btn_open_result.setText(getString(R.string.sms));
                btn_open_result_other.setVisibility(View.GONE);
                break;
            case Text:
                Log.e(TAG, "Barcode TEXT");
                btn_open_result.setText(getString(R.string.web_search));
                btn_open_result_other.setVisibility(View.GONE);
                break;
            case Url:
                Log.e(TAG, "Barcode URL");
                btn_open_result.setText(getString(R.string.open_url));
                btn_open_result_other.setVisibility(View.GONE);
                break;
            case Wifi:
                Log.e(TAG, "Barcode WIFI");
                btn_open_result.setText(getString(R.string.open_wifi));
                btn_open_result_other.setVisibility(View.GONE);
                break;
            case Default:
                Log.e(TAG, "Barcode default");
                btn_open_result.setText(getString(R.string.web_search));
                btn_open_result_other.setVisibility(View.GONE);
                break;
            default:
        }
    }

    /*private void setUiData(Barcode barcode) {
        this.barcode = barcode;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tickAnimation();
            }
        }, Constants.TICK_ANIMATION_DELAY);
        String rawValue = barcode.rawValue;
        if (!TextUtils.isEmpty(rawValue)) {
            tv_result_bar_code.setText(rawValue);
        }
        int valueFormat = barcode.valueFormat;
        switch (valueFormat) {
            case Barcode.CALENDAR_EVENT:
                Log.e(TAG, "Barcode CALENDAR_EVENT");
                btn_open_result.setText(getString(R.string.add_calendar_event));
                btn_open_result_other.setVisibility(View.GONE);
                String description = barcode.calendarEvent.description;
                Log.e(TAG, "description : " + description);
                String location = barcode.calendarEvent.location;
                Log.e(TAG, "location : " + location);
                String organizer = barcode.calendarEvent.organizer;
                Log.e(TAG, "organizer : " + organizer);
                String status = barcode.calendarEvent.status;
                Log.e(TAG, "status : " + status);
                String summary = barcode.calendarEvent.summary;
                Log.e(TAG, "summary : " + summary);
                break;
            case Barcode.CONTACT_INFO:
                Log.e(TAG, "Barcode CONTACT_INFO");
                btn_open_result.setText(getString(R.string.open_contact_info));
                btn_open_result_other.setVisibility(View.GONE);
                break;
            *//*case Barcode.DRIVER_LICENSE:
                Log.e(TAG, "Barcode DRIVER_LICENSE");
                btn_open_result.setText(getString(R.string.open_driving_license));
                btn_open_result_other.setVisibility(View.GONE);
                break;*//*
            case Barcode.EMAIL:
                Log.e(TAG, "Barcode EMAIL");
                btn_open_result.setText(getString(R.string.open_email));
                btn_open_result_other.setVisibility(View.GONE);
                break;
            case Barcode.GEO:
                Log.e(TAG, "Barcode GEO");
                btn_open_result.setText(getString(R.string.open_map));
                btn_open_result_other.setVisibility(View.GONE);
                break;
            *//*case Barcode.ISBN:
                Log.e(TAG, "Barcode ISBN");
                btn_open_result.setText(getString(R.string.open_isbn));
                btn_open_result_other.setVisibility(View.GONE);
                break;*//*
            case Barcode.PHONE:
                Log.e(TAG, "Barcode PHONE");
                btn_open_result.setText(getString(R.string.add_to_contacts));
                btn_open_result_other.setVisibility(View.VISIBLE);
                btn_open_result_other.setText(getString(R.string.call));
                break;
            case Barcode.PRODUCT:
                Log.e(TAG, "Barcode PRODUCT");
                btn_open_result.setText(getString(R.string.web_search));
                btn_open_result_other.setVisibility(View.GONE);
                break;
            case Barcode.SMS:
                Log.e(TAG, "Barcode SMS");
                btn_open_result.setText(getString(R.string.sms));
                btn_open_result_other.setVisibility(View.GONE);
                break;
            case Barcode.TEXT:
                Log.e(TAG, "Barcode TEXT");
                btn_open_result.setText(getString(R.string.web_search));
                btn_open_result_other.setVisibility(View.GONE);
                break;
            case Barcode.URL:
                Log.e(TAG, "Barcode URL");
                btn_open_result.setText(getString(R.string.open_url));
                btn_open_result_other.setVisibility(View.GONE);
                break;
            case Barcode.WIFI:
                Log.e(TAG, "Barcode WIFI");
                btn_open_result.setText(getString(R.string.open_wifi));
                btn_open_result_other.setVisibility(View.GONE);
                String ssid = barcode.wifi.ssid;
                Log.e(TAG, "ssid : " + ssid);
                String password = barcode.wifi.password;
                Log.e(TAG, "password : " + password);
                int encryptionType = barcode.wifi.encryptionType;
                Log.e(TAG, "encryptionType : " + encryptionType);
                break;
            default:
                Log.e(TAG, "Barcode default");
                btn_open_result.setText(getString(R.string.web_search));
                btn_open_result_other.setVisibility(View.GONE);
                break;
        }
    }*/

    private void tickAnimation() {
        Drawable drawable = iv_tick.getDrawable();
        if (drawable instanceof AnimatedVectorDrawableCompat) {
            AnimatedVectorDrawableCompat animatedVectorDrawableCompat = (AnimatedVectorDrawableCompat) drawable;
            animatedVectorDrawableCompat.start();
        } else if (drawable instanceof AnimatedVectorDrawable) {
            AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) drawable;
            animatedVectorDrawable.start();
        }
    }

    private void findViewIds() {
        tv_result_bar_code = findViewById(R.id.tv_result_bar_code);
        tv_date = findViewById(R.id.tv_date);
        String currentDateAndTime = util.getCurrentDateAndTime();
        if (!TextUtils.isEmpty(currentDateAndTime))
            tv_date.setText(currentDateAndTime);
        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(getString(R.string.qr_bar_code_result));
        tv_share = findViewById(R.id.tv_share);
        tv_copy = findViewById(R.id.tv_copy);
        btn_open_result = findViewById(R.id.btn_open_result);
        iv_tick = findViewById(R.id.iv_tick);
        tv_rescan = findViewById(R.id.tv_rescan);
        iv_search = findViewById(R.id.iv_search);
        iv_search.setVisibility(View.GONE);
        iv_home = findViewById(R.id.iv_home);
        btn_open_result_other = findViewById(R.id.btn_open_result_other);
//        nativeLargeAdQRcodeResult = findViewById(R.id.nativeLargeAdQRcodeResult);
        ad_view_banner_container = findViewById(R.id.ad_view_banner_container);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_home) {
            onBackPressed();
        } else if (id == R.id.tv_copy) {
            String textToBeCopy = tv_result_bar_code.getText().toString().trim();
            if (!TextUtils.isEmpty(textToBeCopy)) {
//                util.logQrBarCodeResultEvents(Constants.FirebaseClickEvents.QR_BARCODE_RESULT_COPY);
                util.copyToClipboard(textToBeCopy);
            } else {
                util.showSnackBar(findViewById(android.R.id.content), getString(R.string.oops_no_txt_found));
            }
        } else if (id == R.id.btn_open_result) {/*String textToBeSearch = tv_result_bar_code.getText().toString().trim();
                if (!TextUtils.isEmpty(textToBeSearch)) {
                    util.queryTextOnBrowser(textToBeSearch);
                } else {
                    util.showSnackBar(findViewById(android.R.id.content), getString(R.string.oops_no_txt_found));
                }*/

            if (resultBarCode == null)
                return;
            int valueFormat = resultBarCode.getValueFormat();
            if (!BarcodeType.isBarcodeTypeExist(valueFormat))
                return;
            BarcodeType barcodeType = BarcodeType.forValue(valueFormat);
            switch (barcodeType) {
                case CalendarEvent:
                    int startDay = resultBarCode.getCalendarEvent().getStartDay();
                    int startMonth = resultBarCode.getCalendarEvent().getStartMonth();
                    int startYear = resultBarCode.getCalendarEvent().getStartYear();
                    long startTimeInMillis = util.getTimeInMillis(startDay, startMonth - 1, startYear);
                    Log.e(TAG, "startTimeInMillis : " + startTimeInMillis);
                    int endDay = resultBarCode.getCalendarEvent().getEndDay();
                    int endMonth = resultBarCode.getCalendarEvent().getEndMonth();
                    int endYear = resultBarCode.getCalendarEvent().getEndYear();
                    long endTimeInMillis = util.getTimeInMillis(endDay, endMonth - 1, endYear);
                    Log.e(TAG, "endTimeInMillis : " + endTimeInMillis);
                    String description = resultBarCode.getCalendarEvent().getDescription();
                    String location = resultBarCode.getCalendarEvent().getLocation();
                    String organizer = resultBarCode.getCalendarEvent().getOrganizer();
                    String status = resultBarCode.getCalendarEvent().getStatus();
                    String summary = resultBarCode.getCalendarEvent().getSummary();
                    util.addEventToCalendar(summary, location, startTimeInMillis, endTimeInMillis, description, organizer, status);
                    break;
                case ContactInfo:
                    String formattedName = resultBarCode.getContactInfo().getName();
                    String phone = resultBarCode.getContactInfo().getPhoneNumber();
                    String organization = resultBarCode.getContactInfo().getOrganization();
                    /*String email = resultBarCode.getContactInfo().getEmail();*/
                    ArrayList<String> emails = resultBarCode.getContactInfo().getEmails();
                    String title = resultBarCode.getContactInfo().getTitle();
                    String address = resultBarCode.getContactInfo().getAddress();
                    String[] urls = resultBarCode.getContactInfo().getUrls();
                    ArrayList<MultiplePhones> multiplePhones = resultBarCode.getContactInfo().getMultiplePhones();
                    Log.e(TAG, "ContactInfo : " + " FormattedName : " + formattedName);
                    /*Log.e(TAG, "ContactInfo : " + " email : " + email);*/
                    Log.e(TAG, "ContactInfo : " + " phone : " + phone);
                    Log.e(TAG, "ContactInfo : " + " organization : " + organization);
                    Log.e(TAG, "ContactInfo : " + " title : " + title);
                    Log.e(TAG, "ContactInfo : " + " address : " + address);
                    util.insertContactInfo(formattedName, emails, organization, title, address, urls, multiplePhones);
                    break;
                case Email:
                    String emailAddress = resultBarCode.getEmail().getAddress();
                    String subject = resultBarCode.getEmail().getSubject();
                    String body = resultBarCode.getEmail().getBody();
                    util.intentToEmail(emailAddress, subject, body);
                    break;
                case Geo:
                    double lat = resultBarCode.getGeo().getLat();
                    double lng = resultBarCode.getGeo().getLng();
                    util.openGoogleMap(lat, lng);
                    break;
                case Phone:
                    String number = resultBarCode.getPhone().getNumber();
                    util.addToContacts(number);
                    break;
                case Product:
                    String productText = resultBarCode.getProduct().getProductText();
                    if (!TextUtils.isEmpty(productText)) {
                        util.queryTextOnBrowser(productText);
                    } else {
                        util.showSnackBar(findViewById(android.R.id.content), getString(R.string.oops_no_txt_found));
                    }
                    break;
                case Sms:
                    String phoneNumber = resultBarCode.getSms().getPhoneNumber();
                    String message = resultBarCode.getSms().getMessage();
                    util.sendSms(phoneNumber, message);
                    break;
                case Text:
                    String text = resultBarCode.getText().getText();
                    if (!TextUtils.isEmpty(text)) {
                        util.queryTextOnBrowser(text);
                    } else {
                        util.showSnackBar(findViewById(android.R.id.content), getString(R.string.oops_no_txt_found));
                    }
                    break;
                case Url:
                    String url = resultBarCode.getUrl().getUrl();
                    util.intentToBrowser(url);
                    break;
                case Wifi:
                    String ssid = resultBarCode.getWifi().getSsid();
                    String password = resultBarCode.getWifi().getPassword();
                    int encryptionType = resultBarCode.getWifi().getEncryptionType();
                    util.openWifiSettings();
                    break;
                case Default:
                    String defaultText = resultBarCode.getaDefault().getDefaultText();
                    if (!TextUtils.isEmpty(defaultText)) {
                        util.queryTextOnBrowser(defaultText);
                    } else {
                        util.showSnackBar(findViewById(android.R.id.content), getString(R.string.oops_no_txt_found));
                    }
                    break;
            }
        } else if (id == R.id.btn_open_result_other) {
            if (resultBarCode == null)
                return;
            int value = resultBarCode.getValueFormat();
            if (!BarcodeType.isBarcodeTypeExist(value))
                return;
            BarcodeType type = BarcodeType.forValue(value);
            switch (type) {
                case Phone:
                    String number = resultBarCode.getPhone().getNumber();
                    util.dialCall(number);
                    break;
            }
        } else if (id == R.id.tv_share) {
            String textToBeShare = tv_result_bar_code.getText().toString().trim();
            if (!TextUtils.isEmpty(textToBeShare)) {
//                util.logQrBarCodeResultEvents(Constants.FirebaseClickEvents.QR_BARCODE_RESULT_SHARE);
                util.shareTextContent(textToBeShare);
            } else {
                util.showSnackBar(findViewById(android.R.id.content), getString(R.string.oops_no_txt_found));
            }
        } else if (id == R.id.tv_rescan) {
            openBarCodeReaderActivity();
//            util.logQrBarCodeResultEvents(Constants.FirebaseClickEvents.QR_BARCODE_RESULT_RESCAN);
        }
    }

    private void openBarCodeReaderActivity() {
        /*Intent launchIntent = BarcodeReaderActivity.getLaunchIntent(this, true, false);*/
        Intent intent = new Intent(context, BarcodeReaderActivity.class);
        intent.putExtra(BarcodeReaderActivity.KEY_AUTO_FOCUS, true);
        intent.putExtra(BarcodeReaderActivity.KEY_USE_FLASH, false);
        startActivityForResult(intent, BARCODE_READER_ACTIVITY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BARCODE_READER_ACTIVITY_REQUEST:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        ResultBarCode barCode = data.getParcelableExtra(BarcodeReaderActivity.KEY_CAPTURED_BARCODE);
                        if (barCode != null) {
                            setResultUi(barCode);
                        }
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (iv_tick != null) {
            iv_tick.clearAnimation();
        }
    }
}
