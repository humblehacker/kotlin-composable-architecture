package composablearchitecture.example.casestudies.jetpackcompose

import android.os.Parcelable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import composablearchitecture.android.ComposableStore
import composablearchitecture.Identifiable
import composablearchitecture.StateMap
import composablearchitecture.android.WithViewStore
import composablearchitecture.android.eachStore
import composablearchitecture.debug
import composablearchitecture.withNoEffect
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.parcelize.Parcelize

private val readMe: String = """
This screen demonstrates how to take small features and compose them into a list (`LazyColumn`, `LazyRow`, etc.)
using the `eachStore` operator in `LazyListScope`s, the `pullback` and `combine` operators on reducers, 
and the `scope` operator on stores.
""".replace("\\\n", "")

@Parcelize
data class IdentifiedCounterState(
    override val id: String,
    val count: Int = 0
) : Parcelable, Identifiable<String> {
    companion object
}

@Parcelize
@Immutable
data class ListBasicsState(
    val counters: List<IdentifiedCounterState> = listOf(
        IdentifiedCounterState("A"),
        IdentifiedCounterState("B"),
        IdentifiedCounterState("C"),
        IdentifiedCounterState("D"),
    )
) : Parcelable {
    companion object {
        val countersState: StateMap<ListBasicsState, List<IdentifiedCounterState>> = StateMap(
            toLocal = { it.counters },
            fromLocal = { ls, gs -> gs.copy(counters = ls) }
        )
    }
}

sealed class ListBasicsAction {
    class Counter(val id: String, val action: CounterAction) : ListBasicsAction()

    override fun toString(): String {
        return when (this) {
            is Counter -> "ListBasicsAction.Counter(id: $id, action: $action)"
        }
    }

    companion object {
        val countersAction: ActionMap<ListBasicsAction, Pair<String, CounterAction>> = ActionMap(
            toLocal = { if (it is Counter) (it.id to it.action) else null },
            fromLocal = { (id, action) -> Counter(id, action) }
        )
    }
}

class ListBasicsEnvironment

val identifiedCounterReducer =
    Reducer<IdentifiedCounterState, CounterAction, CounterEnvironment> { state, action, _ ->
        when (action) {

            CounterAction.DecrementButtonTapped -> {
                state
                    .copy(count = state.count - 1)
                    .withNoEffect()
            }

            CounterAction.IncrementButtonTapped -> {
                state
                    .copy(count = state.count + 1)
                    .withNoEffect()
            }
        }
    }

val listBasicsReducer = Reducer.combine<ListBasicsState, ListBasicsAction, ListBasicsEnvironment>(
    identifiedCounterReducer
        .forEach(
            stateMap = ListBasicsState.countersState,
            actionMap = ListBasicsAction.countersAction,
            toLocalEnvironment = { CounterEnvironment() },
            idGetter = { it.id }
        )
).debug()

@Composable
fun ListBasicsView(title: String, store: ComposableStore<ListBasicsState, ListBasicsAction>) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = title) }) },
        backgroundColor = Color(0xF0F0F0FF)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Top) {
            MarkdownText(readMe, style = MaterialTheme.typography.caption)

            Spacer(modifier = Modifier.height(16.dp))

            Card(shape = RoundedCornerShape(10.dp)) {
                LazyColumn {
                    eachStore(
                        store.scope(
                            stateMap = ListBasicsState.countersState,
                            actionMap = ListBasicsAction.countersAction
                        )
                    ) { childStore ->
                        CounterRow("Counter ${childStore.currentState.id}", childStore)
                    }
                }
            }
        }
    }
}

@Composable
private fun CounterRow(title: String, store: ComposableStore<IdentifiedCounterState, CounterAction>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Text(title)
        IdentifiedCounterView(store = store)
    }
}

@Composable
fun IdentifiedCounterView(
    store: ComposableStore<IdentifiedCounterState, CounterAction>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start
) {
    WithViewStore(store) { viewStore ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = horizontalArrangement,
            modifier = modifier
        ) {
            Button(onClick = { viewStore.send(CounterAction.DecrementButtonTapped) }) { Text("-") }

            Spacer(Modifier.width(8.dp))

            Text("${viewStore.state.count}")

            Spacer(Modifier.width(8.dp))

            Button(onClick = { viewStore.send(CounterAction.IncrementButtonTapped) }) { Text("+") }
        }
    }
}
