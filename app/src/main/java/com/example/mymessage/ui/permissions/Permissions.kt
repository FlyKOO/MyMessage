@file:OptIn(ExperimentalPermissionsApi::class)

package com.example.mymessage.ui.permissions

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.mymessage.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlin.OptIn

class RequiredPermissionsState(
    private val permissionsState: MultiplePermissionsState
) {
    val allGranted: Boolean
        get() = permissionsState.allPermissionsGranted

    fun request() {
        permissionsState.launchMultiplePermissionRequest()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberRequiredPermissionsState(permissions: List<String>): RequiredPermissionsState {
    val state = rememberMultiplePermissionsState(permissions)
    return remember(state) { RequiredPermissionsState(state) }
}

@Composable
fun PermissionsDialog(
    state: RequiredPermissionsState,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit
) {
    var open by remember { mutableStateOf(true) }
    if (!open) return

    AlertDialog(
        onDismissRequest = {
            open = false
            onDismiss()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    open = false
                    state.request()
                    onDismiss()
                }
            ) {
                Text(text = confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                open = false
                onDismiss()
            }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        text = {
            Text(text = message)
        }
    )
}
