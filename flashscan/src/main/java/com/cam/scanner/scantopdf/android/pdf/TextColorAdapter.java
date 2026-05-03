package com.cam.scanner.scantopdf.android.pdf;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cam.scanner.scantopdf.android.R;

import java.util.ArrayList;
import java.util.List;

public class TextColorAdapter extends RecyclerView.Adapter<TextColorAdapter.MyViewHolder> {

    private Context context;
    private List<ColorModel> colorList;
    private String selectedColor;

    public TextColorAdapter(Context context, List<ColorModel> colorsList) {
        this.context = context;
        this.colorList = colorsList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_text_color_adapter, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ColorModel colorModel = colorList.get(position);
        if (colorModel != null) {

            if (colorModel.isChecked()) {
                selectedColor = colorModel.getColorCode();
            }

            if (!TextUtils.isEmpty(colorModel.getColorCode())) {
                holder.fl_color_picker.setBackgroundColor(Color.parseColor(colorModel.getColorCode()));
                holder.iv_checked_color.setVisibility(colorModel.isChecked() ? View.VISIBLE : View.GONE);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleClickItem(colorModel.getColorCode());
                }
            });
        }

    }

    private void handleClickItem(String colorCode) {
        if (colorList != null && !colorList.isEmpty()) {
            for (ColorModel model : colorList) {
                if (model.getColorCode().equalsIgnoreCase(colorCode)) {
                    selectedColor = model.getColorCode();
                    model.setChecked(true);
                } else {
                    model.setChecked(false);
                }
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    public String getSelectedColor() {
        return selectedColor;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private FrameLayout fl_color_picker;
        private ImageView iv_checked_color;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            fl_color_picker = itemView.findViewById(R.id.fl_color_picker);
            iv_checked_color = itemView.findViewById(R.id.iv_checked_color);
        }
    }
}
