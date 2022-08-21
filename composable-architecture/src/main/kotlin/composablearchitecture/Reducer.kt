package composablearchitecture

data class Result<out State, Action>(val state: State, val effect: Effect<Action>)

class Reducer<State, Action, Environment>(
    val reducer: (State, Action, Environment) -> Result<State, Action>
) {
    companion object {
        fun <State, Action, Environment> combine(vararg reducers: Reducer<State, Action, Environment>) =
            Reducer<State, Action, Environment> { value, action, environment ->
                reducers.fold(Result(value, Effect.none())) { result, reducer ->
                    val (currentValue, currentEffect) = result
                    val (newValue, newEffect) = reducer.run(currentValue, action, environment)
                    currentEffect.merge(newEffect)
                    Result(newValue, currentEffect)
                }
            }
    }

    fun run(
        state: State,
        action: Action,
        environment: Environment
    ): Result<State, Action> = reducer(state, action, environment)

    fun combine(other: Reducer<State, Action, Environment>) = combine(this, other)

    fun <GlobalState, GlobalAction, GlobalEnvironment> pullback(
        stateMap: StateMap<GlobalState, State>,
        actionMap: ActionMap<GlobalAction, Action>,
        toLocalEnvironment: (GlobalEnvironment) -> Environment
    ): Reducer<GlobalState, GlobalAction, GlobalEnvironment> = pullback(
        toLocalState = stateMap.toLocal,
        fromLocalState = stateMap.fromLocal,
        toLocalAction = actionMap.toLocal,
        fromLocalAction = actionMap.fromLocal,
        toLocalEnvironment = toLocalEnvironment
    )

    fun <GlobalState, GlobalAction, GlobalEnvironment> pullback(
        toLocalState: (GlobalState) -> State,
        fromLocalState: (State, GlobalState) -> GlobalState,
        toLocalAction: (GlobalAction) -> Action?,
        fromLocalAction: (Action) -> GlobalAction,
        toLocalEnvironment: (GlobalEnvironment) -> Environment
    ): Reducer<GlobalState, GlobalAction, GlobalEnvironment> =
        Reducer { globalState, globalAction, globalEnvironment ->
            val (localState, effect) = reducer(
                toLocalState(globalState),
                toLocalAction(globalAction) ?: return@Reducer globalState.withNoEffect(),
                toLocalEnvironment(globalEnvironment)
            )
            Result(
                fromLocalState(localState, globalState),
                effect.map(fromLocalAction)
            )
        }

    fun <GlobalState, GlobalAction, GlobalEnvironment> forEach(
        stateMap: StateMap<GlobalState, List<State>>,
        actionMap: ActionMap<GlobalAction, Pair<Int, Action>>,
        toLocalEnvironment: (GlobalEnvironment) -> Environment
    ): Reducer<GlobalState, GlobalAction, GlobalEnvironment> = forEach(
        toLocalState = stateMap.toLocal,
        fromLocalState = stateMap.fromLocal,
        toLocalAction = actionMap.toLocal,
        fromLocalAction = actionMap.fromLocal,
        toLocalEnvironment = toLocalEnvironment
    )

    fun <GlobalState, GlobalAction, GlobalEnvironment> forEach(
        toLocalState: (GlobalState) -> List<State>,
        fromLocalState: (List<State>, GlobalState) -> GlobalState,
        toLocalAction: (GlobalAction) -> Pair<Int, Action>?,
        fromLocalAction: (Pair<Int, Action>) -> GlobalAction,
        toLocalEnvironment: (GlobalEnvironment) -> Environment
    ): Reducer<GlobalState, GlobalAction, GlobalEnvironment> =
        Reducer { globalState, globalAction, globalEnvironment ->
            val (index, localAction) = toLocalAction(globalAction) ?: return@Reducer globalState.withNoEffect()
            val localState = toLocalState(globalState)
            val (state, effect) = reducer(
                localState[index],
                localAction,
                toLocalEnvironment(globalEnvironment)
            )
            Result(
                fromLocalState(localState.update(index, state), globalState),
                effect.map { fromLocalAction((index to it)) }
            )
        }

    fun <GlobalState, GlobalAction, GlobalEnvironment, ID> forEach(
        stateMap: StateMap<GlobalState, List<State>>,
        actionMap: ActionMap<GlobalAction, Pair<ID, Action>>,
        toLocalEnvironment: (GlobalEnvironment) -> Environment,
        idGetter: (State) -> ID
    ): Reducer<GlobalState, GlobalAction, GlobalEnvironment> = forEach(
        toLocalState = stateMap.toLocal,
        fromLocalState = stateMap.fromLocal,
        toLocalAction = actionMap.toLocal,
        fromLocalAction = actionMap.fromLocal,
        toLocalEnvironment = toLocalEnvironment,
        idGetter = idGetter
    )

    fun <GlobalState, GlobalAction, GlobalEnvironment, ID> forEach(
        toLocalState: (GlobalState) -> List<State>,
        fromLocalState: (List<State>, GlobalState) -> GlobalState,
        toLocalAction: (GlobalAction) -> Pair<ID, Action>?,
        fromLocalAction: (Pair<ID, Action>) -> GlobalAction,
        toLocalEnvironment: (GlobalEnvironment) -> Environment,
        idGetter: (State) -> ID
    ): Reducer<GlobalState, GlobalAction, GlobalEnvironment> =
        Reducer { globalState, globalAction, globalEnvironment ->
            val (id, localAction) = toLocalAction(globalAction) ?: return@Reducer globalState.withNoEffect()
            val localState = toLocalState(globalState)
            val index = localState.indexOfFirst { idGetter(it) == id }
            if (index < 0) {
                return@Reducer globalState.withNoEffect()
            }
            val (state, effect) = reducer(
                localState[index],
                localAction,
                toLocalEnvironment(globalEnvironment)
            )
            Result(
                fromLocalState(localState.update(index, state), globalState),
                effect.map { fromLocalAction((id to it)) }
            )
        }

    fun optional(): Reducer<State?, Action, Environment> = Reducer { state, action, environment ->
        if (state == null) {
            Result(null, Effect.none())
        } else {
            reducer(state, action, environment)
        }
    }
}

data class StateMap<GlobalState, LocalState>(
    val toLocal: (GlobalState) -> LocalState,
    val fromLocal: (LocalState, GlobalState) -> GlobalState
)
data class ActionMap<GlobalAction, LocalAction>(
    val toLocal: (GlobalAction) -> LocalAction?,
    val fromLocal: (LocalAction) -> GlobalAction
)
