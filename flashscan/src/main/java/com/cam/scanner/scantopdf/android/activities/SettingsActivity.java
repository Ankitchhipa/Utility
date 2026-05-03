package com.cam.scanner.scantopdf.android.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.BuildConfig;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.adapters.PdfPageSizesAdapter;
import com.cam.scanner.scantopdf.android.models.PageSize;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.itl.commonres.utils.AdStatusInterface;
import com.itl.commonres.utils.CommonMethods;

import java.util.List;

import static com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_RIGHT;

public class SettingsActivity extends BaseActivity implements View.OnClickListener, AdStatusInterface {

    private TextView tv_toolbar, tv_default_sorting, tv_pdf_page_size, btn_cancel, btn_done, btn_cancel_pdf_pages, btn_done_pdf_pages, tv_ad;
    private ImageView iv_back_toolbar, iv_sort_icon, iv_arrow, iv_pdf_page_size, iv_pdf_page_size_arrow;
    private PrefManager prefManager;
    private Context context;
    private LinearLayout ll_sorting, ll_expanded_sort_lay, ll_pdf_page_size, ll_expanded_lay_pdf_pages;
    private RadioButton rb_modification_time_ascending, rb_modification_time_descending, rb_name_a_to_z, rb_name_z_to_a;
    private RecyclerView rv_pdf_pages;
    private FlashScanUtil flashScanUtil;
    private PdfPageSizesAdapter pdfPageSizesAdapter;
    private SwitchCompat chkBoxStrongShadow,autoCrop;
    private LinearLayout nativeAdSettings;
    private FrameLayout customNativeAdInSettings,nativeSmallAdDuplicatePhoto;
    CardView ad_view_banner_container;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initObjects();
        findViewIds();
        setClickListeners();


