// ktlint-disable filename
package composablearchitecture.example.casestudies.jetpackcompose

import android.content.Context
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import arrow.core.left
import arrow.core.right
import arrow.optics.Prism
import arrow.optics.optics
import composablearchitecture.Effect
import composablearchitecture.Reducer
import composablearchitecture.debug
import composablearchitecture.example.casestudies.jetpackcompose.extras.ScreenshotDetector
import composablearchitecture.withNoEffect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize

@optics
@Parcelize
@Immutable
data class RootState(
    val alertAndConfirmationDialog: AlertAndConfirmationDialogState = AlertAndConfirmationDialogState(),
    val bindingBasics: BindingBasicsState = BindingBasicsState(),
    val counter: CounterState = CounterState(),
    val effectsBasics: EffectsBasicsState = EffectsBasicsState(),
    val effectsCancellation: EffectsCancellationState = EffectsCancellationState(),
    val loadThenNavigate: LoadThenNavigateState = LoadThenNavigateState(),
    val longLivingEffects: LongLivingEffectsState = LongLivingEffectsState(),
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
    class EffectsCancellation(val action: EffectsCancellationAction) : RootAction()
    class LongLivingEffects(val action: LongLivingEffectsAction) : RootAction()
    class NavigateAndLoad(val action: LoadThenNavigateAction) : RootAction()
    class OptionalBasics(val action: OptionalBasicsAction) : RootAction()
    class TwoCounters(val action: TwoCountersAction) : RootAction()

    override fun toString(): String {
        return when (this) {
            is AlertAndConfirmationDialog -> "RootAction.AlertAndConfirmationDialog(action=$action)"
            is BindingBasics -> "RootAction.BindingBasics(action=$action)"
            is Counter -> "RootAction.Counter(action=$action)"
            is EffectsBasics -> "RootAction.EffectsBasics(action=$action)"
            is EffectsCancellation -> "RootAction.EffectsCancellation(action=$action)"
            is LongLivingEffects -> "RootAction.LongLivingEffects(action=$action)"
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
        val effectsCancellationAction: Prism<RootAction, EffectsCancellationAction> = Prism(
            getOrModify = { rootAction ->
                when (rootAction) {
                    is EffectsCancellation -> rootAction.action.right()
                    else -> rootAction.left()
                }
            },
            reverseGet = { action ->
                EffectsCancellation(action = action)
            }
        )
        val longLivingEffectsAction: Prism<RootAction, LongLivingEffectsAction> = Prism(
            getOrModify = { rootAction ->
                when (rootAction) {
                    is LongLivingEffects -> rootAction.action.right()
                    else -> rootAction.left()
                }
            },
            reverseGet = { action ->
                LongLivingEffects(action = action)
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
    val fact: FactClient,
    val screenshotDetector: ScreenshotDetector,
    val userDidTakeScreenshot: Effect<Unit>
) {
    companion object
}

fun RootEnvironment.Companion.live(applicationContext: Context): RootEnvironment {
    val screenshotDetector = ScreenshotDetector(applicationContext)
    return RootEnvironment(
        fact = FactClient.live(),
        screenshotDetector = screenshotDetector,
        userDidTakeScreenshot = liveUserDidTakeScreenshot(screenshotDetector)
    )
}

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
    effectsCancellationReducer.pullback(
        toLocalState = RootState.effectsCancellation,
        toLocalAction = RootAction.effectsCancellationAction,
        toLocalEnvironment = { EffectsCancellationEnvironment(it.fact) }
    ),
    longLivingEffectsReducer.pullback(
        toLocalState = RootState.longLivingEffects,
        toLocalAction = RootAction.longLivingEffectsAction,
        toLocalEnvironment = { LongLivingEffectsEnvironment(it.userDidTakeScreenshot) }
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

private fun liveUserDidTakeScreenshot(
    screenshotDetector: ScreenshotDetector
): Effect<Unit> {
    return Effect(
        screenshotDetector.screenshotTaken
            .onEach { println(it) }
            .map { _ -> Unit }
    )
}
