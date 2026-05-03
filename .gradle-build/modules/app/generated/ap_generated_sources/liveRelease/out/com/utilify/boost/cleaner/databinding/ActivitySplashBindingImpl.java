package com.utilify.boost.cleaner.databinding;
import com.utilify.boost.cleaner.R;
import com.utilify.boost.cleaner.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class ActivitySplashBindingImpl extends ActivitySplashBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.orbTop, 1);
        sViewsWithIds.put(R.id.orbRight, 2);
        sViewsWithIds.put(R.id.orbBottom, 3);
        sViewsWithIds.put(R.id.topBadge, 4);
        sViewsWithIds.put(R.id.headerView, 5);
        sViewsWithIds.put(R.id.logoCard, 6);
        sViewsWithIds.put(R.id.appIcon, 7);
        sViewsWithIds.put(R.id.appTitle, 8);
        sViewsWithIds.put(R.id.tagCard, 9);
        sViewsWithIds.put(R.id.appTag, 10);
        sViewsWithIds.put(R.id.featureRow, 11);
        sViewsWithIds.put(R.id.footerTag, 12);
    }
    // views
    @NonNull
    private final android.widget.RelativeLayout mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActivitySplashBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 13, sIncludes, sViewsWithIds));
    }
    private ActivitySplashBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (androidx.appcompat.widget.AppCompatImageView) bindings[7]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[10]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[8]
            , (android.widget.LinearLayout) bindings[11]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[12]
            , (android.widget.RelativeLayout) bindings[5]
            , (androidx.cardview.widget.CardView) bindings[6]
            , (android.view.View) bindings[3]
            , (android.view.View) bindings[2]
            , (android.view.View) bindings[1]
            , (androidx.cardview.widget.CardView) bindings[9]
            , (androidx.appcompat.widget.AppCompatTextView) bindings[4]
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