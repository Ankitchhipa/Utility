package com.cam.scanner.scantopdf.android.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.activities.OcrActivity;
import com.cam.scanner.scantopdf.android.activities.PremiumActivity;
import com.cam.scanner.scantopdf.android.databinding.OcrChoosePlanDialogBinding;
import com.cam.scanner.scantopdf.android.db.DBHandler;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;


public class OcrChoosePlanDialog extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = OcrChoosePlanDialog.class.getSimpleName();
    OcrChoosePlanDialogBinding binding;
    private DBHandler dbHandler;
    private PrefManager prefManager;

    private int fromNav;
    private FlashScanUtil flashScanUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DataBindingUtil.setContentView(this, R.layout.ocr_choose_plan_dialog);

        initObjs();
        getFromIntent();

        binding.llPremiumPlan.setOnClickListener(this);
        binding.llOcrMonthly.setOnClickListener(this);
    }

    private void getFromIntent() {
        fromNav = getIntent().getIntExtra(Constants.FROM_NAV_CHOOSE_PLAN, 0);
    }

    private void initObjs() {
        prefManager = new PrefManager(this);
        flashScanUtil = new FlashScanUtil(this);
        dbHandler = AppController.getINSTANCE().dbHandler;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ll_premium_plan) {
            askToBePremium();
//                finish();
        } else if (id == R.id.ll_ocr_monthly) {
            askToGetOcrMonthly();
//                finish();
        }
    }

    private void askToGetOcrMonthly() {
        Intent intent = new Intent(OcrChoosePlanDialog.this, OcrPlanDialog.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_OCR_MONTHLY);
//        new OcrPlanDialog(ChoosePlanActivity.this).openDialog();
    }

    private void askToBePremium() {
        Intent intent = new Intent(OcrChoosePlanDialog.this, PremiumActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_PREMIUM_YEALY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.REQUEST_CODE_PREMIUM_YEALY:
                Log.i(TAG, "OcrActivity REQUEST_CODE_PREMIUM_YEALY");
                if (resultCode == RESULT_OK) {
                    //PREMIUM taken
                    handlePremium();
                    if(prefManager.getPurchasedPlanName()==Constants.BUY_NOW_YEARLY) {
                        flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.premium_yearly_success_msg, getString(R.string.app_name)));
                    }
                   /* else{
                        flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.premium_quarterly_success_msg));
                    }*/
//                    flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.premium_yearly_success_msg));
                    /*try {
                        reCreate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                }
                break;
            case Constants.REQUEST_CODE_OCR_MONTHLY:
                Log.i(TAG, "OcrActivity REQUEST_CODE_OCR_MONTHLY");
                if (resultCode == RESULT_OK) {
                    //OCR monthly taken
                    handleOcrMonthly();
                    flashScanUtil.showSnackBar(findViewById(android.R.id.content), getString(R.string.ocr_monthly_success_msg, getString(R.string.app_name)));
                    /*try {
                        reCreate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                }
                break;
        }
    }

    private void handleOcrMonthly() {
        prefManager.setOcrMonthly(true);
        setResult(RESULT_OK);
        if (fromNav == Constants.NAV_FROM_OCRACTIVITY) {
            Intent intent = new Intent(OcrChoosePlanDialog.this, OcrActivity.class);
            intent.putExtra("DISPLAY_MSG",getString(R.string.ocr_monthly_success_msg, getString(R.string.app_name)));
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        } else {
            finish();
        }
    }

    private void handlePremium() {
//        prefManager.setPremiumYearly(true);
        setResult(RESULT_OK);
        if (fromNav == Constants.NAV_FROM_OCRACTIVITY) {
            Intent intent = new Intent(OcrChoosePlanDialog.this, OcrActivity.class);
            intent.putExtra("DISPLAY_MSG",getString(R.string.premium_yearly_success_msg, getString(R.string.app_name)));
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        } else {
            finish();
        }
    }

}
