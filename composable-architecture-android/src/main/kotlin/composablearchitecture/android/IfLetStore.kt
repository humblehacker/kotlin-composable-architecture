package composablearchitecture.android

import androidx.compose.runtime.*

@Composable
fun <State : Any, Action> IfLetStore(
    store: ComposableStore<State?, Action>,
    then: (@Composable (store: ComposableStore<State, Action>) -> Unit),
    `else`: (@Composable () -> Unit)
) {
    WithViewStore(store = store) { viewStore ->
        val state = viewStore.state
        if (state != null) {
            @Suppress("UNCHECKED_CAST")
            then(store.scope { it ?: state })
        } else {
            `else`()
        }
    }
}

@Composable
fun <State : Any, Action> IfLetStore(
    store: ComposableStore<State?, Action>,
    then: (@Composable (store: ComposableStore<State, Action>) -> Unit)
) = IfLetStore(store, then, {})
