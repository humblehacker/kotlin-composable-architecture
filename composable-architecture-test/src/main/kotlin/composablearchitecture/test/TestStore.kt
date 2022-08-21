package composablearchitecture.test

import composablearchitecture.ActionMap
import composablearchitecture.Reducer
import composablearchitecture.StateMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

internal sealed class Step<Action, State, Environment> {
    class Send<Action, State, Environment>(
        val action: Action,
        val block: (State) -> State
    ) : Step<Action, State, Environment>()

    class Receive<Action, State, Environment>(
        val action: Action,
        val block: (State) -> State
    ) : Step<Action, State, Environment>()

    class Environment<Action, State, Environment>(
        val block: (Environment) -> Unit
    ) : Step<Action, State, Environment>()

    class Do<Action, State, Environment>(
        val block: () -> Unit
    ) : Step<Action, State, Environment>()

    class AdvanceTimeBy<Action, State, Environment>(
        val delayTimeMillis: Long
    ) : Step<Action, State, Environment>()

    class AdvanceUntilIdle<Action, State, Environment> :
        Step<Action, State, Environment>()
}

class AssertionBuilder<Action, State, Environment>(private val currentState: () -> State) {

    internal val steps: MutableList<Step<Action, State, Environment>> = mutableListOf()

    fun send(action: Action, block: (State) -> State) = steps.add(Step.Send(action, block))

    fun send(action: Action) = steps.add(Step.Send(action, { currentState() }))

    fun receive(action: Action, block: (State) -> State) = steps.add(Step.Receive(action, block))

    fun receive(action: Action) = steps.add(Step.Receive(action, { currentState() }))

    fun environment(block: (Environment) -> Unit) = steps.add(Step.Environment(block))

    fun doBlock(block: () -> Unit) = steps.add(Step.Do(block))

    fun advanceTimeBy(delayTimeMillis: Long) = steps.add(Step.AdvanceTimeBy(delayTimeMillis))

    fun advanceUntilIdle() = steps.add(Step.AdvanceUntilIdle())
}

class TestStore<State, LocalState, Action, LocalAction, Environment>
private constructor(
    private var state: State,
    private val reducer: Reducer<State, Action, Environment>,
    private val environment: Environment,
    private val stateMap: StateMap<State, LocalState>,
    private val actionMap: ActionMap<Action, LocalAction>,
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    companion object {
        operator fun <State, Action, Environment> invoke(
            state: State,
            reducer: Reducer<State, Action, Environment>,
            environment: Environment,
            testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
        ) =
            TestStore(
                state,
                reducer,
                environment,
                StateMap.id(),
                ActionMap.id(),
                testDispatcher
            )
    }

    fun <S, A> scope(
        stateMap: StateMap<State, S>,
        actionMap: ActionMap<Action, A>
    ): TestStore<State, S, Action, A, Environment> =
        TestStore(
            state,
            reducer,
            environment,
            stateMap,
            actionMap,
            testDispatcher
        )

    suspend fun assert(block: suspend AssertionBuilder<LocalAction, LocalState, Environment>.() -> Unit) {
        val assertion = AssertionBuilder<LocalAction, LocalState, Environment> {
            stateMap.toLocal(state)
        }
        assertion.block()

        val receivedActions: MutableList<Action> = mutableListOf()

        fun runReducer(action: Action) {
            val (newState, effect) = reducer.run(state, action, environment)
            state = newState

            coroutineScope.launch(testDispatcher) {
                try {
                    effect.sink {
                        receivedActions.add(it)
                    }
                } catch (ex: CancellationException) {
                    // ignore
                }
            }
        }

        assertion.steps.forEach { step ->
            var expectedState = stateMap.toLocal(state)

            when (step) {
                is Step.Send<LocalAction, LocalState, Environment> -> {
                    require(receivedActions.isEmpty()) {
                        println("Unhandled actions:")
                        for (action in receivedActions) {
                            println("- $action")
                        }
                        "Must handle all actions"
                    }
                    runReducer(actionMap.fromLocal(step.action))
                    expectedState = step.block(expectedState)
                }
                is Step.Receive<LocalAction, LocalState, Environment> -> {
                    require(receivedActions.isNotEmpty()) { "Expected to receive an action, but received none" }
                    val receivedAction = receivedActions.removeFirst()
                    require(step.action == receivedAction) {
                        println("expected: ${step.action}")
                        println("received: $receivedAction")
                        "Actual and expected actions do not match"
                    }
                    runReducer(actionMap.fromLocal(step.action))
                    expectedState = step.block(expectedState)
                }
                is Step.Environment<LocalAction, LocalState, Environment> -> {
                    require(receivedActions.isEmpty()) { "Must handle all received actions before performing this work" }
                    step.block(environment)
                }
                is Step.Do -> step.block()
                is Step.AdvanceTimeBy -> testDispatcher.scheduler.advanceTimeBy(step.delayTimeMillis)
                is Step.AdvanceUntilIdle -> testDispatcher.scheduler.advanceUntilIdle()
            }

            val actualState = stateMap.toLocal(state)
            require(actualState == expectedState) {
                println(actualState)
                println("---vs---")
                println(expectedState)
                "Actual and expected states do not match"
            }
        }

        require(receivedActions.isEmpty()) {
            println("Unhandled actions:")
            for (action in receivedActions) {
                println("- $action")
            }
            "Must handle all actions"
        }
    }
}
