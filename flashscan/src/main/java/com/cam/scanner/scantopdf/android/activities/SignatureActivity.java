package com.cam.scanner.scantopdf.android.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.BuildConfig;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.signature.ColorAdapter;
import com.cam.scanner.scantopdf.android.signature.SignatureAdapter;
import com.cam.scanner.scantopdf.android.signature.SingleFingerView;
import com.cam.scanner.scantopdf.android.signature.SingleFingerView2;
import com.cam.scanner.scantopdf.android.signature.SingleFingerView3;
import com.cam.scanner.scantopdf.android.signature.ViewOnTouchListener;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.cam.scanner.scantopdf.android.util.ScanConstants;
import com.theartofdev.edmodo.cropper.CropImage;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SignatureActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private static final int REQUEST_TAKE_PHOTO = 101;
    private static final int REQUEST_IMAGE_GET = 102;
    private static final int REQUEST_FOR_GET_SIGNATURE_URI = 103;
    private static final String TAG = SignatureActivity.class.getSimpleName();
    private String imagePath, folderName;
    private SingleFingerView iv_signature;
    private SingleFingerView2 iv_signature_2;
    private SingleFingerView3 iv_signature_3;
    private ImageView imageView, iv_back_toolbar, container_view, push_view, img_done, container_view_2, push_view_2, container_view_3, push_view_3;
    private ImageButton btn_add_sign;
    private FrameLayout img_remove_signature;
    private Context context;
    private TextView txt_no_signature;
    private RecyclerView rv_signature, rv_colors;
    private FlashScanUtil flashScanUtil;
    private Uri imageUri;
    private Bitmap scaledBitmap;
    private long dateTaken = 0;
    private RelativeLayout relative_main;
    private AlertDialog mDialog = null;
    private List<String> colorList;
    private int currentColor, previousColor;
    private List<String> signatureImageList;
    private SignatureAdapter signatureAdapter;
    private PrefManager prefManager;
    private String bmp = "";
    private View currentView;
    private ImageView signatureView;
    private String fileName;
    //    private ImageProcessing sdk = new ImageSdkLibrary().newProcessingInstance();
    private boolean showExitDialog = false;
    private View tut_add_sign, tut_after_add_signature;
    private Button btn_got_it, btn_got_it_1;

    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showExitDialog = true;
            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();

            View previousView = currentView;
            currentView = view;
            if (previousView != null && previousView != currentView) {
                previousView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
            }
            currentView.animate().scaleX(1.20f).scaleY(1.20f).setDuration(100);

            previousColor = currentColor;
            currentColor = Color.parseColor(colorList.get(position));
            if (container_view.getBackground().getConstantState() == context.getResources().getDrawable(R.drawable.iv_signature_focus).getConstantState()) {
                signatureView = container_view;
            } else if (container_view_2.getBackground().getConstantState() == context.getResources().getDrawable(R.drawable.iv_signature_focus).getConstantState()) {
                signatureView = container_view_2;
            } else {
                signatureView = container_view_3;
            }
            signatureView.post(() -> {
                scaledBitmap = replaceColor(scaledBitmap, previousColor, currentColor);
                signatureView.setImageBitmap(scaledBitmap);
            });
        }
    };

    private View.OnClickListener onImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showExitDialog = true;
            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            img_remove_signature.setVisibility(View.VISIBLE);
            img_done.setVisibility(View.VISIBLE);
            if (position == 0 && iv_signature.getVisibility() == View.VISIBLE) {
                if (iv_signature_2.getVisibility() == View.GONE) {
                    iv_signature_2.setVisibility(View.VISIBLE);
                    container_view.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                    container_view_2.setBackground(getResources().getDrawable(R.drawable.iv_signature_focus));
                    container_view_3.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                    push_view.setVisibility(View.GONE);
                    push_view_2.setVisibility(View.VISIBLE);
                    push_view_3.setVisibility(View.GONE);
                    signatureView = container_view_2;
                    rv_colors.setVisibility(View.VISIBLE);
                } else if (iv_signature_2.getVisibility() == View.VISIBLE && iv_signature_3.getVisibility() == View.GONE) {
                    iv_signature_3.setVisibility(View.VISIBLE);
                    container_view.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                    container_view_2.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                    container_view_3.setBackground(getResources().getDrawable(R.drawable.iv_signature_focus));
                    push_view.setVisibility(View.GONE);
                    push_view_2.setVisibility(View.GONE);
                    push_view_3.setVisibility(View.VISIBLE);
                    signatureView = container_view_3;
                    rv_colors.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(SignatureActivity.this, "Can not add more than 3 signatures", Toast.LENGTH_SHORT).show();
                }
            } else if (position == 1 && iv_signature.getVisibility() == View.VISIBLE && iv_signature_2.getVisibility() == View.VISIBLE) {
                if (iv_signature_3.getVisibility() == View.GONE) {
                    iv_signature_3.setVisibility(View.VISIBLE);
                    container_view.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                    container_view_2.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                    container_view_3.setBackground(getResources().getDrawable(R.drawable.iv_signature_focus));
                    push_view.setVisibility(View.GONE);
                    push_view_2.setVisibility(View.GONE);
                    push_view_3.setVisibility(View.VISIBLE);
                    signatureView = container_view_3;
                    rv_colors.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(SignatureActivity.this, "Can not add more than 3 signatures", Toast.LENGTH_SHORT).show();
                }
            }

            if (position == 0) {
                if (iv_signature.getVisibility() == View.GONE) {
                    iv_signature.setVisibility(View.VISIBLE);
                    container_view.setBackground(getResources().getDrawable(R.drawable.iv_signature_focus));
                    container_view_2.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                    container_view_3.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                    push_view.setVisibility(View.VISIBLE);
                    push_view_2.setVisibility(View.GONE);
                    push_view_3.setVisibility(View.GONE);
                    signatureView = container_view;
                    rv_colors.setVisibility(View.VISIBLE);
                }
            } else if (position == 1) {
                if (iv_signature_2.getVisibility() == View.GONE) {
                    iv_signature_2.setVisibility(View.VISIBLE);
                    container_view.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                    container_view_2.setBackground(getResources().getDrawable(R.drawable.iv_signature_focus));
                    container_view_3.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                    push_view.setVisibility(View.GONE);
                    push_view_2.setVisibility(View.VISIBLE);
                    push_view_3.setVisibility(View.GONE);
                    signatureView = container_view_2;
                    rv_colors.setVisibility(View.VISIBLE);
                }
            } else if (position == 2) {
                if (iv_signature_3.getVisibility() == View.GONE) {
                    iv_signature_3.setVisibility(View.VISIBLE);
                    container_view.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                    container_view_2.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                    container_view_3.setBackground(getResources().getDrawable(R.drawable.iv_signature_focus));
                    push_view.setVisibility(View.GONE);
                    push_view_2.setVisibility(View.GONE);
                    push_view_3.setVisibility(View.VISIBLE);
                    signatureView = container_view_3;
                    rv_colors.setVisibility(View.VISIBLE);
                }
            }
            signatureView.post(() -> {
                scaledBitmap = decodeBase64(signatureImageList.get(position));
                currentColor = Color.BLACK;
                signatureView.setImageBitmap(scaledBitmap);
            });
        }
    };

    private View.OnLongClickListener onLongClickListener = v -> {
        RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) v.getTag();
        int position = viewHolder.getAdapterPosition();
        showDeleteDialog(position);
        return true;
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        initObjects();
        getIntentData();
        findViewIds();
        setClickListeners();
        setImageView();
        handleTutorialView();
    }

    private void initObjects() {
        context = this;
        flashScanUtil = new FlashScanUtil(context);
        prefManager = new PrefManager(this);
        colorList = new ArrayList<>();
    }

    private void getIntentData() {
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.FILE_PATH)) {
            folderName = getIntent().getStringExtra(Constants.PutExtraConstants.FOLDER_NAME);
            imagePath = getIntent().getStringExtra(Constants.PutExtraConstants.FILE_PATH);
            fileName = getIntent().getStringExtra(Constants.PutExtraConstants.FILE_NAME);
        }
    }

    private void findViewIds() {
        imageView = findViewById(R.id.imageView);
        btn_add_sign = findViewById(R.id.btn_add_sign);
        img_remove_signature = findViewById(R.id.img_remove_signature);
        img_done = findViewById(R.id.img_done);
        iv_back_toolbar = findViewById(R.id.iv_back_toolbar);
        relative_main = findViewById(R.id.relative_main);
        txt_no_signature = findViewById(R.id.txt_no_signature);
        rv_signature = findViewById(R.id.rv_signature);
        rv_colors = findViewById(R.id.rv_colors);
        tut_add_sign = findViewById(R.id.tut_add_signature);
        tut_after_add_signature = findViewById(R.id.tut_after_add_signature);

        iv_signature = findViewById(R.id.iv_signature);
        iv_signature_2 = findViewById(R.id.iv_signature2);
        iv_signature_3 = findViewById(R.id.iv_signature3);
        container_view = findViewById(R.id.view);
        push_view = findViewById(R.id.push_view);
        container_view_2 = findViewById(R.id.view_2);
        push_view_2 = findViewById(R.id.push_view_2);
        container_view_3 = findViewById(R.id.view_3);
        push_view_3 = findViewById(R.id.push_view_3);
        btn_got_it = findViewById(R.id.btn_got_it);
        btn_got_it_1 = findViewById(R.id.btn_got_it_1);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rv_colors.setLayoutManager(layoutManager);

        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rv_signature.setLayoutManager(lm);
    }

    private void setImageView() {
        if (!TextUtils.isEmpty(imagePath)) {
            if (!isFinishing() || !isDestroyed()) {
                Glide.with(context).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).load(imagePath).into(imageView);
            }
        }
        addColors();
        loadSignatures();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setClickListeners() {
        btn_add_sign.setOnClickListener(this);
        iv_back_toolbar.setOnClickListener(this);
        img_remove_signature.setOnClickListener(this);
        img_done.setOnClickListener(this);
        imageView.setOnTouchListener(this);
        container_view.setOnClickListener(this);
        container_view_2.setOnClickListener(this);
        container_view_3.setOnClickListener(this);
        container_view.setOnTouchListener(new ViewOnTouchListener(context, push_view));
        container_view_2.setOnTouchListener(new ViewOnTouchListener(context, push_view_2));
        container_view_3.setOnTouchListener(new ViewOnTouchListener(context, push_view_3));
        btn_got_it.setOnClickListener(this);
        btn_got_it_1.setOnClickListener(this);
        tut_add_sign.setOnClickListener(this);
        tut_after_add_signature.setOnClickListener(this);
    }

    private void handleTutorialView() {
        if (prefManager.isAddSignatureTutWatched()) {
            tut_add_sign.setVisibility(View.GONE);
        } else {
            tut_add_sign.setVisibility(View.VISIBLE);
            prefManager.setAddSignatureTutWatched(true);
        }
    }

    private void handleAfterSignTutorialView() {
        if (prefManager.isAfterAddSignatureTutWatched()) {
            Log.e(TAG, "if handleAfterSignTutorialView");
            tut_after_add_signature.setVisibility(View.GONE);
        } else {
            Log.e(TAG, "else handleAfterSignTutorialView");
            tut_after_add_signature.setVisibility(View.VISIBLE);
            prefManager.setAfterAddSignatureTutWatched(true);
        }
    }

    private void addColors() {
        colorList.add("#000000");
        colorList.add("#ffffff");
        colorList.add("#ff0000");
        colorList.add("#006B00");
        colorList.add("#0000FF");
        colorList.add("#FFFF00");
        colorList.add("#FFA200");
        colorList.add("#21708D");
        colorList.add("#B7BEC8");
        colorList.add("#00005D");
        colorList.add("#6E0000");
        colorList.add("#62D96A");

        ColorAdapter colorAdapter = new ColorAdapter(this, colorList);
        rv_colors.setAdapter(colorAdapter);
        colorAdapter.setOnItemClickListener(onItemClickListener);
    }

    private void loadSignatures() {
        signatureImageList = prefManager.retrieveSignatureBitmap();
        if (signatureImageList == null) {
            signatureImageList = new ArrayList<>();
        }
        if (!signatureImageList.isEmpty()) {
            rv_signature.setVisibility(View.VISIBLE);
            txt_no_signature.setVisibility(View.GONE);
        } else {
            rv_signature.setVisibility(View.GONE);
            txt_no_signature.setVisibility(View.VISIBLE);
        }
        signatureAdapter = new SignatureAdapter(this, signatureImageList);
        rv_signature.setAdapter(signatureAdapter);
        signatureAdapter.setOnItemClickListener(onImageClickListener);
        signatureAdapter.setOnItemLongClickListener(onLongClickListener);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_got_it) {
            tut_add_sign.setVisibility(View.GONE);
        } else if (id == R.id.btn_got_it_1) {
            tut_after_add_signature.setVisibility(View.GONE);
        } else if (id == R.id.tut_add_signature || id == R.id.tut_after_add_signature) {
        } else if (id == R.id.btn_add_sign) {
            if (signatureImageList.size() >= 3) {
                Toast.makeText(SignatureActivity.this, "Can not add more than 3 signatures", Toast.LENGTH_SHORT).show();
            } else {
                showDialog();
            }
        } else if (id == R.id.img_remove_signature) {
            if (container_view.getBackground().getConstantState() == context.getResources().getDrawable(R.drawable.iv_signature_focus).getConstantState()) {
                iv_signature.setVisibility(View.GONE);
                img_remove_signature.setVisibility(View.GONE);
                rv_colors.setVisibility(View.GONE);
                container_view.setImageBitmap(null);
            } else if (container_view_2.getBackground().getConstantState() == context.getResources().getDrawable(R.drawable.iv_signature_focus).getConstantState()) {
                iv_signature_2.setVisibility(View.GONE);
                img_remove_signature.setVisibility(View.GONE);
                rv_colors.setVisibility(View.GONE);
                container_view_2.setImageBitmap(null);
            } else {
                iv_signature_3.setVisibility(View.GONE);
                img_remove_signature.setVisibility(View.GONE);
                rv_colors.setVisibility(View.GONE);
                container_view_3.setImageBitmap(null);
            }
            if (iv_signature.getVisibility() == View.GONE && iv_signature_2.getVisibility() == View.GONE && iv_signature_3.getVisibility() == View.GONE) {
                img_done.setVisibility(View.GONE);
            }
        } else if (id == R.id.img_done) {
            if (push_view.getVisibility() == View.VISIBLE) {
                container_view.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                push_view.setVisibility(View.GONE);
            }
            if (push_view_2.getVisibility() == View.VISIBLE) {
                container_view_2.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                push_view_2.setVisibility(View.GONE);
            }
            if (push_view_3.getVisibility() == View.VISIBLE) {
                container_view_3.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                push_view_3.setVisibility(View.GONE);
            }
            if (rv_colors.getVisibility() == View.VISIBLE) {
                rv_colors.setVisibility(View.GONE);
            }
            saveCombinedBitmap();
        } else if (id == R.id.iv_back_toolbar) {
            onBackPressed();
        } else if (id == R.id.view) {
            if (container_view.getBackground().getConstantState() == context.getResources().getDrawable(R.drawable.iv_signature_unfocus).getConstantState()) {
                container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
                container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                push_view.setVisibility(View.VISIBLE);
                push_view_2.setVisibility(View.GONE);
                push_view_3.setVisibility(View.GONE);
                rv_colors.setVisibility(View.VISIBLE);
                img_remove_signature.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.view_2) {
            if (container_view_2.getBackground().getConstantState() == context.getResources().getDrawable(R.drawable.iv_signature_unfocus).getConstantState()) {
                container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
                container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                push_view.setVisibility(View.GONE);
                push_view_2.setVisibility(View.VISIBLE);
                push_view_3.setVisibility(View.GONE);
                rv_colors.setVisibility(View.VISIBLE);
                img_remove_signature.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.view_3) {
            if (container_view_3.getBackground().getConstantState() == context.getResources().getDrawable(R.drawable.iv_signature_unfocus).getConstantState()) {
                container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
                container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                push_view.setVisibility(View.GONE);
                push_view_2.setVisibility(View.GONE);
                push_view_3.setVisibility(View.VISIBLE);
                rv_colors.setVisibility(View.VISIBLE);
                img_remove_signature.setVisibility(View.VISIBLE);
            }
        }
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.TRANSPARENT);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    private void saveCombinedBitmap() {
        Bitmap resizedBitmap = getBitmapFromView(relative_main);
        if (!TextUtils.isEmpty(imagePath)) {
            File file = new File(imagePath);
            if (file.isFile() && file.exists()) {
                fileName = file.getName();
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    int delete = context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Images.ImageColumns.DATA + "=?", new String[]{imagePath});
                    if (delete > 0) {
                    }
                }*/
                file.delete();
            }
        }
        if (TextUtils.isEmpty(fileName)) {
            fileName = getFileDateFormatName() + ".jpg";
        }
        String relativePath = null;
        try {
            relativePath = saveBitmapToFolder(context, resizedBitmap, folderName, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            File destOriginalPath = new File(flashScanUtil.getDocOriginalPath(context), folderName + File.separator + fileName);

            FileOutputStream out = new FileOutputStream(destOriginalPath);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            out.flush();
            out.close();

            AppController.getINSTANCE().dbHandler.deleteFile(folderName, fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(relativePath)) {
//            String path = Environment.getExternalStorageDirectory() + "/" + relativePath;
            File file = new File(relativePath);
            if (file.exists()) {
                dateTaken = file.lastModified();
            }
        }

        int screenFrom = getIntent().getIntExtra(ScanConstants.PutExtraConstants.FROM_SCREEN, 0);

        if (screenFrom == ScanConstants.ScreenConstants.FROM_EDIT_SCREEN) {
            setResult(RESULT_OK);
            finish();
        } else if (screenFrom == ScanConstants.ScreenConstants.FROM_MAIN_SCREEN) {
            setResult(RESULT_OK);
            finish();
        } else {
            moveToScanResultActivity(folderName);
        }
    }

    private String getFileDateFormatName() {
        String fileName;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd_HHmmss", Locale.getDefault());
        fileName = simpleDateFormat.format(calendar.getTime());
        return fileName;
    }

    private String saveBitmapToFolder(Context context, Bitmap bitmap, String documentFolderName, String fileName) throws IOException {
        File savedFile;
        String relativePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Constants.ITL_PDF_DOCS_DIRECTORY + "/" + documentFolderName;
        OutputStream outputStream = null;
     /*   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver contentResolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, relativePath);

            Uri imageUri = null;
            try {
                imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (imageUri != null) {
                try {
                    outputStream = contentResolver.openOutputStream(imageUri);
                } catch (FileNotFoundException e) {
                    contentResolver.delete(imageUri, null, null);
                }
            }

        } else {*/
        File directory = new File(relativePath);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            directory = new File(context.getExternalFilesDir(relativePath).toString());
        } else {
            directory = new File(Environment.getExternalStoragePublicDirectory(relativePath).toString());
        }*/

        //String imagesDir = Environment.getExternalStoragePublicDirectory(relativePath).toString();
        // File directory = new File(imagesDir);
        boolean isDirectoryCreated;
        if (!directory.exists()) {
            isDirectoryCreated = directory.mkdirs();
        } else {
            isDirectoryCreated = true;
        }
        if (isDirectoryCreated) {
            savedFile = new File(directory, fileName);
            if (savedFile.exists()) savedFile.delete();
            outputStream = new FileOutputStream(savedFile);
        }
        //}
        try {
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
            }
        } catch (Exception e) {
            outputStream.close();
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        return relativePath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (imageUri != null) {
                        CropImage.activity(imageUri).start(this);
                    }
                }
                break;
            case REQUEST_IMAGE_GET:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        ArrayList<Uri> uriList;
                        if (FlashScanUtil.isOsLessThanR()) {
                            uriList = new ArrayList<>(Matisse.obtainResult(data));
                        } else {
                            uriList = new ArrayList<>(FlashScanUtil.obtainResult(data));
                        }
                        if (!uriList.isEmpty()) {
                            CropImage.activity(uriList.get(0)).start(this);
                        }
                    }
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    assert result != null;
                    Uri uri = result.getUri();
                    Log.e(TAG, "RESULT_OK " + uri);
                    if (uri != null) {
                        InputStream imageStream = null;
                        try {
                            imageStream = getContentResolver().openInputStream(uri);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        showExitDialog = true;
                        rv_signature.setVisibility(View.VISIBLE);
                        txt_no_signature.setVisibility(View.GONE);
                        img_remove_signature.setVisibility(View.VISIBLE);
                        img_done.setVisibility(View.VISIBLE);
                        rv_colors.setVisibility(View.VISIBLE);

                        if (iv_signature.getVisibility() == View.GONE) {
                            iv_signature.setVisibility(View.VISIBLE);
                            iv_signature_2.setVisibility(View.GONE);
                            iv_signature_3.setVisibility(View.GONE);
                            push_view.setVisibility(View.VISIBLE);
                            push_view_2.setVisibility(View.GONE);
                            push_view_3.setVisibility(View.GONE);
                            container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
                            container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                            container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                            signatureView = container_view;
                        } else if (iv_signature.getVisibility() == View.VISIBLE && iv_signature_2.getVisibility() == View.GONE && iv_signature_3.getVisibility() == View.GONE) {
                            iv_signature.setVisibility(View.VISIBLE);
                            iv_signature_2.setVisibility(View.VISIBLE);
                            iv_signature_3.setVisibility(View.GONE);
                            push_view.setVisibility(View.GONE);
                            push_view_2.setVisibility(View.VISIBLE);
                            push_view_3.setVisibility(View.GONE);
                            container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                            container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
                            container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                            signatureView = container_view_2;
                        } else {
                            iv_signature.setVisibility(View.VISIBLE);
                            iv_signature_2.setVisibility(View.VISIBLE);
                            iv_signature_3.setVisibility(View.VISIBLE);
                            push_view.setVisibility(View.GONE);
                            push_view_2.setVisibility(View.GONE);
                            push_view_3.setVisibility(View.VISIBLE);
                            container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                            container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                            container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
                            signatureView = container_view_3;
                        }

                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        new Thread(() -> {
//                            MetaImage source = new MetaImage(selectedImage);
//                            source = sdk.imageBWBinarization(source);
                            Bitmap bitmap = Constants.bwOpenCvMain2(selectedImage);
//                            Bitmap bitmap = source.getBitmap();
                            scaledBitmap = replaceColor(bitmap, Color.WHITE, Color.TRANSPARENT);
                            signatureView.post(() -> {
                                signatureView.setImageBitmap(scaledBitmap);
                                currentColor = Color.BLACK;

                                bmp = encodeTobase64(scaledBitmap);
                                Log.e(TAG, "bmp " + bmp);
                                Log.e(TAG, "signatureImageList before " + signatureImageList.size());
                                signatureImageList.add(bmp);
                                Log.e(TAG, "signatureImageList after " + signatureImageList.size());
                                prefManager.saveSignatureBitmap(signatureImageList);
                                signatureAdapter.notifyDataSetChanged();
                            });
                        }).start();
                        handleAfterSignTutorialView();
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.e(TAG, "CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE ");
                    Exception error = result.getError();
                }
                break;
        }
    }

    private Bitmap replaceColor(Bitmap src, int fromColor, int targetColor) {
        if (src == null) {
            return null;
        }
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[width * height];
        src.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int x = 0; x < pixels.length; ++x) {
            pixels[x] = (pixels[x] == fromColor) ? targetColor : pixels[x];
        }
        Bitmap result = Bitmap.createBitmap(width, height, src.getConfig());
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    private String encodeTobase64(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    private void showDialog() {
        View dialogView = View.inflate(this, R.layout.custom_pick_sign_dialog, null);
        LinearLayout tv_camera = dialogView.findViewById(R.id.tv_camera);
        LinearLayout tv_media = dialogView.findViewById(R.id.tv_media);

        AlertDialog.Builder mMessageBuilder = new AlertDialog.Builder(SignatureActivity.this);
        mDialog = mMessageBuilder.create();
        Window window = mDialog.getWindow();
        assert window != null;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setView(dialogView);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
        WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
        mDialog.getWindow().setAttributes(params);

        tv_camera.setOnClickListener(v -> {
            takePicture();
            if (mDialog != null) {
                mDialog.dismiss();
            }
        });
        tv_media.setOnClickListener(v -> {
            openMedia();
            if (mDialog != null) {
                mDialog.dismiss();
            }
        });
    }

    private void moveToScanResultActivity(String folderName) {
        System.gc();
        if (!TextUtils.isEmpty(folderName)) {
            try {
                Intent intent = new Intent(context, ScanResultActivity.class);
                intent.putExtra(ScanConstants.PutExtraConstants.FROM_SCREEN, ScanConstants.ScreenConstants.FROM_ADD_SIGNATURE);
                intent.putExtra(ScanConstants.PutExtraConstants.FOLDER_NAME, folderName);
                intent.putExtra(ScanConstants.PutExtraConstants.DATE_TAKEN, dateTaken);
                if (context != null) {
                    startActivity(intent);
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openMedia() {
        if (FlashScanUtil.isOsLessThanR()) {
            Matisse.from(this).choose(MimeType.ofImage(), false).countable(true).showSingleMediaType(true).restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT).thumbnailScale(0.9f).maxSelectable(1).imageEngine(new GlideEngine()).forResult(REQUEST_IMAGE_GET);
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = flashScanUtil.createTempImageFile(context);
                Log.e(TAG, "photoFile" + photoFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    imageUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile);
                } else {
                    imageUri = Uri.fromFile(photoFile);
                }
                if (imageUri != null) {
                    Log.e(TAG, "Image uri not null");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, REQUEST_TAKE_PHOTO);
                }
            }
        }
    }

    private void showDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_signature));
        builder.setCancelable(false);
        builder.setPositiveButton("OK", (dialog, which) -> {
            signatureImageList.remove(position);
            prefManager.saveSignatureBitmap(signatureImageList);
            signatureAdapter.notifyDataSetChanged();

            if (signatureImageList.size() == 0) {
                rv_signature.setVisibility(View.GONE);
                txt_no_signature.setVisibility(View.VISIBLE);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void navigateToSignaturePreviewActivity(Uri imageUri) {
        Intent intent = new Intent(context, SignaturePreviewActivity.class);
        intent.putExtra(Constants.PutExtraConstants.URI, imageUri);
        startActivityForResult(intent, REQUEST_FOR_GET_SIGNATURE_URI);
    }

    @Override
    protected void onResume() {
        super.onResume();
        flashScanUtil.initOpenCv();
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View v, MotionEvent event) {
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
            container_view.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
            container_view_2.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
            container_view_3.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
            push_view.setVisibility(View.GONE);
            push_view_2.setVisibility(View.GONE);
            push_view_3.setVisibility(View.GONE);
            rv_colors.setVisibility(View.GONE);
            img_remove_signature.setVisibility(View.GONE);
            if (iv_signature.getVisibility() == View.VISIBLE || iv_signature_2.getVisibility() == View.VISIBLE || iv_signature_3.getVisibility() == View.VISIBLE) {
                img_done.setVisibility(View.VISIBLE);
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if (scaledBitmap != null && !scaledBitmap.isRecycled()) {
            try {
                scaledBitmap.recycle();
                scaledBitmap = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    private void showExitDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setMessage(getString(R.string.leave_without_changes)).setCancelable(false).setPositiveButton(R.string.discard, (dialog, which) -> {
            dialog.dismiss();
            finish();
        }).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (showExitDialog) {
            showExitDialog();
        } else {
            super.onBackPressed();
        }
    }
}
