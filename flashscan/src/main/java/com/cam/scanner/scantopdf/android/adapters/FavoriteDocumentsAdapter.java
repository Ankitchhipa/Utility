package com.cam.scanner.scantopdf.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.interfaces.FileOperationListener;
import com.cam.scanner.scantopdf.android.interfaces.LastItemVisibleListener;
import com.cam.scanner.scantopdf.android.interfaces.OnItemSelectListener;
import com.cam.scanner.scantopdf.android.models.FileModel;
import com.cam.scanner.scantopdf.android.util.FlashScanUtil;

import java.util.List;

public class FavoriteDocumentsAdapter extends RecyclerView.Adapter<FavoriteDocumentsAdapter.ViewHolder> {

    private Context context;
    private List<FileModel> fileModelList;
    private OnItemSelectListener onItemSelectListener;
    private FlashScanUtil flashScanUtil;
    private LastItemVisibleListener lastItemVisibleListener;
    private long mLastClickTime = 0;

    private FileOperationListener fileOperationListener;

    public FavoriteDocumentsAdapter(Context context, List<FileModel> fileModelList, OnItemSelectListener onItemSelectListener, FileOperationListener fileOperationListener) {
        this.context = context;
        this.fileModelList = fileModelList;
        this.onItemSelectListener = onItemSelectListener;
        flashScanUtil = new FlashScanUtil(context);
        this.fileOperationListener = fileOperationListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_favorites_list, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileModel fileModel = fileModelList.get(position);
        if (fileModel != null) {
            if (!TextUtils.isEmpty(fileModel.getName()) && fileModel.getName().equalsIgnoreCase(context.getString(R.string.view_more)) && TextUtils.isEmpty(fileModel.getThumbnailPath())) {
                holder.tv_view_more.setVisibility(View.VISIBLE);
                holder.cardView.setVisibility(View.GONE);
                holder.iv_doc.setVisibility(View.GONE);
                holder.tv_doc.setVisibility(View.GONE);
                holder.tv_date_doc.setVisibility(View.GONE);
            } else {
                holder.tv_view_more.setVisibility(View.GONE);
                holder.cardView.setVisibility(View.VISIBLE);
                holder.iv_doc.setVisibility(View.VISIBLE);
                if (context instanceof Activity) {
                    if (!((Activity) context).isFinishing() || !((Activity) context).isDestroyed()) {
                        Glide.with(context).asBitmap().load(fileModel.getThumbnailPath()).centerCrop().apply(new RequestOptions()
                                .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_notfound))
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE))
                                .into(holder.iv_doc);
                    }
                }

                holder.tv_doc.setText(fileModel.getName());
                holder.tv_date_doc.setText(flashScanUtil.getOnlyDateFromTimeStamp(fileModel.getDateTaken()));


            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (onItemSelectListener != null) {
                        onItemSelectListener.onItemSelect(fileModel);
                    }
                }
            });

            holder.flStar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    if (fileOperationListener != null){
                        fileModel.setStarred(false);
                        fileOperationListener.removeFavourite(fileModel);
                        notifyItemChanged(position);
                    }
            }});
        }
    }

    @Override
    public int getItemCount() {
        return fileModelList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_doc, flStar;
        private TextView tv_doc, tv_date_doc, tv_view_more;
        private CardView cardView;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_doc = itemView.findViewById(R.id.iv_doc);
            tv_doc = itemView.findViewById(R.id.tv_doc);
            tv_date_doc = itemView.findViewById(R.id.tv_date_doc);
            tv_view_more = itemView.findViewById(R.id.tv_view_more);
            cardView = itemView.findViewById(R.id.cardView);
            flStar = itemView.findViewById(R.id.fl_star);
        }
    }
}
