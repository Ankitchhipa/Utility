package com.cam.scanner.scantopdf.android.pdf;

import static com.cam.scanner.scantopdf.android.util.Constants.ROOT_FOLDER_NAME;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.cam.scanner.scantopdf.android.BuildConfig;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.activities.SignaturePreviewActivity;
import com.cam.scanner.scantopdf.android.asynctasks.CreatePdfTask;
import com.cam.scanner.scantopdf.android.interfaces.PDFCreationCallback;
import com.cam.scanner.scantopdf.android.models.FileModel;
import com.cam.scanner.scantopdf.android.models.ImageToPdfOptions;
import com.cam.scanner.scantopdf.android.signature.ColorAdapter;
import com.cam.scanner.scantopdf.android.signature.SignatureAdapter;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;
import com.cam.scanner.scantopdf.android.util.PrefManager;
import com.itl.commonres.utils.CommonMethods;
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
import java.util.Objects;

public class PdfSignatureActivity extends AppCompatActivity implements View.OnClickListener, PDFCreationCallback {

    private RecyclerView recyclerView;
    private Context context;
    public String imagesDirPath;
    private ImageButton btn_add_sign;
    private List<String> signatureImageList;

    private static final int REQUEST_TAKE_PHOTO = 101;
    private static final int REQUEST_IMAGE_GET = 102;
    private static final int REQUEST_FOR_GET_SIGNATURE_URI = 103;
    public String folderName;
    public ImageView iv_back_toolbar, img_done;
    private TextView txt_no_signature, tv_pdf_page_count;
    public RecyclerView rv_signature;
    public RecyclerView rv_colors;
    private FlashScanUtil flashScanUtil;
    private Uri imageUri;
    private static final String TAG = PdfSignatureActivity.class.getSimpleName();
    private AlertDialog mDialog = null;
    private SignatureAdapter signatureAdapter;
    private PrefManager prefManager;
    public CustomLinearLayoutManager customLinearLayoutManager;
    PdfSignatureAdapter pdfSignatureAdapter;
    public List<String> colorList;
    public int currentColor;
    public View currentView;
    //    private ImageProcessing sdk;
    List<FileModel> selectedFileModelList;
    List<String> filePathList;
    public PdfSignatureAdapter.MyViewHolder myViewHolder;
    private boolean showExitDialog = false;

    public FrameLayout img_remove_signature;
    int pageCount = 1;

    public ArrayList<String> getFetchedImagesList() {
        if (fetchedImagesList == null) {
            fetchedImagesList = new ArrayList<>();
        }
        return fetchedImagesList;
    }

