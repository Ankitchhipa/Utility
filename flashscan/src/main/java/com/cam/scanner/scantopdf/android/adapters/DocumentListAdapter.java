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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.interfaces.OnItemSelectListener;
import com.cam.scanner.scantopdf.android.models.FileModel;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;

import java.util.List;

public class DocumentListAdapter extends RecyclerView.Adapter<DocumentListAdapter.ViewHolder> {
    private Context context;
    private List<FileModel> fileModelList;
    private FlashScanUtil flashScanUtil;
    private OnItemSelectListener onItemSelectListener;
    private long mLastClickTime = 0;

    public DocumentListAdapter(Context context, List<FileModel> fileModelList, OnItemSelectListener onItemSelectListener) {
        this.context = context;
        this.fileModelList = fileModelList;
        flashScanUtil = new FlashScanUtil(context);
        this.onItemSelectListener = onItemSelectListener;
    }

    public void clearFilterList(List<FileModel> fetchedFoldersList) {
        this.fileModelList = fetchedFoldersList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_document_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileModel fileModel = fileModelList.get(position);
        if (fileModel != null) {
            if (context instanceof Activity) {
                if (!((Activity) context).isFinishing() || !((Activity) context).isDestroyed()) {
                    Glide.with(context).asBitmap().load(fileModel.getThumbnailPath()).centerCrop().apply(new RequestOptions()
                            .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_notfound)))
                            .into(holder.iv_file);
                }
            }
            holder.tv_file_name.setText(fileModel.getName());
            holder.tv_date.setText(flashScanUtil.getDateFromTimeStamp(fileModel.getDateTaken()));
            holder.tv_file_count.setText(String.valueOf(fileModel.getFileCountInFolder()));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (onItemSelectListener != null)
                        onItemSelectListener.onItemSelect(fileModel);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return fileModelList.size();
    }

    public void filterList(List<FileModel> filterFileList) {
        this.fileModelList = filterFileList;
        notifyDataSetChanged();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_file;
        private TextView tv_file_name, tv_date, tv_file_count;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_file = itemView.findViewById(R.id.iv_file);
            tv_file_name = itemView.findViewById(R.id.tv_file_name);
            tv_date = itemView.findViewById(R.id.tv_date);
            tv_file_count = itemView.findViewById(R.id.tv_file_count);
        }
    }
}
