package composablearchitecture.android

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import composablearchitecture.Identifiable

/** `eachStore` provides the ability to render within a `LazyRow` or `LazyColumn` the contents of
 *  a store containing a list of states.
 *  Example:
 *  ```kotlin
 *    LazyColumn {
 *      eachStore(
 *        store.scope(
 *          state = ListBasicsState.counters,
 *          action = ListBasicsAction.countersAction
 *        )
 *      ) { childStore ->
 *        CounterRow("Counter ${childStore.currentState.id}", childStore)
 *      }
 *    }
 *  ```
 */
inline fun <EachState : Identifiable<ID>, EachAction, ID : Any> LazyListScope.eachStore(
    store: ComposableStore<List<EachState>, Pair<ID, EachAction>>,
    crossinline itemContent: @Composable LazyItemScope.(store: ComposableStore<EachState, EachAction>) -> Unit
) {
    items(store.currentState.map { it.id }) { id ->
        itemContent(
            store.scope(
                state = { store.currentState.find { it.id == id }!! },
                action = { localAction -> (id to localAction) }
            )
        )
    }
}
