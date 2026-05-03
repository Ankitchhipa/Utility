package com.cam.scanner.scantopdf.android.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.dialogs.OcrPlanDialog;
import com.cam.scanner.scantopdf.android.util.Constants;

public class ChoosePlanActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btOcrMonthly, btPremium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_plan);

        findIds();
        clickListeners();
    }

    private void clickListeners() {
        btOcrMonthly.setOnClickListener(this);
        btPremium.setOnClickListener(this);
    }

    private void findIds() {
        btOcrMonthly = findViewById(R.id.btn_ocr_monthly);
        btPremium = findViewById(R.id.btn_premium);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_premium) {
            askToBePremium();
            finish();
        } else if (id == R.id.btn_ocr_monthly) {
            askToGetOcrMonthly();
            finish();
        }
    }

    private void askToGetOcrMonthly() {
        Intent intent = new Intent(ChoosePlanActivity.this, OcrPlanDialog.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_OCR_MONTHLY);
//        new OcrPlanDialog(ChoosePlanActivity.this).openDialog();
    }

    private void askToBePremium() {
        Intent intent = new Intent(ChoosePlanActivity.this, PremiumActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_PREMIUM_YEALY);
    }
}