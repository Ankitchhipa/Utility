package com.cam.scanner.scantopdf.android.databinding;
import com.cam.scanner.scantopdf.android.R;
import com.cam.scanner.scantopdf.android.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class OcrPlanDialogBindingImpl extends OcrPlanDialogBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.progress_lay, 1);
        sViewsWithIds.put(R.id.closeIcon, 2);
        sViewsWithIds.put(R.id.ocrImage, 3);
        sViewsWithIds.put(R.id.ocrHeader, 4);
        sViewsWithIds.put(R.id.ocrDesc, 5);
        sViewsWithIds.put(R.id.tv_ocr_count, 6);
        sViewsWithIds.put(R.id.progress_bar_tv_price, 7);
        sViewsWithIds.put(R.id.tv_price, 8);
        sViewsWithIds.put(R.id.btn_buy_now, 9);
        sViewsWithIds.put(R.id.progress_bar_price, 10);
    }
    // views
    @NonNull
    private final android.widget.RelativeLayout mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public OcrPlanDialogBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 11, sIncludes, sViewsWithIds));
    }
    private OcrPlanDialogBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (android.widget.Button) bindings[9]
            , (androidx.appcompat.widget.AppCompatImageView) bindings[2]
            , (android.widget.TextView) bindings[5]
            , (android.widget.TextView) bindings[4]
            , (androidx.appcompat.widget.AppCompatImageView) bindings[3]
            , (android.widget.ProgressBar) bindings[10]
            , (android.widget.ProgressBar) bindings[7]
            , (bindings[1] != null) ? com.cam.scanner.scantopdf.android.databinding.ProgressLayBinding.bind((android.view.View) bindings[1]) : null
            , (android.widget.TextView) bindings[6]
            , (android.widget.TextView) bindings[8]
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