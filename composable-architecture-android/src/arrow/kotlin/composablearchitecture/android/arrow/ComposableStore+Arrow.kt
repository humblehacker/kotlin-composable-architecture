package composablearchitecture.android.arrow

import arrow.optics.Lens
import arrow.optics.Prism
import composablearchitecture.arrow.scope
import composablearchitecture.android.ComposableStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

fun <GlobalState, GlobalAction, LocalState, LocalAction> ComposableStore<GlobalState, GlobalAction>.scope(
    state: Lens<GlobalState, LocalState>,
    action: Prism<GlobalAction, LocalAction>
): ComposableStore<LocalState, LocalAction> =
    ComposableStore(
        store = store.scope(state, action, CoroutineScope(Dispatchers.Main)),
        navigateTo = navigateTo,
        popBackStack = popBackStack
    )

fun <GlobalState, GlobalAction, LocalState> ComposableStore<GlobalState, GlobalAction>.scope(
    state: Lens<GlobalState, LocalState>
): ComposableStore<LocalState, GlobalAction> {
    return ComposableStore(
        store = store.scope(state, CoroutineScope(Dispatchers.Main)),
        navigateTo = navigateTo,
        popBackStack = popBackStack
    )
}