    private RelativeLayout progress_lay;
    private ArrayList<String> fetchedImagesList = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_signature);

        init();
        findIds();
        setClickListeners();
        getImagesFromDir();
    }

    private View.OnClickListener onImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            showExitDialog = true;
            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();

            img_remove_signature.setVisibility(View.VISIBLE);
            customLinearLayoutManager.setScrollEnabled(false);
            img_done.setVisibility(View.VISIBLE);

            rv_colors.setVisibility(View.VISIBLE);
            Bitmap scaledBitmap = decodeBase64(signatureImageList.get(position));
            pdfSignatureAdapter.setSavedImage(position, scaledBitmap);
        }
    };

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

            int previousColor = currentColor;
            currentColor = Color.parseColor(colorList.get(position));
            pdfSignatureAdapter.changeSignColor(previousColor, currentColor);
        }
    };


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

    public Bitmap replaceColor(Bitmap src, int fromColor, int targetColor) {
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
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    private View.OnLongClickListener onLongClickListener = v -> {
        RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) v.getTag();
        int position = viewHolder.getAdapterPosition();
        showDeleteDialog(position);
        return true;
    };

    private void showDialog() {
        View dialogView = View.inflate(this, R.layout.custom_pick_sign_dialog, null);
        LinearLayout tv_camera = dialogView.findViewById(R.id.tv_camera);
        LinearLayout tv_media = dialogView.findViewById(R.id.tv_media);

        AlertDialog.Builder mMessageBuilder = new AlertDialog.Builder(context);
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

    private void openMedia() {
        if (FlashScanUtil.isOsLessThanR()) {
            try {
                Matisse.from(this).choose(MimeType.ofImage(), false).countable(true)
                        .showSingleMediaType(true)
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .thumbnailScale(0.9f).maxSelectable(1).imageEngine(new GlideEngine())
                        .forResult(REQUEST_IMAGE_GET);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    imageUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider"
                            , photoFile);
                } else {
                    imageUri = Uri.fromFile(photoFile);
                }
                if (imageUri != null) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, REQUEST_TAKE_PHOTO);
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setClickListeners() {
        btn_add_sign.setOnClickListener(this);
        iv_back_toolbar.setOnClickListener(this);
        img_remove_signature.setOnClickListener(this);
        img_done.setOnClickListener(this);
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
                    if (uri != null) {
                        InputStream imageStream = null;
                        try {
                            imageStream = getContentResolver().openInputStream(uri);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        rv_signature.setVisibility(View.VISIBLE);
                        txt_no_signature.setVisibility(View.GONE);
                        img_remove_signature.setVisibility(View.VISIBLE);
                        img_done.setVisibility(View.VISIBLE);
                        rv_colors.setVisibility(View.VISIBLE);
                        customLinearLayoutManager.setScrollEnabled(false);

                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        Bitmap bitmap = Constants.bwOpenCvMain2(selectedImage);
//                        MetaImage source = new MetaImage(selectedImage);
//                        source = sdk.imageBWBinarization(source);
//                        Bitmap bitmap = source.getBitmap();
                        Bitmap scaledBitmap = replaceColor(bitmap, Color.WHITE, Color.TRANSPARENT);
                        pdfSignatureAdapter.setSignature(scaledBitmap);

                        String bmp = encodeTobase64(scaledBitmap);
                        signatureImageList.add(bmp);
                        prefManager.saveSignatureBitmap(signatureImageList);
                        signatureAdapter.notifyDataSetChanged();
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();

                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        flashScanUtil.initOpenCv();
    }

    private void showExitDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setMessage(getString(R.string.leave_without_changes))
                .setCancelable(false)
                .setPositiveButton(R.string.discard, (dialog, which) -> {
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

    private void getImagesFromDir() {
        if (getIntent() != null && getIntent().hasExtra(Constants.PutExtraConstants.FOLDER_PATH)) {
            imagesDirPath = getIntent().getStringExtra(Constants.PutExtraConstants.FOLDER_PATH);
        }

        if (!TextUtils.isEmpty(imagesDirPath)) {
            File dir = new File(imagesDirPath);
            if (dir.isDirectory() && dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    flashScanUtil.sortFilesByNameAtoZ(files);
                    /*Collections.reverse(Arrays.asList(files));*/
                    ArrayList<String> imagesPathList = new ArrayList<>();
                    for (File file : files) {
                        if (file != null && file.isFile() && file.exists()) {
                            imagesPathList.add(file.getPath());
                        }
                    }
                    if (!imagesPathList.isEmpty()) {
                        if (!getFetchedImagesList().isEmpty()) {
                            getFetchedImagesList().clear();
                        }
                        getFetchedImagesList().addAll(imagesPathList);
                        pageCount = getFetchedImagesList().size();
                        updatePageCountText(0);
                        populateRV(getFetchedImagesList());
                    }
                }
            }
        }
    }

    private void populateRV(ArrayList<String> imagesPathList) {
        setUpRecyclerView();
        pdfSignatureAdapter = new PdfSignatureAdapter(context, PdfSignatureActivity.this, imagesPathList);
        recyclerView.setAdapter(pdfSignatureAdapter);

        addColors();
        loadSignatures();
    }

    private void init() {
        context = this;
        flashScanUtil = new FlashScanUtil(context);
        prefManager = new PrefManager(this);
        colorList = new ArrayList<>();
        selectedFileModelList = new ArrayList<>();
        filePathList = new ArrayList<>();

//        sdk = new ImageSdkLibrary().newProcessingInstance();
    }

    private void findIds() {
        recyclerView = findViewById(R.id.recyclerView);
        btn_add_sign = findViewById(R.id.btn_add_sign);
        progress_lay = findViewById(R.id.progress_lay);
        img_remove_signature = findViewById(R.id.img_remove_signature);
        img_done = findViewById(R.id.img_done);
        iv_back_toolbar = findViewById(R.id.iv_back_toolbar);
        txt_no_signature = findViewById(R.id.txt_no_signature);
        rv_signature = findViewById(R.id.rv_signature);
        rv_colors = findViewById(R.id.rv_colors);
        tv_pdf_page_count = findViewById(R.id.tv_pdf_page_count);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        rv_colors.setLayoutManager(layoutManager);

        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rv_signature.setLayoutManager(lm);
    }

    private void setUpRecyclerView() {
        customLinearLayoutManager = new CustomLinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(customLinearLayoutManager);
        recyclerView.setHasFixedSize(true);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int pos = customLinearLayoutManager.findFirstCompletelyVisibleItemPosition();
                    if (pos > -1) {
                        updatePageCountText(pos);
                    }
                    RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(pos);
                    myViewHolder = (PdfSignatureAdapter.MyViewHolder) viewHolder;
                    pdfSignatureAdapter.updateViewHolder(myViewHolder);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
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

        ColorAdapter colorAdapter = new ColorAdapter((Activity) context, colorList);
        rv_colors.setAdapter(colorAdapter);
        colorAdapter.setOnItemClickListener(onItemClickListener);
    }


    @Override
    public void onClick(View v) {
        if (CommonMethods.multipleClicked()) {
            return;
        }
        int id = v.getId();
        if (id == R.id.btn_add_sign) {
            if (signatureImageList.size() >= 3) {
                Toast.makeText(context, "Can not add more than 3 signatures",
                        Toast.LENGTH_SHORT).show();
            } else {
                showDialog();
            }
        } else if (id == R.id.img_remove_signature) {
            if (pdfSignatureAdapter.myViewHolder.container_view.getBackground().getConstantState() ==
                    context.getResources().getDrawable(R.drawable.iv_signature_focus).getConstantState()) {
                pdfSignatureAdapter.myViewHolder.iv_signature.setVisibility(View.GONE);
                img_remove_signature.setVisibility(View.GONE);
                rv_colors.setVisibility(View.GONE);
                pdfSignatureAdapter.myViewHolder.container_view.setImageBitmap(null);
            } else if (pdfSignatureAdapter.myViewHolder.container_view_2.getBackground().getConstantState() ==
                    context.getResources().getDrawable(R.drawable.iv_signature_focus).getConstantState()) {
                pdfSignatureAdapter.myViewHolder.iv_signature_2.setVisibility(View.GONE);
                img_remove_signature.setVisibility(View.GONE);
                rv_colors.setVisibility(View.GONE);
                pdfSignatureAdapter.myViewHolder.container_view_2.setImageBitmap(null);
            } else {
                pdfSignatureAdapter.myViewHolder.iv_signature_3.setVisibility(View.GONE);
                img_remove_signature.setVisibility(View.GONE);
                rv_colors.setVisibility(View.GONE);
                pdfSignatureAdapter.myViewHolder.container_view_3.setImageBitmap(null);
            }
            if (pdfSignatureAdapter.myViewHolder.iv_signature.getVisibility() == View.GONE
                    && pdfSignatureAdapter.myViewHolder.iv_signature_2.getVisibility() == View.GONE
                    && pdfSignatureAdapter.myViewHolder.iv_signature_3.getVisibility() == View.GONE) {
                img_done.setVisibility(View.GONE);
            }
        } else if (id == R.id.img_done) {
            progress_lay.setVisibility(View.VISIBLE);
            if (pdfSignatureAdapter.myViewHolder.push_view.getVisibility() == View.VISIBLE) {
                pdfSignatureAdapter.myViewHolder.container_view.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                pdfSignatureAdapter.myViewHolder.push_view.setVisibility(View.GONE);
            }
            if (pdfSignatureAdapter.myViewHolder.push_view_2.getVisibility() == View.VISIBLE) {
                pdfSignatureAdapter.myViewHolder.container_view_2.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                pdfSignatureAdapter.myViewHolder.push_view_2.setVisibility(View.GONE);
            }
            if (pdfSignatureAdapter.myViewHolder.push_view_3.getVisibility() == View.VISIBLE) {
                pdfSignatureAdapter.myViewHolder.container_view_3.setBackground(getResources().getDrawable(R.drawable.iv_signature_unfocus));
                pdfSignatureAdapter.myViewHolder.push_view_3.setVisibility(View.GONE);
            }
            if (rv_colors.getVisibility() == View.VISIBLE) {
                rv_colors.setVisibility(View.GONE);
            }

            for (int i = 0; i < pdfSignatureAdapter.getItemCount(); i++) {
                PdfSignatureAdapter.MyViewHolder holder = ((PdfSignatureAdapter)
                        Objects.requireNonNull(recyclerView.getAdapter())).getViewByPosition(i);

                File imageFile = new File(fetchedImagesList.get(i));
                Uri uri = Uri.fromFile(imageFile);
                Bitmap selectedImage = null;
                if (uri != null) {
                    InputStream imageStream;
                    try {
                        imageStream = getContentResolver().openInputStream(uri);
                        selectedImage = BitmapFactory.decodeStream(imageStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                String parentName = "";
                if (imageFile.getParentFile() != null && imageFile.getParentFile().exists()) {
                    parentName = imageFile.getParentFile().getName();
                }
                if (holder == null) {
                    saveCombinedBitmap(i, null, selectedImage, parentName, imageFile.getName());
                } else {
                    if (holder.itemView.getWidth() <= 0 || holder.itemView.getHeight() <= 0) {
                        saveCombinedBitmap(i, null, selectedImage, parentName, imageFile.getName());
                    } else {
                        saveCombinedBitmap(i, holder.itemView, null, parentName, imageFile.getName());
                    }
                }

                if (imageFile.isFile() && imageFile.exists()) {
                    imageFile.delete();
                }

            }
            progress_lay.setVisibility(View.GONE);
            Intent intent = new Intent();
            intent.putExtra(Constants.PutExtraConstants.FOLDER_PATH, imagesDirPath);
            setResult(RESULT_OK, intent);
            finish();

        } else if (id == R.id.iv_back_toolbar) {
            onBackPressed();
        }
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    private void saveCombinedBitmap(int pos, View relative_main, Bitmap bitmap, String folderName, String originalFileName) {
        String fileName = "";
        Bitmap resizedBitmap;
        if (relative_main == null) {
            resizedBitmap = bitmap;
        } else {
            resizedBitmap = getBitmapFromView(relative_main);
        }
        if (TextUtils.isEmpty(fileName)) {
            fileName = getFileDateFormatName() + "_121" + pos + ".jpg";
        }
        String relativePath = "";
        try {
            relativePath = saveBitmapToFolder(context, resizedBitmap, fileName, folderName, originalFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(relativePath)) {
            /*String path = Environment.getExternalStorageDirectory() + "/" + relativePath;*/
          /*  File file = new File(relativePath);
            FileModel fileModel = new FileModel();
            fileModel.setName(fileName);
            fileModel.setPath(relativePath + "/" + fileModel.getName());
            fileModel.setFolder(relativePath);
            fileModel.setPdfFileName(fileModel.getName());*/
            filePathList.add(relativePath);
            /*selectedFileModelList.add(fileModel);*/
        }
    }

    private String getFileDateFormatName() {
        String fileName;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd_HHmmss", Locale.getDefault());
        fileName = simpleDateFormat.format(calendar.getTime());
        return fileName;
    }

    private String saveBitmapToFolder(Context context, Bitmap bitmap, String fileName, String folderName, String originalFileName) throws IOException {
        File savedFile;
        String relativePath = context.getCacheDir().getAbsolutePath() + File.separator + Constants.ITL_PDF_DOCS_DIRECTORY + File.separator + folderName;
        String originalPath = context.getCacheDir().getAbsolutePath() + File.separator + Constants.ITL_PDF_ORIGINAL_DIRECTORY + File.separator + folderName + File.separator + originalFileName;
        String newOriginalPath = context.getCacheDir().getAbsolutePath() + File.separator + Constants.ITL_PDF_ORIGINAL_DIRECTORY + File.separator + folderName + File.separator + fileName;
        OutputStream outputStream = null;

        String imagesDir = relativePath;
        File directory = new File(imagesDir);
        File originalDirectory = new File(originalPath);
        File newOriginalDirectory = new File(newOriginalPath);
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
            originalDirectory.renameTo(newOriginalDirectory);
        }
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
        return relativePath + File.separator + fileName;
    }

    private void createPdf(List<String> imagesUriList, String pdfFileName, boolean isWaterMarkToBeShown) {
        ImageToPdfOptions imageToPdfOptions = new ImageToPdfOptions();
        imageToPdfOptions.setPageSize(Constants.PdfConstants.DEFAULT_PDF_PAGE_SIZE);
        imageToPdfOptions.setPageColor(Constants.PdfConstants.DEFAULT_PDF_PAGE_COLOR);
        imageToPdfOptions.setMargins(0, 0, 0, 0);
        imageToPdfOptions.setPdfQuality(Constants.PdfConstants.DEFAULT_PDF_QUALITY);
        imageToPdfOptions.setBorderWidth(Constants.PdfConstants.DEFAULT_BORDER_WIDTH);
        imageToPdfOptions.setWaterMarkAdded(isWaterMarkToBeShown);
        imageToPdfOptions.setWaterMark(flashScanUtil.getWaterMark());
        new CreatePdfTask(context, pdfFileName, imageToPdfOptions, imagesUriList, this, false).execute();
    }

    private void clearTempFiles() {
        String imagesDir = Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/" + ROOT_FOLDER_NAME + "/" + "Temp").toString();
        File directory = new File(imagesDir);
        if (directory.exists() && directory.isDirectory()) {
            String[] children = directory.list();
            if (children != null) {
                for (String child : children) {
                    new File(directory, child).delete();
                }
            }
            directory.delete();
        }
    }

    private boolean sharePdfDirectWithoutOpen = false;

    @Override
    public void onPdfCreationStarted() {
        progress_lay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPdfCreated(String savedPdfPath) {
        progress_lay.setVisibility(View.GONE);
        setResult(RESULT_OK);
        finish();
    }

    private void shareMultiple(ArrayList<Uri> uriList) {
        if (uriList == null || uriList.isEmpty()) return;
        flashScanUtil.shareMultiple(uriList, this);
    }

    private void showPdfPathDialog(String savedPdfPath) {
        Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.saved_pdf_dialog);
        TextView tv_pdf_path = dialog.findViewById(R.id.tv_pdf_path);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_open = dialog.findViewById(R.id.btn_open);

        tv_pdf_path.setText(savedPdfPath);
        btn_cancel.setOnClickListener(v -> dialog.dismiss());
        btn_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFile(savedPdfPath);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void openFile(String savedPdfPath) {
        File file = new File(savedPdfPath);
        if (file.isFile()) {
            flashScanUtil.openFile(context, file);
        }
    }

    private void updatePageCountText(int page) {
        tv_pdf_page_count.setText((page + 1) + "/" + pageCount);
    }
}
