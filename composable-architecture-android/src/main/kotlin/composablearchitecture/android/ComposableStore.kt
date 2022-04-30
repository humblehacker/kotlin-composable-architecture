package composablearchitecture.android

import androidx.compose.runtime.Stable
import arrow.optics.Lens
import arrow.optics.Prism
import composablearchitecture.Reducer
import composablearchitecture.Result
import composablearchitecture.Store
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

@Stable
class ComposableStore<State, Action> private constructor(val store: Store<State, Action>) {
    private constructor(
        initialState: State,
        reducer: (State, Action) -> Result<State, Action>,
        mainDispatcher: CoroutineDispatcher
    ) : this(Store(initialState, reducer, mainDispatcher))

    val currentState get() = store.currentState
    val states: Flow<State> get() = store.states
    fun send(action: Action) = store.send(action)
    fun <LocalState, LocalAction> scope(
        toLocalState: Lens<State, LocalState>,
        fromLocalAction: Prism<Action, LocalAction>,
        coroutineScope: CoroutineScope
    ): ComposableStore<LocalState, LocalAction> = ComposableStore(store.scope(toLocalState, fromLocalAction, coroutineScope))
    fun <LocalState, LocalAction> scope(
        state: Lens<State, LocalState>,
        action: Prism<Action, LocalAction>
    ): ComposableStore<LocalState, LocalAction> = ComposableStore(store = store.scope(state, action))

    companion object {
        operator fun <State, Action, Environment> invoke(
            initialState: State,
            reducer: Reducer<State, Action, Environment>,
            environment: Environment,
            mainDispatcher: CoroutineDispatcher = Dispatchers.Main
        ): ComposableStore<State, Action> =
            ComposableStore(
                initialState,
                { state, action -> reducer.run(state, action, environment) },
                mainDispatcher
            )
    }
}
