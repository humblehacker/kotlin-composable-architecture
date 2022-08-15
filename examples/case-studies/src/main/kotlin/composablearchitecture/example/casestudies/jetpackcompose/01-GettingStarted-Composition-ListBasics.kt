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
import arrow.core.left
import arrow.core.right
import arrow.optics.PPrism
import arrow.optics.Prism
import arrow.optics.optics
import composablearchitecture.Reducer
import composablearchitecture.android.ComposableStore
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.parcelize.Parcelize

private val readMe: String = """
This screen demonstrates how to take small features and compose them into a list (`LazyColumn`, `LazyRow`, etc.)
using the `eachStore` operator in `LazyListScope`s, the `pullback` and `combine` operators on reducers, 
and the `scope` operator on stores.
""".replace("\\\n", "")

@optics
@Parcelize
@Immutable
data class ListBasicsState(
    val counter1: CounterState = CounterState(),
    val counter2: CounterState = CounterState()
) : Parcelable {
    companion object
}

sealed class ListBasicsAction {
    class Counter1(val action: CounterAction) : ListBasicsAction()
    class Counter2(val action: CounterAction) : ListBasicsAction()

    companion object {
        val counter1Action: Prism<ListBasicsAction, CounterAction> = PPrism(
            getOrModify = { action ->
                when (action) {
                    is Counter1 -> action.action.right()
                    else -> action.left()
                }
            },
            reverseGet = { action ->
                Counter1(action)
            }
        )

        val counter2Action: Prism<ListBasicsAction, CounterAction> = PPrism(
            getOrModify = { action ->
                when (action) {
                    is Counter2 -> action.action.right()
                    else -> action.left()
                }
            },
            reverseGet = { action ->
                Counter2(action)
            }
        )
    }
}

class ListBasicsEnvironment

val listBasicsReducer = Reducer.combine(
    counterReducer.pullback<ListBasicsState, ListBasicsAction, ListBasicsEnvironment>(
        toLocalState = ListBasicsState.counter1,
        toLocalAction = ListBasicsAction.counter1Action,
        toLocalEnvironment = { CounterEnvironment() }
    ),
    counterReducer.pullback(
        toLocalState = ListBasicsState.counter2,
        toLocalAction = ListBasicsAction.counter2Action,
        toLocalEnvironment = { CounterEnvironment() }
    )
)

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
                Column {
                    CounterRow(
                        "Counter 1",
                        store.scope(
                            state = ListBasicsState.counter1,
                            action = ListBasicsAction.counter1Action
                        )
                    )

                    Divider(color = Color.LightGray, thickness = 0.5.dp)

                    CounterRow(
                        "Counter 2",
                        store.scope(
                            state = ListBasicsState.counter2,
                            action = ListBasicsAction.counter2Action
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
