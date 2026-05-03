package com.cam.scanner.scantopdf.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.databinding.ItemFeatureDescriptionBinding;
import com.cam.scanner.scantopdf.android.models.DescriptionModel;

import java.util.List;

public class DescriptionAdapter extends RecyclerView.Adapter<DescriptionAdapter.MyViewHolder> {
    private List<DescriptionModel> descriptionModelList;
    private Context mContext;

    public DescriptionAdapter(Context mContext, List<DescriptionModel> descriptionModelList) {
        this.descriptionModelList = descriptionModelList;
        this.mContext = mContext;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ItemFeatureDescriptionBinding binding;
        public MyViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
        }
        public void setData(int pos){
            DescriptionModel descriptionModel = descriptionModelList.get(pos);
            String title= descriptionModel.getTitle();
            String description=descriptionModel.getDescription();
            int image=descriptionModel.getImage();

            binding.titleTv.setText(title);
            binding.descriptionTv.setText(description);
            binding.iconImg.setImageResource(image);
        }
    }

    @Override
    public DescriptionAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feature_description, parent, false);
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