package com.cam.scanner.scantopdf.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_RIGHT;

public class FileModelAdapter extends RecyclerView.Adapter<FileModelAdapter.MyViewHolder> {

    private Context context;
    private List<FileModel> filesList;
    private FlashScanUtil flashScanUtil;
    private OnItemSelectListener onItemSelectListener;
    private FileOperationListener fileOperationListener;
    //private UnifiedNativeAd nativeAd;
    private static final String TAG = FileModelAdapter.class.getSimpleName();
    //private boolean isNativeAdAlreadyLoaded = false;
    private long mLastClickTime = 0;


    public List<FileModel> getSelectedFileModelList() {
        if (selectedFileModelList == null) {
            selectedFileModelList = new ArrayList<>();
        }
        return selectedFileModelList;
    }

    private List<FileModel> selectedFileModelList = new ArrayList<>();

    public boolean isVisibleAllCheckbox() {
        return visibleAllCheckbox;
    }

    private void setVisibleAllCheckbox(boolean visibleAllCheckbox) {
        this.visibleAllCheckbox = visibleAllCheckbox;
    }

    private boolean visibleAllCheckbox = false;

    public FileModelAdapter(Context context, List<FileModel> fileModelList, OnItemSelectListener onItemSelectListener, FileOperationListener fileOperationListener) {
        this.context = context;
        this.filesList = fileModelList;
        flashScanUtil = new FlashScanUtil(context);
        this.onItemSelectListener = onItemSelectListener;
        this.fileOperationListener = fileOperationListener;
    }


