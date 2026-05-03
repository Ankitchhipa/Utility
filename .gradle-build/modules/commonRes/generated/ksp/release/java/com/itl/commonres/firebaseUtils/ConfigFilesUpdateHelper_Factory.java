package com.itl.commonres.firebaseUtils;

import android.content.Context;
import com.itl.commonres.utils.SharedPrefUtil;
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
public final class ConfigFilesUpdateHelper_Factory implements Factory<ConfigFilesUpdateHelper> {
  private final Provider<Context> contextProvider;

  private final Provider<SharedPrefUtil> sharedPrefUtilProvider;

  private ConfigFilesUpdateHelper_Factory(Provider<Context> contextProvider,
      Provider<SharedPrefUtil> sharedPrefUtilProvider) {
    this.contextProvider = contextProvider;
    this.sharedPrefUtilProvider = sharedPrefUtilProvider;
  }

  @Override
  public ConfigFilesUpdateHelper get() {
    return newInstance(contextProvider.get(), sharedPrefUtilProvider.get());
  }

  public static ConfigFilesUpdateHelper_Factory create(Provider<Context> contextProvider,
      Provider<SharedPrefUtil> sharedPrefUtilProvider) {
    return new ConfigFilesUpdateHelper_Factory(contextProvider, sharedPrefUtilProvider);
  }

  public static ConfigFilesUpdateHelper newInstance(Context context,
      SharedPrefUtil sharedPrefUtil) {
    return new ConfigFilesUpdateHelper(context, sharedPrefUtil);
  }
}
