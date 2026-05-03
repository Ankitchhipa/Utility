package com.cam.scanner.scantopdf.android.signature;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cam.scanner.scantopdf.android.R;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.MyView> {

    private Activity mActivity;
    private List<String> list;
    private View.OnClickListener mOnItemClickListener;

    public class MyView
            extends RecyclerView.ViewHolder {

        CardView cardView;

        public MyView(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_view);
            itemView.setTag(this);
            itemView.setOnClickListener(mOnItemClickListener);
        }
    }

    public ColorAdapter(Activity activity, List<String> horizontalList) {
        this.mActivity = activity;
        this.list = horizontalList;
    }

    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        this.mOnItemClickListener = itemClickListener;
    }

    @NonNull
    public MyView onCreateViewHolder(ViewGroup parent,
                                     int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color_picker,
                parent,
                false);

        return new MyView(itemView);
    }

    @Override
    public void onBindViewHolder(final MyView holder,
                                 final int position) {

        holder.cardView.setBackgroundColor(Color.parseColor(list.get(position)));
        holder.cardView.setAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.zoom_in));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
