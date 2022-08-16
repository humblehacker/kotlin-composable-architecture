// ktlint-disable filename
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
import arrow.optics.optics
import composablearchitecture.Reducer
import composablearchitecture.android.ComposableStore
import composablearchitecture.android.WithViewStore
import composablearchitecture.withNoEffect
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.parcelize.Parcelize

private val readMe = """
This screen demonstrates the basics of the Composable Architecture in an archetypal counter \
application.

The domain of the application is modeled using simple data types that correspond to the mutable \
state of the application and any actions that can affect that state or the outside world.
""".replace("\\\n", "")

@optics
@Parcelize
@Immutable
data class CounterState(
    val count: Int = 0
) : Parcelable {
    companion object
}

sealed class CounterAction {
    object DecrementButtonTapped : CounterAction()
    object IncrementButtonTapped : CounterAction()

    override fun toString(): String {
        return when (this) {
            DecrementButtonTapped -> "CounterAction.DecrementButtonTapped"
            IncrementButtonTapped -> "CounterAction.IncrementButtonTapped"
        }
    }
}

class CounterEnvironment

val counterReducer = Reducer<CounterState, CounterAction, CounterEnvironment> { state, action, _ ->
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

@Composable
fun CounterView(
    store: ComposableStore<CounterState, CounterAction>,
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

@Composable
fun CounterDemoView(title: String, store: ComposableStore<CounterState, CounterAction>) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = title) }) },
        backgroundColor = Color(0xF0F0F0FF)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Top) {
            MarkdownText(readMe, style = MaterialTheme.typography.caption)

            Spacer(modifier = Modifier.height(16.dp))

            Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                CounterView(store = store, horizontalArrangement = Arrangement.Center)
            }
        }
    }
}
