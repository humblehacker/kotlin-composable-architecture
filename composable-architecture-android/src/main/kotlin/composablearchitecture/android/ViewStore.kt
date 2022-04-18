package composablearchitecture.android

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import composablearchitecture.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

class ViewStore<State, Action> internal constructor(
    private val store: Store<State, Action>,
    private val mutableState: MutableState<State>,
    private val coroutineScope: CoroutineScope
) : CoroutineScope {
    val state: State get() = mutableState.value

    fun send(action: Action) {
        store.send(action)
    }

    override val coroutineContext: CoroutineContext
        get() = coroutineScope.coroutineContext

    companion object {
        @Composable
        operator fun <State, Action> invoke(store: Store<State, Action>): ViewStore<State, Action> {
            val state: MutableState<State> = remember { mutableStateOf(store.currentState) }
            val scope = rememberCoroutineScope()

            LaunchedEffect("Store") {
                store.states.onEach { state.value = it }.collect()
            }

            return ViewStore(store, state, scope)
        }
    }
}
