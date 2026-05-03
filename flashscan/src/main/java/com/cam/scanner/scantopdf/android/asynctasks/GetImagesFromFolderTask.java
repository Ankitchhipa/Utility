package com.cam.scanner.scantopdf.android.asynctasks;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.cam.scanner.scantopdf.android.interfaces.FetchImagesFromFolderListener;
import com.cam.scanner.scantopdf.android.models.ImageModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class GetImagesFromFolderTask extends AsyncTask<Void, Void, List<ImageModel>> {
    private WeakReference<Context> contextRef;
    private FetchImagesFromFolderListener fetchImagesFromFolderListener;
    private String folderPath;

    public GetImagesFromFolderTask(Context context, String folderPath, FetchImagesFromFolderListener fetchImagesFromFolderListener) {
        contextRef = new WeakReference<>(context);
        this.fetchImagesFromFolderListener = fetchImagesFromFolderListener;
        this.folderPath = folderPath;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (fetchImagesFromFolderListener != null) {
            fetchImagesFromFolderListener.onFetchingStart();
        }
    }

    @Override
    protected List<ImageModel> doInBackground(Void... voids) {
        return fetchAllImagesByFolder(folderPath);
    }

    private List<ImageModel> fetchAllImagesByFolder(String folderPath) {
        List<ImageModel> imagesList = new ArrayList<>();
        Uri externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DISPLAY_NAME};
        String orderBy = MediaStore.Images.ImageColumns.DATE_TAKEN;
        Cursor cursor = contextRef.get().getContentResolver().query(externalContentUri, projection, MediaStore.Images.ImageColumns.DATA + " like ? ",
                new String[]{"%" + folderPath + "%"}, orderBy + " DESC");
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    ImageModel imageModel = new ImageModel();
                    imageModel.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME)));
                    imageModel.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA)));
                    imagesList.add(imageModel);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return imagesList;
    }

    @Override
    protected void onPostExecute(List<ImageModel> imageModels) {
        super.onPostExecute(imageModels);
        if (fetchImagesFromFolderListener != null) {
            fetchImagesFromFolderListener.onFetchingComplete(imageModels);
        }

    }
}
