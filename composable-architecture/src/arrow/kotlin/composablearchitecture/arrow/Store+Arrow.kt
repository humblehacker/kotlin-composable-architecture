package composablearchitecture.arrow

import arrow.optics.Lens
import arrow.optics.Prism
import composablearchitecture.Store
import kotlinx.coroutines.CoroutineScope

fun <GlobalState, GlobalAction, LocalState, LocalAction> Store<GlobalState, GlobalAction>.scope(
    toLocalState: Lens<GlobalState, LocalState>,
    fromLocalAction: Prism<GlobalAction, LocalAction>,
    coroutineScope: CoroutineScope
): Store<LocalState, LocalAction> {
    return scope(
        toLocalState = { state -> toLocalState.get(state) },
        fromLocalAction = { action -> fromLocalAction.reverseGet(action) },
        coroutineScope = coroutineScope
    )
}

fun <GlobalState, GlobalAction, LocalState> Store<GlobalState, GlobalAction>.scope(
    toLocalState: Lens<GlobalState, LocalState>,
    coroutineScope: CoroutineScope
): Store<LocalState, GlobalAction> {
    return scope(
        toLocalState = { state -> toLocalState.get(state) },
        coroutineScope = coroutineScope
    )
}
