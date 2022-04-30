// ktlint-disable filename
package composablearchitecture.example.casestudies.jetpackcompose

import android.os.Parcelable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import arrow.optics.optics
import composablearchitecture.Reducer
import composablearchitecture.android.ComposableStore
import composablearchitecture.android.WithViewStore
import composablearchitecture.withNoEffect
import kotlinx.parcelize.Parcelize

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
fun CounterView(store: ComposableStore<CounterState, CounterAction>) {
    WithViewStore(store) { viewStore ->
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { viewStore.send(CounterAction.DecrementButtonTapped) }) { Text("-") }

            Spacer(Modifier.width(8.dp))

            Text("${viewStore.state.count}")

            Spacer(Modifier.width(8.dp))

            Button(onClick = { viewStore.send(CounterAction.IncrementButtonTapped) }) { Text("+") }
        }
    }
}
