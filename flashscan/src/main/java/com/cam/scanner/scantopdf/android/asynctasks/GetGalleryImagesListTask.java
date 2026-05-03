package com.cam.scanner.scantopdf.android.asynctasks;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.cam.scanner.scantopdf.android.interfaces.FetchGalleryImagesListener;
import com.cam.scanner.scantopdf.android.models.ImageFolder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class GetGalleryImagesListTask extends AsyncTask<Void, Void, List<ImageFolder>> {

    private WeakReference<Context> contextRef;
    private FetchGalleryImagesListener fetchGalleryImagesListener;

    public GetGalleryImagesListTask(Context context, FetchGalleryImagesListener fetchGalleryImagesListener) {
        contextRef = new WeakReference<>(context);
        this.fetchGalleryImagesListener = fetchGalleryImagesListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (fetchGalleryImagesListener != null) {
            fetchGalleryImagesListener.onFetchingStart();
        }
    }

    @Override
    protected List<ImageFolder> doInBackground(Void... voids) {
        List<ImageFolder> picFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DISPLAY_NAME,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME};
        String orderBy = MediaStore.Images.ImageColumns.DATE_TAKEN;
        Cursor cursor = contextRef.get().getContentResolver().query(allImagesUri, projection, null, null, orderBy + " DESC");
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    ImageFolder imageFolder = new ImageFolder();
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                    String folderName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
                    String dataPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));

                    if (!TextUtils.isEmpty(dataPath) && !TextUtils.isEmpty(folderName)) {
                        String folderpaths = dataPath.substring(0, dataPath.lastIndexOf(folderName + "/"));
                        folderpaths = folderpaths + folderName + "/";
                        if (!picPaths.contains(folderpaths)) {
                            picPaths.add(folderpaths);
                            imageFolder.setPath(folderpaths);
                            imageFolder.setFolderName(folderName);
                            imageFolder.setFirstPic(dataPath);
                            imageFolder.addpics();
                            picFolders.add(imageFolder);
                        } else {
                            for (int i = 0; i < picFolders.size(); i++) {
                                if (picFolders.get(i).getPath().equals(folderpaths)) {
                                    picFolders.get(i).setFirstPic(dataPath);
                                    picFolders.get(i).addpics();
                                }
                            }
                        }
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return picFolders;
    }

    @Override
    protected void onPostExecute(List<ImageFolder> picsFolders) {
        super.onPostExecute(picsFolders);
        if (fetchGalleryImagesListener != null) {
            fetchGalleryImagesListener.onFetchingComplete(picsFolders);
        }
    }
}
