package composablearchitecture.android

import android.os.Bundle
import android.os.Environment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import composablearchitecture.Reducer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class StoreViewModel<State, Action, Environment>(
    initialState: State,
    reducer: Reducer<State, Action, Environment>,
    environment: Environment,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val handle: SavedStateHandle
) : ViewModel() {

    var store: ComposableStore<State, Action>

    init {
        val state = handle.get("state") ?: initialState
        store = ComposableStore(
            initialState = state,
            reducer = reducer,
            environment = environment,
            mainDispatcher = mainDispatcher
        )
    }

    fun saveInstanceState() {
        handle.set("state", store.currentState)
    }

    class Factory<State, Action, Environment>(
        val initialState: State,
        val reducer: Reducer<State, Action, Environment>,
        val environment: Environment,
        val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle? = null
    ) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

        override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
            if (modelClass.isAssignableFrom(StoreViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StoreViewModel(initialState, reducer, environment, mainDispatcher, handle) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
