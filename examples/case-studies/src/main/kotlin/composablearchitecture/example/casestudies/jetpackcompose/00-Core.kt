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
    val optionalBasics: OptionalBasicsState = OptionalBasicsState()
) : Parcelable {
    companion object
}

sealed class RootAction {
    class OptionalBasics(val action: OptionalBasicsAction) : RootAction()

    companion object {
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

val rootReducer = Reducer<RootState, RootAction, RootEnvironment> { state, action, _ ->
    when (action) {
        else -> state.withNoEffect()
    }
}.combine(
    optionalBasicsReducer.pullback(
        toLocalState = RootState.optionalBasics,
        toLocalAction = RootAction.optionalBasicsAction,
        toLocalEnvironment = { OptionalBasicsEnvironment() }
    )
).debug()
