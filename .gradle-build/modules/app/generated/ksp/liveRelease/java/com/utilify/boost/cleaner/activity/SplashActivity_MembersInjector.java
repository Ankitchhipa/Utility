package com.utilify.boost.cleaner.activity;

import com.itl.commonres.firebaseUtils.ConfigFilesUpdateHelper;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class SplashActivity_MembersInjector implements MembersInjector<SplashActivity> {
  private final Provider<ConfigFilesUpdateHelper> configFilesUpdateHelperProvider;

  private SplashActivity_MembersInjector(
      Provider<ConfigFilesUpdateHelper> configFilesUpdateHelperProvider) {
    this.configFilesUpdateHelperProvider = configFilesUpdateHelperProvider;
  }

  @Override
  public void injectMembers(SplashActivity instance) {
    injectConfigFilesUpdateHelper(instance, configFilesUpdateHelperProvider.get());
  }

  public static MembersInjector<SplashActivity> create(
      Provider<ConfigFilesUpdateHelper> configFilesUpdateHelperProvider) {
    return new SplashActivity_MembersInjector(configFilesUpdateHelperProvider);
  }

  @InjectedFieldSignature("com.utilify.boost.cleaner.activity.SplashActivity.configFilesUpdateHelper")
  public static void injectConfigFilesUpdateHelper(SplashActivity instance,
      ConfigFilesUpdateHelper configFilesUpdateHelper) {
    instance.configFilesUpdateHelper = configFilesUpdateHelper;
  }
}
