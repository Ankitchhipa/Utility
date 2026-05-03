package com.cam.scanner.scantopdf.android.databinding;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class ActivityPremiumPlanBindingImpl extends ActivityPremiumPlanBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.progress_lay, 1);
        sViewsWithIds.put(R.id.view1, 2);
        sViewsWithIds.put(R.id.rl_top, 3);
        sViewsWithIds.put(R.id.closeIcon, 4);
        sViewsWithIds.put(R.id.tv_getAccess, 5);
        sViewsWithIds.put(R.id.linear, 6);
        sViewsWithIds.put(R.id.cardView, 7);
        sViewsWithIds.put(R.id.headerFeature, 8);
        sViewsWithIds.put(R.id.basic, 9);
        sViewsWithIds.put(R.id.premium, 10);
        sViewsWithIds.put(R.id.featureRecycler, 11);
        sViewsWithIds.put(R.id.descriptionRecycler, 12);
        sViewsWithIds.put(R.id.bottomLayout, 13);
        sViewsWithIds.put(R.id.card_quartely, 14);
        sViewsWithIds.put(R.id.btn_buy_now_quartely, 15);
        sViewsWithIds.put(R.id.progress_bar_price_quartely, 16);
        sViewsWithIds.put(R.id.btn_buy_now, 17);
        sViewsWithIds.put(R.id.progress_bar_price, 18);
    }
    // views
    @NonNull
    private final android.widget.RelativeLayout mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActivityPremiumPlanBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 19, sIncludes, sViewsWithIds));
    }
    private ActivityPremiumPlanBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (android.widget.TextView) bindings[9]
            , (android.widget.LinearLayout) bindings[13]
            , (androidx.appcompat.widget.AppCompatButton) bindings[17]
            , (android.widget.Button) bindings[15]
            , (androidx.cardview.widget.CardView) bindings[14]
            , (androidx.cardview.widget.CardView) bindings[7]
            , (androidx.appcompat.widget.AppCompatImageView) bindings[4]
            , (androidx.recyclerview.widget.RecyclerView) bindings[12]
            , (androidx.recyclerview.widget.RecyclerView) bindings[11]
            , (android.widget.LinearLayout) bindings[8]
            , (android.widget.LinearLayout) bindings[6]
            , (android.widget.TextView) bindings[10]
            , (android.widget.ProgressBar) bindings[18]
            , (android.widget.ProgressBar) bindings[16]
            , (bindings[1] != null) ? com.cam.scanner.scantopdf.android.databinding.ProgressLayBinding.bind((android.view.View) bindings[1]) : null
            , (android.widget.RelativeLayout) bindings[3]
            , (android.widget.TextView) bindings[5]
            , (android.view.View) bindings[2]
            );
        this.mboundView0 = (android.widget.RelativeLayout) bindings[0];
        this.mboundView0.setTag(null);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x1L;
        }
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setVariable(int variableId, @Nullable Object variable)  {
        boolean variableSet = true;
            return variableSet;
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
        }
        return false;
    }

    @Override
    protected void executeBindings() {
        long dirtyFlags = 0;
        synchronized(this) {
            dirtyFlags = mDirtyFlags;
            mDirtyFlags = 0;
        }
        // batch finished
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;
    /* flag mapping
        flag 0 (0x1L): null
    flag mapping end*/
    //end
}