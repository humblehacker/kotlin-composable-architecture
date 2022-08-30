package composablearchitecture.sandbox.optional

import composablearchitecture.ActionMap
import composablearchitecture.Reducer
import composablearchitecture.StateMap
import composablearchitecture.Store
import composablearchitecture.withNoEffect
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher

data class AppState(val text: String? = null) {
    companion object {
        val nullableText = StateMap<AppState, String?>(
            toLocal = { it.text },
            fromLocal = { ls, gs -> gs.copy(text = ls) }
        )
    }
}

sealed class AppAction {
    data class UpdateText(val to: String) : AppAction()
}

val optionalReducer = Reducer<String, AppAction, Unit> { _, action, _ ->
    when (action) {
        is AppAction.UpdateText -> action.to.withNoEffect()
    }
}.optional()

val appReducer: Reducer<AppState, AppAction, Unit> = optionalReducer.pullback(
    stateMap = AppState.nullableText,
    actionMap = ActionMap.id(),
    toLocalEnvironment = { Unit }
)

fun main() {
    runBlocking {
        val testDispatcher = TestCoroutineDispatcher()

        println("🎬 initial state is non-null")

        var store = Store(
            initialState = AppState(text = ""),
            reducer = appReducer,
            environment = Unit,
            mainDispatcher = testDispatcher
        )
        var job = launch(testDispatcher) { store.states.collect { println(it) } }
        store.send(AppAction.UpdateText("Update non-null state"))
        job.cancel()

        println("🎬 initial state is null")

        store = Store(
            initialState = AppState(text = null),
            reducer = appReducer,
            environment = Unit,
            mainDispatcher = testDispatcher
        )
        job = launch(testDispatcher) { store.states.collect { println(it) } }
        store.send(AppAction.UpdateText("Update null state"))
        job.cancel()

        println("✅")
    }
}
