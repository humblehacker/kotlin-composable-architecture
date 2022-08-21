package composablearchitecture.android

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.optics.Lens
import arrow.optics.Prism
import composablearchitecture.Store
import composablearchitecture.arrow.scope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

open class ScopedViewModel<State, Action> : ViewModel() {

    val state: MutableLiveData<State> = MutableLiveData()

    protected lateinit var store: Store<State, Action>

    fun <GlobalState, GlobalAction> launch(
        globalStore: Store<GlobalState, GlobalAction>,
        lens: Lens<GlobalState, State>,
        prism: Prism<GlobalAction, Action>
    ): Job {
        store = globalStore.scope(lens, prism, viewModelScope)
        return viewModelScope.launch {
            store.states.collect { state.value = it }
        }
    }

    fun <GlobalState, GlobalAction> launch(
        globalStore: Store<GlobalState, GlobalAction>,
        toLocalState: (GlobalState) -> State,
        fromLocalAction: (Action) -> GlobalAction,
    ): Job {
        store = globalStore.scope(toLocalState, fromLocalAction, viewModelScope)
        return viewModelScope.launch {
            store.states.collect { state.value = it }
        }
    }

    fun launch(globalStore: Store<State, Action>): Job {
        store = globalStore
        return viewModelScope.launch {
            store.states.collect { state.value = it }
        }
    }
}
