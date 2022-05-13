// ktlint-disable filename
package composablearchitecture.example.casestudies.jetpackcompose

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import arrow.core.left
import arrow.core.right
import arrow.optics.Prism
import arrow.optics.optics
import composablearchitecture.Reducer
import composablearchitecture.debug
import composablearchitecture.withNoEffect
import kotlinx.parcelize.Parcelize

@optics
@Parcelize
@Immutable
data class RootState(
    val alertAndConfirmationDialog: AlertAndConfirmationDialogState = AlertAndConfirmationDialogState(),
    val bindingBasics: BindingBasicsState = BindingBasicsState(),
    val counter: CounterState = CounterState(),
    val effectsBasics: EffectsBasicsState = EffectsBasicsState(),
    val loadThenNavigate: LoadThenNavigateState = LoadThenNavigateState(),
    val optionalBasics: OptionalBasicsState = OptionalBasicsState(),
    val twoCounters: TwoCountersState = TwoCountersState(),
) : Parcelable {
    companion object
}

sealed class RootAction {
    class AlertAndConfirmationDialog(val action: AlertAndConfirmationDialogAction) : RootAction()
    class BindingBasics(val action: BindingBasicsAction) : RootAction()
    class Counter(val action: CounterAction) : RootAction()
    class EffectsBasics(val action: EffectsBasicsAction) : RootAction()
    class NavigateAndLoad(val action: LoadThenNavigateAction) : RootAction()
    class OptionalBasics(val action: OptionalBasicsAction) : RootAction()
    class TwoCounters(val action: TwoCountersAction) : RootAction()

    override fun toString(): String {
        return when (this) {
            is AlertAndConfirmationDialog -> "RootAction.AlertAndConfirmationDialog(action=$action)"
            is BindingBasics -> "RootAction.BindingBasics(action=$action)"
            is Counter -> "RootAction.Counter(action=$action)"
            is EffectsBasics -> "RootAction.EffectsBasics(action=$action)"
            is NavigateAndLoad -> "RootAction.NavigateAndLoad(action=$action)"
            is OptionalBasics -> "RootAction.OptionalBasics(action=$action)"
            is TwoCounters -> "RootAction.TwoCounters(action=$action)"
        }
    }

    companion object {
        val alertAndConfirmationDialogAction: Prism<RootAction, AlertAndConfirmationDialogAction> =
            Prism(
                getOrModify = { rootAction ->
                    when (rootAction) {
                        is AlertAndConfirmationDialog -> rootAction.action.right()
                        else -> rootAction.left()
                    }
                },
                reverseGet = { action ->
                    AlertAndConfirmationDialog(action = action)
                }
            )
        val bindingBasicsAction: Prism<RootAction, BindingBasicsAction> = Prism(
            getOrModify = { rootAction ->
                when (rootAction) {
                    is BindingBasics -> rootAction.action.right()
                    else -> rootAction.left()
                }
            },
            reverseGet = { action ->
                BindingBasics(action = action)
            }
        )
        val counterAction: Prism<RootAction, CounterAction> = Prism(
            getOrModify = { rootAction ->
                when (rootAction) {
                    is Counter -> rootAction.action.right()
                    else -> rootAction.left()
                }
            },
            reverseGet = { action ->
                Counter(action = action)
            }
        )
        val effectsBasicsAction: Prism<RootAction, EffectsBasicsAction> = Prism(
            getOrModify = { rootAction ->
                when (rootAction) {
                    is EffectsBasics -> rootAction.action.right()
                    else -> rootAction.left()
                }
            },
            reverseGet = { action ->
                EffectsBasics(action = action)
            }
        )
        val loadThenNavigateAction: Prism<RootAction, LoadThenNavigateAction> = Prism(
            getOrModify = { rootAction ->
                when (rootAction) {
                    is NavigateAndLoad -> rootAction.action.right()
                    else -> rootAction.left()
                }
            },
            reverseGet = { action ->
                NavigateAndLoad(action = action)
            }
        )
        val optionalBasicsAction: Prism<RootAction, OptionalBasicsAction> = Prism(
            getOrModify = { rootAction ->
                when (rootAction) {
                    is OptionalBasics -> rootAction.action.right()
                    else -> rootAction.left()
                }
            },
            reverseGet = { action ->
                OptionalBasics(action = action)
            }
        )
        val twoCountersAction: Prism<RootAction, TwoCountersAction> = Prism(
            getOrModify = { rootAction ->
                when (rootAction) {
                    is TwoCounters -> rootAction.action.right()
                    else -> rootAction.left()
                }
            },
            reverseGet = { action ->
                TwoCounters(action = action)
            }
        )
    }
}

data class RootEnvironment(
    val fact: FactClient
) {
    companion object
}

fun RootEnvironment.Companion.live() = RootEnvironment(
    fact = FactClient.live()
)

val rootReducer = Reducer.combine<RootState, RootAction, RootEnvironment>(
    Reducer { state, action, _ ->
        when (action) {
            else -> state.withNoEffect()
        }
    },
    alertAndConfirmationDialogReducer.pullback(
        toLocalState = RootState.alertAndConfirmationDialog,
        toLocalAction = RootAction.alertAndConfirmationDialogAction,
        toLocalEnvironment = { AlertAndConfirmationDialogEnvironment() }
    ),
    bindingBasicsReducer.pullback(
        toLocalState = RootState.bindingBasics,
        toLocalAction = RootAction.bindingBasicsAction,
        toLocalEnvironment = { BindingBasicsEnvironment() }
    ),
    counterReducer.pullback(
        toLocalState = RootState.counter,
        toLocalAction = RootAction.counterAction,
        toLocalEnvironment = { CounterEnvironment() }
    ),
    effectsBasicsReducer.pullback(
        toLocalState = RootState.effectsBasics,
        toLocalAction = RootAction.effectsBasicsAction,
        toLocalEnvironment = { EffectsBasicsEnvironment(it.fact) }
    ),
    loadThenNavigateReducer.pullback(
        toLocalState = RootState.loadThenNavigate,
        toLocalAction = RootAction.loadThenNavigateAction,
        toLocalEnvironment = { LoadThenNavigateEnvironment() }
    ),
    optionalBasicsReducer.pullback(
        toLocalState = RootState.optionalBasics,
        toLocalAction = RootAction.optionalBasicsAction,
        toLocalEnvironment = { OptionalBasicsEnvironment() }
    ),
    twoCountersReducer.pullback(
        toLocalState = RootState.twoCounters,
        toLocalAction = RootAction.twoCountersAction,
        toLocalEnvironment = { TwoCountersEnvironment() }
    ),
).debug()
