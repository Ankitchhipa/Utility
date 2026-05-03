package com.cam.scanner.scantopdf.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.interfaces.OnItemSelectListener;
import com.cam.scanner.scantopdf.android.models.FileModel;

import java.util.List;

public class SelectedImagesListAdapter extends RecyclerView.Adapter<SelectedImagesListAdapter.MyViewHolder> {

    private Context context;
    private List<FileModel> selectedImagesList;
    private OnItemSelectListener onItemSelectListener;
    private long mLastClickTime = 0;

    public SelectedImagesListAdapter(Context context, List<FileModel> selectedImagesList, OnItemSelectListener onItemSelectListener) {
        this.context = context;
        this.selectedImagesList = selectedImagesList;
        this.onItemSelectListener = onItemSelectListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_selected_images_list, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        FileModel fileModel = selectedImagesList.get(position);
        if (fileModel != null) {
            if (context instanceof Activity) {
                if (!((Activity) context).isFinishing() || !((Activity) context).isDestroyed()) {
                    Glide.with(context).load(fileModel.getPath()).centerCrop().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.iv_image);
                }
            }


            holder.itemView.setOnClickListener(v -> {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (onItemSelectListener != null) {
                    onItemSelectListener.onItemSelect(fileModel);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return selectedImagesList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_image;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_image = itemView.findViewById(R.id.iv_image);
        }
    }
}
