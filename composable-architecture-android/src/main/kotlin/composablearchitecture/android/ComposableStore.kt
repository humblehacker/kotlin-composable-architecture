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
class ComposableStore<State, Action> private constructor(
    val store: Store<State, Action>,
    val navigateTo: (route: String, onDismiss: () -> Unit) -> Unit,
    val popBackStack: () -> Unit
) {
    private constructor(
        initialState: State,
        reducer: (State, Action) -> Result<State, Action>,
        mainDispatcher: CoroutineDispatcher,
        navigateTo: (route: String, onDismiss: () -> Unit) -> Unit,
        popBackStack: () -> Unit
    ) : this(Store(initialState, reducer, mainDispatcher), navigateTo, popBackStack)

    val currentState get() = store.currentState
    val states: Flow<State> get() = store.states
    fun send(action: Action) = store.send(action)

    fun <LocalState, LocalAction> scope(
        state: (State) -> LocalState,
        action: (LocalAction) -> Action
    ): ComposableStore<LocalState, LocalAction> =
        ComposableStore(
            store = store.scope(state, action, CoroutineScope(Dispatchers.Main)),
            navigateTo = navigateTo,
            popBackStack = popBackStack
        )

    fun <LocalState, LocalAction> scope(
        state: Lens<State, LocalState>,
        action: Prism<Action, LocalAction>
    ): ComposableStore<LocalState, LocalAction> =
        ComposableStore(
            store = store.scope(state, action, CoroutineScope(Dispatchers.Main)),
            navigateTo = navigateTo,
            popBackStack = popBackStack
        )

    fun <LocalState> scope(
        state: (State) -> LocalState,
    ): ComposableStore<LocalState, Action> {
        return ComposableStore(
            store = store.scope(state, { it }, CoroutineScope(Dispatchers.Main)),
            navigateTo = navigateTo,
            popBackStack = popBackStack
        )
    }

    fun <LocalState> scope(
        state: Lens<State, LocalState>
    ): ComposableStore<LocalState, Action> {
        return ComposableStore(
            store = store.scope(state, CoroutineScope(Dispatchers.Main)),
            navigateTo = navigateTo,
            popBackStack = popBackStack
        )
    }

    companion object {
        operator fun <State, Action, Environment> invoke(
            initialState: State,
            reducer: Reducer<State, Action, Environment>,
            environment: Environment,
            navigateTo: (route: String, onDismiss: () -> Unit) -> Unit,
            popBackStack: () -> Unit,
            mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
        ): ComposableStore<State, Action> =
            ComposableStore(
                initialState,
                { state, action -> reducer.run(state, action, environment) },
                mainDispatcher,
                navigateTo,
                popBackStack
            )
    }
}
