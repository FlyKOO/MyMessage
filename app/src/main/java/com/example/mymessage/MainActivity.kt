package com.example.mymessage

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.mymessage.ui.MyMessageRoot
import com.example.mymessage.ui.permissions.RequiredPermissionsState
import com.example.mymessage.ui.permissions.rememberRequiredPermissionsState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val requiredPermissions = rememberRequiredPermissionsState(
                        permissions = buildList {
                            add(Manifest.permission.READ_SMS)
                            add(Manifest.permission.SEND_SMS)
                            add(Manifest.permission.WRITE_SMS)
                            add(Manifest.permission.RECEIVE_SMS)
                            add(Manifest.permission.RECEIVE_MMS)
                            add(Manifest.permission.READ_CONTACTS)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )

                    MyMessageScaffold(
                        requiredPermissionsState = requiredPermissions,
                        viewModel = viewModel,
                        activity = this@MainActivity
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDefaultStatus()
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (intent.action == Intent.ACTION_SENDTO && data.scheme in listOf("sms", "smsto")) {
            viewModel.openConversationForAddress(data.schemeSpecificPart)
        }
    }
}

@Composable
private fun MyMessageScaffold(
    requiredPermissionsState: RequiredPermissionsState,
    viewModel: MainViewModel,
    activity: Activity
) {
    val isDefaultSmsApp by viewModel.isDefaultSmsApp.collectAsState()
    val launcher = remember {
        mutableStateOf(false)
    }
    val hasAutoRequested = remember { mutableStateOf(false) }

    val roleRequestLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        launcher.value = false
        viewModel.refreshDefaultStatus()
    }

    LaunchedEffect(isDefaultSmsApp) {
        if (isDefaultSmsApp) {
            hasAutoRequested.value = false
        } else if (!hasAutoRequested.value) {
            hasAutoRequested.value = true
            val defaultIntent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                .putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, activity.packageName)
            launcher.value = true
            roleRequestLauncher.launch(defaultIntent)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MyMessageRoot(
            requiredPermissionsState = requiredPermissionsState,
            mainViewModel = viewModel,
            onRequestDefaultSms = {
                val defaultIntent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                    .putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, activity.packageName)
                launcher.value = true
                hasAutoRequested.value = true
                roleRequestLauncher.launch(defaultIntent)
            }
        )
    }
}
