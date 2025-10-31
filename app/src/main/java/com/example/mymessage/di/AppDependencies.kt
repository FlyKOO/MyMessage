package com.example.mymessage.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymessage.MainViewModel
import com.example.mymessage.data.ContactsRepository
import com.example.mymessage.data.SmsRepository
import com.example.mymessage.sms.SmsNotificationManager

object AppDependencies {
    private val lock = Any()

    @Volatile
    private var initialized = false
    private lateinit var smsRepositoryInstance: SmsRepository
    private lateinit var contactsRepositoryInstance: ContactsRepository
    private lateinit var smsNotificationManagerInstance: SmsNotificationManager

    fun initialize(context: Context) {
        if (initialized) return
        synchronized(lock) {
            if (!initialized) {
                val appContext = context.applicationContext
                smsRepositoryInstance = SmsRepository(appContext)
                contactsRepositoryInstance = ContactsRepository(appContext)
                smsNotificationManagerInstance = SmsNotificationManager(appContext)
                initialized = true
            }
        }
    }

    private fun ensureInitialized(context: Context) {
        if (!initialized) {
            initialize(context)
        }
    }

    fun smsRepository(context: Context): SmsRepository {
        ensureInitialized(context)
        return smsRepositoryInstance
    }

    fun contactsRepository(context: Context): ContactsRepository {
        ensureInitialized(context)
        return contactsRepositoryInstance
    }

    fun smsNotificationManager(context: Context): SmsNotificationManager {
        ensureInitialized(context)
        return smsNotificationManagerInstance
    }

    fun mainViewModelFactory(context: Context): ViewModelProvider.Factory {
        ensureInitialized(context)
        val appContext = context.applicationContext
        return MainViewModelFactory(
            smsRepositoryInstance,
            contactsRepositoryInstance,
            appContext
        )
    }

    private class MainViewModelFactory(
        private val smsRepository: SmsRepository,
        private val contactsRepository: ContactsRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(smsRepository, contactsRepository, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
