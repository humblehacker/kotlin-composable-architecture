package composablearchitecture.android

import androidx.compose.runtime.*
import composablearchitecture.Store

@Composable
fun <State : Any, Action> IfLetStore(
    store: Store<State?, Action>,
    then: (@Composable (store: Store<State, Action>) -> Unit),
    `else`: (@Composable () -> Unit) = {}
) {
    WithViewStore(store = store) { viewStore ->
        if (viewStore.state != null) {
            then(store as Store<State, Action>)
        } else {
            `else`()
        }
    }
}

@Composable
fun <State : Any, Action> IfLetStore(
    store: Store<State?, Action>,
    then: (@Composable (store: Store<State, Action>) -> Unit)
) = IfLetStore(store, then, {})
