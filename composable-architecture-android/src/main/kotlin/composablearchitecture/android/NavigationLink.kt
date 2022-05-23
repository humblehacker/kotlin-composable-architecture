package composablearchitecture.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <State, Action> ViewStore<State, Action>.NavigationLink(
    destination: String,
    isActive: Boolean,
    sendIsActive: (Boolean) -> Action,
    content: @Composable (() -> Unit)? = null
) {
    content?.let {
        Column(modifier = Modifier.clickable { send(sendIsActive(true)) }) {
            it()
        }
    }

    If(isActive) {
        navigateTo(destination, onDismiss = { this.send(sendIsActive(false)) })
    }
}

@Composable fun If(condition: Boolean, then: () -> Unit) {
    if (condition) {
        then()
    }
}
