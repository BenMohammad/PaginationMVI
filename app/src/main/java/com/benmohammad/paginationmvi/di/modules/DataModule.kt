package com.benmohammad.paginationmvi.di.modules

import com.benmohammad.paginationmvi.BuildConfig
import com.benmohammad.paginationmvi.data.remote.ApiService
import com.benmohammad.paginationmvi.data.remote.BASE_URL
import com.benmohammad.paginationmvi.di.ApplicationScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

@Module
class DataModule {
    @ApplicationScope
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService {
        return ApiService(retrofit)
    }

    @ApplicationScope
    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi
                        .Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
                )
            ).build()
    }

    @ApplicationScope
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .apply {
                if(BuildConfig.DEBUG) {
                    HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY)
                        .let(::addInterceptor)
                }
            }.build()
    }
}