// ktlint-disable filename
package composablearchitecture.android

import android.os.Parcelable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.parcelize.Parcelize
import java.util.*

object Alert {
    @Parcelize
    data class State<Action>(
        val id: UUID = UUID.randomUUID(),
        val buttons: List<Button<Action>>,
        val message: TextState? = null,
        val title: TextState
    ) : Parcelable {

        @Suppress("unused")
        constructor(
            title: TextState,
            message: TextState? = null,
            dismissButton: Button<Action>? = null
        ) : this(
            title = title,
            message = message,
            buttons = dismissButton?.let { listOf(it) } ?: listOf()
        )

        @Suppress("unused")
        constructor(
            title: TextState,
            message: TextState? = null,
            primaryButton: Button<Action>,
            secondaryButton: Button<Action>
        ) : this(
            title = title,
            message = message,
            buttons = listOf(primaryButton, secondaryButton)
        )
    }

    enum class ButtonRole {
        Cancel,
        Destructive
    }

    @Parcelize
    class Button<Action>(
        val action: ButtonAction<Action>?,
        val label: TextState,
        val role: ButtonRole?
    ) : Parcelable {
        companion object {

            fun <Action> cancel(
                label: TextState,
                action: ButtonAction<Action>? = null
            ): Button<Action> {
                return Button(action = action, label = label, role = ButtonRole.Cancel)
            }

            fun <Action> default(
                label: TextState,
                action: ButtonAction<Action>? = null
            ): Button<Action> {
                return Button(action = action, label = label, role = null)
            }

            fun <Action> destructive(
                label: TextState,
                action: ButtonAction<Action>? = null
            ): Button<Action> {
                return Button(action = action, label = label, role = ButtonRole.Destructive)
            }
        }
    }

    @Parcelize
    class ButtonAction<Action>(val type: ActionType) : Parcelable {
        companion object {
            fun <Action : Parcelable> send(action: Action): ButtonAction<Action> {
                return ButtonAction(ActionType.Send(action))
            }
            // TODO: animatedSend if it makes sense for Compose
        }
    }

    sealed class ActionType : Parcelable {
        @Parcelize
        class Send<Action : Parcelable>(val action: Action) : ActionType()
        // TODO: AnimatedSend if it makes sense for Compose
    }
}

@Composable
fun <Action> Alert(
    store: ComposableStore<Alert.State<Action>?, Action>,
    dismiss: Action
) {
    IfLetStore(store) { storeWithState ->
        WithViewStore(storeWithState) { viewStore ->
            AlertDialog(
                onDismissRequest = { viewStore.send(dismiss) },
                buttons = {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp, bottom = 8.dp)
                    ) {
                        // TODO: handle button role
                        if (viewStore.state.buttons.isEmpty()) {
                            TextButton(onClick = { viewStore.send(dismiss) }) {
                                Text("OK")
                            }
                        } else {
                            viewStore.state.buttons.forEach {
                                TextButton(onClick = it.action.onClick(storeWithState, dismiss)) {
                                    Text(it.label.text)
                                }
                            }
                        }
                    }
                },
                title = { Text(viewStore.state.title.text) },
                text = viewStore.state.message?.let { { Text(it.text) } }
            )
        }
    }
}

private fun <Action> Alert.ButtonAction<Action>?.onClick(
    store: ComposableStore<Alert.State<Action>, Action>,
    dismiss: Action
): () -> Unit {
    val type = this?.type ?: return { store.send(dismiss) }

    return when (type) {
        is Alert.ActionType.Send<*> -> {
            { store.send(type.action as Action) }
        }
        else -> {
            { store.send(dismiss) }
        }
    }
}
