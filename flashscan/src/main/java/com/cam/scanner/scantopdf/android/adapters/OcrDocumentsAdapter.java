package com.cam.scanner.scantopdf.android.adapters;

import static com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_RIGHT;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.BuildConfig;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.interfaces.FileOperationListener;
import com.cam.scanner.scantopdf.android.interfaces.OnDeselectAllFiles;
import com.cam.scanner.scantopdf.android.interfaces.OnItemSelectListener;
import com.cam.scanner.scantopdf.android.interfaces.OnSelectAllFiles;
import com.cam.scanner.scantopdf.android.models.FileModel;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.itl.commonres.utils.AdsPlacementsEnum;
import com.itl.commonres.utils.CommonMethods;

import java.util.ArrayList;
import java.util.List;

public class OcrDocumentsAdapter extends RecyclerView.Adapter<OcrDocumentsAdapter.ViewHolder> {
    private Context context;
    private List<FileModel> fileModelList;
    private FlashScanUtil flashScanUtil;
    private boolean visibleAllCheckbox = false;
    private OnItemSelectListener onItemSelectListener;
    private FileOperationListener fileOperationListener;
    //    private UnifiedNativeAd nativeAd;
    private static final String TAG = OcrDocumentsAdapter.class.getSimpleName();
    private long mLastClickTime = 0;

    public List<FileModel> getSelectedFileModelList() {
        if (selectedFileModelList == null) {
            selectedFileModelList = new ArrayList<>();
        }
        return selectedFileModelList;
    }

    private List<FileModel> selectedFileModelList = new ArrayList<>();

    public OcrDocumentsAdapter(Context context, List<FileModel> fileModelList, OnItemSelectListener onItemSelectListener, FileOperationListener fileOperationListener) {
        this.context = context;
        this.fileModelList = fileModelList;
        flashScanUtil = new FlashScanUtil(context);
        this.onItemSelectListener = onItemSelectListener;
        this.fileOperationListener = fileOperationListener;
    }


    public void selectAllDocuments(OnSelectAllFiles onSelectAllFiles) {
        if (fileModelList != null && !fileModelList.isEmpty()) {
            List<FileModel> totalFiles = new ArrayList<>();
            for (FileModel fileModel : fileModelList) {
                if (fileModel.isAdView()) {
                    continue;
                }
                fileModel.setChecked(true);
                totalFiles.add(fileModel);
            }
            setVisibleAllCheckbox(true);
            if (!getSelectedFileModelList().isEmpty()) {
                getSelectedFileModelList().clear();
            }
            getSelectedFileModelList().addAll(totalFiles);
            notifyDataSetChanged();
            if (onSelectAllFiles != null) {
                onSelectAllFiles.onSelectedAllFiles();
            }
        }
    }

    public void deSelectAllFies(OnDeselectAllFiles onDeselectAllFiles) {
        if (fileModelList != null && !fileModelList.isEmpty()) {
            for (FileModel fileModel : fileModelList) {
                fileModel.setChecked(false);
            }
        }
        if (!getSelectedFileModelList().isEmpty()) {
            getSelectedFileModelList().clear();
        }
        notifyDataSetChanged();
        if (onDeselectAllFiles != null) {
            onDeselectAllFiles.onDeselect();
        }
    }

    public void filterList(List<FileModel> filterNames) {
        this.fileModelList = filterNames;
        notifyDataSetChanged();
    }

