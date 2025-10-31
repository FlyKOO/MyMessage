package com.example.mymessage.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mymessage.MainViewModel
import com.example.mymessage.R
import com.example.mymessage.data.model.ConversationMessage
import com.example.mymessage.data.model.ConversationSummary
import com.example.mymessage.data.model.Contact
import com.example.mymessage.ui.components.ComposeMessageSheet
import com.example.mymessage.ui.contacts.ContactsScreen
import com.example.mymessage.ui.conversation.ConversationScreen
import com.example.mymessage.ui.inbox.InboxScreen
import com.example.mymessage.ui.permissions.PermissionsDialog
import com.example.mymessage.ui.permissions.RequiredPermissionsState

private const val RouteInbox = "inbox"
private const val RouteContacts = "contacts"
private const val RouteConversation = "conversation/{threadId}"
private const val RouteNewMessage = "new_message"

@Composable
fun MyMessageRoot(
    requiredPermissionsState: RequiredPermissionsState,
    mainViewModel: MainViewModel,
    onRequestDefaultSms: () -> Unit
) {
    val navController = rememberNavController()
    val pendingAddress by mainViewModel.pendingAddress.collectAsState()
    val messageDraft by mainViewModel.messageDraft.collectAsState()
    val contacts by mainViewModel.contacts.collectAsState()
    val conversations by mainViewModel.conversations.collectAsState()
    val messages by mainViewModel.messages.collectAsState()
    val isDefaultSmsApp by mainViewModel.isDefaultSmsApp.collectAsState()
    val selectedThreadId by mainViewModel.selectedThreadId.collectAsState()

    LaunchedEffect(pendingAddress) {
        pendingAddress?.let { address ->
            navController.navigate(RouteNewMessage) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(selectedThreadId) {
        selectedThreadId?.let { threadId ->
            navController.navigate("conversation/$threadId") {
                launchSingleTop = true
            }
        }
    }

    val showPermissionDialog = remember { mutableStateOf(false) }

    if (!requiredPermissionsState.allGranted && showPermissionDialog.value) {
        PermissionsDialog(
            state = requiredPermissionsState,
            message = stringResource(id = R.string.permission_rationale),
            confirmLabel = stringResource(id = R.string.default_sms_confirm)
        ) {
            showPermissionDialog.value = false
        }
    }

    MyMessageScaffold(
        navController = navController,
        conversations = conversations,
        contacts = contacts,
        messages = messages,
        messageDraft = messageDraft,
        isDefaultSmsApp = isDefaultSmsApp,
        onConversationSelected = {
            mainViewModel.selectThread(it.threadId)
        },
        onContactSelected = { contact ->
            mainViewModel.openConversationForAddress(contact.numbers.firstOrNull().orEmpty())
        },
        onComposeRequested = {
            navController.navigate(RouteNewMessage)
        },
        onSendExistingMessage = { address, body ->
            mainViewModel.sendMessage(address, body)
        },
        onSendNewMessage = { address, body ->
            mainViewModel.sendMessage(address, body)
            mainViewModel.clearPendingAddress()
            navController.popBackStack()
        },
        onDraftChanged = mainViewModel::updateDraft,
        pendingAddress = pendingAddress,
        onRequestPermissions = {
            showPermissionDialog.value = true
        },
        requiredPermissionsState = requiredPermissionsState,
        onRequestDefaultSms = onRequestDefaultSms,
        onClearPendingAddress = mainViewModel::clearPendingAddress
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyMessageScaffold(
    navController: NavHostController,
    conversations: List<ConversationSummary>,
    contacts: List<Contact>,
    messages: List<ConversationMessage>,
    messageDraft: String,
    isDefaultSmsApp: Boolean,
    onConversationSelected: (ConversationSummary) -> Unit,
    onContactSelected: (Contact) -> Unit,
    onComposeRequested: () -> Unit,
    onSendExistingMessage: (String, String) -> Unit,
    onSendNewMessage: (String, String) -> Unit,
    onDraftChanged: (String) -> Unit,
    pendingAddress: String?,
    onRequestPermissions: () -> Unit,
    requiredPermissionsState: RequiredPermissionsState,
    onRequestDefaultSms: () -> Unit,
    onClearPendingAddress: () -> Unit
) {
    val tabs = listOf(
        BottomNavItem(
            route = RouteInbox,
            titleRes = R.string.inbox_title,
            selectedIcon = Icons.Filled.Message,
            unselectedIcon = Icons.Outlined.Message
        ),
        BottomNavItem(
            route = RouteContacts,
            titleRes = R.string.contacts_title,
            selectedIcon = Icons.Filled.Contacts,
            unselectedIcon = Icons.Outlined.Contacts
        )
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: RouteInbox

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(RouteInbox, RouteContacts)) {
                NavigationBar {
                    tabs.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentRoute == item.route) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(id = item.titleRes)
                                )
                            },
                            label = { Text(text = stringResource(id = item.titleRes)) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = RouteInbox,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                composable(RouteInbox) {
                    InboxScreen(
                        conversations = conversations,
                        onConversationClick = onConversationSelected,
                        onComposeClick = onComposeRequested,
                        requiredPermissionsState = requiredPermissionsState,
                        onRequestPermissions = onRequestPermissions,
                        onRequestDefaultSms = onRequestDefaultSms,
                        isDefaultSmsApp = isDefaultSmsApp
                    )
                }
                composable(RouteContacts) {
                    ContactsScreen(
                        contacts = contacts,
                        onContactSelected = onContactSelected,
                        requiredPermissionsState = requiredPermissionsState,
                        onRequestPermissions = onRequestPermissions
                    )
                }
                composable(RouteConversation) { backStackEntry ->
                    val threadId = backStackEntry.arguments?.getString("threadId")?.toLongOrNull()
                    ConversationScreen(
                        threadId = threadId,
                        messages = messages,
                        onBack = { navController.popBackStack() },
                        onSend = onSendExistingMessage,
                        draft = messageDraft,
                        onDraftChanged = onDraftChanged
                    )
                }
                composable(RouteNewMessage) {
                    ComposeMessageSheet(
                        draft = messageDraft,
                        initialAddress = pendingAddress,
                        contacts = contacts,
                        onDraftChanged = onDraftChanged,
                        onSend = onSendNewMessage,
                        onDismiss = {
                            navController.popBackStack()
                            onDraftChanged("")
                            onClearPendingAddress()
                        }
                    )
                }
            }
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val titleRes: Int,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)
