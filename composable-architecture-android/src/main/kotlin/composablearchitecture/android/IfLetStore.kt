package composablearchitecture.android

import androidx.compose.runtime.*

@Composable
fun <State : Any, Action> IfLetStore(
    store: ComposableStore<State?, Action>,
    then: (@Composable (store: ComposableStore<State, Action>) -> Unit),
    `else`: (@Composable () -> Unit) = {}
) {
    WithViewStore(store = store) { viewStore ->
        if (viewStore.state != null) {
            @Suppress("UNCHECKED_CAST")
            then(store as ComposableStore<State, Action>)
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
