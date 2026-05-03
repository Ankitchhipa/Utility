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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.activities.ImagePreviewActivity;
import com.cam.scanner.scantopdf.android.interfaces.OnItemSelectListener;
import com.cam.scanner.scantopdf.android.models.FileModel;
import com.cam.scanner.scantopdf.android.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class PdfToImagesImportAdapter extends RecyclerView.Adapter<PdfToImagesImportAdapter.ViewHolder> {

    private Context context;
    private List<FileModel> list;
    private OnItemSelectListener onItemSelectListener;
    private long mLastClickTime = 0;

    public List<FileModel> getSelectedImagesList() {
        if (selectedImagesList == null) {
            selectedImagesList = new ArrayList<>();
        }
        return selectedImagesList;
    }

    public void setSelectedImagesList(List<FileModel> selectedImagesList) {
        if (!getSelectedImagesList().isEmpty()) {
            getSelectedImagesList().clear();
        }
        getSelectedImagesList().addAll(selectedImagesList);
    }

    public void deSelectAllImages() {
        if (list != null && !list.isEmpty()) {
            for (FileModel fileModel : list) {
                fileModel.setChecked(false);
            }
        }
        if (!getSelectedImagesList().isEmpty()) {
            getSelectedImagesList().clear();
        }
        notifyDataSetChanged();
    }

    public void selectAllImages() {
        if (list != null && !list.isEmpty()) {
            for (FileModel fileModel : list) {
                fileModel.setChecked(true);
            }

            if (!getSelectedImagesList().isEmpty()) {
                getSelectedImagesList().clear();
            }
            getSelectedImagesList().addAll(list);
            notifyDataSetChanged();
        }
    }

    private List<FileModel> selectedImagesList = new ArrayList<>();

    public PdfToImagesImportAdapter(Context context, List<FileModel> list, OnItemSelectListener onItemSelectListener) {
        this.context = context;
        this.list = list;
        this.onItemSelectListener = onItemSelectListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_display_images, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileModel fileModel = list.get(position);
        if (fileModel != null) {
            if (context instanceof Activity) {
                if (!((Activity) context).isFinishing() || !((Activity) context).isDestroyed()) {
                    Glide.with(context).load(fileModel.getPath()).centerCrop().into(holder.iv_image);
                }
            }

            holder.checkBox.setChecked(fileModel.isChecked());
            holder.transparent_lay.setVisibility(fileModel.isChecked() ? View.VISIBLE : View.GONE);

            holder.btn_checkBox.setOnClickListener(v -> {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                fileModel.setChecked(!fileModel.isChecked());
                if (fileModel.isChecked()) {
                    getSelectedImagesList().add(fileModel);
                } else {
                    if (!getSelectedImagesList().isEmpty()) {
                        getSelectedImagesList().remove(fileModel);
                    }
                }
                notifyItemChanged(position);
                if (onItemSelectListener != null) {
                    onItemSelectListener.onItemSelect(fileModel);
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    imagePreview(fileModel.getPath());
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
        return list.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView iv_image;
        private Button btn_checkBox;
        private CheckBox checkBox;
        private View transparent_lay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_image = itemView.findViewById(R.id.iv_image);
            btn_checkBox = itemView.findViewById(R.id.btn_checkBox);
            checkBox = itemView.findViewById(R.id.checkBox);
            transparent_lay = itemView.findViewById(R.id.transparent_lay);
        }
    }
}
