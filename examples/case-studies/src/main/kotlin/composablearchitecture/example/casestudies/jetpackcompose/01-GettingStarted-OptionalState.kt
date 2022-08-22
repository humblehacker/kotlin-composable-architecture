package composablearchitecture.example.casestudies.jetpackcompose

import android.os.Parcelable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import arrow.core.left
import arrow.core.right
import arrow.optics.Prism
import arrow.optics.optics
import composablearchitecture.Reducer
import composablearchitecture.android.ComposableStore
import composablearchitecture.android.IfLetStore
import composablearchitecture.android.WithViewStore
import composablearchitecture.android.arrow.scope
import composablearchitecture.arrow.pullback
import composablearchitecture.withNoEffect
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.parcelize.Parcelize

private val readMe by lazy {
    """
This screen demonstrates how to show and hide views based on the presence of some optional child \
state.

The parent state holds a `CounterState?` value. When it is `nil` we will default to a plain text \
view. But when it is non-`nil` we will show a view fragment for a counter that operates on the \
non-optional counter state.

Tapping "Toggle counter state" will flip between the `nil` and non-`nil` counter states.
""".replace("\\\n", "")
}

@optics
@Parcelize
@Immutable
data class OptionalBasicsState(
    val optionalCounter: CounterState? = null
) : Parcelable {
    companion object
}

sealed class OptionalBasicsAction {
    class OptionalCounter(val action: CounterAction) : OptionalBasicsAction()
    object ToggleCounterButtonTapped : OptionalBasicsAction()

    companion object {
        val optionalCounterAction: Prism<OptionalBasicsAction, CounterAction> = Prism(
            getOrModify = { obAction ->
                when (obAction) {
                    is OptionalCounter -> obAction.action.right()
                    else -> obAction.left()
                }
            },
            reverseGet = { action -> OptionalCounter(action) }
        )
    }
}

class OptionalBasicsEnvironment

val optionalBasicsReducer =
    counterReducer
        .optional()
        .pullback(
            toLocalState = OptionalBasicsState.nullableOptionalCounter,
            toLocalAction = OptionalBasicsAction.optionalCounterAction,
            toLocalEnvironment = { _: OptionalBasicsEnvironment -> CounterEnvironment() }
        )
        .combine(
            other = Reducer<OptionalBasicsState, OptionalBasicsAction, OptionalBasicsEnvironment> { state, action, _ ->
                when (action) {
                    is OptionalBasicsAction.ToggleCounterButtonTapped -> {
                        state
                            .copy(optionalCounter = if (state.optionalCounter == null) CounterState() else null)
                            .withNoEffect()
                    }
                    is OptionalBasicsAction.OptionalCounter -> {
                        state.withNoEffect()
                    }
                }
            }
        )

@Composable
fun OptionalBasicsView(
    title: String,
    store: ComposableStore<OptionalBasicsState, OptionalBasicsAction>
) {
    WithViewStore(store) { viewStore ->
        Scaffold(topBar = {
            TopAppBar(title = { Text(text = title) })
        }, backgroundColor = Color(0xF0F0F0FF)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Top) {

                MarkdownText(readMe, style = MaterialTheme.typography.caption)

                Spacer(modifier = Modifier.height(16.dp))

                Card(shape = RoundedCornerShape(10.dp)) {
                    Column {

                        Row {
                            Button(
                                modifier = Modifier.padding(8.dp),
                                onClick = { viewStore.send(OptionalBasicsAction.ToggleCounterButtonTapped) }
                            ) {
                                Text("Toggle counter state")
                            }
                        }

                        Divider(color = Color.LightGray, thickness = 0.5.dp)

                        Column(modifier = Modifier.padding(8.dp)) {
                            IfLetStore(
                                store.scope(
                                    state = OptionalBasicsState.nullableOptionalCounter,
                                    action = OptionalBasicsAction.optionalCounterAction
                                ),
                                then = { store ->
                                    MarkdownText("`CounterState` is non-`null`")
                                    CounterView(store)
                                },
                                `else` = {
                                    MarkdownText("`CounterState` is `null`")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
