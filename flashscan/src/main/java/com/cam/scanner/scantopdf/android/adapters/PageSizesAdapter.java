package com.cam.scanner.scantopdf.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.models.PageSize;

import java.util.List;

public class PageSizesAdapter extends ArrayAdapter<PageSize> {


    public PageSizesAdapter(Context context, List<PageSize> pageSizeList) {
        super(context, 0, pageSizeList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }


    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_view_pagesize_spinner, parent, false);
        }
        TextView tv_page_key = convertView.findViewById(R.id.tv_page_key);

        PageSize pageSize = getItem(position);
        if (pageSize != null) {
            tv_page_key.setText(pageSize.getSizeKey());
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }
}
