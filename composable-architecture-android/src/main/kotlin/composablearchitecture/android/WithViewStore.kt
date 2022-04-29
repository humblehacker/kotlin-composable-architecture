package composablearchitecture.android

import androidx.compose.runtime.*
import composablearchitecture.Store

// A Composable function that transforms a store into an observable view store in order to compute views from store state
@Composable
fun <State, Action> WithViewStore(
    store: Store<State, Action>,
    content: @Composable (viewStore: ViewStore<State, Action>) -> Unit
) {
    content(ViewStore(store))
}
