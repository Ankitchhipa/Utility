package com.cam.scanner.scantopdf.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.models.ImageCropping;
import java.util.ArrayList;
import java.util.List;

public class ImagesCropAdapter extends PagerAdapter {

    private Context context;
    private List<ImageCropping> cropArr;

    public ImagesCropAdapter(Context context, ArrayList<ImageCropping> cropArr) {
        this.context = context;
        this.cropArr = cropArr;
    }

    @Override
    public int getCount() {
        return cropArr.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    private ViewGroup vGroup;
    public void setViewGroup(ViewGroup viewGroup) {
        this.vGroup = viewGroup;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = (ViewGroup) inflater.inflate(R.layout.crop_items, container, false);

        ImageView ivCrop = layout.findViewById(R.id.crop_iv);

        if(cropArr.get(position).processBmp == null) {
            Glide.with(context).load(cropArr.get(position).processedPath)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).into(ivCrop);

            /*Glide.with(context).asBitmap().load(cropArr.get(position).processedPath).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    ivCrop.setImageToCrop(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });*/
        }
        else{
            //Glide.with(context).asBitmap().load(cropArr.get(position).processBmp).into(ivCrop);
            ivCrop.setImageBitmap(cropArr.get(position).processBmp);

//            ivCrop.setImageToCrop(cropArr.get(position).processBmp);
        }


        //ivCrop.setParentViewGroup(vGroup);

            /*ivCrop.setImage(ImageSource.uri(cropArr.get(position).processedPath));
        else
            ivCrop.setImage(ImageSource.bitmap(cropArr.get(position).processBmp));

        ivCrop.setMaxScale(3);*/

        TextView countTxt = layout.findViewById(R.id.page_count);

        countTxt.setText(""+(position+1) +"/"+cropArr.size());

        container.addView(layout);
        return layout;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }
}
