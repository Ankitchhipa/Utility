package com.cam.scanner.scantopdf.android.adapters;

import static com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_RIGHT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.BuildConfig;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.activities.ImageCropActivity;
import com.cam.scanner.scantopdf.android.interfaces.OnDeselectAllFiles;
import com.cam.scanner.scantopdf.android.interfaces.OnItemSelectListener;
import com.cam.scanner.scantopdf.android.interfaces.OnSelectAllFiles;
import com.cam.scanner.scantopdf.android.interfaces.PDFCreationCallback;
import com.cam.scanner.scantopdf.android.models.FileModel;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.cam.scanner.scantopdf.android.util.ScanConstants;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.itl.commonres.utils.AdsPlacementsEnum;
import com.itl.commonres.utils.CommonMethods;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScanResultAdapter extends RecyclerView.Adapter<ScanResultAdapter.MyViewHolder> {

    private Context context;
    private List<FileModel> fileModelList;
    private OnItemSelectListener onItemSelectListener;
    private PDFCreationCallback pdfCreationCallback;
    private FlashScanUtil flashScanUtil;
    private static final String TAG = ScanResultAdapter.class.getSimpleName();
    private List<String> selectedImagesList = new ArrayList<>();
    private List<FileModel> selectedFileModelList = new ArrayList<>();
    private boolean visibleAllCheckBox = false;
    private long mLastClickTime = 0;
    private PrefManager prefManager;
    private boolean isSaveOnGoogleDrive = false;
    private String googleDriveFolderId;

    public List<FileModel> getSelectedFileModelList() {
        if (selectedFileModelList == null) {
            selectedFileModelList = new ArrayList<>();
        }
        return selectedFileModelList;
    }

    public boolean isVisibleAllCheckBox() {
        return visibleAllCheckBox;
    }

    private void setVisibleAllCheckBox(boolean visibleAllCheckBox) {
        this.visibleAllCheckBox = visibleAllCheckBox;
    }

    private List<String> getSelectedImagesList() {
        if (selectedImagesList == null) {
            selectedImagesList = new ArrayList<>();
        }
        return selectedImagesList;
    }

    private ArrayList<String> pathLists = new ArrayList<>();
    private String folderName;

    public ScanResultAdapter(Context context, PrefManager prefManager, List<FileModel> fileModelList, OnItemSelectListener onItemSelectListener, PDFCreationCallback pdfCreationCallback, String folderName, boolean isSaveOnGoogleDrive, String googleDriveFolderId) {
        this.context = context;
        this.fileModelList = fileModelList;
        this.onItemSelectListener = onItemSelectListener;
        this.pdfCreationCallback = pdfCreationCallback;
        flashScanUtil = new FlashScanUtil(context);
        this.prefManager = prefManager;

        this.folderName = folderName;
        this.isSaveOnGoogleDrive = isSaveOnGoogleDrive;
        this.googleDriveFolderId = googleDriveFolderId;

        for (int i = 0; i < fileModelList.size(); i++) {
            if (TextUtils.isEmpty(fileModelList.get(i).getPath()))
                continue;
            pathLists.add(fileModelList.get(i).getPath());
        }
        Log.e(TAG, "ScanResultAdapter: isSaveOnGoogleDrive = " + isSaveOnGoogleDrive);
        Log.e(TAG, "ScanResultAdapter: googleDriveFolderId = " + googleDriveFolderId);
    }

    public void selectAllFiles(OnSelectAllFiles onSelectAllFiles) {
        if (fileModelList != null && !fileModelList.isEmpty()) {
            List<FileModel> totalFiles = new ArrayList<>();
            for (FileModel fileModel : fileModelList) {
                if (!TextUtils.isEmpty(fileModel.getName()) &&
                        fileModel.getName().equalsIgnoreCase(context.getString(R.string.tap_camera_icon))
                        && TextUtils.isEmpty(fileModel.getPath()) && TextUtils.isEmpty(fileModel.getFileExtension())) {
                    continue;
                } else if (!TextUtils.isEmpty(fileModel.getName()) &&
                        fileModel.getName().equalsIgnoreCase("AD")
                        && TextUtils.isEmpty(fileModel.getPath()) && TextUtils.isEmpty(fileModel.getFileExtension())) {
                    continue;
                }
                fileModel.setChecked(true);
                totalFiles.add(fileModel);
            }
            setVisibleAllCheckBox(true);
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

    public void deSelectAllFiles(OnDeselectAllFiles onDeselectAllFiles) {
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

    public int getFileModelListSize() {
        return fileModelList.size();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_scanned_adapter, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        FileModel fileModel = fileModelList.get(position);

        if (fileModel != null) {
            if (!TextUtils.isEmpty(fileModel.getName()) &&
                    fileModel.getName().equalsIgnoreCase(context.getString(R.string.tap_camera_icon))
                    && TextUtils.isEmpty(fileModel.getPath()) && TextUtils.isEmpty(fileModel.getFileExtension())) {
                holder.ll_tap_camera.setVisibility(View.VISIBLE);
                holder.card_view.setVisibility(View.GONE);
                holder.nativeAdScanResult.setVisibility(View.GONE);
                holder.ll_tap_camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemSelectListener != null) {
                            onItemSelectListener.onItemSelect(fileModel);
                        }
                    }
                });
            } else if (com.itl.commonres.utils.Constants.isAdShow && flashScanUtil.isConnectingToInternet() && CommonMethods.isAdActive(AdsPlacementsEnum.SH_SCANNED_IMAGE_LIST.getValue()) && !TextUtils.isEmpty(fileModel.getName()) &&
                    fileModel.getName().equalsIgnoreCase("AD")
                    && TextUtils.isEmpty(fileModel.getPath()) && TextUtils.isEmpty(fileModel.getFileExtension())) {
                holder.nativeAdScanResult.setVisibility(View.VISIBLE);
                holder.ll_tap_camera.setVisibility(View.GONE);
                holder.card_view.setVisibility(View.GONE);
                callNativeAd(holder.customNativeAdScanResult);
            } else {
                holder.ll_tap_camera.setVisibility(View.GONE);
                holder.nativeAdScanResult.setVisibility(View.GONE);
                holder.card_view.setVisibility(View.VISIBLE);
                File file = new File(fileModel.getPath());
                Log.e("if", "file path " + fileModel.getPath() + "  position  " + position);
                if (file.exists()) {
                    if (context instanceof Activity) {
                        if (!((Activity) context).isFinishing() || !((Activity) context).isDestroyed()) {
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                            if (bitmap == null) {
                                Glide.with(context).asBitmap().load(file.getPath()).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .into(holder.iv_scanned_thumbnail);
                            } else {
                                holder.iv_scanned_thumbnail.setImageBitmap(bitmap);
                            }
                        }
                    }

                }


                if (!TextUtils.isEmpty(fileModel.getFileExtension())) {
                    String fileExtension = null;
                    switch (fileModel.getFileExtension()) {
                        case Constants.FileExtensions.PDF:
                            fileExtension = "PDF";
                            break;
                        case Constants.FileExtensions.JPG, Constants.FileExtensions.GIF,
                             Constants.FileExtensions.WEBP, Constants.FileExtensions.JPEG:
                            fileExtension = "JPG";
                            break;
                        case Constants.FileExtensions.PNG:
                            fileExtension = "PNG";
                            break;
                    }
                    holder.tv_extension.setText(fileExtension);
                }

                holder.itemView.setOnClickListener(v -> {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 400) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    /*if (onItemSelectListener != null) onItemSelectListener.onItemSelect(fileModel);*/
                    Log.e(TAG, "position" + position);
                    handleClickedItemView(fileModel, position, holder.checkBox);
                    /*if (position == 0 && position == fileModelList.size() - 1) {
                        handleClickedItemView(fileModel, position,holder.checkBox);
                    } else {
                        if (flashScanUtil.isConnectingToInternet() && !prefManager.isAppAdFree()) {
                            handleClickedItemView(fileModel, position - 1, holder.checkBox);
                        } else {
                            handleClickedItemView(fileModel, position, holder.checkBox);
                        }
                    }*/

                });

                holder.btnCheckbox.setOnClickListener(v -> {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 400) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    handleClickedItemView(fileModel, position, holder.checkBox);
                });

                holder.btnCheckbox.setOnLongClickListener(v -> {
                    handleLongClick(fileModel);
                    return true;
                });

                holder.itemView.setOnLongClickListener(v -> {

                    // select current file and show all check boxes
                    handleLongClick(fileModel);
                    return true;
                });
                holder.tv_name.setText(flashScanUtil.removeExtensionFromFileName(fileModel.getName()));

                holder.iv_more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();
                        if (isVisibleAllCheckBox())
                            return;
                        if (onItemSelectListener != null) {
                            onItemSelectListener.onItemAction(fileModel, v);
                        }
                    }
                });
                holder.checkBox.setVisibility(isVisibleAllCheckBox() ? View.VISIBLE : View.GONE);
                holder.transparent_lay.setVisibility(isVisibleAllCheckBox() ? View.VISIBLE : View.GONE);
                holder.checkBox.setChecked(fileModel.isChecked());

                holder.tv_file_number.setText(String.valueOf(fileModel.getFileNumber()));
            }

        }
    }

    private void handleLongClick(FileModel fileModel) {
        fileModel.setChecked(!fileModel.isChecked());
        if (fileModel.isChecked()) {
            getSelectedFileModelList().add(fileModel);
        } else {
            if (!getSelectedFileModelList().isEmpty())
                getSelectedFileModelList().remove(fileModel);
        }
        showAllCheckBoxes();
        if (onItemSelectListener != null) onItemSelectListener.onItemLongPress(fileModel);
    }

    private void handleClickedItemView(FileModel fileModel, int position, CheckBox checkBox) {
        checkBox.setVisibility(isVisibleAllCheckBox() ? View.VISIBLE : View.GONE);
        if (isVisibleAllCheckBox()) {
            fileModel.setChecked(!fileModel.isChecked());
            checkBox.setChecked(fileModel.isChecked());
            if (fileModel.isChecked()) {
                getSelectedFileModelList().add(fileModel);
            } else {
                if (!getSelectedFileModelList().isEmpty())
                    getSelectedFileModelList().remove(fileModel);
            }
            notifyItemChanged(position);
            if (onItemSelectListener != null) {
                onItemSelectListener.onItemSelect(fileModel);
            }
        } else {
            /*Toast.makeText(context, "openFile", Toast.LENGTH_SHORT).show();*/
            openFile(position);
        }
    }

    /*private void openFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile()) {
            flashScanUtil.openFile(context, file);
        }
    }*/

    private void openFile(int position) {
        Intent intent = new Intent(context, ImageCropActivity.class);
        intent.putExtra("folder_name", folderName);
        intent.putStringArrayListExtra(Constants.PutExtraConstants.SELECTED_IMAGES_LIST, pathLists);
        intent.putExtra("pos", position);
        intent.putExtra(ScanConstants.PutExtraConstants.IS_FOLDER_EXISTS_ON_DRIVE, isSaveOnGoogleDrive);
        intent.putExtra(ScanConstants.PutExtraConstants.GOOGLE_DRIVE_FOLDER_ID, googleDriveFolderId);
        context.startActivity(intent);
    }

    private void showAllCheckBoxes() {
        setVisibleAllCheckBox(true);
        notifyDataSetChanged();
    }

    public void hideAllCheckBoxes() {
        setVisibleAllCheckBox(false);
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

    /*private void showPopUpMenu(FileModel fileModel, View v) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        Field[] fields = popupMenu.getClass().getDeclaredFields();
        for (Field field : fields) {
            if ("mPopup".equals(field.getName())) {
                field.setAccessible(true);
                try {
                    Object menuPopupHelper = field.get(popupMenu);
                    if (menuPopupHelper != null) {
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceShowIcon = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceShowIcon.invoke(menuPopupHelper, true);
                        break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        popupMenu.getMenuInflater().inflate(R.menu.more_popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_save_as_pdf:
                    if (fileModel != null) {
                        createPdf(fileModel);
                    }
                    break;
                case R.id.menu_save_to_gallery:
                    Toast.makeText(context, "save to gallery called", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.menu_share:
                    Toast.makeText(context, "share called", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.menu_rename:
                    Toast.makeText(context, "rename called", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.menu_delete:
                    Toast.makeText(context, "delete called", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        });
        popupMenu.show();
    }*/

    /*private void createPdf(FileModel fileModel) {
        String fileName = util.removeExtensionFromFileName(fileModel.getName());
        ImageToPdfOptions imageToPdfOptions = new ImageToPdfOptions();
        imageToPdfOptions.setPageSize(Constants.PdfConstants.DEFAULT_PDF_PAGE_SIZE);
        imageToPdfOptions.setPageColor(Constants.PdfConstants.DEFAULT_PDF_PAGE_COLOR);
        imageToPdfOptions.setMargins(0, 0, 0, 0);
        imageToPdfOptions.setPdfQuality(Constants.PdfConstants.DEFAULT_PDF_QUALITY);
        imageToPdfOptions.setBorderWidth(Constants.PdfConstants.DEFAULT_BORDER_WIDTH);
        getSelectedImagesList().add(fileModel.getPath());
        if (!getSelectedImagesList().isEmpty())
            new CreatePdfTask(context, fileName, imageToPdfOptions, getSelectedImagesList(), pdfCreationCallback).execute();
    }*/

    @Override
    public int getItemCount() {
        return fileModelList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView iv_scanned_thumbnail, iv_more;
        private TextView tv_name, tv_extension, tv_file_number;
        private CheckBox checkBox;
        private Button btnCheckbox;
        private FrameLayout transparent_lay, customNativeAdScanResult;
        private LinearLayout ll_tap_camera;
        private CardView card_view, nativeAdScanResult;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_scanned_thumbnail = itemView.findViewById(R.id.iv_scanned_thumbnail);
            tv_name = itemView.findViewById(R.id.tv_name);
            iv_more = itemView.findViewById(R.id.iv_more);
            checkBox = itemView.findViewById(R.id.checkBox);
            btnCheckbox = itemView.findViewById(R.id.btnCheckbox);
            tv_extension = itemView.findViewById(R.id.tv_extension);
            transparent_lay = itemView.findViewById(R.id.transparent_lay);
            customNativeAdScanResult = itemView.findViewById(R.id.customNativeAdScanResult);
            ll_tap_camera = itemView.findViewById(R.id.ll_tap_camera);
            nativeAdScanResult = itemView.findViewById(R.id.nativeAdScanResult);
            card_view = itemView.findViewById(R.id.card_view);
            tv_file_number = itemView.findViewById(R.id.tv_file_number);
        }
    }

    public void callNativeAd(FrameLayout customNativeAdScanResult) {
        if (AppController.nativeAdThumbnail == null) {
            AdLoader customEventNativeLoader = new AdLoader.Builder(context,
                    BuildConfig.AD_UNIT_ID_DOC_SCREEN_NATIVE_AD)
                    .forNativeAd(nativeAd -> {
                        Log.e("SCAN_RESULT", "onUnifiedNativeAdLoaded G `> " + "");
                        AppController.nativeAdThumbnail = nativeAd;
                        nativeAdSquare(nativeAd, customNativeAdScanResult);
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            Log.e("SCAN_RESULT ", "onAdFailedToLoad G > " + loadAdError.getMessage());
                        }
                    }).withNativeAdOptions(new NativeAdOptions.Builder().setRequestCustomMuteThisAd(true)
                            .setAdChoicesPlacement(ADCHOICES_TOP_RIGHT).build())
                    .build();
            customEventNativeLoader.loadAd(new AdRequest.Builder().build());
        } else {
            nativeAdSquare(AppController.nativeAdThumbnail, customNativeAdScanResult);
        }
    }

    public void nativeAdSquare(NativeAd nativeAd, FrameLayout customNativeViewSet) {
        if (nativeAd != null && this != null) {
            NativeAdView adView = (NativeAdView) ((Activity) context).getLayoutInflater().inflate(R.layout.ad_unified_square, null);
            populateUnifiedNativeAdViewInList(nativeAd, adView);
            customNativeViewSet.removeAllViews();
            customNativeViewSet.addView(adView);
            //int padding = (int) context.getResources().getDimension(com.intuit.sdp.R.dimen._1sdp);
            customNativeViewSet.setPadding(0, 0, 0, 0);
        }
    }

    public void populateUnifiedNativeAdViewInList(NativeAd nativeAd, NativeAdView adView) {

        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

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
            adView.getIconView().setVisibility(View.GONE);
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
            adView.getAdvertiserView().setVisibility(View.GONE);
        }
        adView.setNativeAd(nativeAd);
    }
}
