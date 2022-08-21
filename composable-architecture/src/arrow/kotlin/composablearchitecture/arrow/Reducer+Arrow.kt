package composablearchitecture.arrow

import arrow.optics.Getter
import arrow.optics.Lens
import arrow.optics.Prism
import composablearchitecture.Effect
import composablearchitecture.Reducer
import composablearchitecture.Result
import composablearchitecture.update

fun <LocalState, LocalAction, LocalEnvironment, GlobalState, GlobalAction, GlobalEnvironment> Reducer<LocalState, LocalAction, LocalEnvironment>.pullback(
    toLocalState: Lens<GlobalState, LocalState>,
    toLocalAction: Prism<GlobalAction, LocalAction>,
    toLocalEnvironment: (GlobalEnvironment) -> LocalEnvironment
): Reducer<GlobalState, GlobalAction, GlobalEnvironment> =
    Reducer { globalState, globalAction, globalEnvironment ->
        toLocalAction.getOrModify(globalAction).fold(
            { Result(globalState, Effect.none()) },
            { localAction ->
                val (state, effect) = reducer(
                    toLocalState.get(globalState),
                    localAction,
                    toLocalEnvironment(globalEnvironment)
                )
                Result(
                    toLocalState.set(globalState, state),
                    effect.map(toLocalAction::reverseGet)
                )
            }
        )
    }

fun <LocalState, LocalAction, LocalEnvironment, GlobalState, GlobalAction, GlobalEnvironment> Reducer<LocalState, LocalAction, LocalEnvironment>.forEach(
    toLocalState: Lens<GlobalState, List<LocalState>>,
    toLocalAction: Prism<GlobalAction, Pair<Int, LocalAction>>,
    toLocalEnvironment: (GlobalEnvironment) -> LocalEnvironment
): Reducer<GlobalState, GlobalAction, GlobalEnvironment> =
    Reducer { globalState, globalAction, globalEnvironment ->
        toLocalAction.getOrModify(globalAction).fold(
            { Result(globalState, Effect.none()) },
            { (index, localAction) ->
                val localState = toLocalState.get(globalState)
                val (state, effect) = reducer(
                    localState[index],
                    localAction,
                    toLocalEnvironment(globalEnvironment)
                )
                Result(
                    toLocalState.set(globalState, localState.update(index, state)),
                    effect.map { toLocalAction.reverseGet(index to localAction) }
                )
            }
        )
    }

fun <LocalState, LocalAction, LocalEnvironment, GlobalState, GlobalAction, GlobalEnvironment, ID> Reducer<LocalState, LocalAction, LocalEnvironment>.forEach(
    toLocalState: Lens<GlobalState, List<LocalState>>,
    toLocalAction: Prism<GlobalAction, Pair<ID, LocalAction>>,
    toLocalEnvironment: (GlobalEnvironment) -> LocalEnvironment,
    idGetter: Getter<LocalState, ID>
): Reducer<GlobalState, GlobalAction, GlobalEnvironment> =
    Reducer { globalState, globalAction, globalEnvironment ->
        toLocalAction.getOrModify(globalAction).fold(
            { Result(globalState, Effect.none()) },
            { (id, localAction) ->
                val localState = toLocalState.get(globalState)
                val index = localState.indexOfFirst { idGetter.get(it) == id }
                if (index < 0) {
                    Result(globalState, Effect.none())
                } else {
                    val (state, effect) = reducer(
                        localState[index],
                        localAction,
                        toLocalEnvironment(globalEnvironment)
                    )
                    Result(
                        toLocalState.set(globalState, localState.update(index, state)),
                        effect.map { toLocalAction.reverseGet(id to localAction) }
                    )
                }
            }
        )
    }

