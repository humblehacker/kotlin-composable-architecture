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
    items(store.currentState) { item ->
        itemContent(
            store.scope(
                state = {
                    val childState = store.currentState.find { it.id == item.id }
                    // NB: We use cached `item` here as a fallback to avoid a potential crash
                    // where Compose may re-compose views for elements no longer in the collection.
                    return@scope childState ?: item
                },
                action = { localAction -> (item.id to localAction) }
            )
        )
    }
}