        /*populatePdfPagesRV();*/
    }

    private void populatePdfPagesRV() {
        List<PageSize> pageSizeList = flashScanUtil.getPageSizeList();
        if (pageSizeList != null && !pageSizeList.isEmpty()) {
            for (PageSize pageSize : pageSizeList) {
                if (pageSize.getSizeValue().equalsIgnoreCase(prefManager.getSelectedPdfSizeForWholeApp())) {
                    pageSize.setChecked(true);
                    /*pageSize.setSizeKey(pageSize.getSizeKey() + " " + "(" + getString(R.string.str_default) + ")");*/
                }
            }

            pdfPageSizesAdapter = new PdfPageSizesAdapter(context, pageSizeList);
            rv_pdf_pages.setAdapter(pdfPageSizesAdapter);
        }
    }

    private void initObjects() {
        context = this;
        prefManager = new PrefManager(context);
        flashScanUtil = new FlashScanUtil(context);
    }

    private void setClickListeners() {
        iv_back_toolbar.setOnClickListener(this);
        ll_sorting.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        btn_done.setOnClickListener(this);
        ll_pdf_page_size.setOnClickListener(this);
        btn_cancel_pdf_pages.setOnClickListener(this);
        btn_done_pdf_pages.setOnClickListener(this);
        chkBoxStrongShadow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefManager.setStrongShadowEnabled(isChecked);
            }
        });
        autoCrop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefManager.setAutoCropEnabled(isChecked);
            }
        });
    }

    private void findViewIds() {
        tv_toolbar = findViewById(R.id.tv_toolbar);
        tv_toolbar.setText(getString(R.string.settings));
        iv_back_toolbar = findViewById(R.id.iv_back_toolbar);
        tv_default_sorting = findViewById(R.id.tv_default_sorting);
        ll_sorting = findViewById(R.id.ll_sorting);
        ll_expanded_sort_lay = findViewById(R.id.ll_expanded_sort_lay);
        iv_sort_icon = findViewById(R.id.iv_sort_icon);
        iv_arrow = findViewById(R.id.iv_arrow);
        btn_cancel = findViewById(R.id.btn_cancel);
        rb_modification_time_ascending = findViewById(R.id.rb_modification_time_ascending);
        rb_modification_time_descending = findViewById(R.id.rb_modification_time_descending);
        rb_name_a_to_z = findViewById(R.id.rb_name_a_to_z);
        rb_name_z_to_a = findViewById(R.id.rb_name_z_to_a);
        btn_done = findViewById(R.id.btn_done);
        rv_pdf_pages = findViewById(R.id.rv_pdf_pages);
        setUpRecyclerView(rv_pdf_pages);
        ll_pdf_page_size = findViewById(R.id.ll_pdf_page_size);
        iv_pdf_page_size = findViewById(R.id.iv_pdf_page_size);
        tv_pdf_page_size = findViewById(R.id.tv_pdf_page_size);
        iv_pdf_page_size_arrow = findViewById(R.id.iv_pdf_page_size_arrow);
        btn_cancel_pdf_pages = findViewById(R.id.btn_cancel_pdf_pages);
        btn_done_pdf_pages = findViewById(R.id.btn_done_pdf_pages);
        ll_expanded_lay_pdf_pages = findViewById(R.id.ll_expanded_lay_pdf_pages);
        chkBoxStrongShadow = findViewById(R.id.chkBoxStrongShadow);
        chkBoxStrongShadow.setChecked(prefManager.isStrongShadowEnabled());
        autoCrop = findViewById(R.id.auto_crop);
        autoCrop.setChecked(prefManager.isAutoCropEnabled());

        ad_view_banner_container = findViewById(R.id.ad_view_banner_container);
        nativeSmallAdDuplicatePhoto = findViewById(R.id.nativeSmallAdDuplicatePhoto);
    }

    private void setUpRecyclerView(RecyclerView rv_pdf_pages) {
       /* LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rv_pdf_pages.setLayoutManager(linearLayoutManager);*/
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2);
        rv_pdf_pages.setLayoutManager(gridLayoutManager);
        rv_pdf_pages.setHasFixedSize(true);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back_toolbar) {
            onBackPressed();
        } else if (id == R.id.ll_sorting) {
            if (ll_expanded_sort_lay.getVisibility() == View.GONE) {
                expandSortingView();
            } else {
                collapseSortingView();
            }
        } else if (id == R.id.ll_pdf_page_size) {
            if (ll_expanded_lay_pdf_pages.getVisibility() == View.GONE) {
                expandPdfPagesView();
            } else {
                collapsePdfPagesView();
            }
        } else if (id == R.id.btn_cancel) {
            collapseSortingView();
        } else if (id == R.id.btn_done) {
            applySelectedSorting();
            collapseSortingView();
        } else if (id == R.id.btn_cancel_pdf_pages) {
            collapsePdfPagesView();
        } else if (id == R.id.btn_done_pdf_pages) {
            setSelectedPageSizeForWholeApp();
            collapsePdfPagesView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //CommonMethods.loadBannerAd(context, CommonMethods.getAdSize(this),this,nativeSmallAdDuplicatePhoto);
    }
    private void checkAds() {
        if (flashScanUtil.isConnectingToInternet() && !prefManager.isAppAdFree() ) {
            nativeAdSettings.setVisibility(View.VISIBLE);
            callNativeAd(customNativeAdInSettings);
        } else {
            nativeAdSettings.setVisibility(View.GONE);
        }
    }

    private void callNativeAd(FrameLayout nativeSmallAdNoDoc) {
        if (AppController.nativeAdDoc == null) {
            AdLoader adLoader = new AdLoader.Builder(this, BuildConfig.NATIVE_DOC)
                    .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                        @Override
                        public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                            Log.e("HOME_NO_DOC_NATIVE_AD ", "onUnifiedNativeAdLoaded G `> ");
                            AppController.nativeAdDoc = nativeAd;
                            smallDocNativeAdSet(nativeAd, nativeSmallAdNoDoc, false);
                        }
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.e("HOME_NO_DOC_NATIVE_AD ", "onAdFailedToLoad G > " + loadAdError.getMessage());
                        }
                    })
                    .withNativeAdOptions(
                            new NativeAdOptions.Builder()
                                    .setRequestCustomMuteThisAd(true)
                                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                                    .build()
                    )
                    .build();

            adLoader.loadAd(new AdRequest.Builder().build());
        } else {
            smallDocNativeAdSet(AppController.nativeAdDoc, nativeSmallAdNoDoc, false);
        }
    }

    private void setSelectedPageSizeForWholeApp() {
        if (pdfPageSizesAdapter != null) {
            String selectedPageSize = pdfPageSizesAdapter.getSelectedPageSize();
            if (!TextUtils.isEmpty(selectedPageSize)) {
                prefManager.saveSelectedPdfPageSizeForWholeApp(selectedPageSize);
            }
        }
    }

    private void collapsePdfPagesView() {
        iv_pdf_page_size.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pdf_page_size));
        tv_pdf_page_size.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        iv_pdf_page_size_arrow.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_right_arrow));
        iv_pdf_page_size_arrow.setRotation(0);
        ll_expanded_lay_pdf_pages.setVisibility(View.GONE);
    }

    private void expandPdfPagesView() {
        iv_pdf_page_size.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pdf_page_blue));
        tv_pdf_page_size.setTextColor(ContextCompat.getColor(context, R.color.sky_blue));
        iv_pdf_page_size_arrow.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_right_arrow));
        iv_pdf_page_size_arrow.setRotation(90);
        ll_expanded_lay_pdf_pages.setVisibility(View.VISIBLE);
        if(ll_expanded_sort_lay.getVisibility() == View.VISIBLE){
            collapseSortingView();
        }
        populatePdfPagesRV();
    }

    private void applySelectedSorting() {
        if (rb_modification_time_ascending.isChecked()) {
            prefManager.saveAppSortingOrder(Constants.SORT_BY.modificationTimeAscending);
        } else if (rb_modification_time_descending.isChecked()) {
            prefManager.saveAppSortingOrder(Constants.SORT_BY.modificationTimeDescending);
        } else if (rb_name_a_to_z.isChecked()) {
            prefManager.saveAppSortingOrder(Constants.SORT_BY.nameAtoZ);
        } else if (rb_name_z_to_a.isChecked()) {
            prefManager.saveAppSortingOrder(Constants.SORT_BY.nameZtoA);
        }
    }

    private void collapseSortingView() {
        iv_sort_icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sort));
        tv_default_sorting.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        iv_arrow.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_right_arrow));
        iv_arrow.setRotation(0);
        ll_expanded_sort_lay.setVisibility(View.GONE);
    }

    private void expandSortingView() {
        iv_sort_icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sort_blue));
        tv_default_sorting.setTextColor(ContextCompat.getColor(context, R.color.sky_blue));
        iv_arrow.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_right_arrow));
        iv_arrow.setRotation(90);
        if(ll_expanded_lay_pdf_pages.getVisibility() == View.VISIBLE){
            collapsePdfPagesView();
        }
        ll_expanded_sort_lay.setVisibility(View.VISIBLE);


        int appSortingOrder = prefManager.getAppSortingOrder();
        switch (appSortingOrder) {
            case Constants.SORT_BY.defaultOrder:
            case Constants.SORT_BY.modificationTimeDescending:
                rb_modification_time_descending.setChecked(true);
                break;
            case Constants.SORT_BY.modificationTimeAscending:
                rb_modification_time_ascending.setChecked(true);
                break;
            case Constants.SORT_BY.nameAtoZ:
                rb_name_a_to_z.setChecked(true);
                break;
            case Constants.SORT_BY.nameZtoA:
                rb_name_z_to_a.setChecked(true);
                break;
        }
    }

    /*private void showSortingDialog() {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.sorting_dialog);

        RadioButton rbModificationTimeAscending,
                rbModificationTimeDescending, rbNameAtoZ, rbNameZtoA;

        rbModificationTimeAscending = dialog.findViewById(R.id.rb_modification_time_ascending);
        rbModificationTimeDescending = dialog.findViewById(R.id.rb_modification_time_descending);
        rbNameAtoZ = dialog.findViewById(R.id.rb_name_a_to_z);
        rbNameZtoA = dialog.findViewById(R.id.rb_name_z_to_a);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_done = dialog.findViewById(R.id.btn_done);
        TextView tv_note = dialog.findViewById(R.id.tv_note);
        TextView tv_settings = dialog.findViewById(R.id.tv_settings);
        tv_note.setVisibility(View.GONE);
        tv_settings.setVisibility(View.GONE);
        *//*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tv_note.setText(Html.fromHtml(getString(R.string.sorting_note_txt), Html.FROM_HTML_MODE_LEGACY));
            tv_settings.setText(Html.fromHtml(getString(R.string.underlined_settings), Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv_note.setText(Html.fromHtml(getString(R.string.sorting_note_txt)));
            tv_settings.setText(Html.fromHtml(getString(R.string.underlined_settings)));
        }

        tv_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                openSettingScreen();
            }
        });*//*

        int appSortingOrder = prefManager.getAppSortingOrder();
        switch (appSortingOrder) {
            case Constants.SORT_BY.defaultOrder:
            case Constants.SORT_BY.modificationTimeDescending:
                rbModificationTimeDescending.setChecked(true);
                break;
            case Constants.SORT_BY.modificationTimeAscending:
                rbModificationTimeAscending.setChecked(true);
                break;
            case Constants.SORT_BY.nameAtoZ:
                rbNameAtoZ.setChecked(true);
                break;
            case Constants.SORT_BY.nameZtoA:
                rbNameZtoA.setChecked(true);
                break;
        }
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rbModificationTimeAscending.isChecked()) {
                    prefManager.saveAppSortingOrder(Constants.SORT_BY.modificationTimeAscending);
                } else if (rbModificationTimeDescending.isChecked()) {
                    prefManager.saveAppSortingOrder(Constants.SORT_BY.modificationTimeDescending);
                } else if (rbNameAtoZ.isChecked()) {
                    prefManager.saveAppSortingOrder(Constants.SORT_BY.nameAtoZ);
                } else if (rbNameZtoA.isChecked()) {
                    prefManager.saveAppSortingOrder(Constants.SORT_BY.nameZtoA);
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*if (ll_expanded_sort_lay.getVisibility() == View.VISIBLE) {
            collapseSortingView();
        }*/

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ll_expanded_sort_lay.getVisibility() == View.VISIBLE) {
            collapseSortingView();
        }
    }

    @Override
    public void onAdFailed() {
        ad_view_banner_container.setVisibility(View.GONE);
    }

    @Override
    public void onAdLoaded() {
        ad_view_banner_container.setVisibility(View.VISIBLE);
    }
}
