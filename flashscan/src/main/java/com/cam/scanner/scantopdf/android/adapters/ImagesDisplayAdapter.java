package com.cam.scanner.scantopdf.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.activities.ImagePreviewActivity;
import com.cam.scanner.scantopdf.android.interfaces.OnItemSelectListener;
import com.cam.scanner.scantopdf.android.models.ImageModel;
import com.cam.scanner.scantopdf.android.util.Constants;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImagesDisplayAdapter extends RecyclerView.Adapter<ImagesDisplayAdapter.ViewHolder> {

    private Context context;
    private List<ImageModel> imagesList;
    private OnItemSelectListener onItemSelectListener;
    private FlashScanUtil flashScanUtil;
    private long mLastClickTime = 0;

    public List<ImageModel> getSelectedPicsList() {
        if (selectedPicsList == null) {
            selectedPicsList = new ArrayList<>();
        }
        return selectedPicsList;
    }

    private List<ImageModel> selectedPicsList = new ArrayList<>();

    public ImagesDisplayAdapter(Context context, List<ImageModel> imagesList, OnItemSelectListener onItemSelectListener) {
        this.context = context;
        this.imagesList = imagesList;
        this.onItemSelectListener = onItemSelectListener;
        flashScanUtil = new FlashScanUtil(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_display_images, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageModel imageModel = imagesList.get(position);
        if (imageModel != null) {
            if (context instanceof Activity) {
                if (!((Activity) context).isFinishing() || !((Activity) context).isDestroyed()) {
                    Glide.with(context).load(imageModel.getPath()).centerCrop().into(holder.iv_image);
                }
            }

            holder.checkBox.setChecked(imageModel.isChecked());
            holder.transparent_lay.setVisibility(imageModel.isChecked() ? View.VISIBLE : View.GONE);

            holder.btn_checkBox.setOnClickListener(v -> {
                imageModel.setChecked(!imageModel.isChecked());
                if (imageModel.isChecked()) {
                    getSelectedPicsList().add(imageModel);
                } else {
                    if (!getSelectedPicsList().isEmpty()) {
                        getSelectedPicsList().remove(imageModel);
                    }
                }
                notifyItemChanged(position);
                if (onItemSelectListener != null) {
                    onItemSelectListener.onItemSelect(imageModel);
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    imagePreview(imageModel.getPath());
                }
            });
        }
    }

    private void imagePreview(String path) {
        Intent intent = new Intent(context, ImagePreviewActivity.class);
        intent.putExtra(Constants.PutExtraConstants.FILE_PATH, path);
        context.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_image;
        private Button btn_checkBox;
        private CheckBox checkBox;
        private View transparent_lay;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_image = itemView.findViewById(R.id.iv_image);
            btn_checkBox = itemView.findViewById(R.id.btn_checkBox);
            checkBox = itemView.findViewById(R.id.checkBox);
            transparent_lay = itemView.findViewById(R.id.transparent_lay);
        }
    }
}
