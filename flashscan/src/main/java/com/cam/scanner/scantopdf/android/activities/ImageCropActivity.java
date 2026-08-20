package com.cam.scanner.scantopdf.android.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.BuildConfig;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.adapters.ImagesCropAdapter;
import com.cam.scanner.scantopdf.android.ads.AdClosed;
import com.cam.scanner.scantopdf.android.asynctasks.CopyFileTask;
import com.cam.scanner.scantopdf.android.db.DBConstants;
import com.cam.scanner.scantopdf.android.db.DBHandler;
import com.cam.scanner.scantopdf.android.db.StringUtils;
import com.cam.scanner.scantopdf.android.interfaces.CopyOperationListener;
import com.cam.scanner.scantopdf.android.interfaces.GoogleDriveDataUploadListener;
import com.cam.scanner.scantopdf.android.models.GoogleDriveFolderModel;
import com.cam.scanner.scantopdf.android.models.ImageCropping;
import com.cam.scanner.scantopdf.android.models.enums.FilterType;
import com.cam.scanner.scantopdf.android.ui.PreviewViewPager;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.cam.scanner.scantopdf.android.util.ScanConstants;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.itl.commonres.utils.AdsPlacementsEnum;
import com.itl.commonres.utils.CommonMethods;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import org.checkerframework.checker.units.qual.A;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


public class ImageCropActivity extends BaseActivity implements View.OnClickListener, AdClosed {

    private static final String TAG = ImageCropActivity.class.getSimpleName();
    private final int SIGNATURE_CODE = 47;
    private boolean isAnyChanges = false;

    private ArrayList<String> selectedImagesPathList;
    private ArrayList<ImageCropping> cropArr = new ArrayList<>();

    private LinearLayout cropPagerLinear, cropLinear, filterLinear;

    private ImagesCropAdapter cropAdapter;

    private PreviewViewPager imagesCropViewPager;

    private AppCompatTextView original, magic, gray, bw1, bw2, filter, cropSave, cropClick, rotateClick, addSignature, add;

    private CropImageView cropImageView;

    private ImageView iv;

    private int pageSelectPosition = 0;

    private String folderName;

//    private ImageProcessing sdk = new ImageSdkLibrary().newProcessingInstance();

    private Bitmap filterBMP;
    private View progress_lay;

    private FilterType mFilterType = FilterType.Original;
    private PrefManager prefManager;
    private Context context;
    private FlashScanUtil flashScanUtil;
    private View tut_image_crop;
    private Button btn_got_it;

    private boolean isBMP = false;

//    private MainIdentity mIdentity;

    private DBHandler dbHandler;
    private boolean shouldIntersCreateShow;

    private FrameLayout flMain;

    private ImageView ivCrownSign;
    private FrameLayout ad_view_banner_container;

