package composablearchitecture.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <State, Action> ViewStore<State, Action>.NavigationLink(
    destination: String,
    isActive: Boolean,
    sendIsActive: (Boolean) -> Action,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.clickable { send(sendIsActive(true)) }) {
        content()
    }

    if (isActive) {
        navigateTo(
            route = destination,
            onDismiss = { send(sendIsActive(false)) }
        )
    }
}
