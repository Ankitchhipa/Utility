package com.cam.scanner.scantopdf.android.databinding;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class ActivityCurrentPlanBindingImpl extends ActivityCurrentPlanBinding  {

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
        sViewsWithIds.put(R.id.tv_current_plan_name, 5);
        sViewsWithIds.put(R.id.progress_bar_active_plan, 6);
        sViewsWithIds.put(R.id.tv_current_plan_price, 7);
        sViewsWithIds.put(R.id.linear, 8);
        sViewsWithIds.put(R.id.upgradePlanFeatureRecycler, 9);
        sViewsWithIds.put(R.id.dotsLinearLayout, 10);
        sViewsWithIds.put(R.id.img_indicator1, 11);
        sViewsWithIds.put(R.id.img_indicator2, 12);
        sViewsWithIds.put(R.id.card_upgradePlan, 13);
        sViewsWithIds.put(R.id.tv_upgrade_plan_name, 14);
        sViewsWithIds.put(R.id.tv_upgrade_plan_price, 15);
        sViewsWithIds.put(R.id.progress_bar_upgrade_plan, 16);
        sViewsWithIds.put(R.id.progress_bar, 17);
        sViewsWithIds.put(R.id.card_manage_subscription, 18);
        sViewsWithIds.put(R.id.tv_manage_subscription, 19);
        sViewsWithIds.put(R.id.tv_manage_info, 20);
        sViewsWithIds.put(R.id.tv_disclaimer, 21);
        sViewsWithIds.put(R.id.bottomLayout, 22);
    }
    // views
    @NonNull
    private final android.widget.RelativeLayout mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActivityCurrentPlanBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 23, sIncludes, sViewsWithIds));
    }
    private ActivityCurrentPlanBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (android.widget.LinearLayout) bindings[22]
            , (androidx.cardview.widget.CardView) bindings[18]
            , (androidx.cardview.widget.CardView) bindings[13]
            , (androidx.appcompat.widget.AppCompatImageView) bindings[4]
            , (android.widget.LinearLayout) bindings[10]
            , (android.widget.ImageView) bindings[11]
            , (android.widget.ImageView) bindings[12]
            , (android.widget.LinearLayout) bindings[8]
            , (android.widget.ProgressBar) bindings[17]
            , (android.widget.ProgressBar) bindings[6]
            , (android.widget.ProgressBar) bindings[16]
            , (bindings[1] != null) ? com.cam.scanner.scantopdf.android.databinding.ProgressLayBinding.bind((android.view.View) bindings[1]) : null
            , (android.widget.RelativeLayout) bindings[3]
            , (android.widget.TextView) bindings[5]
            , (android.widget.TextView) bindings[7]
            , (android.widget.TextView) bindings[21]
            , (android.widget.ImageView) bindings[20]
            , (android.widget.LinearLayout) bindings[19]
            , (android.widget.TextView) bindings[14]
            , (android.widget.TextView) bindings[15]
            , (androidx.recyclerview.widget.RecyclerView) bindings[9]
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