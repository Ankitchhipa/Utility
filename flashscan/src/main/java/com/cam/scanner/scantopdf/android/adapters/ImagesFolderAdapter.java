package com.cam.scanner.scantopdf.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.interfaces.OnItemClickCallback;
import com.cam.scanner.scantopdf.android.models.ImageFolder;

import java.util.List;

public class ImagesFolderAdapter extends RecyclerView.Adapter<ImagesFolderAdapter.MyViewHolder> {

    private Context context;
    private List<ImageFolder> imagesFolderList;
    private OnItemClickCallback onItemClickCallback;
    private long mLastClickTime = 0;

    public ImagesFolderAdapter(Context context, List<ImageFolder> imagesFolderList, OnItemClickCallback onItemClickCallback) {
        this.context = context;
        this.imagesFolderList = imagesFolderList;
        this.onItemClickCallback = onItemClickCallback;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_gallery_images_folders, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ImageFolder imageFolder = imagesFolderList.get(position);
        if (imageFolder != null) {
            if (context instanceof Activity) {
                if (!((Activity) context).isFinishing() || !((Activity) context).isDestroyed()) {
                    Glide.with(context).load(imageFolder.getFirstPic()).centerCrop().into(holder.iv_folder_pic);
                }
            }

            String folderName = imageFolder.getFolderName();
            holder.tv_folder_name.setText(folderName);
            holder.tv_photos_count.setText(String.valueOf(imageFolder.getNumberOfPics()));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (onItemClickCallback != null) {
                        onItemClickCallback.onItemClick(imageFolder);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return imagesFolderList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_folder_pic;
        private TextView tv_folder_name, tv_photos_count;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_folder_pic = itemView.findViewById(R.id.iv_folder_pic);
            tv_folder_name = itemView.findViewById(R.id.tv_folder_name);
            tv_photos_count = itemView.findViewById(R.id.tv_photos_count);
        }
    }
}
