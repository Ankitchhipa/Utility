package com.cam.scanner.scantopdf.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.models.SpModel;

import java.util.ArrayList;

public class SharedPrefsAdapter extends RecyclerView.Adapter<SharedPrefsAdapter.ItemHolder> {

    private String TAG = SharedPrefsAdapter.class.getSimpleName();
    private LayoutInflater layoutInflater;
    private Context context;
    private ArrayList<SpModel> alSpModel;

    public SharedPrefsAdapter(Context context, ArrayList<SpModel> alSpModel) {

        this.context = context;
        this.alSpModel = alSpModel;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public SharedPrefsAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = layoutInflater.inflate(R.layout.sp_child, parent, false);
        final ItemHolder mViewHolder = new ItemHolder(itemView);
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(SharedPrefsAdapter.ItemHolder holder, final int position) {

        SpModel spModel = alSpModel.get(position);

        holder.tvKey.setText(spModel.getKey());
        holder.tvDetail.setText(spModel.getValue());
    }

    @Override
    public int getItemCount() {
        return alSpModel.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {


        public TextView tvKey;
        public TextView tvDetail;

        public ItemHolder(View itemView) {
            super(itemView);
            tvKey = itemView.findViewById(R.id.tv_key);
            tvDetail = itemView.findViewById(R.id.tvDetail);
        }
    }

}