    public void clearFilterList(List<FileModel> fetchedFileList) {
        this.fileModelList = fetchedFileList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_ocr_documents, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileModel fileModel = fileModelList.get(position);
        if (fileModel != null) {
            if (fileModel.isAdView()) {
                holder.cv_document.setVisibility(View.GONE);
                if (flashScanUtil.isConnectingToInternet() && com.itl.commonres.utils.Constants.isAdShow && CommonMethods.isAdActive(AdsPlacementsEnum.SH_OCR_LIST.getValue())) {
                    /*holder.ll_ad_view.setVisibility(View.VISIBLE);*/
                    /*loadAd(holder.adView);*/
                    //holder.ll_native_ad_view.setVisibility(View.VISIBLE);
                    holder.ad_view_banner_container.setVisibility(View.VISIBLE);
                    callNativeAd(holder.nativeSmallAdDocAdapter);
                    try {
                        //loadNativeAd(holder.fl_native_ad);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    /*holder.ll_ad_view.setVisibility(View.GONE);*/
                    //holder.ll_native_ad_view.setVisibility(View.GONE);
                    holder.ad_view_banner_container.setVisibility(View.GONE);
                }
            } else {
                holder.cv_document.setVisibility(View.VISIBLE);
//                holder.ll_ad_view.setVisibility(View.GONE);
//                holder.ll_native_ad_view.setVisibility(View.GONE);
                holder.ad_view_banner_container.setVisibility(View.GONE);
                if (context instanceof Activity) {
                    if (!((Activity) context).isFinishing() || !((Activity) context).isDestroyed()) {
                        Glide.with(context).asBitmap().load(fileModel.getThumbnailPath()).centerCrop().apply(new RequestOptions()
                                        .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_notfound)))
                                .into(holder.ivFile);
                    }
                }


                holder.tvFileName.setText(fileModel.getName());
                holder.tvDateAdded.setText(flashScanUtil.getDateFromTimeStamp(fileModel.getDateTaken()));
                holder.tv_file_count.setText(String.valueOf(fileModel.getFileCountInFolder()));

                holder.btnCheckbox.setOnClickListener(v -> handleClickedPosition(fileModel, position));
                holder.btnCheckbox.setOnLongClickListener(v -> {
                    handleLongClick(fileModel);
                    return true;
                });

                holder.itemView.setOnClickListener(v -> {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    handleClickedPosition(fileModel, position);
                });
                holder.itemView.setOnLongClickListener(v -> {
                    handleLongClick(fileModel);
                    return true;
                });
                holder.checkBox.setVisibility(isVisibleAllCheckbox() ? View.VISIBLE : View.GONE);
                holder.checkBox.setChecked(fileModel.isChecked());

                holder.iv_drive.setImageDrawable(fileModel.isSavedOnGoogleDrive() ? ContextCompat.getDrawable(context, R.drawable.ic_uploaded_drive) :
                        ContextCompat.getDrawable(context, R.drawable.ic_add_to_drive));

                holder.iv_pdf.setOnClickListener(v -> {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (isVisibleAllCheckbox())
                        return;
                    if (fileOperationListener != null)
                        fileOperationListener.actionSaveAsPdf(fileModel);
                });

                holder.iv_share.setOnClickListener(v -> {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (isVisibleAllCheckbox())
                        return;
                    if (fileOperationListener != null)
                        fileOperationListener.actionShare(fileModel);
                });

                holder.iv_rename.setOnClickListener(v -> {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (isVisibleAllCheckbox())
                        return;
                    if (fileOperationListener != null)
                        fileOperationListener.actionRename(fileModel);
                });

                holder.iv_delete.setOnClickListener(v -> {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (isVisibleAllCheckbox())
                        return;
                    if (fileOperationListener != null)
                        fileOperationListener.actionDelete(fileModel);
                });

                holder.iv_drive.setOnClickListener(v -> {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (isVisibleAllCheckbox())
                        return;
                    if (fileOperationListener != null)
                        fileOperationListener.actionAddToDrive(fileModel, holder.getAdapterPosition());
                });
            }

        }
    }

  /*  private void loadNativeAd(FrameLayout fl_native_ad) {
        AdLoader adLoader = new AdLoader.Builder(context, BuildConfig.NATIVE_AD_ID)
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        if (((Activity) context).isDestroyed()) {
                            unifiedNativeAd.destroy();
                            return;
                        }
                        if (nativeAd != null) {
                            nativeAd.destroy();
                        }
                        nativeAd = unifiedNativeAd;
                        UnifiedNativeAdView unifiedNativeAdView = (UnifiedNativeAdView) LayoutInflater.from(context).inflate(R.layout.item_view_native_ad, null, false);
                        if (unifiedNativeAdView != null) {
                            mapUnifiedNativeAdToLayout(unifiedNativeAd, unifiedNativeAdView);
                            fl_native_ad.removeAllViews();
                            fl_native_ad.addView(unifiedNativeAdView);
                        }

                    }
                }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void mapUnifiedNativeAdToLayout(UnifiedNativeAd adFromGoogle, UnifiedNativeAdView myAdView) {

        MediaView mediaView = myAdView.findViewById(R.id.ad_media);
        myAdView.setMediaView(mediaView);
        myAdView.setHeadlineView(myAdView.findViewById(R.id.ad_headline));
        myAdView.setBodyView(myAdView.findViewById(R.id.ad_body));
        myAdView.setCallToActionView(myAdView.findViewById(R.id.ad_call_to_action));
        CardView cardView = myAdView.findViewById(R.id.cv_app_icon);
        myAdView.setIconView(myAdView.findViewById(R.id.ad_app_icon));
        myAdView.setPriceView(myAdView.findViewById(R.id.ad_price));
        myAdView.setStarRatingView(myAdView.findViewById(R.id.ad_stars));
        myAdView.setStoreView(myAdView.findViewById(R.id.ad_store));
        myAdView.setAdvertiserView(myAdView.findViewById(R.id.ad_advertiser));

        *//*myAdView.setImageView();
        myAdView.setClickConfirmingView();
        myAdView.setAdChoicesView();*//*

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        myAdView.getMediaView().setMediaContent(adFromGoogle.getMediaContent());
        Log.e(TAG, "ad headline :" + adFromGoogle.getHeadline());
        ((TextView) myAdView.getHeadlineView()).setText(adFromGoogle.getHeadline());


        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        Log.e(TAG, "ad body :" + adFromGoogle.getBody());
        if (adFromGoogle.getBody() == null) {
            myAdView.getBodyView().setVisibility(View.GONE);
        } else {
            ((TextView) myAdView.getBodyView()).setText(adFromGoogle.getBody());
        }

        Log.e(TAG, "ad call to action :" + adFromGoogle.getCallToAction());
        if (adFromGoogle.getCallToAction() == null) {
            myAdView.getCallToActionView().setVisibility(View.GONE);
        } else {
            ((Button) myAdView.getCallToActionView()).setText(adFromGoogle.getCallToAction());
        }

        if (adFromGoogle.getIcon() == null) {
            myAdView.getIconView().setVisibility(View.GONE);
            cardView.setVisibility(View.GONE);
        } else {
            cardView.setVisibility(View.VISIBLE);
            myAdView.getIconView().setVisibility(View.VISIBLE);
            ((ImageView) myAdView.getIconView()).setImageDrawable(adFromGoogle.getIcon().getDrawable());
        }


        Log.e(TAG, "ad price :" + adFromGoogle.getPrice());
        if (adFromGoogle.getPrice() == null) {
            myAdView.getPriceView().setVisibility(View.GONE);
        } else {
            ((TextView) myAdView.getPriceView()).setText(adFromGoogle.getPrice());
        }


        if (adFromGoogle.getStarRating() == null) {
            myAdView.getStarRatingView().setVisibility(View.GONE);
        } else {
            ((RatingBar) myAdView.getStarRatingView()).setRating(adFromGoogle.getStarRating().floatValue());
        }

        Log.e(TAG, "ad store :" + adFromGoogle.getStore());
        if (adFromGoogle.getStore() == null) {
            myAdView.getStoreView().setVisibility(View.GONE);
        } else {
            ((TextView) myAdView.getStoreView()).setText(adFromGoogle.getStore());
        }

        Log.e(TAG, "ad advertiser :" + adFromGoogle.getAdvertiser());
        if (adFromGoogle.getAdvertiser() == null) {
            myAdView.getAdvertiserView().setVisibility(View.GONE);
        } else {
            ((TextView) myAdView.getAdvertiserView()).setText(adFromGoogle.getAdvertiser());
        }

        myAdView.setNativeAd(adFromGoogle);
    }*/

    private void loadAd(AdView adView) {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void handleLongClick(FileModel fileModel) {
        fileModel.setChecked(!fileModel.isChecked());
        if (fileModel.isChecked()) {
            getSelectedFileModelList().add(fileModel);
        } else {
            if (!getSelectedFileModelList().isEmpty()) {
                getSelectedFileModelList().remove(fileModel);
            }
        }
        showAllCheckBoxes();
        if (onItemSelectListener != null)
            onItemSelectListener.onItemLongPress(fileModel);
    }

    private void showAllCheckBoxes() {

        setVisibleAllCheckbox(true);
        notifyDataSetChanged();
    }

    public boolean isVisibleAllCheckbox() {
        return visibleAllCheckbox;
    }

    private void setVisibleAllCheckbox(boolean visibleAllCheckbox) {
        this.visibleAllCheckbox = visibleAllCheckbox;
    }

    private void handleClickedPosition(FileModel fileModel, int position) {
        if (isVisibleAllCheckbox()) {
            fileModel.setChecked(!fileModel.isChecked());
            if (fileModel.isChecked()) {
                getSelectedFileModelList().add(fileModel);
            } else {
                if (!getSelectedFileModelList().isEmpty()) {
                    getSelectedFileModelList().remove(fileModel);
                }
            }
            notifyItemChanged(position);
        }
        if (onItemSelectListener != null)
            onItemSelectListener.onItemSelect(fileModel);
    }

    @Override
    public int getItemCount() {
        return fileModelList.size();
    }

    public void hideAllCheckBoxes() {
        setVisibleAllCheckbox(false);
        if (fileModelList != null && !fileModelList.isEmpty()) {
            for (FileModel fileModel : fileModelList) {
                fileModel.setChecked(false);
            }
        }
        if (!getSelectedFileModelList().isEmpty()) {
            getSelectedFileModelList().clear();
        }
        notifyDataSetChanged();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivFile, iv_pdf, iv_share, iv_rename, iv_delete, iv_drive;
        private TextView tvFileName, tvDateAdded, tv_file_count;
        private CheckBox checkBox;
        private Button btnCheckbox;
        //private LinearLayout ll_ad_view, ll_native_ad_view;
        private CardView cv_document;
        //private AdView adView;
        //private FrameLayout fl_native_ad;
        private CardView ad_view_banner_container;
        private FrameLayout nativeSmallAdDocAdapter;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFile = itemView.findViewById(R.id.iv_file);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            tvDateAdded = itemView.findViewById(R.id.tv_date);
            tv_file_count = itemView.findViewById(R.id.tv_file_count);
            checkBox = itemView.findViewById(R.id.checkBox);
            btnCheckbox = itemView.findViewById(R.id.btnCheckbox);
            iv_pdf = itemView.findViewById(R.id.iv_pdf);
            iv_share = itemView.findViewById(R.id.iv_share);
            iv_rename = itemView.findViewById(R.id.iv_rename);
            iv_delete = itemView.findViewById(R.id.iv_delete);
            iv_drive = itemView.findViewById(R.id.iv_drive);
            cv_document = itemView.findViewById(R.id.cv_document);
            ad_view_banner_container = itemView.findViewById(R.id.ad_view_banner_container);
            nativeSmallAdDocAdapter = itemView.findViewById(R.id.nativeSmallAdDocAdapter);
            //ll_ad_view = itemView.findViewById(R.id.ll_ad_view);
            //adView = itemView.findViewById(R.id.adView);
            //ll_native_ad_view = itemView.findViewById(R.id.ll_native_ad_view);
            //fl_native_ad = itemView.findViewById(R.id.fl_native_ad);
        }
    }

    public void destroyAdapterNativeAd() {
       /* if (nativeAd != null) {
            nativeAd.destroy();
        }*/
    }

    private void callNativeAd(FrameLayout nativeSmallAdDocAdapter) {
        if (AppController.nativeAdOcr == null) {
            Log.e(TAG, "callNativeAd " + BuildConfig.NATIVE_OCR);
            AdLoader customEventNativeLoader = new AdLoader.Builder(context,
                    BuildConfig.AD_UNIT_ID_OCR_LIST_SCREEN_NATIVE_AD)
                    .forNativeAd(nativeAd -> {
                        Log.e("DOCUMENT_ADAPTER", "onUnifiedNativeAdLoaded G `> " + "");
                        AppController.nativeAdOcr = nativeAd;
                        smallNativeAdSet(nativeAd, nativeSmallAdDocAdapter, false);
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            Log.e("DOCUMENT_ADAPTER ", "onAdFailedToLoad G > " + loadAdError.getMessage());
                        }
                    }).withNativeAdOptions(new NativeAdOptions.Builder().setRequestCustomMuteThisAd(true)
                            .setAdChoicesPlacement(ADCHOICES_TOP_RIGHT).build())
                    .build();
            customEventNativeLoader.loadAd(new AdRequest.Builder().build());
        } else {
            smallNativeAdSet(AppController.nativeAdOcr, nativeSmallAdDocAdapter, false);
        }
    }

    public void smallNativeAdSet(NativeAd nativeAd, FrameLayout customNativeViewSet, boolean showArrow) {
        if (nativeAd != null) {
            NativeAdView adView = (NativeAdView) ((Activity) context).getLayoutInflater().inflate(R.layout.ad_unified_doc_item, null);
            populateSmallUnifiedNativeAdView(nativeAd, adView, showArrow);
            customNativeViewSet.removeAllViews();
            customNativeViewSet.addView(adView);
            int padding = (int) context.getResources().getDimension(com.intuit.sdp.R.dimen._1sdp);
            customNativeViewSet.setPadding(padding, padding, padding, padding);
        }
    }

    public void populateSmallUnifiedNativeAdView(NativeAd nativeAd, NativeAdView adView, boolean showArrow) {

        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

       /* if(nativeAd.getHeadline().toString().length()>25){
            String subStr = nativeAd.getHeadline().substring(0,25);
            ((TextView) adView.getHeadlineView()).setText( subStr+"...");
        }
        else {
            ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        }*/

        if (showArrow) {
            ((TextView) adView.getHeadlineView()).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_small, 0);
        } else {
            ((TextView) adView.getHeadlineView()).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.GONE);
        } else {
            adView.getBodyView().setVisibility(View.GONE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }
        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.GONE);
        } else {
            adView.getPriceView().setVisibility(View.GONE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.GONE);
        } else {
            adView.getStoreView().setVisibility(View.GONE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null || nativeAd.getStarRating() < 3) {
            adView.getStarRatingView().setVisibility(View.GONE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.GONE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.GONE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
    }
}
