// ktlint-disable filename
package composablearchitecture.example.casestudies.jetpackcompose

import android.content.Context
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import composablearchitecture.ActionMap
import composablearchitecture.Effect
import composablearchitecture.Reducer
import composablearchitecture.StateMap
import composablearchitecture.debug
import composablearchitecture.example.casestudies.jetpackcompose.extras.ScreenshotDetector
import composablearchitecture.withNoEffect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize

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
    val listBasics: ListBasicsState = ListBasicsState(),
) : Parcelable {
    companion object {
        val alertAndConfirmationDialogStateMap = StateMap<RootState, AlertAndConfirmationDialogState>(
            toLocal = { it.alertAndConfirmationDialog },
            fromLocal = { ls, gs -> gs.copy(alertAndConfirmationDialog = ls) }
        )
        val bindingBasicsState = StateMap<RootState, BindingBasicsState> (
            toLocal = { it.bindingBasics },
            fromLocal = { ls, gs -> gs.copy(bindingBasics = ls) }
        )
        val counterState: StateMap<RootState, CounterState> = StateMap(
            toLocal = { it.counter },
            fromLocal = { ls, gs -> gs.copy(counter = ls) }
        )
        val effectsBasicsState: StateMap<RootState, EffectsBasicsState> = StateMap(
            toLocal = { it.effectsBasics },
            fromLocal = { ls, gs -> gs.copy(effectsBasics = ls) }
        )
        val effectsCancellationState: StateMap<RootState, EffectsCancellationState> = StateMap(
            toLocal = { it.effectsCancellation },
            fromLocal = { ls, gs -> gs.copy(effectsCancellation = ls) }
        )
        val longLivingEffectsState: StateMap<RootState, LongLivingEffectsState> = StateMap(
            toLocal = { it.longLivingEffects },
            fromLocal = { ls, gs -> gs.copy(longLivingEffects = ls) }
        )
        val loadThenNavigateState: StateMap<RootState, LoadThenNavigateState> = StateMap(
            toLocal = { it.loadThenNavigate },
            fromLocal = { ls, gs -> gs.copy(loadThenNavigate = ls) }
        )
        val optionalBasicsState: StateMap<RootState, OptionalBasicsState> = StateMap(
            toLocal = { it.optionalBasics },
            fromLocal = { ls, gs -> gs.copy(optionalBasics = ls) }
        )
        val twoCountersState: StateMap<RootState, TwoCountersState> = StateMap(
            toLocal = { it.twoCounters },
            fromLocal = { ls, gs -> gs.copy(twoCounters = ls) }
        )
        val listBasicsState: StateMap<RootState, ListBasicsState> = StateMap(
            toLocal = { it.listBasics },
            fromLocal = { ls, gs -> gs.copy(listBasics = ls) }
        )
    }
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
    class ListBasics(val action: ListBasicsAction) : RootAction()

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
            is ListBasics -> "RootAction.ListBasics(action=$action)"
        }
    }

    companion object {
        val alertAndConfirmationDialogAction: ActionMap<RootAction, AlertAndConfirmationDialogAction> =
            ActionMap(
                toLocal = { if (it is AlertAndConfirmationDialog) it.action else null },
                fromLocal = { AlertAndConfirmationDialog(it) }
            )
        val bindingBasicsAction: ActionMap<RootAction, BindingBasicsAction> = ActionMap(
            toLocal = { if (it is BindingBasics) it.action else null },
            fromLocal = { BindingBasics(action = it) }
        )
        val counterAction: ActionMap<RootAction, CounterAction> = ActionMap(
            toLocal = { if (it is Counter) it.action else null },
            fromLocal = { Counter(it) }
        )
        val effectsBasicsAction: ActionMap<RootAction, EffectsBasicsAction> = ActionMap(
            toLocal = { if (it is EffectsBasics) it.action else null },
            fromLocal = { EffectsBasics(it) }
        )
        val effectsCancellationAction: ActionMap<RootAction, EffectsCancellationAction> = ActionMap(
            toLocal = { if (it is EffectsCancellation) it.action else null },
            fromLocal = { EffectsCancellation(it) }
        )
        val longLivingEffectsAction: ActionMap<RootAction, LongLivingEffectsAction> = ActionMap(
            toLocal = { if (it is LongLivingEffects) it.action else null },
            fromLocal = { LongLivingEffects(it) }
        )
        val loadThenNavigateAction: ActionMap<RootAction, LoadThenNavigateAction> = ActionMap(
            toLocal = { if (it is NavigateAndLoad) it.action else null },
            fromLocal = { NavigateAndLoad(it) }
        )
        val optionalBasicsAction: ActionMap<RootAction, OptionalBasicsAction> = ActionMap(
            toLocal = { if (it is OptionalBasics) it.action else null },
            fromLocal = { OptionalBasics(it) }
        )
        val twoCountersAction: ActionMap<RootAction, TwoCountersAction> = ActionMap(
            toLocal = { if (it is TwoCounters) it.action else null },
            fromLocal = { TwoCounters(it) }
        )
        val listBasicsAction: ActionMap<RootAction, ListBasicsAction> = ActionMap(
            toLocal = { if (it is ListBasics) it.action else null },
            fromLocal = { ListBasics(it) }
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
    Reducer { state: RootState, action: RootAction, _: RootEnvironment ->
        when (action) {
            else -> state.withNoEffect()
        }
    },
    alertAndConfirmationDialogReducer.pullback(
        stateMap = StateMap(
            toLocal = { it.alertAndConfirmationDialog },
            fromLocal = { ls, gs -> gs.copy(alertAndConfirmationDialog = ls) }
        ),
        actionMap = RootAction.alertAndConfirmationDialogAction,
        toLocalEnvironment = { AlertAndConfirmationDialogEnvironment() }
    ),
    bindingBasicsReducer.pullback(
        stateMap = RootState.bindingBasicsState,
        actionMap = RootAction.bindingBasicsAction,
        toLocalEnvironment = { BindingBasicsEnvironment() }
    ),
    counterReducer.pullback(
        stateMap = RootState.counterState,
        actionMap = RootAction.counterAction,
        toLocalEnvironment = { CounterEnvironment() }
    ),
    effectsBasicsReducer.pullback(
        stateMap = RootState.effectsBasicsState,
        actionMap = RootAction.effectsBasicsAction,
        toLocalEnvironment = { EffectsBasicsEnvironment(it.fact) }
    ),
    effectsCancellationReducer.pullback(
        stateMap = RootState.effectsCancellationState,
        actionMap = RootAction.effectsCancellationAction,
        toLocalEnvironment = { EffectsCancellationEnvironment(it.fact) }
    ),
    longLivingEffectsReducer.pullback(
        stateMap = RootState.longLivingEffectsState,
        actionMap = RootAction.longLivingEffectsAction,
        toLocalEnvironment = { LongLivingEffectsEnvironment(it.userDidTakeScreenshot) }
    ),
    loadThenNavigateReducer.pullback(
        stateMap = RootState.loadThenNavigateState,
        actionMap = RootAction.loadThenNavigateAction,
        toLocalEnvironment = { LoadThenNavigateEnvironment() }
    ),
    optionalBasicsReducer.pullback(
        stateMap = RootState.optionalBasicsState,
        actionMap = RootAction.optionalBasicsAction,
        toLocalEnvironment = { OptionalBasicsEnvironment() }
    ),
    twoCountersReducer.pullback(
        stateMap = RootState.twoCountersState,
        actionMap = RootAction.twoCountersAction,
        toLocalEnvironment = { TwoCountersEnvironment() }
    ),
    listBasicsReducer.pullback(
        stateMap = RootState.listBasicsState,
        actionMap = RootAction.listBasicsAction,
        toLocalEnvironment = { ListBasicsEnvironment() }
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
