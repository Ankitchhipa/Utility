package com.cam.scanner.scantopdf.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.databinding.ItemUpgradePlanFeatureBinding;
import com.cam.scanner.scantopdf.android.models.DescriptionModel;

import java.util.List;

public class UpgradePlanAdapter extends RecyclerView.Adapter<UpgradePlanAdapter.MyViewHolder> {
    private List<DescriptionModel> descriptionModelList;
    private Context mContext;

    public UpgradePlanAdapter(Context mContext, List<DescriptionModel> descriptionModelList) {
        this.descriptionModelList = descriptionModelList;
        this.mContext = mContext;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ItemUpgradePlanFeatureBinding binding;
        public MyViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
        }
        public void setData(int pos){
            DescriptionModel descriptionModel = descriptionModelList.get(pos);
            String title= descriptionModel.getTitle();
            int image=descriptionModel.getImage();

            binding.titleTv.setText(title);
            binding.iconImg.setImageResource(image);
        }
    }

    @Override
    public UpgradePlanAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_upgrade_plan_feature, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.setData(position);

    }

    @Override
    public int getItemCount() {
        return descriptionModelList!=null ? descriptionModelList.size() : 0;
    }
}
