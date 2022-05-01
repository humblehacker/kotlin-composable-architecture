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
    val bindingBasics: BindingBasicsState = BindingBasicsState(),
    val counter: CounterState = CounterState(),
    val optionalBasics: OptionalBasicsState = OptionalBasicsState(),
    val twoCounters: TwoCountersState = TwoCountersState(),
) : Parcelable {
    companion object
}

sealed class RootAction {
    class BindingBasics(val action: BindingBasicsAction) : RootAction()
    class Counter(val action: CounterAction) : RootAction()
    class TwoCounters(val action: TwoCountersAction) : RootAction()
    class OptionalBasics(val action: OptionalBasicsAction) : RootAction()

    override fun toString(): String {
        return when (this) {
            is BindingBasics -> "RootAction.BindingBasics(action=$action)"
            is Counter -> "RootAction.Counter(action=$action)"
            is OptionalBasics -> "RootAction.OptionalBasics(action=$action)"
            is TwoCounters -> "RootAction.TwoCounters(action=$action)"
        }
    }

    companion object {
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
    }
}

data class RootEnvironment(val placeholder: Int = 0)

fun RootEnvironment.live() = RootEnvironment()

val rootReducer = Reducer.combine<RootState, RootAction, RootEnvironment>(
    Reducer { state, action, _ ->
        when (action) {
            else -> state.withNoEffect()
        }
    },
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
    twoCountersReducer.pullback(
        toLocalState = RootState.twoCounters,
        toLocalAction = RootAction.twoCountersAction,
        toLocalEnvironment = { TwoCountersEnvironment() }
    ),
    optionalBasicsReducer.pullback(
        toLocalState = RootState.optionalBasics,
        toLocalAction = RootAction.optionalBasicsAction,
        toLocalEnvironment = { OptionalBasicsEnvironment() }
    )
).debug()
