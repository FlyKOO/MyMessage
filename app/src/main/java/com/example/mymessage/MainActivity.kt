package com.example.mymessage

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.mymessage.ui.MyMessageRoot
import com.example.mymessage.ui.permissions.rememberRequiredPermissionsState
import com.example.mymessage.ui.theme.MyMessageTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            MyMessageTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val permissions = remember {
                        buildList {
                            add(Manifest.permission.READ_SMS)
                            add(Manifest.permission.RECEIVE_SMS)
                            add(Manifest.permission.SEND_SMS)
                            add(Manifest.permission.RECEIVE_MMS)
                            add(Manifest.permission.RECEIVE_WAP_PUSH)
                            add(Manifest.permission.READ_CONTACTS)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }
                    val requiredPermissionsState = rememberRequiredPermissionsState(permissions)

                    MyMessageRoot(
                        requiredPermissionsState = requiredPermissionsState,
                        mainViewModel = mainViewModel,
                        onRequestDefaultSms = ::requestDefaultSmsApp
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.refreshDefaultStatus()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let(::handleIntent)
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SENDTO -> {
                val data = intent.data
                val address = data?.schemeSpecificPart?.substringBefore("?")
                if (!address.isNullOrEmpty()) {
                    mainViewModel.openConversationForAddress(address)
                }
                val bodyFromExtra = intent.getStringExtra(Intent.EXTRA_TEXT)
                val bodyFromSmsExtra = intent.getStringExtra("sms_body")
                val bodyFromQuery = data?.getQueryParameter("body")
                val body = bodyFromExtra ?: bodyFromSmsExtra ?: bodyFromQuery
                if (!body.isNullOrBlank()) {
                    mainViewModel.updateDraft(body)
                }
            }
            else -> {
                val threadId = intent.getLongExtra(EXTRA_THREAD_ID, -1L)
                if (threadId > 0) {
                    mainViewModel.selectThread(threadId)
                }
                val address = intent.getStringExtra(EXTRA_ADDRESS)
                if (!address.isNullOrEmpty()) {
                    mainViewModel.openConversationForAddress(address)
                }
            }
        }
    }

    private fun requestDefaultSmsApp() {
        if (Telephony.Sms.getDefaultSmsPackage(this) != packageName) {
            val changeDefaultIntent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
            }
            startActivity(changeDefaultIntent)
        }
    }

    companion object {
        const val EXTRA_THREAD_ID = "extra_thread_id"
        const val EXTRA_ADDRESS = "extra_address"
    }
}
