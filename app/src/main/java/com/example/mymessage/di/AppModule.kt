package com.example.mymessage.di

import android.content.Context
import com.example.mymessage.data.ContactsRepository
import com.example.mymessage.data.SmsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSmsRepository(@ApplicationContext context: Context): SmsRepository = SmsRepository(context)

    @Provides
    @Singleton
    fun provideContactsRepository(@ApplicationContext context: Context): ContactsRepository = ContactsRepository(context)
}
