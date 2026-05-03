package com.cam.scanner.scantopdf.android.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.dialogs.OcrChoosePlanDialog;
import com.cam.scanner.scantopdf.android.dialogs.OcrPlanDialog;
import com.cam.scanner.scantopdf.android.rest.UpdateEmailToApi;
import com.cam.scanner.scantopdf.android.rest.callbacks.OnApiResult;
import com.cam.scanner.scantopdf.android.util.Constants;

public class AskEmailActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = AskEmailActivity.class.getSimpleName();
    private EditText etEmail;
    private Button btSubmit;
    private View incPb;

    private int navToPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_ask_email);

        getFromIntent();
        findIds();

        if(etEmail.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        clickListeners();
    }

    private void getFromIntent() {
        if(getIntent() != null){
            navToPlan = getIntent().getIntExtra(Constants.EXTRA_NAV_TO_PLAN, 0);
        }
    }

    private void clickListeners() {
        btSubmit.setOnClickListener(this);
    }

    private void findIds() {
        etEmail = findViewById(R.id.et_email);
        btSubmit = findViewById(R.id.btn_submit);
        incPb = findViewById(R.id.progress_lay);
    }

    public static boolean checkEmail(String email) {
        return !(email == null || TextUtils.isEmpty(email)) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void openPremiumActivity() {
        Intent intent = new Intent(AskEmailActivity.this, PremiumActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_PREMIUM_YEALY);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_submit) {
            submitClicked();
        }
    }

    private void submitClicked() {
        String inputEmail = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(inputEmail)) {
            Toast.makeText(this, "" + getString(R.string.please_enter_email), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checkEmail(inputEmail)) {
            Toast.makeText(this, "" + getString(R.string.enter_valid_email_address), Toast.LENGTH_SHORT).show();
            return;
        }

        incPb.setVisibility(View.VISIBLE);
        UpdateEmailToApi updateEmailToApi = new UpdateEmailToApi(AskEmailActivity.this, new OnApiResult() {
            @Override
            public void onApiResponse() {
                Log.i(TAG, "onApiResponse");
                incPb.setVisibility(View.GONE);

                switch (navToPlan){
                    case Constants.PLAN_ACTIVITY_CHOOSE_PLAN:
                        openChoosePlanDialogActivity();
                        break;
                    case Constants.PLAN_ACTIVITY_OCR_MONTHLY:
                        openOcrMonthlyDialogActivity();
                        break;
                    case Constants.PLAN_ACTIVITY_PREMIUM:
                        openPremiumActivity();
                        break;
                }
                finish();
            }

            @Override
            public void onApiFailure() {
                Log.i(TAG, "onApiFailure");
            }
        });

        updateEmailToApi.doUpdateEmail(inputEmail);

    }

    private void openOcrMonthlyDialogActivity() {
        Intent intent = new Intent(AskEmailActivity.this, OcrPlanDialog.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_OCR_MONTHLY);
    }

    private void openChoosePlanDialogActivity() {
        Intent intent = new Intent(AskEmailActivity.this, OcrChoosePlanDialog.class);
        intent.putExtra(Constants.FROM_NAV_CHOOSE_PLAN, Constants.NAV_FROM_OCRACTIVITY);
        startActivity(intent);
    }
}