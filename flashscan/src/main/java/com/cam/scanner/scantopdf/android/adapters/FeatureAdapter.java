package com.cam.scanner.scantopdf.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;


import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.databinding.ItemFeaturePlanBinding;
import com.cam.scanner.scantopdf.android.models.FeatureModel;

import java.util.List;

public class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.MyViewHolder> {
    private List<FeatureModel> featureModelList;
    private Context mContext;

    public FeatureAdapter(Context mContext, List<FeatureModel> featureModelList) {
        this.featureModelList = featureModelList;
        this.mContext = mContext;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ItemFeaturePlanBinding binding;
        public MyViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
        }
        public void setData(int pos){
            FeatureModel featureModel = featureModelList.get(pos);
            String features= featureModel.getFeature();
            String basic=featureModel.getBasic_val();
            String premium=featureModel.getPremium_val();

            binding.feature.setText(features);
            binding.basic.setText(basic);
            binding.premium.setText(premium);

            binding.feature.setVisibility(features.isEmpty()? View.GONE: View.VISIBLE);
            binding.basic.setVisibility(basic.isEmpty()? View.GONE: View.VISIBLE);
            binding.premium.setVisibility(premium.isEmpty()? View.GONE: View.VISIBLE);

            int basicIcon =featureModel.getBasic_icon();
            int premiumIcon =featureModel.getPremium_icon();

            binding.basicIcon.setImageResource(basicIcon);
            binding.premiumIcon.setImageResource(premiumIcon);

            binding.basicIcon.setVisibility(basicIcon!=0 ? View.VISIBLE : View.GONE);
            binding.premiumIcon.setVisibility(premiumIcon!=0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public FeatureAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feature_plan, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.setData(position);

    }

    @Override
    public int getItemCount() {
        return featureModelList!=null ? featureModelList.size() : 0;
    }
}