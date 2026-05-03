package com.itl.commonres;

import android.content.Context;
import com.itl.commonres.firebaseUtils.ConfigFilesUpdateHelper;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class Module_GetConfigFilesUpdateHelperFactory implements Factory<ConfigFilesUpdateHelper> {
  private final Module module;

  private final Provider<Context> contextProvider;

  private Module_GetConfigFilesUpdateHelperFactory(Module module,
      Provider<Context> contextProvider) {
    this.module = module;
    this.contextProvider = contextProvider;
  }

  @Override
  public ConfigFilesUpdateHelper get() {
    return getConfigFilesUpdateHelper(module, contextProvider.get());
  }

  public static Module_GetConfigFilesUpdateHelperFactory create(Module module,
      Provider<Context> contextProvider) {
    return new Module_GetConfigFilesUpdateHelperFactory(module, contextProvider);
  }

  public static ConfigFilesUpdateHelper getConfigFilesUpdateHelper(Module instance,
      Context context) {
    return Preconditions.checkNotNullFromProvides(instance.getConfigFilesUpdateHelper(context));
  }
}
