package com.cam.scanner.scantopdf.android.activities;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class ImageCaptureHelper {

    private static final String TAG = "ImageCaptureHelper";

    private final Context context;
    private final LifecycleOwner lifecycleOwner;
    private final ImageCapture imageCapture;
    private final PreviewView previewView;
    //    private final List<Uri> capturedImageUris;
    private final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private final ArrayList<String> capturedImagesPaths;
    private final OnImageCaptureListener listener;
    private final Executor executor;

    public interface OnImageCaptureListener {
        void onImageCaptured(@NonNull Uri uri);

        void onImageCaptureError(@NonNull ImageCaptureException exception);

        void onImageCaptureComplete();
    }

    public ImageCaptureHelper(@NonNull Context context, LifecycleOwner lifecycleOwner, PreviewView previewView, @NonNull OnImageCaptureListener listener, @NonNull Executor executor) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.previewView = previewView;
        this.listener = listener;
        this.executor = executor;
        this.capturedImagesPaths = new ArrayList<>();
        this.imageCapture = new ImageCapture.Builder().build();
    }

    public void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera: " + e.getMessage(), e);
            }
        }, executor);
    }

    public void captureImage() {
        File photoFile = new File(context.getExternalCacheDir(), new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, executor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Uri savedUri = Uri.fromFile(photoFile);
                /*try (FileOutputStream fos = new FileOutputStream(photoFile)) {
                    fos.write(pictureBuffer);
                    fos.close();
                } catch (IOException e) {
                    Log.d(TAG, "Cannot process camera picture", e);
                }*/
                capturedImagesPaths.add(photoFile.getAbsolutePath());
                listener.onImageCaptured(savedUri);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                listener.onImageCaptureError(exception);
            }
        });
    }

    public ArrayList<String> getCapturedImagesPaths() {
        return capturedImagesPaths;
    }

    public void clearCapturedImages() {
        capturedImagesPaths.clear();
    }
}