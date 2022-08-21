package composablearchitecture.example.casestudies.jetpackcompose

import android.os.Parcelable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import composablearchitecture.ActionMap
import composablearchitecture.Reducer
import composablearchitecture.StateMap
import composablearchitecture.android.ComposableStore
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.parcelize.Parcelize

private val readMe: String = """
This screen demonstrates how to take small features and compose them into bigger ones using the \
`pullback` and `combine` operators on reducers, and the `scope` operator on stores.

It reuses the the domain of the counter screen and embeds it, twice, in a larger domain.
""".replace("\\\n", "")

@Parcelize
@Immutable
data class TwoCountersState(
    val counter1: CounterState = CounterState(),
    val counter2: CounterState = CounterState()
) : Parcelable {
    companion object {
        val counter1State = StateMap<TwoCountersState, CounterState>(
            toLocal = { it.counter1 },
            fromLocal = { ls, gs -> gs.copy(counter1 = ls) }
        )
        val counter2State = StateMap<TwoCountersState, CounterState>(
            toLocal = { it.counter2 },
            fromLocal = { ls, gs -> gs.copy(counter2 = ls) }
        )
    }
}

sealed class TwoCountersAction {
    class Counter1(val action: CounterAction) : TwoCountersAction()
    class Counter2(val action: CounterAction) : TwoCountersAction()

    companion object {
        val counter1Action = ActionMap<TwoCountersAction, CounterAction>(
            toLocal = { if (it is Counter1) it.action else null },
            fromLocal = { Counter1(it) }
        )

        val counter2Action = ActionMap<TwoCountersAction, CounterAction>(
            toLocal = { if (it is Counter2) it.action else null },
            fromLocal = { Counter2(it) }
        )
    }
}

class TwoCountersEnvironment

val twoCountersReducer = Reducer.combine(
    counterReducer.pullback(
        stateMap = TwoCountersState.counter1State,
        actionMap = TwoCountersAction.counter1Action,
        toLocalEnvironment = { _: TwoCountersEnvironment -> CounterEnvironment() }
    ),
    counterReducer.pullback(
        stateMap = TwoCountersState.counter2State,
        actionMap = TwoCountersAction.counter2Action,
        toLocalEnvironment = { CounterEnvironment() }
    )
)

@Composable
fun TwoCountersView(title: String, store: ComposableStore<TwoCountersState, TwoCountersAction>) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = title) }) },
        backgroundColor = Color(0xF0F0F0FF)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Top) {

            MarkdownText(readMe, style = MaterialTheme.typography.caption)

            Spacer(modifier = Modifier.height(16.dp))

            Card(shape = RoundedCornerShape(10.dp)) {
                Column {
                    CounterRow(
                        "Counter 1",
                        store.scope(
                            toLocalState = { it.counter1 },
                            fromLocalAction = { TwoCountersAction.Counter1(it) }
                        )
                    )

                    Divider(color = Color.LightGray, thickness = 0.5.dp)

                    CounterRow(
                        "Counter 2",
                        store.scope(
                            toLocalState = { it.counter2 },
                            fromLocalAction = { TwoCountersAction.Counter2(it) }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CounterRow(title: String, store: ComposableStore<CounterState, CounterAction>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Text(title)
        CounterView(store = store)
    }
}
