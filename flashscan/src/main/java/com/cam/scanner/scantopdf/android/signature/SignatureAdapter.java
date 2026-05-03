package com.cam.scanner.scantopdf.android.signature;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.activities.SignatureActivity;
import com.cam.scanner.scantopdf.android.pdf.PdfSignatureActivity;

import java.util.List;

public class SignatureAdapter extends RecyclerView.Adapter<SignatureAdapter.MyView> {

    private Activity mActivity;
    private List<String> list;
    private View.OnClickListener mOnItemClickListener;
    private View.OnLongClickListener onLongClickListener;

    public class MyView extends RecyclerView.ViewHolder {

        ImageView iv_thumb;

        public MyView(View itemView) {
            super(itemView);
            iv_thumb = itemView.findViewById(R.id.iv_thumb);

            itemView.setTag(this);
            itemView.setOnClickListener(mOnItemClickListener);
            itemView.setOnLongClickListener(onLongClickListener);
        }
    }

    public SignatureAdapter(Activity activity, List<String> horizontalList) {
        this.mActivity = activity;
        this.list = horizontalList;
    }

    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        mOnItemClickListener = itemClickListener;
    }

    public void setOnItemLongClickListener(View.OnLongClickListener itemClickListener) {
        this.onLongClickListener = itemClickListener;
    }

    @NonNull
    public SignatureAdapter.MyView onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_signature_thumb, parent, false);
        return new SignatureAdapter.MyView(itemView);
    }

    @Override
    public void onBindViewHolder(final SignatureAdapter.MyView holder,
                                 final int position) {

        if (mActivity instanceof SignatureActivity) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    holder.iv_thumb.setImageBitmap((((SignatureActivity) mActivity).decodeBase64(list.get(position))));
                }
            });
        }
        if (mActivity instanceof PdfSignatureActivity) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    holder.iv_thumb.setImageBitmap((((PdfSignatureActivity) mActivity).decodeBase64(list.get(position))));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}

