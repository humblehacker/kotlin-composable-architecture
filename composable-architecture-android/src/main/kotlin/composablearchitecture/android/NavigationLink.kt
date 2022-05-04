package composablearchitecture.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/*
  Caveat: If you put a NavigationLink with empty content in a Row/Column, it can still
  affect the layout depending on the arrangement. For example, with SpaceBetween, even though
  there is no content, space will still be inserted between the empty NavigationLinks.

  Workaround is to place your view's content and the NavigationLinks inside a Box.
 */
@Composable
fun <State, Action> ViewStore<State, Action>.NavigationLink(
    destination: String,
    isActive: Boolean,
    sendIsActive: (Boolean) -> Action,
    content: @Composable () -> Unit = {}
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
