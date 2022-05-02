package composablearchitecture.android

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.savedstate.SavedStateRegistryOwner
import composablearchitecture.Reducer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

class StoreViewModel<State, Action, Environment>(
    initialState: State,
    reducer: Reducer<State, Action, Environment>,
    environment: Environment,
    navController: NavHostController,
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
            navigateTo = { route, onDismiss ->
                dismissWhenRouteLeavesBackStack(navController, route, onDismiss)
            },
            popBackStack = { navController.popBackStack() },
            mainDispatcher = mainDispatcher
        )
    }

    private fun dismissWhenRouteLeavesBackStack(
        navController: NavHostController,
        route: String,
        onDismiss: () -> Unit
    ) {
        navController.navigate(route)
        viewModelScope.launch {
            navController
                .currentBackStackEntryFlow
                .takeWhile { navController.backQueue.firstOrNull { it.destination.route == route } != null }
                .collect { onDismiss() }
        }
    }

    fun saveInstanceState() {
        handle.set("state", store.currentState)
    }

    class Factory<State, Action, Environment>(
        val initialState: State,
        val reducer: Reducer<State, Action, Environment>,
        val environment: Environment,
        val navController: NavHostController,
        val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle? = null
    ) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

        override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            if (modelClass.isAssignableFrom(StoreViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StoreViewModel(
                    initialState,
                    reducer,
                    environment,
                    navController,
                    mainDispatcher,
                    handle
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
