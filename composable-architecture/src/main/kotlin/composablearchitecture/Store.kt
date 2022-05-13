package composablearchitecture

import arrow.optics.Lens
import arrow.optics.Prism
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class Store<State, Action> constructor(
    initialState: State,
    private val reducer: (State, Action) -> Result<State, Action>,
    private val mainDispatcher: CoroutineDispatcher
) {
    private val mutableState = MutableStateFlow(initialState)

    private var scopeCollectionJob: Job? = null

    val states: Flow<State> = mutableState

    val currentState: State
        get() = mutableState.value

    companion object {
        operator fun <State, Action, Environment> invoke(
            initialState: State,
            reducer: Reducer<State, Action, Environment>,
            environment: Environment,
            mainDispatcher: CoroutineDispatcher = Dispatchers.Main
        ): Store<State, Action> =
            Store(
                initialState,
                { state, action -> reducer.run(state, action, environment) },
                mainDispatcher
            )
    }

    fun <LocalState, LocalAction> scope(
        toLocalState: (State) -> LocalState,
        fromLocalAction: (LocalAction) -> Action,
        coroutineScope: CoroutineScope
    ): Store<LocalState, LocalAction> {
        val localStore = Store<LocalState, LocalAction>(
            initialState = toLocalState(mutableState.value),
            reducer = { _, localAction ->
                send(fromLocalAction(localAction))
                toLocalState(mutableState.value).withNoEffect()
            },
            mainDispatcher = mainDispatcher
        )
        localStore.scopeCollectionJob = coroutineScope.launch(Dispatchers.Unconfined) {
            mutableState.collect { newValue ->
                localStore.mutableState.value = toLocalState(newValue)
            }
        }
        return localStore
    }

    fun <LocalState, LocalAction> scope(
        toLocalState: Lens<State, LocalState>,
        fromLocalAction: Prism<Action, LocalAction>,
        coroutineScope: CoroutineScope
    ): Store<LocalState, LocalAction> {
        return scope(
            toLocalState = { state -> toLocalState.get(state) },
            fromLocalAction = { action -> fromLocalAction.reverseGet(action) },
            coroutineScope = coroutineScope
        )
    }

    fun <LocalState> scope(
        toLocalState: (State) -> LocalState,
        coroutineScope: CoroutineScope
    ): Store<LocalState, Action> {
        return scope(
            toLocalState = toLocalState,
            fromLocalAction = { it },
            coroutineScope = coroutineScope
        )
    }

    fun <LocalState> scope(
        toLocalState: Lens<State, LocalState>,
        coroutineScope: CoroutineScope
    ): Store<LocalState, Action> {
        return scope(
            toLocalState = { state -> toLocalState.get(state) },
            coroutineScope = coroutineScope
        )
    }

    fun send(action: Action) {
        val currentThread = Thread.currentThread()
        require(
            currentThread.name.startsWith("main") || currentThread.name.contains("Test worker")
        ) {
            "Sending actions from background threads is not allowed. action: $action"
        }

        val (newState, effect) = reducer(mutableState.value, action)
        mutableState.value = newState

        GlobalScope.launch(mainDispatcher) {
            try {
                effect.sink {
                    withContext(mainDispatcher) {
                        send(it)
                    }
                }
            } catch (ex: CancellationException) {
                // Ignore
            }
        }
    }

    fun cancel() {
        scopeCollectionJob?.cancel()
    }
}
