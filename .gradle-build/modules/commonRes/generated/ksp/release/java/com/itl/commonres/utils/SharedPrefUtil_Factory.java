package com.itl.commonres.utils;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class SharedPrefUtil_Factory implements Factory<SharedPrefUtil> {
  private final Provider<Context> mContextProvider;

  private SharedPrefUtil_Factory(Provider<Context> mContextProvider) {
    this.mContextProvider = mContextProvider;
  }

  @Override
  public SharedPrefUtil get() {
    return newInstance(mContextProvider.get());
  }

  public static SharedPrefUtil_Factory create(Provider<Context> mContextProvider) {
    return new SharedPrefUtil_Factory(mContextProvider);
  }

  public static SharedPrefUtil newInstance(Context mContext) {
    return new SharedPrefUtil(mContext);
  }
}
