package composablearchitecture.example.casestudies.jetpackcompose

import android.os.Parcelable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import arrow.optics.optics
import composablearchitecture.Reducer
import composablearchitecture.android.*
import composablearchitecture.withNoEffect
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.parcelize.Parcelize

private val readMe: String = """
This demonstrates how to best handle alerts and confirmation dialogs in the Composable \
Architecture.

The library comes with two types, `AlertState` and `ConfirmationDialogState`, which can \
be constructed from reducers and control whether or not an alert or confirmation dialog is \
displayed. Further, it automatically handles sending actions when you tap their buttons, which \
allows you to properly handle their functionality in the reducer.

The benefit of doing this is that you can get full test coverage on how a user interacts with \
alerts and dialogs in your application
""".replace("\\\n", "")

// TODO: Add ConfirmationDialog

@optics
@Parcelize
@Immutable
data class AlertAndConfirmationDialogState(
    val alert: Alert.State<AlertAndConfirmationDialogAction>? = null,
    // val confirmationDialog: ConfirmationDialogState<AlertAndConfirmationDialogAction>?,
    val count: Int = 0
) : Parcelable {
    companion object
}

sealed class AlertAndConfirmationDialogAction : Parcelable {
    @Parcelize object AlertButtonTapped : AlertAndConfirmationDialogAction()
    @Parcelize object AlertDismissed : AlertAndConfirmationDialogAction()
    @Parcelize object ConfirmationDialogButtonTapped : AlertAndConfirmationDialogAction()
    @Parcelize object ConfirmationDialogDismissed : AlertAndConfirmationDialogAction()
    @Parcelize object DecrementButtonTapped : AlertAndConfirmationDialogAction()
    @Parcelize object IncrementButtonTapped : AlertAndConfirmationDialogAction()

    override fun toString(): String {
        return when (this) {
            AlertButtonTapped -> "AlertAndConfirmationDialog.AlertButtonTapped"
            AlertDismissed -> "AlertAndConfirmationDialog.AlertDismissed"
            ConfirmationDialogButtonTapped -> "AlertAndConfirmationDialog.ConfirmationDialogButtonTapped"
            ConfirmationDialogDismissed -> "AlertAndConfirmationDialog.ConfirmationDialogDismissed"
            DecrementButtonTapped -> "AlertAndConfirmationDialog.DecrementButtonTapped"
            IncrementButtonTapped -> "AlertAndConfirmationDialog.IncrementButtonTapped"
        }
    }
}

class AlertAndConfirmationDialogEnvironment

val alertAndConfirmationDialogReducer =
    Reducer<AlertAndConfirmationDialogState, AlertAndConfirmationDialogAction, AlertAndConfirmationDialogEnvironment> { state, action, _ ->
        when (action) {
            AlertAndConfirmationDialogAction.AlertButtonTapped -> {
                state
                    .copy(
                        alert = Alert.State(
                            title = TextState("Alert!"),
                            message = TextState("This is an alert"),
                            primaryButton = Alert.Button.cancel(TextState("Cancel")),
                            secondaryButton = Alert.Button.default(
                                TextState("Increment"),
                                action = Alert.ButtonAction.send(AlertAndConfirmationDialogAction.IncrementButtonTapped)
                            )
                        )
                    )
                    .withNoEffect()
            }

            AlertAndConfirmationDialogAction.AlertDismissed -> {
                state.copy(alert = null).withNoEffect()
            }

            AlertAndConfirmationDialogAction.ConfirmationDialogButtonTapped -> TODO()
            AlertAndConfirmationDialogAction.ConfirmationDialogDismissed -> TODO()

            AlertAndConfirmationDialogAction.DecrementButtonTapped -> {
                state
                    .copy(
                        alert = Alert.State(title = TextState("Decremented!")),
                        count = state.count - 1
                    )
                    .withNoEffect()
            }

            AlertAndConfirmationDialogAction.IncrementButtonTapped -> {
                state
                    .copy(
                        alert = Alert.State(title = TextState("Incremented!")),
                        count = state.count + 1
                    )
                    .withNoEffect()
            }
        }
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlertAndConfirmationDialogView(
    title: String,
    store: ComposableStore<AlertAndConfirmationDialogState, AlertAndConfirmationDialogAction>
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = title) }) },
        backgroundColor = Color(0xF0F0F0FF)
    ) {
        WithViewStore(store) { viewStore ->
            Box {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Top,
                ) {

                    MarkdownText(readMe, style = MaterialTheme.typography.caption)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Count: ${viewStore.state.count}")

                    Button(onClick = { viewStore.send(AlertAndConfirmationDialogAction.AlertButtonTapped) }) {
                        Text("Alert")
                    }
                }

                Alert(
                    store = store.scope(state = AlertAndConfirmationDialogState.nullableAlert),
                    dismiss = AlertAndConfirmationDialogAction.AlertDismissed)
            }
        }
    }
}
