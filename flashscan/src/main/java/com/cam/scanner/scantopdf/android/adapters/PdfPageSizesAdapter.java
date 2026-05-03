package com.cam.scanner.scantopdf.android.adapters;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.models.PageSize;

import java.util.List;

public class PdfPageSizesAdapter extends RecyclerView.Adapter<PdfPageSizesAdapter.MyViewHolder> {
    private Context context;
    private List<PageSize> pageSizeList;
    private long mLastClickTime = 0;

    public String getSelectedPageSize() {
        return selectedPageSize;
    }

    private String selectedPageSize;

    public PdfPageSizesAdapter(Context context, List<PageSize> pageSizeList) {
        this.context = context;
        this.pageSizeList = pageSizeList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_pdf_page_sizes, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        PageSize pageSize = pageSizeList.get(position);
        if (pageSize != null) {
            holder.radioBtn.setText(pageSize.getSizeKey());

            holder.radioBtn.setChecked(pageSize.isChecked());

            if (pageSize.isChecked()) {
                selectedPageSize = pageSize.getSizeValue();
            }

            holder.btn_page_size.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleClickedPageSize(pageSize);
                }
            });
        }
    }

    private void handleClickedPageSize(PageSize selectedPageSize) {
        for (PageSize pageSize : pageSizeList) {
            pageSize.setChecked(selectedPageSize.getSizeValue().equalsIgnoreCase(pageSize.getSizeValue()));
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return pageSizeList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private RadioButton radioBtn;
        private Button btn_page_size;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            radioBtn = itemView.findViewById(R.id.radioBtn);
            btn_page_size = itemView.findViewById(R.id.btn_page_size);
        }
    }
}
