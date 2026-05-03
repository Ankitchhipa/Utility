package com.itl.commonres

import android.content.Context
import com.itl.commonres.firebaseUtils.ConfigFilesUpdateHelper
import com.itl.commonres.utils.SharedPrefUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class Module {


    @Provides
    @Singleton
    fun getConfigFilesUpdateHelper(@ApplicationContext context: Context) =
        ConfigFilesUpdateHelper(context, getSharedPreferences(context))

    @Provides
    @Singleton
    fun getSharedPreferences(@ApplicationContext context: Context) = SharedPrefUtil(context)
}