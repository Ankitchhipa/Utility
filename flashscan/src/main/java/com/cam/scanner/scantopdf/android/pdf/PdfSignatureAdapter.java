package com.cam.scanner.scantopdf.android.pdf;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.signature.SingleFingerView;
import com.cam.scanner.scantopdf.android.signature.SingleFingerView2;
import com.cam.scanner.scantopdf.android.signature.SingleFingerView3;
import com.cam.scanner.scantopdf.android.signature.ViewOnTouchListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfSignatureAdapter extends RecyclerView.Adapter<PdfSignatureAdapter.MyViewHolder> implements
        View.OnClickListener {

    private Context context;
    private ArrayList<String> imagesPathList;
    public MyViewHolder myViewHolder;
    private Bitmap scaledBitmap;
    private AppCompatActivity activity;
    public String imagePath;
    public HashMap<Integer, MyViewHolder> holderList;
    private ImageView signatureView;

    public void updateViewHolder(MyViewHolder viewHolder) {
        this.myViewHolder = viewHolder;
    }

    public PdfSignatureAdapter(Context context, AppCompatActivity appCompatActivity, ArrayList<String> imagesPathList) {
        this.context = context;
        this.activity = appCompatActivity;
        this.imagesPathList = imagesPathList;
        holderList = new HashMap<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_pdf_signature_adapter, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (position == 0) {
            myViewHolder = holder;
        }
        imagePath = imagesPathList.get(position);
        if (!holderList.containsKey(position)) {
            holderList.put(position, holder);
        }
        if (!TextUtils.isEmpty(imagePath)) {
            Glide.with(context).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).load(imagePath).into(holder.imageView);
        }
        holder.container_view.setOnClickListener(this);
        holder.container_view_2.setOnClickListener(this);
        holder.container_view_3.setOnClickListener(this);
        holder.imageView.setOnClickListener(this);

        holder.container_view.setOnTouchListener(new ViewOnTouchListener(context, holder.push_view));
        holder.container_view_2.setOnTouchListener(new ViewOnTouchListener(context, holder.push_view_2));
        holder.container_view_3.setOnTouchListener(new ViewOnTouchListener(context, holder.push_view_3));
    }

    public void setSignature(Bitmap bitmap) {
        scaledBitmap = bitmap;
        if (myViewHolder.iv_signature.getVisibility() == View.GONE) {
            myViewHolder.iv_signature.setVisibility(View.VISIBLE);
            myViewHolder.iv_signature_2.setVisibility(View.GONE);
            myViewHolder.iv_signature_3.setVisibility(View.GONE);
            myViewHolder.push_view.setVisibility(View.VISIBLE);
            myViewHolder.push_view_2.setVisibility(View.GONE);
            myViewHolder.push_view_3.setVisibility(View.GONE);
            myViewHolder.container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
            myViewHolder.container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
            myViewHolder.container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
            signatureView = myViewHolder.container_view;
        } else if (myViewHolder.iv_signature.getVisibility() == View.VISIBLE &&
                myViewHolder.iv_signature_2.getVisibility() == View.GONE && myViewHolder.iv_signature_3.getVisibility() == View.GONE) {
            myViewHolder.iv_signature.setVisibility(View.VISIBLE);
            myViewHolder.iv_signature_2.setVisibility(View.VISIBLE);
            myViewHolder.iv_signature_3.setVisibility(View.GONE);
            myViewHolder.push_view.setVisibility(View.GONE);
            myViewHolder.push_view_2.setVisibility(View.VISIBLE);
            myViewHolder.push_view_3.setVisibility(View.GONE);
            myViewHolder.container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
            myViewHolder.container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
            myViewHolder.container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
            signatureView = myViewHolder.container_view_2;
        } else {
            myViewHolder.iv_signature.setVisibility(View.VISIBLE);
            myViewHolder.iv_signature_2.setVisibility(View.VISIBLE);
            myViewHolder.iv_signature_3.setVisibility(View.VISIBLE);
            myViewHolder.push_view.setVisibility(View.GONE);
            myViewHolder.push_view_2.setVisibility(View.GONE);
            myViewHolder.push_view_3.setVisibility(View.VISIBLE);
            myViewHolder.container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
            myViewHolder.container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
            myViewHolder.container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
            signatureView = myViewHolder.container_view_3;
        }
        signatureView.post(() -> {
            signatureView.setImageBitmap(scaledBitmap);
            if (context instanceof PdfSignatureActivity) {
                ((PdfSignatureActivity) context).currentColor = Color.BLACK;
            }
        });
    }

    public void changeSignColor(int previousColor, int currentColor) {
        if (context instanceof PdfSignatureActivity) {
            scaledBitmap = ((PdfSignatureActivity) context).replaceColor(scaledBitmap, previousColor, currentColor);
        }
        if (myViewHolder.container_view.getBackground().getConstantState() ==
                context.getResources().getDrawable(R.drawable.iv_signature_focus).getConstantState()) {
            signatureView = myViewHolder.container_view;
        } else if (myViewHolder.container_view_2.getBackground().getConstantState() ==
                context.getResources().getDrawable(R.drawable.iv_signature_focus).getConstantState()) {
            signatureView = myViewHolder.container_view_2;
        } else {
            signatureView = myViewHolder.container_view_3;
        }
        signatureView.post(() -> {
            signatureView.setImageBitmap(scaledBitmap);
        });
    }

    public void setSavedImage(int position, Bitmap bitmap) {
        scaledBitmap = bitmap;
        if (position == 0 && myViewHolder.iv_signature.getVisibility() == View.VISIBLE) {
            if (myViewHolder.iv_signature_2.getVisibility() == View.GONE) {
                myViewHolder.iv_signature_2.setVisibility(View.VISIBLE);
                myViewHolder.container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                myViewHolder.container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
                myViewHolder.container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                myViewHolder.push_view.setVisibility(View.GONE);
                myViewHolder.push_view_2.setVisibility(View.VISIBLE);
                myViewHolder.push_view_3.setVisibility(View.GONE);
                signatureView = myViewHolder.container_view_2;
            } else if (myViewHolder.iv_signature_2.getVisibility() == View.VISIBLE
                    && myViewHolder.iv_signature_3.getVisibility() == View.GONE) {
                myViewHolder.iv_signature_3.setVisibility(View.VISIBLE);
                myViewHolder.container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                myViewHolder.container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                myViewHolder.container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
                myViewHolder.push_view.setVisibility(View.GONE);
                myViewHolder.push_view_2.setVisibility(View.GONE);
                myViewHolder.push_view_3.setVisibility(View.VISIBLE);
                signatureView = myViewHolder.container_view_3;
            } else {
                Toast.makeText(context, "Can not add more than 3 signatures",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (position == 1 && myViewHolder.iv_signature.getVisibility() == View.VISIBLE &&
                myViewHolder.iv_signature_2.getVisibility() == View.VISIBLE) {
            if (myViewHolder.iv_signature_3.getVisibility() == View.GONE) {
                myViewHolder.iv_signature_3.setVisibility(View.VISIBLE);
                myViewHolder.container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                myViewHolder.container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                myViewHolder.container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
                myViewHolder.push_view.setVisibility(View.GONE);
                myViewHolder.push_view_2.setVisibility(View.GONE);
                myViewHolder.push_view_3.setVisibility(View.VISIBLE);
                signatureView = myViewHolder.container_view_3;
            } else {
                Toast.makeText(context, "Can not add more than 3 signatures",
                        Toast.LENGTH_SHORT).show();
            }
        }

        if (position == 0) {
            if (myViewHolder.iv_signature.getVisibility() == View.GONE) {
                myViewHolder.iv_signature.setVisibility(View.VISIBLE);
                myViewHolder.container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
                myViewHolder.container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                myViewHolder.container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                myViewHolder.push_view.setVisibility(View.VISIBLE);
                myViewHolder.push_view_2.setVisibility(View.GONE);
                myViewHolder.push_view_3.setVisibility(View.GONE);
                signatureView = myViewHolder.container_view;
            }
        } else if (position == 1) {
            if (myViewHolder.iv_signature_2.getVisibility() == View.GONE) {
                myViewHolder.iv_signature_2.setVisibility(View.VISIBLE);
                myViewHolder.container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                myViewHolder.container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
                myViewHolder.container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                myViewHolder.push_view.setVisibility(View.GONE);
                myViewHolder.push_view_2.setVisibility(View.VISIBLE);
                myViewHolder.push_view_3.setVisibility(View.GONE);
                signatureView = myViewHolder.container_view_2;
            }
        } else if (position == 2) {
            if (myViewHolder.iv_signature_3.getVisibility() == View.GONE) {
                myViewHolder.iv_signature_3.setVisibility(View.VISIBLE);
                myViewHolder.container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                myViewHolder.container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                myViewHolder.container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
                myViewHolder.push_view.setVisibility(View.GONE);
                myViewHolder.push_view_2.setVisibility(View.GONE);
                myViewHolder.push_view_3.setVisibility(View.VISIBLE);
                signatureView = myViewHolder.container_view_3;
            }
        }
        signatureView.post(() -> {
            signatureView.setImageBitmap(scaledBitmap);
            if (context instanceof PdfSignatureActivity) {
                ((PdfSignatureActivity) context).currentColor = Color.BLACK;
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagesPathList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.imageView) {
            if (context instanceof PdfSignatureActivity) {
                ((PdfSignatureActivity) context).customLinearLayoutManager.setScrollEnabled(true);
            }
            if (myViewHolder.container_view != null) {
                myViewHolder.container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
            }
            if (myViewHolder.container_view_2 != null) {
                myViewHolder.container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
            }
            if (myViewHolder.container_view_3 != null) {
                myViewHolder.container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
            }
            myViewHolder.push_view.setVisibility(View.GONE);
            myViewHolder.push_view_2.setVisibility(View.GONE);
            myViewHolder.push_view_3.setVisibility(View.GONE);
            if (context instanceof PdfSignatureActivity) {
                ((PdfSignatureActivity) context).rv_colors.setVisibility(View.GONE);
                ((PdfSignatureActivity) context).img_remove_signature.setVisibility(View.GONE);
            }
        } else if (id == R.id.view) {
            if (myViewHolder.container_view.getBackground().getConstantState() ==
                    context.getResources().getDrawable(R.drawable.iv_signature_unfocus).getConstantState()) {
                if (myViewHolder.container_view != null) {
                    myViewHolder.container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
                }
                if (myViewHolder.container_view_2 != null) {
                    myViewHolder.container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                }
                if (myViewHolder.container_view_3 != null) {
                    myViewHolder.container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                }
                myViewHolder.push_view.setVisibility(View.VISIBLE);
                myViewHolder.push_view_2.setVisibility(View.GONE);
                myViewHolder.push_view_3.setVisibility(View.GONE);
                if (context instanceof PdfSignatureActivity) {
                    ((PdfSignatureActivity) context).rv_colors.setVisibility(View.VISIBLE);
                    ((PdfSignatureActivity) context).img_remove_signature.setVisibility(View.VISIBLE);
                    ((PdfSignatureActivity) context).customLinearLayoutManager.setScrollEnabled(false);
                }
            }
        } else if (id == R.id.view_2) {
            if (myViewHolder.container_view_2.getBackground().getConstantState() ==
                    context.getResources().getDrawable(R.drawable.iv_signature_unfocus).getConstantState()) {
                if (myViewHolder.container_view_2 != null) {
                    myViewHolder.container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
                }
                if (myViewHolder.container_view != null) {
                    myViewHolder.container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                }
                if (myViewHolder.container_view_3 != null) {
                    myViewHolder.container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                }
                myViewHolder.push_view.setVisibility(View.GONE);
                myViewHolder.push_view_2.setVisibility(View.VISIBLE);
                myViewHolder.push_view_3.setVisibility(View.GONE);
                if (context instanceof PdfSignatureActivity) {
                    ((PdfSignatureActivity) context).rv_colors.setVisibility(View.VISIBLE);
                    ((PdfSignatureActivity) context).img_remove_signature.setVisibility(View.VISIBLE);
                    ((PdfSignatureActivity) context).customLinearLayoutManager.setScrollEnabled(false);
                }
            }
        } else if (id == R.id.view_3) {
            if (myViewHolder.container_view_3.getBackground().getConstantState() ==
                    context.getResources().getDrawable(R.drawable.iv_signature_unfocus).getConstantState()) {
                if (myViewHolder.container_view_3 != null) {
                    myViewHolder.container_view_3.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_focus));
                }
                if (myViewHolder.container_view != null) {
                    myViewHolder.container_view.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                }
                if (myViewHolder.container_view_2 != null) {
                    myViewHolder.container_view_2.setBackground(context.getResources().getDrawable(R.drawable.iv_signature_unfocus));
                }
                myViewHolder.push_view.setVisibility(View.GONE);
                myViewHolder.push_view_2.setVisibility(View.GONE);
                myViewHolder.push_view_3.setVisibility(View.VISIBLE);
                if (context instanceof PdfSignatureActivity) {
                    ((PdfSignatureActivity) context).rv_colors.setVisibility(View.VISIBLE);
                    ((PdfSignatureActivity) context).img_remove_signature.setVisibility(View.VISIBLE);
                    ((PdfSignatureActivity) context).customLinearLayoutManager.setScrollEnabled(false);
                }
            }
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public SingleFingerView iv_signature;
        public SingleFingerView2 iv_signature_2;
        public SingleFingerView3 iv_signature_3;
        public ImageView container_view, push_view, container_view_2, push_view_2,
                container_view_3, push_view_3;
        public RelativeLayout relative_main;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            iv_signature = itemView.findViewById(R.id.iv_signature);
            iv_signature_2 = itemView.findViewById(R.id.iv_signature2);
            iv_signature_3 = itemView.findViewById(R.id.iv_signature3);
            container_view = itemView.findViewById(R.id.view);
            push_view = itemView.findViewById(R.id.push_view);
            container_view_2 = itemView.findViewById(R.id.view_2);
            push_view_2 = itemView.findViewById(R.id.push_view_2);
            container_view_3 = itemView.findViewById(R.id.view_3);
            push_view_3 = itemView.findViewById(R.id.push_view_3);
            relative_main = itemView.findViewById(R.id.relative_main);
        }
    }

    public PdfSignatureAdapter.MyViewHolder getViewByPosition(int position) {
        return holderList.get(position);
    }
}
