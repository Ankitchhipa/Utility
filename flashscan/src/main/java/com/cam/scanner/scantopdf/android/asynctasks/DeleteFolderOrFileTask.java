package com.cam.scanner.scantopdf.android.asynctasks;

import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.cam.scanner.scantopdf.android.AppController;
import com.cam.scanner.scantopdf.android.interfaces.FileOrFolderDeleteListener;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static androidx.core.app.ActivityCompat.startIntentSenderForResult;

public class DeleteFolderOrFileTask extends AsyncTask<Void, Void, Void> {

    private static final int REQUEST_PERM_DELETE = 44;
    private String path;
    private FileOrFolderDeleteListener fileOrFolderDeleteListener;
    private List<String> filePathList;
    private Context context;
    private WeakReference<Context> contextRef;
    ContentResolver contentResolver;
    FlashScanUtil flashScanUtil;

    public DeleteFolderOrFileTask(Context context) {
        this.context = context;
        contentResolver = context.getContentResolver();
        flashScanUtil = new FlashScanUtil(context);
    }

    public DeleteFolderOrFileTask(Context context,String path, FileOrFolderDeleteListener fileOrFolderDeleteListener) {
        this.context = context;
        this.path = path;
        this.fileOrFolderDeleteListener = fileOrFolderDeleteListener;
        flashScanUtil = new FlashScanUtil(context);
    }

    public DeleteFolderOrFileTask(Context context, List<String> filePathList, FileOrFolderDeleteListener fileOrFolderDeleteListener) {
        this.context = context;
        this.filePathList = filePathList;
        this.fileOrFolderDeleteListener = fileOrFolderDeleteListener;
        flashScanUtil = new FlashScanUtil(context);
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (filePathList != null && !filePathList.isEmpty()) {
            for (String path : filePathList) {
                File dir = new File(path);
                deleteRecursive(dir);
            }
        } else {
            File dir = new File(path);
            deleteRecursive(dir);
        }


        return null;
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (!fileOrDirectory.exists())
            return;
        String path = fileOrDirectory.getPath();
        if (fileOrDirectory.isDirectory()) {
            File[] files = fileOrDirectory.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    deleteRecursive(file);
                }
            }
        }
        if (fileOrDirectory.isFile()) {
            boolean isDeleted;
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    Uri uri = Uri.fromFile(fileOrDirectory);
                    ContentResolver contentResolver = context.getContentResolver();
                    contentResolver.delete(uri, null, null);

                    ArrayList<Uri> collection = new ArrayList<>();
                    collection.add(uri);
                    PendingIntent pendingIntent = MediaStore.createDeleteRequest(context.getContentResolver(), collection);
                    //requestDeletePermission(collection);

                    if (pendingIntent != null) {
                        IntentSender sender = pendingIntent.getIntentSender();
                        IntentSenderRequest request = new IntentSenderRequest.Builder(sender).build();
                        launcher.launch(request);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            else{*/
                Log.e("DeleteFolderOrFileTask", "filesUri.."+fileOrDirectory.getAbsolutePath());
                isDeleted = fileOrDirectory.delete();
                Log.e("DeleteFolderOrFileTask", "isDeleted "+isDeleted);
            //}
           /* if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                //isDeleted = delete(fileOrDirectory);
                final Uri filesUri = MediaStore.Files.getContentUri("external");
                Log.e("DeleteFolderOrFileTask", "filesUri.."+filesUri);
                isDeleted = deleteFileUsingDisplayName(fileOrDirectory.getPath(),filesUri);
            } else {*/
            File parent = fileOrDirectory.getParentFile();
            try {
                if (parent != null && parent.exists()) {
                    File originalPath = new File(flashScanUtil.getDocOriginalPath(context) + File.separator + parent.getName(), fileOrDirectory.getName());
                    originalPath.delete();
                    File parentPath = new File(flashScanUtil.getDocOriginalPath(context) + File.separator + parent.getName());
                    boolean deleteDirectory = true;
                    if (parentPath != null && parentPath.exists() && parentPath.isDirectory()) {
                        File[] files_list = parentPath.listFiles();
                        if (files_list != null && files_list.length > 0) {
                            deleteDirectory = false;
                        } else {
                            parentPath.delete();
                        }
                    }
                    File docsParent = new File(parent.getAbsolutePath()).getParentFile();
                    if (docsParent != null && docsParent.exists() && docsParent.getName().equalsIgnoreCase(Constants.docs) && deleteDirectory) {
                        File itlPdfPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.ITL_PDF_DIRECTORY, parent.getName() + ".pdf");
                        itlPdfPath.delete();
                    }
                }
            } catch (Exception e) {
            }
            //}
            if (isDeleted) {
                AppController.getINSTANCE().dbHandler.deleteFile(fileOrDirectory.getParentFile().getName(), fileOrDirectory.getName());
                Log.e("DeleteFolderOrFileTask", "isDeleted if "+path);
                if (!TextUtils.isEmpty(path)) {
                    deleteFileFromMediaAlso(path);
                }
            }
        } else {
            Log.e("DeleteFolderOrFileTask", "is not file");
            if (fileOrDirectory.delete()) {
                AppController.getINSTANCE().dbHandler.deleteFile(fileOrDirectory.getParentFile().getName(), fileOrDirectory.getName());
            }
        }

    }

    private void deleteFileFromMediaAlso(String path) {
        try {
            Uri uriFromFilePath = getUriFromFilePath(path);
            if (uriFromFilePath != null) {
                AppController.getINSTANCE().context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uriFromFilePath));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Uri getUriFromFilePath(String path) {
        Uri uri;
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(AppController.getINSTANCE(), BuildConfig.APPLICATION_ID + ".fileprovider", new File(path));
        } else {
            uri = Uri.fromFile(new File(path));
        }*/
        File file = new File(path);
        uri = Uri.fromFile(new File(path));
        return uri;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (fileOrFolderDeleteListener != null)
            fileOrFolderDeleteListener.onFileOrFolderDeleted();
    }

    private boolean delete(final File file) {
        final String where = MediaStore.MediaColumns.DATA + "=?";
        final String[] selectionArgs = new String[]{
                file.getAbsolutePath()
        };
        //if(context!=null) {
        final Uri filesUri = MediaStore.Files.getContentUri("external");

        if (AppController.getINSTANCE().contentResolver != null) {
            //AppController.getINSTANCE().contentResolver.delete(filesUri, where, selectionArgs);
            if (file.exists()) {

                AppController.getINSTANCE().contentResolver.delete(filesUri, where, selectionArgs);
                return true;
            } else {
                return false;
            }
            //}
            //return !file.exists();
        } else {
            Log.e("Error", "" + null);
        }
        return false;
    }
}
