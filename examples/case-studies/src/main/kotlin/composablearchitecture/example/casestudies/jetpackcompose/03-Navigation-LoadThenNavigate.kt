package composablearchitecture.example.casestudies.jetpackcompose

import android.os.Parcelable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import composablearchitecture.ActionMap
import composablearchitecture.Reducer
import composablearchitecture.StateMap
import composablearchitecture.android.ComposableStore
import composablearchitecture.android.IfLetStore
import composablearchitecture.android.NavigationLink
import composablearchitecture.android.WithViewStore
import composablearchitecture.cancel
import composablearchitecture.cancellable
import composablearchitecture.withEffect
import composablearchitecture.withNoEffect
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize
import kotlin.time.Duration.Companion.seconds

private val readMe: String = """
This screen demonstrates navigation that depends on loading optional state.

Tapping "Load optional counter" simultaneously navigates to a screen that depends on optional \
counter state and fires off an effect that will load this state a second later.
""".replace("\\\n", "")

@Parcelize
data class LoadThenNavigateState(
    val optionalCounter: CounterState? = null,
    val isActivityIndicatorVisible: Boolean = false
) : Parcelable {
    val isNavigationActive: Boolean get() = optionalCounter != null

    companion object {
        val optionalCounterState: StateMap<LoadThenNavigateState, CounterState?> = StateMap(
            toLocal = { it.optionalCounter },
            fromLocal = { ls, gs -> gs.copy(optionalCounter = ls) }
        )
    }
}

sealed class LoadThenNavigateAction {
    object OnDisappear : LoadThenNavigateAction()
    data class OptionalCounter(val action: CounterAction) : LoadThenNavigateAction()
    data class SetNavigation(val isActive: Boolean) : LoadThenNavigateAction()
    object SetNavigationIsActiveDelayCompleted : LoadThenNavigateAction()

    override fun toString(): String {
        return when (this) {
            OnDisappear -> "LoadThenNavigateAction.OnDisappear"
            is OptionalCounter -> "LoadThenNavigateAction.OptionalCounter(action=$action)"
            is SetNavigation -> "LoadThenNavigateAction.SetNavigation(isActive=$isActive)"
            SetNavigationIsActiveDelayCompleted -> "LoadThenNavigateAction.SetNavigationIsActiveDelayCompleted"
        }
    }

    companion object {
        val optionalCounterAction: ActionMap<LoadThenNavigateAction, CounterAction> = ActionMap(
            toLocal = { if (it is OptionalCounter) it.action else null },
            fromLocal = ::OptionalCounter,
        )
    }
}

class LoadThenNavigateEnvironment

val loadThenNavigateReducer =
    counterReducer
        .optional()
        .pullback<LoadThenNavigateState, LoadThenNavigateAction, LoadThenNavigateEnvironment>(
            stateMap = LoadThenNavigateState.optionalCounterState,
            actionMap = LoadThenNavigateAction.optionalCounterAction,
            toLocalEnvironment = { CounterEnvironment() }
        )
        .combine(
            other = Reducer { state, action, _ ->

                val cancelId = "cancel"

                when (action) {
                    LoadThenNavigateAction.OnDisappear -> {
                        state
                            .cancel(cancelId)
                    }

                    is LoadThenNavigateAction.SetNavigation -> {
                        if (action.isActive) {
                            state
                                .copy(isActivityIndicatorVisible = true)
                                .withEffect<LoadThenNavigateState, LoadThenNavigateAction> {
                                    delay(5.seconds)
                                    emit(LoadThenNavigateAction.SetNavigationIsActiveDelayCompleted)
                                }
                                .cancellable(cancelId)
                        } else {
                            state
                                .copy(optionalCounter = null)
                                .withNoEffect()
                        }
                    }

                    LoadThenNavigateAction.SetNavigationIsActiveDelayCompleted -> {
                        state
                            .copy(
                                isActivityIndicatorVisible = false,
                                optionalCounter = CounterState()
                            )
                            .withNoEffect()
                    }

                    is LoadThenNavigateAction.OptionalCounter -> {
                        state.withNoEffect()
                    }
                }
            }
        )

fun NavGraphBuilder.loadThenNavigateGraph(store: ComposableStore<LoadThenNavigateState, LoadThenNavigateAction>) {
    navigation(startDestination = "counterview", route = "load-then-navigate") {
        composable("counterview") {
            IfLetStore(
                store.scope<CounterState?, CounterAction>(
                    toLocalState = { it.optionalCounter },
                    fromLocalAction = { LoadThenNavigateAction.OptionalCounter(it) }
                ),
                then = {
                    CounterView(
                        store = it,
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center
                    )
                }
            )
        }
    }
}

@Composable
fun LoadThenNavigateView(
    title: String,
    store: ComposableStore<LoadThenNavigateState, LoadThenNavigateAction>,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = title) }) },
        backgroundColor = Color(0xF0F0F0FF)
    ) {
        WithViewStore(store) { viewStore ->
            DisposableEffect(lifecycleOwner) {
                onDispose {
                    viewStore.send(LoadThenNavigateAction.OnDisappear)
                }
            }
            Box {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Top,
                ) {

                    MarkdownText(readMe, style = MaterialTheme.typography.caption)

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(shape = RoundedCornerShape(10.dp)) {

                        viewStore.NavigationLink(
                            destination = "counterview",
                            isActive = viewStore.state.isNavigationActive,
                            sendIsActive = { isActive -> LoadThenNavigateAction.SetNavigation(isActive) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(ButtonDefaults.MinHeight)
                                    .padding(ButtonDefaults.ContentPadding),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Load optional counter",
                                    style = MaterialTheme.typography.subtitle2
                                )
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = "navigate to optional counter"
                                )
                            }
                        }
                    }
                }
            }

            if (viewStore.state.isActivityIndicatorVisible) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
