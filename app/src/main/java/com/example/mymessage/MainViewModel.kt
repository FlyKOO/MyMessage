package com.example.mymessage

import android.content.Context
import android.provider.Telephony
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymessage.data.ContactsRepository
import com.example.mymessage.data.SmsRepository
import com.example.mymessage.data.model.Contact
import com.example.mymessage.data.model.ConversationMessage
import com.example.mymessage.data.model.ConversationSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val smsRepository: SmsRepository,
    private val contactsRepository: ContactsRepository,
    private val context: Context
) : ViewModel() {

    private val _selectedThreadId = MutableStateFlow<Long?>(null)
    val selectedThreadId: StateFlow<Long?> = _selectedThreadId

    private val _pendingAddress = MutableStateFlow<String?>(null)
    val pendingAddress: StateFlow<String?> = _pendingAddress

    private val _messageDraft = MutableStateFlow("")
    val messageDraft: StateFlow<String> = _messageDraft

    private val _isDefaultSmsApp = MutableStateFlow(checkIsDefault())
    val isDefaultSmsApp: StateFlow<Boolean> = _isDefaultSmsApp

    val conversations: StateFlow<List<ConversationSummary>> = smsRepository
        .getConversationSummaries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val contacts: StateFlow<List<Contact>> = contactsRepository
        .getContacts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val messages: StateFlow<List<ConversationMessage>> = selectedThreadId
        .filterNotNull()
        .flatMapLatest { threadId ->
            smsRepository.getMessages(threadId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun refreshDefaultStatus() {
        _isDefaultSmsApp.value = checkIsDefault()
    }

    fun selectThread(threadId: Long) {
        viewModelScope.launch {
            smsRepository.markThreadRead(threadId)
            _selectedThreadId.value = threadId
        }
    }

    fun openConversationForAddress(address: String) {
        _pendingAddress.value = address
        _selectedThreadId.value = null
    }

    fun clearPendingAddress() {
        _pendingAddress.value = null
    }

    fun updateDraft(value: String) {
        _messageDraft.value = value
    }

    fun sendMessage(address: String, body: String) {
        if (body.isBlank()) return
        viewModelScope.launch {
            smsRepository.sendMessage(address, body)
            _messageDraft.value = ""
        }
    }

    private fun checkIsDefault(): Boolean =
        Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
}