    //This variable used for uploading newly added files to drive
    private boolean isSaveOnGoogleDrive = false;
    private String googleDriveFolderId;
    private CardView delete, finalSave, filterSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pre_img_editing);

        initObjects();
        findIds();

        setClickListeners();

        getData();
        fillProcessArr();
        setupViews();
        handlePremiumIconVisibility();
        handleTutorialView();

        loadAndShowBannerAd();
    }

    private void loadAndShowBannerAd() {
        if (flashScanUtil.isConnectingToInternet() && !prefManager.isAppAdFree() && com.itl.commonres.utils.Constants.isAdShow && CommonMethods.isAdActive(AdsPlacementsEnum.SH_IMAGE_EDIT.getValue())) {

            AdView adView = new AdView(this);
            //adView.setAdSize(AdSize.BANNER);
            AdSize adSize = getAdSize();
            // Step 4 - Set the adaptive ad size on the ad view.
            adView.setAdSize(adSize);
            adView.setAdUnitId(BuildConfig.AD_UNIT_ID_QR_RESULT_SCREEN_ADAPTIVE_AD);

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
        } else {
            Log.e(TAG, "Mobibuz : Ad Not Showing");
            ad_view_banner_container.setVisibility(View.GONE);
        }
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
   /* private Mat imageMat;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    imageMat = new Mat();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };*/

    @Override
    public void onResume() {
        super.onResume();

        /*if (flashScanUtil.isConnectingToInternet() && !prefManager.isAppAdFree() && com.itl.commonres.utils.Constants.isAdShow && CommonMethods.isAdActive(AdsPlacementsEnum.SH_IMAGE_EDIT.getValue())) {
            ad_view_banner_container.setVisibility(View.VISIBLE);
        } else {
            ad_view_banner_container.setVisibility(View.GONE);
        }*/
        flashScanUtil.initOpenCv();
    }

    private void handleTutorialView() {
        if (cropArr != null && !cropArr.isEmpty()) {
            if (cropArr.size() > 1) {
                if (prefManager.isImageCropTutorialWatched()) {
                    tut_image_crop.setVisibility(View.GONE);
                } else {
                    tut_image_crop.setVisibility(View.VISIBLE);
                    prefManager.setImageCropTutorialWatched(true);
                }
            }
        }
    }

    private void setClickListeners() {
        btn_got_it.setOnClickListener(this);
        tut_image_crop.setOnClickListener(this);
        original.setOnClickListener(this);
        magic.setOnClickListener(this);
        gray.setOnClickListener(this);
        bw1.setOnClickListener(this);
        bw2.setOnClickListener(this);
        filter.setOnClickListener(this);
        delete.setOnClickListener(this);
        finalSave.setOnClickListener(this);
        cropSave.setOnClickListener(this);
        cropClick.setOnClickListener(this);
        rotateClick.setOnClickListener(this);
        addSignature.setOnClickListener(this);
        add.setOnClickListener(this);
        filterSave.setOnClickListener(this);
    }

    private void initObjects() {
        context = this;
        prefManager = new PrefManager(context);
        flashScanUtil = new FlashScanUtil(context);
        dbHandler = AppController.getINSTANCE().dbHandler;

        /*mIdentity = new ViewModelProvider(this).get(MainIdentity.class);
        mIdentity.loadSettings();*/
    }

    private void findIds() {
        progress_lay = findViewById(R.id.progress_lay);
        filterLinear = findViewById(R.id.filter_linear);
        cropPagerLinear = findViewById(R.id.crop_pager_linear);
        cropLinear = findViewById(R.id.crop_linear);
        imagesCropViewPager = findViewById(R.id.cropped_views);
        cropImageView = findViewById(R.id.iv_crop);
        iv = findViewById(R.id.iv);
        original = findViewById(R.id.original);
        magic = findViewById(R.id.magicColor);
        gray = findViewById(R.id.grayMode);
        bw1 = findViewById(R.id.BWMode1);
        bw2 = findViewById(R.id.BWMode2);
        tut_image_crop = findViewById(R.id.tut_image_crop);
        btn_got_it = findViewById(R.id.btn_got_it);
        ad_view_banner_container = findViewById(R.id.ad_view_banner_container);
        filter = findViewById(R.id.filter);
        delete = findViewById(R.id.delete);
        finalSave = findViewById(R.id.final_save);
        cropSave = findViewById(R.id.save);
        cropClick = findViewById(R.id.crop);
        rotateClick = findViewById(R.id.rotate);
        addSignature = findViewById(R.id.add_signature);
        add = findViewById(R.id.add);
        filterSave = findViewById(R.id.fab_save);

        flMain = findViewById(R.id.fl_main);

        ivCrownSign = findViewById(R.id.iv_sign_crown);
    }

    private void getData() {
        pageSelectPosition = getIntent().getIntExtra("pos", 0);
        selectedImagesPathList = getIntent().getStringArrayListExtra(Constants.PutExtraConstants.SELECTED_IMAGES_LIST);
        folderName = getIntent().getStringExtra("folder_name");
        isBMP = getIntent().getBooleanExtra("is_bmp", false);

        if (getIntent() != null && getIntent().hasExtra(ScanConstants.PutExtraConstants.IS_FOLDER_EXISTS_ON_DRIVE)) {
            isSaveOnGoogleDrive = getIntent().getBooleanExtra(ScanConstants.PutExtraConstants.IS_FOLDER_EXISTS_ON_DRIVE, false);
            if (isSaveOnGoogleDrive) {
                googleDriveFolderId = getIntent().getStringExtra(ScanConstants.PutExtraConstants.GOOGLE_DRIVE_FOLDER_ID);
            }
        }
    }

    private boolean isAdShowedForSignature = false;

    public void addSignatureClick() {
        if (CommonMethods.isConnectingToInternet(this) && com.itl.commonres.utils.Constants.isAdShow && isAdShowedForSignature) {
            Constants.isAppInBackground = false;
            loadInterstitialAd(context, BuildConfig.INTERSTITIAL_PDF, this);
        } else {
            isAdShowedForSignature = false;
            showSignatureWarningDialog();
        }
        /*if (*//*prefManager.isPremiumYearly() || prefManager.isPremiumQuarterly()*//**//*isAdShowedForSignature*//* !com.itl.commonres.utils.Constants.isAdShow) {
            isAdShowedForSignature = false;
            new AlertDialog.Builder(this).setTitle(getString(R.string.warning))
                    .setMessage(getString(R.string.sign_save_warning))
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        pageSelectPosition = imagesCropViewPager.getCurrentItem();

                        Intent intent = new Intent(context, SignatureActivity.class);
                        intent.putExtra(Constants.PutExtraConstants.FOLDER_NAME, folderName);
                        intent.putExtra(Constants.PutExtraConstants.FILE_PATH, selectedImagesPathList.get(pageSelectPosition));
                        intent.putExtra(Constants.PutExtraConstants.FILE_NAME, cropArr.get(pageSelectPosition).fileName);
                        intent.putExtra(ScanConstants.PutExtraConstants.FROM_SCREEN, ScanConstants.ScreenConstants.FROM_EDIT_SCREEN);
                        startActivityForResult(intent, SIGNATURE_CODE);
                    }).setNegativeButton(android.R.string.cancel, null).show();
        } else {
            Constants.isAppInBackground = false;
            loadInterstitialAd(context, BuildConfig.INTERSTITIAL_PDF, this);
//            askToBePremium();
        }*/
    }

    private void showSignatureWarningDialog() {
        AlertDialog warningDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.warning)
                .setMessage(R.string.sign_save_warning)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> openSignatureScreen())
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        warningDialog.setOnShowListener(dialogInterface -> {
            warningDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, com.itl.commonres.R.color.black_text_color));
            warningDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this, com.itl.commonres.R.color.black_text_color));
        });
        warningDialog.show();
    }

    private void openSignatureScreen() {
        pageSelectPosition = imagesCropViewPager.getCurrentItem();

        Intent intent = new Intent(context, SignatureActivity.class);
        intent.putExtra(Constants.PutExtraConstants.FOLDER_NAME, folderName);
        intent.putExtra(Constants.PutExtraConstants.FILE_PATH, selectedImagesPathList.get(pageSelectPosition));
        intent.putExtra(Constants.PutExtraConstants.FILE_NAME, cropArr.get(pageSelectPosition).fileName);
        intent.putExtra(ScanConstants.PutExtraConstants.FROM_SCREEN, ScanConstants.ScreenConstants.FROM_EDIT_SCREEN);
        startActivityForResult(intent, SIGNATURE_CODE);
    }

    private void handlePremiumIconVisibility() {
        if (/*prefManager.isPremiumYearly() || prefManager.isPremiumQuarterly()*/true) {
            ivCrownSign.setVisibility(View.GONE);
        } else {
            ivCrownSign.setVisibility(View.VISIBLE);
        }
    }

    private void askToBePremium() {
        String deviceIdOfInstallTime = prefManager.getDeviceIdOfInstallTime();
        String existingEmailInDb = dbHandler.getEmail(deviceIdOfInstallTime);
        if (Constants.IS_OWN_API_IMPLEMENT && (existingEmailInDb == null || existingEmailInDb.isEmpty())) {
            openAskEmailActivity(Constants.PLAN_ACTIVITY_PREMIUM);
        } else {
            openPremiumActivity();
        }
    }

    private void openAskEmailActivity(int whichPlanActivity) {
        Intent intent = new Intent(ImageCropActivity.this, AskEmailActivity.class);
        intent.putExtra(Constants.EXTRA_NAV_TO_PLAN, whichPlanActivity);
        startActivity(intent);
    }

    private void openPremiumActivity() {
        Intent intent = new Intent(ImageCropActivity.this, PremiumActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CODE_PREMIUM_YEALY);
    }

    private void fillProcessArr() {
        for (int index = 0; index < selectedImagesPathList.size(); index++) {
            File file = new File(selectedImagesPathList.get(index));

            ImageCropping editingData = AppController.getINSTANCE().dbHandler.fetchRecord(folderName, file.getName());
            if (editingData == null) editingData = new ImageCropping();
            else editingData.mLastFilterType = FilterType.forValue(editingData.filterType);

            if (isBMP && prefManager.isAutoCropEnabled()) {

                editingData.processBmp = getBitmapFromPath(FlashScanUtil.getDocProcessingPath(context).getAbsolutePath(), selectedImagesPathList.get(index));

                if (editingData.processBmp == null) {
                    editingData.processBmp = getBitmapFromPath(FlashScanUtil.getDocOriginalPath(context).getAbsolutePath(), selectedImagesPathList.get(index));
                } else {
                    try {
                        CropImageView cropImageView = new CropImageView(this);
                        cropImageView.setImageBitmap(editingData.processBmp);
                        editingData.processBmp = cropImageView.getCroppedImage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            editingData.processedPath = selectedImagesPathList.get(index);
            editingData.fileName = file.getName();

            cropArr.add(editingData);

            file = null;
        }
    }

    private Bitmap getBitmapFromPath(String folderPath, String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;

        File file = new File(path);
        try {
            File originalPath = new File(folderPath, folderName + File.separator + file.getName());

            return BitmapFactory.decodeFile(originalPath.getAbsolutePath(), options);
//                cropping.processBmp = BitmapFactory.decodeFile(path, options);
        } finally {
            file = null;
        }
    }

    private void cropFilter(int i) {
        System.out.println("filter type==" + cropArr.get(i).filterType);
        switch (FilterType.forValue(cropArr.get(i).filterType)) {
            case Original:
                break;
            case GRAY:
                cropArr.get(i).processBmp = bitmapToGrayScale(cropArr.get(i).processBmp);
                break;
            case Magic:

                //Pixel Netica
                /*MetaImage source = new MetaImage(cropArr.get(i).processBmp);
                source.setStrongShadows(prefManager.isStrongShadowEnabled());   // if need
                source = sdk.imageColorBinarization(source);
                cropArr.get(i).processBmp = source.getBitmap();*/

                // androidhive magic filter
                /*Bitmap bitmap = cropArr.get(i).processBmp.copy(cropArr.get(i).processBmp.getConfig(), true);
                Filter myfilter = FilterPack.getClarendon(getApplicationContext());
                cropArr.get(i).processBmp = myfilter.processFilter(bitmap);*/
                Mat adaptiveTh = new Mat();
                try {
                    Utils.bitmapToMat(cropArr.get(i).processBmp, adaptiveTh);
                    adaptiveTh.convertTo(adaptiveTh, -1, 1.5, 60);
                    Bitmap bitmap = Bitmap.createBitmap(adaptiveTh.cols(), adaptiveTh.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(adaptiveTh, bitmap);
                    cropArr.get(i).processBmp = bitmap;
                } finally {
                    adaptiveTh = null;
                }
                break;

            case BW1:
                cropArr.get(i).processBmp = createBW1(cropArr.get(i).processBmp, 50);
                break;
            case BW2:
                // Black and white invert colors
                //cropArr.get(i).processBmp = ChangetoSketch(cropArr.get(i).processBmp);

                // Pixel Netica
                /*MetaImage sourceBw = new MetaImage(cropArr.get(i).processBmp);
                sourceBw.setStrongShadows(prefManager.isStrongShadowEnabled());   // if need
                sourceBw = sdk.imageBWBinarization(sourceBw);
                cropArr.get(i).processBmp = sourceBw.getBitmap();*/


                //androidhive black and white filter
               /* Filter myfilter = FilterPack.getMetropolis(getApplicationContext());
                iv.setImageBitmap(myfilter.processFilter(cropArr.get(i).processBmp));
                */

                // OpenCV
                Mat adaptiveTh1 = new Mat();
                try {
                    Utils.bitmapToMat(cropArr.get(i).processBmp, adaptiveTh1);
                    Imgproc.cvtColor(adaptiveTh1, adaptiveTh1, Imgproc.COLOR_RGBA2GRAY);
                    Imgproc.adaptiveThreshold(adaptiveTh1, adaptiveTh1, 200, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 4);
                    Bitmap bitmap = Bitmap.createBitmap(adaptiveTh1.cols(), adaptiveTh1.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(adaptiveTh1, bitmap);
                    cropArr.get(i).processBmp = bitmap;
                } finally {
                    adaptiveTh = null;
                }
                /*Mat adaptiveTh1 = new Mat();
                try {
                    Utils.bitmapToMat(cropArr.get(i).processBmp, adaptiveTh1);
                    Imgproc.cvtColor(adaptiveTh1, adaptiveTh1, Imgproc.COLOR_BGR2GRAY);
//                    Imgproc.medianBlur(adaptiveTh1, adaptiveTh1, 3);
                    Imgproc.threshold(adaptiveTh1, adaptiveTh1, 0, 255, Imgproc.THRESH_OTSU);
                    Imgproc.adaptiveThreshold(adaptiveTh1, adaptiveTh1, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 4);
                    Utils.matToBitmap(adaptiveTh1, cropArr.get(i).processBmp);
                    cropArr.get(i).processBmp = Bitmap.createBitmap(adaptiveTh1.cols(), adaptiveTh1.rows(), Bitmap.Config.ARGB_8888);
                } finally {
                    adaptiveTh1 = null;
                }*/
                break;
        }
    }

    private void setupViews() {
        cropAdapter = new ImagesCropAdapter(this, cropArr);
        cropAdapter.setViewGroup(imagesCropViewPager);
        imagesCropViewPager.setAdapter(cropAdapter);
        imagesCropViewPager.setCurrentItem(pageSelectPosition);
    }

    private void fillOriginalBMP() {
        if (cropArr.get(pageSelectPosition).originalBmp == null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;

            File file = new File(cropArr.get(pageSelectPosition).processedPath);
            try {
                cropArr.get(pageSelectPosition).originalBmp = BitmapFactory.decodeFile(getOriginalPath());

                if (cropArr.get(pageSelectPosition).originalBmp == null)
                    cropArr.get(pageSelectPosition).originalBmp = returnBMP(file);

            } finally {
                file = null;
                options = null;
            }
        }
    }

    private String getOriginalPath() {
        String originalPath = selectedImagesPathList.get(pageSelectPosition);
        if (!originalPath.contains(Constants.originalFolderName)) {
            String[] split = originalPath.split(Constants.docs + "/");
            for (int i = 0; i < split.length; i++) {
                if (i == 0) {
                    originalPath = split[i] + Constants.docs + File.separator;
                } else if (i == 1) {
                    originalPath = originalPath + Constants.originalFolderName + File.separator + split[i];
                }
            }
        }
        selectedImagesPathList.set(pageSelectPosition, originalPath);
        Log.e("splitOriginalPath::", originalPath);
        return originalPath;
    }

    private Bitmap returnBMP(File path) {
        FutureTarget<Bitmap> futureTarget = null;
        try {
            futureTarget = Glide.with(context).asBitmap().load(path).submit(800, 800);

            return futureTarget.get();

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (futureTarget != null) Glide.with(context).clear(futureTarget);
        }
        return null;
    }

    public void cropClick() {
        try {
            pageSelectPosition = imagesCropViewPager.getCurrentItem();

            filterLinear.setVisibility(View.GONE);
            cropPagerLinear.setVisibility(View.GONE);
            cropLinear.setVisibility(View.VISIBLE);

            progress_lay.setVisibility(View.VISIBLE);

            fillOriginalBMP();

            progress_lay.setVisibility(View.GONE);

            cropImageView.setVisibility(View.VISIBLE);

            try {
                cropImageView.setImageBitmap(cropArr.get(pageSelectPosition).originalBmp);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (cropArr.get(pageSelectPosition).cropRect == null) {
                Bitmap currentBitmap = cropArr.get(pageSelectPosition).originalBmp;
                if (currentBitmap != null) {
                    cropArr.get(pageSelectPosition).cropRect = new Rect(0, 0, currentBitmap.getWidth(), currentBitmap.getHeight());
                }
            }

            cropImageView.setCropRect(cropArr.get(pageSelectPosition).cropRect);

            /*String x = cropArr.get(pageSelectPosition).x;
            String y = cropArr.get(pageSelectPosition).y;

            if (x != null && y != null && !TextUtils.isEmpty(x)) {
                Point[] points = new Point[4];

                String[] pointX = StringUtils.convertStringToArray(x);
                String[] pointY = StringUtils.convertStringToArray(y);

                for (int i = 0; i < pointX.length; i++) {
                    points[i] = new Point();
                    points[i].x = (int) Double.parseDouble(pointX[i]);
                    points[i].y = (int) Double.parseDouble(pointY[i]);
                }

                try {
                    //cropImageView.setCropPoints(points);
                    cropImageView.setCropRect(new Rect(points[0].x, points[0].y, points[2].x, points[2].y));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Bitmap currentBitmap = cropArr.get(pageSelectPosition).originalBmp;
                    if (currentBitmap != null) {
                        cropImageView.setCropRect(new Rect(0, 0, currentBitmap.getWidth(), currentBitmap.getHeight()));
                    } else {
                        // Fallback if bitmap is unexpectedly null
                        cropImageView.setCropRect(new Rect(0, 0, 0, 0));
                        Log.e("ImageCropActivity", "originalBmp is null when setting initial full crop rect");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }*/
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void saveCropClick() {
        isAnyChanges = true;
        progress_lay.setVisibility(View.VISIBLE);
        AsyncTask.execute(() -> {
            try {
                cropArr.get(pageSelectPosition).processBmp = cropImageView.getCroppedImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
            cropFilter(pageSelectPosition);

            /*float[] points = new float[0];
            try {
                points = cropImageView.getCropPoints();
            } catch (Exception e) {
                e.printStackTrace();
            }

            ArrayList<String> pointX = new ArrayList<>();
            ArrayList<String> pointY = new ArrayList<>();

            Log.e(TAG, "saveCropClick: points"+points);

            for (int i = 0; i < points.length; i++) {
                Log.e(TAG, "saveCropClick: points"+points[i]);
                if (i % 2 == 0) {
                    pointX.add(String.valueOf(points[i]));
                } else {
                    pointY.add(String.valueOf(points[i]));
                }
            }

            Log.e(TAG, "saveCropClick: pointx :    "+pointX);
            Log.e(TAG, "saveCropClick: pointy :    "+pointY);

            cropArr.get(pageSelectPosition).x = StringUtils.convertArrayToString(pointX);
            cropArr.get(pageSelectPosition).y = StringUtils.convertArrayToString(pointY);*/

            cropArr.get(pageSelectPosition).cropRect = cropImageView.getCropRect();
            cropArr.get(pageSelectPosition).processBmp = rotateImage(cropArr.get(pageSelectPosition).processBmp, cropArr.get(pageSelectPosition).rotation);

            runOnUiThread(() -> {
                cropAdapter.notifyDataSetChanged();

                cropPagerLinear.setVisibility(View.VISIBLE);
                cropLinear.setVisibility(View.GONE);
                filterLinear.setVisibility(View.GONE);

                cropImageView.setVisibility(View.INVISIBLE);

                progress_lay.setVisibility(View.GONE);
            });
        });

    }

    public void rotateClick() {
        isAnyChanges = true;

        pageSelectPosition = imagesCropViewPager.getCurrentItem();

        progress_lay.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                cropArr.get(pageSelectPosition).rotation += 90;

                if (cropArr.get(pageSelectPosition).rotation == 360)
                    cropArr.get(pageSelectPosition).rotation = 0;

                if (cropArr.get(pageSelectPosition).processBmp == null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;
                    cropArr.get(pageSelectPosition).processBmp = BitmapFactory.decodeFile(cropArr.get(pageSelectPosition).processedPath, options);

                    options = null;
                }

                cropArr.get(pageSelectPosition).processBmp = rotateImage(cropArr.get(pageSelectPosition).processBmp, 90);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cropAdapter.notifyDataSetChanged();
                        progress_lay.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    public void filterClick() {

        pageSelectPosition = imagesCropViewPager.getCurrentItem();

        filterLinear.setVisibility(View.VISIBLE);
        cropPagerLinear.setVisibility(View.GONE);
        cropLinear.setVisibility(View.GONE);

        progress_lay.setVisibility(View.VISIBLE);
//        AsyncTask.execute(() -> {

        fillOriginalBMP();

        progress_lay.setVisibility(View.GONE);
        iv.setVisibility(View.VISIBLE);
        originalClick();
        //iv.setImageBitmap(cropArr.get(pageSelectPosition).originalBmp);
        //applyDefaultFilter(pageSelectPosition);
    }

    /*public void applyDefaultFilter(int index) {
        FilterType type = FilterType.forValue(cropArr.get(index).filterType);

        cropArr.get(index).mLastFilterType = type;

        if (type == FilterType.Original) {
            originalClick();
        } else if (type == FilterType.Magic) {
            magicClick();
        } else if (type == FilterType.GRAY) {
            grayClick();
        } else if (type == FilterType.BW1) {
            bw1Click();
        } else if (type == FilterType.BW2) {
            bw2Click();
        }else {
            originalClick();
        }
    }*/

    public void originalClick() {
        mFilterType = FilterType.Original;
        filterBMP = cropArr.get(pageSelectPosition).originalBmp;
        iv.setImageBitmap(cropArr.get(pageSelectPosition).originalBmp);
        /*highlightSelectedFilter(original);*/
        highlightSelectedFilter(original, mFilterType);
        TextView[] textViews = new TextView[]{magic, gray, bw1, bw2};
        /*unHighLightOtherViews(textViews);*/
        unHighLightOtherViews(textViews, mFilterType);
    }

    private void highlightSelectedFilter(TextView textView, FilterType mFilterType) {
        textView.setBackground(ContextCompat.getDrawable(this,R.drawable.filter_selection));
        /*switch (mFilterType) {
            case Original:
                textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_original_white, 0, 0);
                break;
            case BW1:
            case BW2:
                textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_b_w_1_white, 0, 0);
                break;
            case GRAY:
                textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_gray_mode_white, 0, 0);
                break;
            case Magic:
                textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_magic_color_white, 0, 0);
                break;
        }*/
        //textView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        //textView.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void unHighLightOtherViews(TextView[] textViews, FilterType mFilterType) {
        /*for (TextView textView : textViews) {
            textView.setBackgroundColor(ContextCompat.getColor(this,com.itl.commonres.R.color.black_text_color));
            textView.setTextColor(ContextCompat.getColor(this,com.itl.commonres.R.color.black_text_color));
        }*/

        switch (mFilterType) {
            case Original:
                magic.setBackgroundResource(android.R.color.transparent);
                gray.setBackgroundResource(android.R.color.transparent);
                bw1.setBackgroundResource(android.R.color.transparent);
                bw2.setBackgroundResource(android.R.color.transparent);
                break;
            case Magic:
                original.setBackgroundResource(android.R.color.transparent);
                gray.setBackgroundResource(android.R.color.transparent);
                bw1.setBackgroundResource(android.R.color.transparent);
                bw2.setBackgroundResource(android.R.color.transparent);
                break;
            case GRAY:
                magic.setBackgroundResource(android.R.color.transparent);
                original.setBackgroundResource(android.R.color.transparent);
                bw1.setBackgroundResource(android.R.color.transparent);
                bw2.setBackgroundResource(android.R.color.transparent);
                break;

            case BW1:
                magic.setBackgroundResource(android.R.color.transparent);
                gray.setBackgroundResource(android.R.color.transparent);
                original.setBackgroundResource(android.R.color.transparent);
                bw2.setBackgroundResource(android.R.color.transparent);
                break;
            case BW2:
                magic.setBackgroundResource(android.R.color.transparent);
                gray.setBackgroundResource(android.R.color.transparent);
                bw1.setBackgroundResource(android.R.color.transparent);
                original.setBackgroundResource(android.R.color.transparent);
                break;
        }
    }

    public void magicClick() {

        mFilterType = FilterType.Magic;


        // Pixel Netica
        /*MetaImage source = new MetaImage(cropArr.get(pageSelectPosition).originalBmp);
        source.setStrongShadows(prefManager.isStrongShadowEnabled());
        source = sdk.imageColorBinarization(source);
        filterBMP = source.getBitmap();
        iv.setImageBitmap(filterBMP);*/


        // androidhive magic filter
       /* Bitmap bitmap = cropArr.get(pageSelectPosition).originalBmp.copy(cropArr.get(pageSelectPosition).originalBmp.getConfig(), true);
        Filter myfilter = FilterPack.getClarendon(getApplicationContext());
        iv.setImageBitmap(myfilter.processFilter(bitmap));
        filterBMP = bitmap;*/

        // OpenCV
        Mat adaptiveTh = new Mat();
        try {
            Utils.bitmapToMat(cropArr.get(pageSelectPosition).originalBmp, adaptiveTh);
            adaptiveTh.convertTo(adaptiveTh, -1, 1.5, 60);
            Bitmap bitmap = Bitmap.createBitmap(adaptiveTh.cols(), adaptiveTh.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(adaptiveTh, bitmap);
            filterBMP = bitmap;
            iv.setImageBitmap(filterBMP);
        } finally {
            adaptiveTh = null;
        }

        /*highlightSelectedFilter(magic);*/
        highlightSelectedFilter(magic, mFilterType);
        TextView[] textViews = new TextView[]{original, gray, bw1, bw2};
        /*unHighLightOtherViews(textViews);*/
        unHighLightOtherViews(textViews, mFilterType);
    }

    public void grayClick() {
        mFilterType = FilterType.GRAY;

        filterBMP = bitmapToGrayScale(cropArr.get(pageSelectPosition).originalBmp);
        iv.setImageBitmap(filterBMP);
        /*highlightSelectedFilter(gray);*/
        highlightSelectedFilter(gray, mFilterType);
        TextView[] textViews = new TextView[]{original, magic, bw1, bw2};
        /*unHighLightOtherViews(textViews);*/
        unHighLightOtherViews(textViews, mFilterType);

    }

    public void bw1Click() {

        mFilterType = FilterType.BW1;

        progress_lay.setVisibility(View.VISIBLE);
        AsyncTask.execute(() -> {
            filterBMP = createBW1(cropArr.get(pageSelectPosition).originalBmp, 50);
            runOnUiThread(() -> {
                iv.setImageBitmap(filterBMP);
                progress_lay.setVisibility(View.GONE);
                /*highlightSelectedFilter(bw1);*/
                highlightSelectedFilter(bw1, mFilterType);
                TextView[] textViews = new TextView[]{original, magic, gray, bw2};
                /*unHighLightOtherViews(textViews);*/
                unHighLightOtherViews(textViews, mFilterType);
            });
        });
    }

    public void bw2Click() {
        mFilterType = FilterType.BW2;

        // black and white invertcolors
      /*  filterBMP = ChangetoSketch(cropArr.get(pageSelectPosition).originalBmp);
        iv.setImageBitmap(filterBMP);
        highlightSelectedFilter(bw2, mFilterType);
        TextView[] textViews = new TextView[]{original, magic, gray, bw1};
        unHighLightOtherViews(textViews, mFilterType);*/


        // Pixel Netica
        /*MetaImage source = new MetaImage(cropArr.get(pageSelectPosition).originalBmp);
        source.setStrongShadows(prefManager.isStrongShadowEnabled());
        source = sdk.imageBWBinarization(source);
        filterBMP = source.getBitmap();
        iv.setImageBitmap(filterBMP);
        highlightSelectedFilter(bw2, mFilterType);
        TextView[] textViews = new TextView[]{original, magic, gray, bw1};
        unHighLightOtherViews(textViews, mFilterType);*/


        //OpenCV
        Mat adaptiveTh = new Mat();
        try {
            Utils.bitmapToMat(cropArr.get(pageSelectPosition).originalBmp, adaptiveTh);
            Imgproc.cvtColor(adaptiveTh, adaptiveTh, Imgproc.COLOR_RGBA2GRAY);
//            Imgproc.medianBlur(adaptiveTh, adaptiveTh, 3);
            Imgproc.adaptiveThreshold(adaptiveTh, adaptiveTh, 200, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 4);
            filterBMP = Bitmap.createBitmap(adaptiveTh.cols(), adaptiveTh.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(adaptiveTh, filterBMP);
            iv.setImageBitmap(filterBMP);
            //highlightSelectedFilter(bw2);
            highlightSelectedFilter(bw2, mFilterType);
            TextView[] textViews = new TextView[]{original, magic, gray, bw1};
            //unHighLightOtherViews(textViews);
            unHighLightOtherViews(textViews, mFilterType);
        } finally {
            adaptiveTh = null;
        }
    }

    private Bitmap ChangetoSketch(Bitmap bmp) {
        Bitmap Copy, Invert;
        Copy = bmp;
        Copy = bitmapToGrayScale(Copy);
        Invert = createInvertedBitmap(Copy);
        return Invert;
    }

    public void saveFilter() {

        isAnyChanges = true;

        progress_lay.setVisibility(View.VISIBLE);


           // Rect rect = cropImageView.getCropRect();

            /*String x = cropArr.get(pageSelectPosition).x;
            String y = cropArr.get(pageSelectPosition).y;*/
                try {
                    if(cropArr.get(pageSelectPosition).cropRect == null) {
                        cropArr.get(pageSelectPosition).processBmp = filterBMP;
                    }else {
                        cropArr.get(pageSelectPosition).processBmp = cropBitmap(filterBMP, cropArr.get(pageSelectPosition).cropRect);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            /*if (x != null && y != null && !TextUtils.isEmpty(x)) {
                Point[] points = new Point[4];

                String[] pointX = StringUtils.convertStringToArray(x);
                String[] pointY = StringUtils.convertStringToArray(y);

                for (int i = 0; i < pointX.length; i++) {
                    points[i] = new Point();
                    points[i].x = (int) Double.parseDouble(pointX[i]);
                    points[i].y = (int) Double.parseDouble(pointY[i]);
                }

                cropImageView.setCropRect(new Rect(
                        points[0].x,
                        points[0].y,
                        points[2].x,
                        points[2].y
                ));

                cropArr.get(pageSelectPosition).processBmp = cropImageView.getCroppedImage();
            } else {
                cropArr.get(pageSelectPosition).processBmp = filterBMP;
            }*/

            cropArr.get(pageSelectPosition).processBmp = rotateImage(cropArr.get(pageSelectPosition).processBmp, cropArr.get(pageSelectPosition).rotation);

            cropArr.get(pageSelectPosition).filterApplyCount = 1;
            cropArr.get(pageSelectPosition).filterType = mFilterType.getValue();

                cropAdapter.notifyDataSetChanged();

                filterLinear.setVisibility(View.GONE);
                cropPagerLinear.setVisibility(View.VISIBLE);
                cropLinear.setVisibility(View.GONE);

                iv.setVisibility(View.INVISIBLE);

                progress_lay.setVisibility(View.GONE);



    }

    public Bitmap cropBitmap(Bitmap sourceBitmap, Rect cropRect) {
        if (sourceBitmap == null) {
            Log.e("BitmapCrop", "Source bitmap is null.");
            return null;
        }
        if (cropRect == null) {
            Log.e("BitmapCrop", "Crop rectangle is null.");
            return sourceBitmap; // Or return null, depending on desired behavior
        }

        // Ensure the crop rectangle is within the bounds of the source bitmap
        int cropX = Math.max(0, cropRect.left);
        int cropY = Math.max(0, cropRect.top);
        int cropWidth = Math.min(sourceBitmap.getWidth() - cropX, cropRect.width());
        int cropHeight = Math.min(sourceBitmap.getHeight() - cropY, cropRect.height());

        if (cropWidth <= 0 || cropHeight <= 0) {
            Log.e("BitmapCrop", "Crop rectangle has zero or negative dimensions or is outside the source bitmap.");
            // Optionally, you could return the original bitmap or a part of it that is valid
            // For now, returning null or a specific error bitmap might be appropriate.
            return null; // Or handle as an error
        }

        try {
            Bitmap croppedBitmap = Bitmap.createBitmap(
                    sourceBitmap,
                    cropX,
                    cropY,
                    cropWidth,
                    cropHeight
            );
            return croppedBitmap;
        } catch (IllegalArgumentException e) {
            // This can happen if, despite checks, the calculated crop dimensions are invalid
            // (e.g. x + width > source.getWidth()) due to floating point inaccuracies or complex Rects.
            // The above boundary checks should minimize this.
            Log.e("BitmapCrop", "Failed to create cropped bitmap: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
            Log.e("BitmapCrop", "OutOfMemoryError while creating cropped bitmap: " + e.getMessage());
            // Handle OOM, perhaps by suggesting a smaller crop or scaling
            return null;
        }
    }

    public void finalSave() {
        int totalApplyFilterCount = 0;
        for (int i = 0; i < cropArr.size(); i++) {
            if (cropArr.get(i).filterApplyCount == 1) {
                totalApplyFilterCount += 1;
                mFilterType = FilterType.forValue(cropArr.get(i).filterType);
            }
        }

        boolean isApplyFilterAll = AppController.getINSTANCE().dbHandler.isApplyAllFilterOptionAvailable(folderName);

        if (!isApplyFilterAll) {
            if (cropArr.size() == 1 || totalApplyFilterCount > 1) {
                new SaveBMP().execute();
            } else if (totalApplyFilterCount == 1) {
                new AlertDialog.Builder(this).setTitle(getString(R.string.warning)).setMessage(getString(R.string.apply_same_filter_msg)).setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                    progress_lay.setVisibility(View.VISIBLE);
                    AsyncTask.execute(() -> {
                        for (int k = 0; k < selectedImagesPathList.size(); k++) {

                            if (cropArr.get(k).processBmp == null) {
                                cropArr.get(k).processBmp = getBitmapFromPath(FlashScanUtil.getDocProcessingPath(context).getAbsolutePath(), selectedImagesPathList.get(k));
                            }
                            cropArr.get(k).filterType = mFilterType.getValue();
                            cropFilter(k);
                        }
                        runOnUiThread(() -> new SaveBMP().execute());
                    });
                }).setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                    new SaveBMP().execute();
                }).show();
            } else {
                new SaveBMP().execute();
            }
        } else {
            new SaveBMP().execute();
        }
    }

    private boolean isAdShow() {
        for (int i = 0; i < cropArr.size(); i++) {
            if (cropArr.get(i).filterType != cropArr.get(i).mLastFilterType.getValue()) {
                return true;
            }
        }
        return false;
    }

    public void delClick() {
        AlertDialog deleteDialog = new AlertDialog.Builder(this).setMessage(getString(R.string.sure_you_want_delete)).setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {
            pageSelectPosition = imagesCropViewPager.getCurrentItem();

            File dstFolderName = new File(flashScanUtil.getDocProcessingPath(context), folderName);
            File dstOriginalFolderName = new File(flashScanUtil.getDocOriginalPath(context), folderName);

            dstFolderName = new File(dstFolderName, cropArr.get(pageSelectPosition).fileName);
            dstFolderName.delete();

            dstOriginalFolderName = new File(dstOriginalFolderName, cropArr.get(pageSelectPosition).fileName);
            dstOriginalFolderName.delete();

            selectedImagesPathList.remove(pageSelectPosition);
            AppController.getINSTANCE().dbHandler.deleteFile(folderName, cropArr.get(pageSelectPosition).fileName);
            cropArr.remove(pageSelectPosition);

            if (cropArr.size() < 1) {
                finish();
                return;
            }

            cropAdapter.notifyDataSetChanged();
        }).setNegativeButton(android.R.string.no, null).create();

        deleteDialog.setOnShowListener(dialog -> {
            deleteDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            deleteDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        });
        deleteDialog.show();
    }

    static Bitmap createBW1(Bitmap src, double value) {
        //rishav black and white
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int) (((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (R < 0) {
                    R = 0;
                } else if (R > 255) {
                    R = 255;
                }

                G = Color.red(pixel);
                G = (int) (((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (G < 0) {
                    G = 0;
                } else if (G > 255) {
                    G = 255;
                }

                B = Color.red(pixel);
                B = (int) (((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (B < 0) {
                    B = 0;
                } else if (B > 255) {
                    B = 255;
                }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        return bmOut;
    }

    static Bitmap bitmapToGrayScale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    @Override
    public void onBackPressed() {
        if (filterLinear.getVisibility() == View.VISIBLE || cropLinear.getVisibility() == View.VISIBLE) {
            filterLinear.setVisibility(View.GONE);
            cropLinear.setVisibility(View.GONE);
            cropPagerLinear.setVisibility(View.VISIBLE);

            cropImageView.setVisibility(View.INVISIBLE);
        } else {
            if (isAnyChanges) {
                new AlertDialog.Builder(this).setMessage(getString(R.string.leave_without_changes)).setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    finish();
                }).setNegativeButton(getString(R.string.no), null).show();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (CommonMethods.multipleClicked()) {
            return;
        }
        int id = v.getId();
        if (id == R.id.btn_got_it) {
            tut_image_crop.setVisibility(View.GONE);
        } else if (id == R.id.tut_image_crop) {
        } else if (id == R.id.original) {
            originalClick();
        } else if (id == R.id.magicColor) {
            magicClick();
        } else if (id == R.id.grayMode) {
            grayClick();
        } else if (id == R.id.BWMode1) {
            bw1Click();
        } else if (id == R.id.BWMode2) {
            bw2Click();
        } else if (id == R.id.filter) {
            filterClick();
        } else if (id == R.id.delete) {
            delClick();
        } else if (id == R.id.final_save) {
            finalSave();
        } else if (id == R.id.save) {
            saveCropClick();
        } else if (id == R.id.crop) {
            cropClick();
        } else if (id == R.id.rotate) {
            rotateClick();
        } else if (id == R.id.add_signature) {
            addSignatureClick();
        } else if (id == R.id.add) {
            onAddImgClick();
        } else if (id == R.id.fab_save) {
            saveFilter();
        }
    }

    @Override
    public void onAdClosed() {
        isAdShowedForSignature = true;
        findViewById(R.id.add_signature).performClick();
    }

    @Override
    public void onAdLoadedOrFailed(boolean isLoaded) {
        isAdShowedForSignature = true;
        findViewById(R.id.add_signature).performClick();
    }

    public class SaveBMP extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress_lay.setVisibility(View.VISIBLE);
            AppController.getINSTANCE().dbHandler.insertApplyAllFilter(folderName, DBConstants.APPLY_ALL_FILTER_VALUE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i < cropArr.size(); i++) {
                try {
                    FileOutputStream out;
                    if (cropArr.get(i).processBmp == null) continue;

                    File file = new File(flashScanUtil.getDocProcessingPath(context), folderName);
                    file = new File(file, cropArr.get(i).fileName);

                    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        ContentResolver resolver = getContentResolver();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName() + ".jpg");
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                        out = (FileOutputStream) resolver.openOutputStream(Objects.requireNonNull(imageUri));
                    } else {*/
                    out = new FileOutputStream(file);
                    //}


//                    cropArr.get(i).processBmp = rotateImage(cropArr.get(i).processBmp, cropArr.get(i).rotation);
                    cropArr.get(i).processBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    Objects.requireNonNull(out).close();

                    Log.e(TAG, "doInBackground: " + cropArr.get(i).fileName);
                    //Save in DB
                    if (AppController.getINSTANCE().dbHandler.isRecordExists(folderName, file.getName())) {
                        AppController.getINSTANCE().dbHandler.updatePoints(folderName, file.getName(), cropArr.get(i).x, cropArr.get(i).y);

                        AppController.getINSTANCE().dbHandler.updateRotation(folderName, file.getName(), cropArr.get(i).rotation);

                        AppController.getINSTANCE().dbHandler.updateRect(folderName, file.getName(), cropArr.get(i).cropRect);

                        AppController.getINSTANCE().dbHandler.updateFilter(folderName, file.getName(), cropArr.get(i).filterType);
                    } else {
                        AppController.getINSTANCE().dbHandler.insertDefaultFilter(folderName, file.getName(), cropArr.get(i).filterType, cropArr.get(i).x, cropArr.get(i).y, cropArr.get(pageSelectPosition).rotation, cropArr.get(i).cropRect);
                    }

                    out.flush();
                    out.close();

                    file = null;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progress_lay.setVisibility(View.GONE);

            boolean showIntersCreation = dbHandler.showIntersCreation();

            int existingFreq = dbHandler.getIntersCreateFreq();
            Log.i(TAG, "at the time of load ::: existingFreq: " + existingFreq);

            int allowedFreq = dbHandler.intersCreateFreqInSession();
            Log.i(TAG, "allowedFreq: " + allowedFreq);

            if (existingFreq < allowedFreq) {
                shouldIntersCreateShow = true;
            } else {
                shouldIntersCreateShow = false;
            }

            Log.i(TAG, "showIntersCreation: " + showIntersCreation + ", " + "shouldIntersCreateShow: " + shouldIntersCreateShow + ", isAdShow: " + isAdShow());

            Log.i(TAG, "isAdShow confirms filter was applied.");

            //        if (!prefManager.isAppAdFree() && Constants.SHOW_INTERSTITIAL_ADS.FOR_SCAN_RESULT_ACTIVITY) {

            if (/*!prefManager.isAppAdFree() && showIntersCreation && shouldIntersCreateShow && isAdShow()*/true) {
                if (flashScanUtil.isConnectingToInternet() && com.itl.commonres.utils.Constants.isAdShow) {

                    //show((AdClosed) this);
                    Constants.isAppInBackground = false;

                    loadInterstitialAd(ImageCropActivity.this, BuildConfig.INTERSTITIAL_PDF, new AdClosed() {
                        @Override
                        public void onAdClosed() {
                            finish();
                        }

                        @Override
                        public void onAdLoadedOrFailed(boolean isLoaded) {
                            finish();
                        }
                    });


           /*         if (!AdManager.getInstance().isAdLoaded()) {
                        loadAndShowAd();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!isAdLoaded) {
                                    progress_lay.setVisibility(View.GONE);
                                    finish();

                                }
                            }
                        }, Constants.AD_HOLDING_TIME);
                    } else {

                        increaseIntersCount();

                        AdManager.getInstance().showAd(new AdManagerListener() {
                            @Override
                            public void onAdLoaded() {

                            }

                            @Override
                            public void onAdFailedToLoad() {

                            }

                            @Override
                            public void onAdClosed() {
                                finish();
                            }
                        });
                    }*/
                } else {
                    Log.e(TAG, "Mobibuz : Ad Not Showing");
                    finish();
                }
            } else {
                finish();
            }

        }
    }

    private boolean isAdLoaded = false;

   /* private void loadAndShowAd() {
        progress_lay.setVisibility(View.VISIBLE);
        loadInterstitialAd(context, BuildConfig.INTERSTITIAL_AD_ID_FOR_SCAN_RESULT_ACTIVITY, new AdManagerListener() {
            @Override
            public void onAdLoaded() {
                progress_lay.setVisibility(View.GONE);
                isAdLoaded = true;
                showAd(new AdManagerListener() {
                    @Override
                    public void onAdLoaded() {

                    }

                    @Override
                    public void onAdFailedToLoad() {

                    }

                    @Override
                    public void onAdClosed() {
                        finish();
                    }
                });

                increaseIntersCount();
            }

            @Override
            public void onAdFailedToLoad() {
                progress_lay.setVisibility(View.GONE);
                finish();
            }

            @Override
            public void onAdClosed() {

            }
        });
    }*/

    private void increaseIntersCount() {
        int existingFreq = dbHandler.getIntersCreateFreq();
        Log.i(TAG, "at the time of show::: existingFreq: " + existingFreq);

        int currentFreq = existingFreq + 1;
        Log.i(TAG, "currentAttempt: " + currentFreq);

        if (dbHandler.existIntersCreateFreq()) {
            dbHandler.updateIntersCreateFreq(currentFreq);
            Log.i(TAG, "update");
        } else {
            dbHandler.insertIntersCreateFreq(currentFreq);
            Log.i(TAG, "insert");
        }
    }

    public void onAddImgClick() {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_add_new_page);
        dialog.setCancelable(true);

        LinearLayout ll_camera = dialog.findViewById(R.id.ll_camera);
        LinearLayout ll_gallery = dialog.findViewById(R.id.ll_gallery);

        ll_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                /*tempFile = new File(getExternalFilesDir("img"), System.currentTimeMillis() + ".jpg");
                Intent startCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(ImageCropActivity.this, BuildConfig.APPLICATION_ID + ".fileprovider", tempFile);
                } else {
                    uri = Uri.fromFile(tempFile);
                }
                startCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                if (startCameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(startCameraIntent, 201);
                }*/

                Intent captureIntent = new Intent(ImageCropActivity.this, CaptureImagesActivity.class);
                startActivityForResult(captureIntent, 201);
                /*final File fileSink = getExternalCacheDir();
                Intent intent = CameraActivity.newIntent(
                        ImageCropActivity.this,
                        mIdentity.SdkFactory,
                        fileSink.getAbsolutePath(),
                        "camera-prefs",
                        true);
                startActivityForResult(intent, 201);*/

            }
        });

        ll_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                /*if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }*/
                if (FlashScanUtil.isOsLessThanR()) {
                    try {
                        Matisse.from(ImageCropActivity.this).choose(MimeType.ofImage(), false).countable(true).showSingleMediaType(true).restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT).thumbnailScale(0.9f).maxSelectable(1).imageEngine(new GlideEngine()).forResult(200);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, 200);
                }

            }
        });

        dialog.show();
       /* new AlertDialog.Builder(this)
                .setSingleChoiceItems(new String[]{"Camera", "Gallery"}, 0, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        switch (selectedPosition) {
                            case 0:
                                tempFile = new File(getExternalFilesDir("img"), System.currentTimeMillis() + ".jpg");
                                Intent startCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                Uri uri;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    uri = FileProvider.getUriForFile(ImageCropActivity.this, BuildConfig.APPLICATION_ID + ".fileprovider", tempFile);
                                } else {
                                    uri = Uri.fromFile(tempFile);
                                }
                                startCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                                if (startCameraIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivityForResult(startCameraIntent, 201);
                                }
                                break;
                            case 1:
                                Matisse.from(ImageCropActivity.this)
                                        .choose(MimeType.ofImage(), false)
                                        .countable(true)
                                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                        .thumbnailScale(0.9f)
                                        .maxSelectable(1)
                                        .imageEngine(new GlideEngine())
                                        .forResult(200);
                                break;
                        }
                    }
                }).setNegativeButton(getString(android.R.string.cancel), null)
                .show();*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        try {
            if (requestCode == 201) {
                ArrayList<String> capturedPaths = (ArrayList<String>) data.getSerializableExtra("cam_paths");
                copyFiles(capturedPaths);
            } else if (requestCode == 200) {

                int corruptFileCount = 0;

                ArrayList<String> selectedImagesPathList;
                if (FlashScanUtil.isOsLessThanR()) {
                    selectedImagesPathList = new ArrayList<>(Matisse.obtainPathResult(data));
                } else {
                    selectedImagesPathList = new ArrayList(FlashScanUtil.getClipData(data, this));
                }
                ArrayList<String> tempList = new ArrayList<>();
                try {
                    Log.e(TAG, "aa " + selectedImagesPathList.size());

                    for (int i = 0; i < selectedImagesPathList.size(); i++) {
                        File file = new File(selectedImagesPathList.get(i));

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                        int imageHeight = options.outHeight;
                        int imageWidth = options.outWidth;


                        if ((imageHeight == -1 && imageWidth == -1) || file.length() <= 0) {
                            corruptFileCount++;
                        } else {
                            tempList.add(selectedImagesPathList.get(i));

                        }

                    }
                    selectedImagesPathList = new ArrayList<>(tempList);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (corruptFileCount > 0) {
                    flashScanUtil.showSnackBar(findViewById(android.R.id.content), String.format(getString(R.string.corrupted_file_error) + "", corruptFileCount));
                }
                if (selectedImagesPathList.size() > 0) {
                    copyFiles(selectedImagesPathList);
                }


            } else if (requestCode == SIGNATURE_CODE) {
                cropArr.get(pageSelectPosition).processBmp = null;
                cropArr.get(pageSelectPosition).originalBmp = null;

                cropAdapter.notifyDataSetChanged();
                imagesCropViewPager.setCurrentItem(pageSelectPosition);
            } else if (requestCode == Constants.REQUEST_CODE_PREMIUM_YEALY) {
                Log.i(TAG, "onActivityResult REQUEST_CODE_PREMIUM_YEALY");
                if (resultCode == RESULT_OK) {
                    //PREMIUM taken
                    handlePremium();
                    if (prefManager.getPurchasedPlanName() == Constants.BUY_NOW_YEARLY) {
                        flashScanUtil.showSnackBar(flMain, getString(R.string.premium_yearly_success_msg, getString(R.string.app_name)));
                    } else {
                        flashScanUtil.showSnackBar(flMain, getString(R.string.premium_quarterly_success_msg, getString(R.string.app_name)));
                    }
                    //flashScanUtil.showSnackBar(flMain, getString(R.string.premium_yearly_success_msg));
                    try {
                        reCreate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePremium() {
        //Do required UI or any changes
        handlePremiumIconVisibility();
    }

    private void reCreate() {
        //No need to reCreate here
        /*finish();
        startActivity(getIntent());*/
    }

    private void copyFiles(ArrayList<String> selectedImagesPathList) {


        File dstFolderName = new File(flashScanUtil.getDocProcessingPath(context), folderName);
        File dstOriginalFolderName = new File(flashScanUtil.getDocOriginalPath(context), folderName);

        if (!dstFolderName.exists()) dstFolderName.mkdirs();

        if (!dstOriginalFolderName.exists()) dstOriginalFolderName.mkdirs();

        new CopyFileTask(this, selectedImagesPathList, dstFolderName.getAbsolutePath(), dstOriginalFolderName.getAbsolutePath(), new CopyOperationListener() {
            @Override
            public void onCopyStart() {
                progress_lay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCopyComplete(int fileOperation) {

                AsyncTask.execute(() -> {
                    ImageCropActivity.this.selectedImagesPathList.add(selectedImagesPathList.get(0));

                    for (int i = 0; i < selectedImagesPathList.size(); i++) {
                        ImageCropping cropping = new ImageCropping();
                        final File file = new File(selectedImagesPathList.get(i));

                        cropping.processBmp = getBitmapFromPath(flashScanUtil.getDocOriginalPath(context).getAbsolutePath(), selectedImagesPathList.get(i));

                        cropping.processedPath = selectedImagesPathList.get(i);
                        cropping.fileName = file.getName();

                        cropArr.add(cropping);
                    }

                    runOnUiThread(() -> {
                        cropAdapter.notifyDataSetChanged();
                        progress_lay.setVisibility(View.GONE);

                        imagesCropViewPager.setCurrentItem(cropArr.size());
                        Log.e(TAG, "isSaveOnGoogleDrive: " + isSaveOnGoogleDrive);
                        Log.e(TAG, "googleDriveFolderId: " + googleDriveFolderId);
                        if (isSaveOnGoogleDrive && googleDriveFolderId != null) {
                            Log.e(TAG, "onCopyComplete: ");
                            uploadFilesToDrive(selectedImagesPathList, context.getResources().getString(R.string.uploading_files_to_drive), false, "");
                        }
                    });

                });
            }
        }, true).execute();
    }

    public static Bitmap rotateImage(@NonNull Bitmap imageToOrient, int degreesToRotate) {
        Bitmap result = imageToOrient;
        try {

            Matrix matrix = new Matrix();
            matrix.setRotate(degreesToRotate);
            result = Bitmap.createBitmap(imageToOrient, 0, 0, imageToOrient.getWidth(), imageToOrient.getHeight(), matrix, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static Bitmap createContrast(Bitmap src, double value) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int) (((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (R < 0) {
                    R = 0;
                } else if (R > 255) {
                    R = 255;
                }

                G = Color.red(pixel);
                G = (int) (((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (G < 0) {
                    G = 0;
                } else if (G > 255) {
                    G = 255;
                }

                B = Color.red(pixel);
                B = (int) (((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (B < 0) {
                    B = 0;
                } else if (B > 255) {
                    B = 255;
                }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        return bmOut;
    }

    public static Bitmap createBlackAndWhite(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                int gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);

                // use 128 as threshold, above -> white, below -> black
                if (gray > 128) gray = 255;
                else gray = 0;
                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, gray, gray, gray));
            }
        }
        return bmOut;
    }


    public static Bitmap createInvertedBitmap(Bitmap src) {
        ColorMatrix colorMatrix_Inverted = new ColorMatrix(new float[]{-1, 0, 0, 0, 255, 0, -1, 0, 0, 255, 0, 0, -1, 0, 255, 0, 0, 0, 1, 0});

        ColorFilter ColorFilter_Sepia = new ColorMatrixColorFilter(colorMatrix_Inverted);

        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();

        paint.setColorFilter(ColorFilter_Sepia);
        canvas.drawBitmap(src, 0, 0, paint);

        return bitmap;
    }

    private void uploadFilesToDrive(ArrayList<String> selectedImagesPathList, String dialogMsg, boolean isRename, String driveFileId) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(dialogMsg);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        Log.d(TAG, "saveFileInDrive: " + new Gson().toJson(selectedImagesPathList));

        List<GoogleDriveFolderModel> googleDriveFolderModelList = prefManager.getGoogleDriveDataList();
        for (GoogleDriveFolderModel googleDriveFolderModel : googleDriveFolderModelList) {
            if (googleDriveFolderModel.getId().equalsIgnoreCase(googleDriveFolderId)) {
                int fileCount = 0;
                boolean isDissmissDialog = false;
                for (String filePath : selectedImagesPathList) {
                    File file = new File(filePath);
                    fileCount++;
                    if (fileCount == selectedImagesPathList.size()) {
                        isDissmissDialog = true;
                    }
                    Log.e(TAG, "uploadFilesToDrive: googleDriveFolderId = " + googleDriveFolderId);
                    Log.e(TAG, "uploadFilesToDrive: folderName = " + folderName);
                    Log.e(TAG, "uploadFilesToDrive: file.getName() = " + file.getName());
                    Log.e(TAG, "uploadFilesToDrive: file.getGoogleDriveChildFileModelList() = " + new Gson().toJson(googleDriveFolderModel.getGoogleDriveChildFileModelList()));
                    Log.e(TAG, "uploadFilesToDrive: file.getAbsolutePath() = " + file.getAbsolutePath());
                    flashScanUtil.uploadFile(googleDriveFolderId, folderName, file.getName(), googleDriveFolderModel.getGoogleDriveChildFileModelList(), file, flashScanUtil.getMimeType(file.getAbsolutePath()), progressDialog, isDissmissDialog, true, new GoogleDriveDataUploadListener() {
                        @Override
                        public void onUploadFinish(String folderId) {
                            // Toast.makeText(context, context.getResources().getString(R.string.file_uploded),Toast.LENGTH_SHORT).show();
                            //getFolderNameAndFetchFiles();
                            if (isRename) {
                                flashScanUtil.deleteFolderByIdFromGoogleDrive(context, driveFileId, context.getResources().getString(R.string.updating_file_metadata), () -> {
                                    prefManager.deleteFolderFromGoogleDriveDataList(driveFileId);
                                    Log.e(TAG, "onUploadFinish: file deleted");
                                    Toast.makeText(context, context.getResources().getString(R.string.file_updated), Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                //getFolderNameAndFetchFiles();
                                Toast.makeText(context, context.getResources().getString(R.string.file_uploded), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                break;
            }
        }
    }

}