    public void selectAllFiles(OnSelectAllFiles onSelectAllFiles) {
        if (filesList != null && !filesList.isEmpty()) {
            List<FileModel> totalFiles = new ArrayList<>();
            for (FileModel fileModel : filesList) {
                if (!TextUtils.isEmpty(fileModel.getName()) && fileModel.getName().equalsIgnoreCase(context.getString(R.string.view_all)) && TextUtils.isEmpty(fileModel.getThumbnailPath())) {
                    continue;
                } else if (!TextUtils.isEmpty(fileModel.getName()) && fileModel.getName().equalsIgnoreCase(context.getString(R.string.show_camera_media__floating_view))
                        && TextUtils.isEmpty(fileModel.getThumbnailPath())) {
                    continue;
                } else if (fileModel.isAdView()) {
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


    public void deSelectAllDocuments(OnDeselectAllFiles onDeselectAllFiles) {
        if (filesList != null && !filesList.isEmpty()) {
            for (FileModel fileModel : filesList) {
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
        this.filesList = filterNames;
        notifyDataSetChanged();
    }

    public void clearFilterList(List<FileModel> fetchedFileList) {
        this.filesList = fetchedFileList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_files_list, parent, false);
        return new MyViewHolder(view);
    }

 /*   private int AD_LAYOUT = 0;
    private int ITEM_LAYOUT = 1;*/

/*
    @Override
    public int getItemViewType(int position) {
        return filesList.get(position).isAdView() ? AD_LAYOUT : ITEM_LAYOUT;
    }*/

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        FileModel fileModel = filesList.get(position);
        int type = fileModel.getType();
        if (fileModel != null) {
            if (!TextUtils.isEmpty(fileModel.getName()) && fileModel.getName().equalsIgnoreCase(context.getString(R.string.view_all)) && TextUtils.isEmpty(fileModel.getThumbnailPath())) {

                // for recents document view all
                holder.tv_view_more_recent.setVisibility(View.VISIBLE);
                holder.cvDocument.setVisibility(View.GONE);
                holder.ll_floating_view.setVisibility(View.GONE);
                holder.ll_ad_view.setVisibility(View.GONE);
                holder.ad_view_banner_container.setVisibility(View.GONE);
                holder.itemView.setOnClickListener(v -> {
                    handleClickedPosition(fileModel, position);
                });
            } else if (!TextUtils.isEmpty(fileModel.getName()) && fileModel.getName().equalsIgnoreCase(context.getString(R.string.show_camera_media__floating_view))
                    && TextUtils.isEmpty(fileModel.getThumbnailPath())) {
                // for recents document
                holder.ll_floating_view.setVisibility(View.VISIBLE);
                holder.tv_view_more_recent.setVisibility(View.GONE);
                holder.cvDocument.setVisibility(View.GONE);
                holder.ll_ad_view.setVisibility(View.GONE);
                holder.ad_view_banner_container.setVisibility(View.GONE);
                /*holder.fl_camera.setOnClickListener(v -> startScan(ScanConstants.OPEN_CAMERA));

                holder.fl_media.setOnClickListener(v -> startScan(ScanConstants.OPEN_MEDIA));*/
            } else if (fileModel.isAdView()) {
                if (flashScanUtil.isConnectingToInternet()) {
                    /*holder.ll_ad_view.setVisibility(View.VISIBLE);*/
                    holder.ad_view_banner_container.setVisibility(View.VISIBLE);
                    callNativeAd(holder.nativeSmallAdDocAdapter);
                    /*loadAd(holder.adView);*/
                    /*try {
                        loadNativeAd(holder.fl_native_ad);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                } else {
                    /*holder.ll_ad_view.setVisibility(View.GONE);*/
                    holder.ad_view_banner_container.setVisibility(View.GONE);
                }

                holder.ll_floating_view.setVisibility(View.GONE);
                holder.tv_view_more_recent.setVisibility(View.GONE);
                holder.cvDocument.setVisibility(View.GONE);
            } else {
                holder.ll_ad_view.setVisibility(View.GONE);
                holder.ad_view_banner_container.setVisibility(View.GONE);
                holder.tv_view_more_recent.setVisibility(View.GONE);
                holder.cvDocument.setVisibility(View.VISIBLE);
                holder.ll_floating_view.setVisibility(View.GONE);

               /* if(filesList.get(position).getSize()>0){
                    for(int i =0; i<filesList.get(position).getSize();i++){

                                long filesize = filesList.get(position).getSize();
                                if(filesize<=0){
                                  filesList.remove(filesList.get(i));
                                }

                    }
                }*/
               /* holder.iv_delete.setVisibility(View.GONE);*/

                if(fileModel.getThumbnailPath()!=null){
                   holder.iv_pdf.setVisibility(View.VISIBLE);
                   holder.iv_share.setVisibility(View.VISIBLE);
                   holder.iv_drive.setVisibility(View.VISIBLE);
                }
                else{
                    holder.iv_pdf.setVisibility(View.GONE);
                    holder.iv_share.setVisibility(View.GONE);
                    holder.iv_drive.setVisibility(View.GONE);
                }

                /*if(type == DocumentTypeEnum.OCR.getValue()){
                    holder.iv_pdf.setVisibility(View.GONE);
                    holder.iv_share.setVisibility(View.GONE);
                    holder.iv_delete.setVisibility(View.VISIBLE);
                }*/

                if (context instanceof Activity) {
                    if (!((Activity) context).isFinishing() || !((Activity) context).isDestroyed()) {
                        Glide.with(context).asBitmap().load(fileModel.getThumbnailPath()).centerCrop().apply(new RequestOptions()
                                .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_notfound))
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE))
                                .into(holder.ivFile);
                    }
                }
                holder.tvFileName.setText(fileModel.getName());
                holder.tvDateAdded.setText(flashScanUtil.getDateFromTimeStamp(fileModel.getDateTaken()));
                holder.tv_file_count.setText(String.valueOf(fileModel.getFileCountInFolder()));


                holder.btnCheckbox.setOnClickListener(v ->
                        handleClickedPosition(fileModel, position));
                holder.btnCheckbox.setOnLongClickListener(v -> {
                    handleLongClick(fileModel);
                    return true;
                });

                holder.itemView.setOnLongClickListener(v -> {
                    handleLongClick(fileModel);
                    return true;
                });
                holder.checkBox.setVisibility(isVisibleAllCheckbox() ? View.VISIBLE : View.GONE);
                holder.checkBox.setChecked(fileModel.isChecked());
                holder.iv_star.setImageDrawable(fileModel.isStarred() ? ContextCompat.getDrawable(context, com.itl.commonres.R.drawable.ic_btn_fav_rad) :
                        ContextCompat.getDrawable(context, com.itl.commonres.R.drawable.ic_btn_fav_gray));

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

                /*holder.iv_delete.setOnClickListener(v -> {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (isVisibleAllCheckbox())
                        return;
                    if (fileOperationListener != null)
                        fileOperationListener.actionDelete(fileModel);
                });*/

                holder.iv_drive.setOnClickListener(v -> {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (isVisibleAllCheckbox())
                        return;
                    if (fileOperationListener != null)
                        fileOperationListener.actionAddToDrive(fileModel,holder.getAdapterPosition());
                });

                holder.fl_star.setOnClickListener(v -> {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (isVisibleAllCheckbox())
                        return;
                    if (fileModel.isStarred()) {
                        fileModel.setStarred(false);  // unstarred
                        if (fileOperationListener != null)
                            fileOperationListener.removeFavourite(fileModel);
                    } else {
                        fileModel.setStarred(true);   // starred
                        if (fileOperationListener != null)
                            fileOperationListener.makeFavourite(fileModel);
                    }
                    notifyItemChanged(position);
                });
                holder.itemView.setOnClickListener(v -> {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    handleClickedPosition(fileModel, position);
                });



                /*holder.tv_view_more_recent.setVisibility(View.GONE);
                holder.cvDocument.setVisibility(View.VISIBLE);
                holder.ll_floating_view.setVisibility(View.GONE);

                Glide.with(context).asBitmap().load(fileModel.getThumbnailPath()).centerCrop().apply(new RequestOptions()
                        .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_notfound)))
                        .into(holder.ivFile);

                holder.tvFileName.setText(fileModel.getName());
                holder.tvDateAdded.setText(flashScanUtil.getDateFromTimeStamp(fileModel.getDateTaken()));
                holder.tv_file_count.setText(String.valueOf(fileModel.getFileCountInFolder()));


                holder.btnCheckbox.setOnClickListener(v -> handleClickedPosition(fileModel, position));
                holder.btnCheckbox.setOnLongClickListener(v -> {
                    handleLongClick(fileModel);
                    return true;
                });

                holder.itemView.setOnLongClickListener(v -> {
                    handleLongClick(fileModel);
                    return true;
                });
                holder.checkBox.setVisibility(isVisibleAllCheckbox() ? View.VISIBLE : View.GONE);
                holder.checkBox.setChecked(fileModel.isChecked());
                holder.iv_star.setImageDrawable(fileModel.isStarred() ? ContextCompat.getDrawable(context, R.drawable.ic_starred) :
                        ContextCompat.getDrawable(context, R.drawable.ic_unstarred));

                holder.iv_pdf.setOnClickListener(v -> {
                    if (isVisibleAllCheckbox())
                        return;
                    if (fileOperationListener != null)
                        fileOperationListener.actionSaveAsPdf(fileModel);
                });

                holder.iv_share.setOnClickListener(v -> {
                    if (isVisibleAllCheckbox())
                        return;
                    if (fileOperationListener != null)
                        fileOperationListener.actionShare(fileModel);
                });

                holder.iv_rename.setOnClickListener(v -> {
                    if (isVisibleAllCheckbox())
                        return;
                    if (fileOperationListener != null)
                        fileOperationListener.actionRename(fileModel);
                });

                holder.iv_delete.setOnClickListener(v -> {
                    if (isVisibleAllCheckbox())
                        return;
                    if (fileOperationListener != null)
                        fileOperationListener.actionDelete(fileModel);
                });

                holder.fl_star.setOnClickListener(v -> {
                    if (fileModel.isStarred()) {
                        fileModel.setStarred(false);  // unstarred
                        if (fileOperationListener != null)
                            fileOperationListener.removeFavourite(fileModel);
                    } else {
                        fileModel.setStarred(true);   // starred
                        if (fileOperationListener != null)
                            fileOperationListener.makeFavourite(fileModel);
                    }
                    notifyItemChanged(position);
                });
                holder.itemView.setOnClickListener(v -> {
                    handleClickedPosition(fileModel, position);
                });*/
            }


        }


    }

    private void callNativeAd(FrameLayout nativeSmallAdDocAdapter) {
        if (AppController.nativeAdDoc == null) {
            Log.e(TAG, "callNativeAd "+BuildConfig.NATIVE_DOC);
            AdLoader customEventNativeLoader = new AdLoader.Builder(context,
                    BuildConfig.NATIVE_DOC)
                    .forNativeAd(nativeAd -> {
                        Log.e("DOCUMENT_ADAPTER", "onUnifiedNativeAdLoaded G `> " + "");
                        AppController.nativeAdDoc = nativeAd;
                        smallNativeAdSet(nativeAd,nativeSmallAdDocAdapter , false);
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
        }
        else{
            smallNativeAdSet(AppController.nativeAdDoc, nativeSmallAdDocAdapter, false);
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

        if(showArrow){
            ((TextView) adView.getHeadlineView()).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_small, 0);
        }
        else{
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


    /*private void loadNativeAd(FrameLayout fl_native_ad) {
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
                        Log.e(TAG, "onUnifiedNativeAdLoaded called");
                        nativeAd = unifiedNativeAd;

                        UnifiedNativeAdView unifiedNativeAdView = (UnifiedNativeAdView) LayoutInflater.from(context).inflate(R.layout.item_view_native_ad, null, false);
                        if (unifiedNativeAdView != null) {
                            mapUnifiedNativeAdToLayout(nativeAd, unifiedNativeAdView);
                            fl_native_ad.removeAllViews();
                            fl_native_ad.addView(unifiedNativeAdView);
                        }

                    }
                }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }*/

   /* private void mapUnifiedNativeAdToLayout(UnifiedNativeAd adFromGoogle, UnifiedNativeAdView myAdView) {

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

    public void hideAllCheckBoxes() {

        setVisibleAllCheckbox(false);
        if (filesList != null && !filesList.isEmpty()) {
            for (FileModel fileModel : filesList) {
                fileModel.setChecked(false);
            }
        }
        if (!getSelectedFileModelList().isEmpty()) {
            getSelectedFileModelList().clear();
        }
        notifyDataSetChanged();
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
        return filesList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivFile, iv_more, iv_pdf, iv_share, iv_rename, iv_delete, iv_star,iv_drive;
        private TextView tvFileName, tvDateAdded, tv_file_count, tv_view_more_recent;
        private CheckBox checkBox;
        private Button btnCheckbox;
        private FrameLayout fl_star, fl_camera, fl_media, fl_native_ad;
        private CardView cvDocument;
        private LinearLayout ll_floating_view, ll_ad_view;
        //private AdView adView;
        private CardView ad_view_banner_container;
        private FrameLayout nativeSmallAdDocAdapter;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cvDocument = itemView.findViewById(R.id.cv_document);
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
            iv_star = itemView.findViewById(R.id.iv_star);
            fl_star = itemView.findViewById(R.id.fl_star);
            tv_view_more_recent = itemView.findViewById(R.id.tv_view_more_recent);
            ll_floating_view = itemView.findViewById(R.id.ll_floating_view);
            fl_camera = itemView.findViewById(R.id.fl_camera);
            fl_media = itemView.findViewById(R.id.fl_media);
            ll_ad_view = itemView.findViewById(R.id.ll_ad_view);
            iv_drive = itemView.findViewById(R.id.iv_drive);
            //adView = itemView.findViewById(R.id.adView);
            //fl_native_ad = itemView.findViewById(R.id.fl_native_ad);
            //ll_native_ad_view = itemView.findViewById(R.id.ll_native_ad_view);
            ad_view_banner_container = itemView.findViewById(R.id.ad_view_banner_container);
            nativeSmallAdDocAdapter = itemView.findViewById(R.id.nativeSmallAdDocAdapter);
        }
    }

    public void destroyAdapterNativeAd() {
        /*if (nativeAd != null) {
            nativeAd.destroy();
        }*/
    }

}
