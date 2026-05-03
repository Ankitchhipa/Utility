package com.cam.scanner.scantopdf.android;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import androidx.databinding.DataBinderMapper;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import com.cam.scanner.scantopdf.android.databinding.ActivityCurrentPlanBindingImpl;
import com.cam.scanner.scantopdf.android.databinding.ActivityPremiumPlanBindingImpl;
import com.cam.scanner.scantopdf.android.databinding.ItemFeatureDescriptionBindingImpl;
import com.cam.scanner.scantopdf.android.databinding.ItemFeaturePlanBindingImpl;
import com.cam.scanner.scantopdf.android.databinding.ItemUpgradePlanFeatureBindingImpl;
import com.cam.scanner.scantopdf.android.databinding.OcrChoosePlanDialogBindingImpl;
import com.cam.scanner.scantopdf.android.databinding.OcrPlanDialogBindingImpl;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataBinderMapperImpl extends DataBinderMapper {
  private static final int LAYOUT_ACTIVITYCURRENTPLAN = 1;

  private static final int LAYOUT_ACTIVITYPREMIUMPLAN = 2;

  private static final int LAYOUT_ITEMFEATUREDESCRIPTION = 3;

  private static final int LAYOUT_ITEMFEATUREPLAN = 4;

  private static final int LAYOUT_ITEMUPGRADEPLANFEATURE = 5;

  private static final int LAYOUT_OCRCHOOSEPLANDIALOG = 6;

  private static final int LAYOUT_OCRPLANDIALOG = 7;

  private static final SparseIntArray INTERNAL_LAYOUT_ID_LOOKUP = new SparseIntArray(7);

  static {
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.cam.scanner.scantopdf.android.R.layout.activity_current_plan, LAYOUT_ACTIVITYCURRENTPLAN);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.cam.scanner.scantopdf.android.R.layout.activity_premium_plan, LAYOUT_ACTIVITYPREMIUMPLAN);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.cam.scanner.scantopdf.android.R.layout.item_feature_description, LAYOUT_ITEMFEATUREDESCRIPTION);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.cam.scanner.scantopdf.android.R.layout.item_feature_plan, LAYOUT_ITEMFEATUREPLAN);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.cam.scanner.scantopdf.android.R.layout.item_upgrade_plan_feature, LAYOUT_ITEMUPGRADEPLANFEATURE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.cam.scanner.scantopdf.android.R.layout.ocr_choose_plan_dialog, LAYOUT_OCRCHOOSEPLANDIALOG);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.cam.scanner.scantopdf.android.R.layout.ocr_plan_dialog, LAYOUT_OCRPLANDIALOG);
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View view, int layoutId) {
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = view.getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
        case  LAYOUT_ACTIVITYCURRENTPLAN: {
          if ("layout/activity_current_plan_0".equals(tag)) {
            return new ActivityCurrentPlanBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_current_plan is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYPREMIUMPLAN: {
          if ("layout/activity_premium_plan_0".equals(tag)) {
            return new ActivityPremiumPlanBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_premium_plan is invalid. Received: " + tag);
        }
        case  LAYOUT_ITEMFEATUREDESCRIPTION: {
          if ("layout/item_feature_description_0".equals(tag)) {
            return new ItemFeatureDescriptionBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for item_feature_description is invalid. Received: " + tag);
        }
        case  LAYOUT_ITEMFEATUREPLAN: {
          if ("layout/item_feature_plan_0".equals(tag)) {
            return new ItemFeaturePlanBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for item_feature_plan is invalid. Received: " + tag);
        }
        case  LAYOUT_ITEMUPGRADEPLANFEATURE: {
          if ("layout/item_upgrade_plan_feature_0".equals(tag)) {
            return new ItemUpgradePlanFeatureBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for item_upgrade_plan_feature is invalid. Received: " + tag);
        }
        case  LAYOUT_OCRCHOOSEPLANDIALOG: {
          if ("layout/ocr_choose_plan_dialog_0".equals(tag)) {
            return new OcrChoosePlanDialogBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for ocr_choose_plan_dialog is invalid. Received: " + tag);
        }
        case  LAYOUT_OCRPLANDIALOG: {
          if ("layout/ocr_plan_dialog_0".equals(tag)) {
            return new OcrPlanDialogBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for ocr_plan_dialog is invalid. Received: " + tag);
        }
      }
    }
    return null;
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View[] views, int layoutId) {
    if(views == null || views.length == 0) {
      return null;
    }
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = views[0].getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
      }
    }
    return null;
  }

  @Override
  public int getLayoutId(String tag) {
    if (tag == null) {
      return 0;
    }
    Integer tmpVal = InnerLayoutIdLookup.sKeys.get(tag);
    return tmpVal == null ? 0 : tmpVal;
  }

  @Override
  public String convertBrIdToString(int localId) {
    String tmpVal = InnerBrLookup.sKeys.get(localId);
    return tmpVal;
  }

  @Override
  public List<DataBinderMapper> collectDependencies() {
    ArrayList<DataBinderMapper> result = new ArrayList<DataBinderMapper>(2);
    result.add(new androidx.databinding.library.baseAdapters.DataBinderMapperImpl());
    result.add(new com.itl.commonres.DataBinderMapperImpl());
    return result;
  }

  private static class InnerBrLookup {
    static final SparseArray<String> sKeys = new SparseArray<String>(1);

    static {
      sKeys.put(0, "_all");
    }
  }

  private static class InnerLayoutIdLookup {
    static final HashMap<String, Integer> sKeys = new HashMap<String, Integer>(7);

    static {
      sKeys.put("layout/activity_current_plan_0", com.cam.scanner.scantopdf.android.R.layout.activity_current_plan);
      sKeys.put("layout/activity_premium_plan_0", com.cam.scanner.scantopdf.android.R.layout.activity_premium_plan);
      sKeys.put("layout/item_feature_description_0", com.cam.scanner.scantopdf.android.R.layout.item_feature_description);
      sKeys.put("layout/item_feature_plan_0", com.cam.scanner.scantopdf.android.R.layout.item_feature_plan);
      sKeys.put("layout/item_upgrade_plan_feature_0", com.cam.scanner.scantopdf.android.R.layout.item_upgrade_plan_feature);
      sKeys.put("layout/ocr_choose_plan_dialog_0", com.cam.scanner.scantopdf.android.R.layout.ocr_choose_plan_dialog);
      sKeys.put("layout/ocr_plan_dialog_0", com.cam.scanner.scantopdf.android.R.layout.ocr_plan_dialog);
    }
  }
}
