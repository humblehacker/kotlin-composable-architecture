package composablearchitecture.example.casestudies.jetpackcompose

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import arrow.optics.optics
import composablearchitecture.Reducer
import composablearchitecture.android.ComposableStore
import composablearchitecture.android.WithViewStore
import composablearchitecture.withEffect
import composablearchitecture.withNoEffect
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize
import kotlin.time.Duration.Companion.seconds

private val readMe = """
This screen demonstrates how to introduce side effects into a feature built with the \
Composable Architecture.

A side effect is a unit of work that needs to be performed in the outside world. For example, an \
API request needs to reach an external service over HTTP, which brings with it lots of \
uncertainty and complexity.

Many things we do in our applications involve side effects, such as timers, database requests, \
file access, socket connections, and anytime a scheduler is involved (such as debouncing, \
throttling and delaying), and they are typically difficult to test.

This application has two simple side effects:

- Each time you count down the number will be incremented back up after a delay of 1 second.
- Tapping "Number fact" will trigger an API request to load a piece of trivia about that number.

Both effects are handled by the reducer, and a full test suite is written to confirm that the \
effects behave in the way we expect.
""".replace("\\\n", "")

@optics
@Parcelize
@Immutable
data class EffectsBasicsState(
    val count: Int = 0,
    val isNumberFactRequestInFlight: Boolean = false,
    val numberFact: String? = null
) : Parcelable {
    companion object
}

sealed class EffectsBasicsAction {
    object DecrementButtonTapped : EffectsBasicsAction()
    object IncrementButtonTapped : EffectsBasicsAction()
    object NumberFactButtonTapped : EffectsBasicsAction()
    data class NumberFactResponse(val result: Result<String>) : EffectsBasicsAction()

    override fun toString(): String {
        return when (this) {
            DecrementButtonTapped -> "EffectsBasicsAction.DecrementButtonTapped"
            IncrementButtonTapped -> "EffectsBasicsAction.IncrementButtonTapped"
            NumberFactButtonTapped -> "EffectsBasicsAction.NumberFactButtonTapped"
            is NumberFactResponse -> "EffectsBasicsAction.NumberFactResponse(result: $result)"
        }
    }
}

data class EffectsBasicsEnvironment(
    val fact: FactClient,
    // val mainQueue: AnySchedulerOf<DispatchQueue>
)

val effectsBasicsReducer =
    Reducer<EffectsBasicsState, EffectsBasicsAction, EffectsBasicsEnvironment> { state, action, environment ->
        when (action) {
            EffectsBasicsAction.DecrementButtonTapped -> {
                state
                    .copy(
                        count = state.count - 1,
                        numberFact = null
                    )
                    .withEffect {
                        delay(1.seconds)
                        emit(EffectsBasicsAction.IncrementButtonTapped)
                    }
            }

            EffectsBasicsAction.IncrementButtonTapped -> {
                state
                    .copy(
                        count = state.count + 1,
                        numberFact = null
                    )
                    .withNoEffect()
            }

            EffectsBasicsAction.NumberFactButtonTapped -> {
                state
                    .copy(
                        isNumberFactRequestInFlight = true,
                        numberFact = null
                    )
                    .withEffect(
                        environment.fact.fetch(state.count)
                            .map { EffectsBasicsAction.NumberFactResponse(Result.success(it)) }
                    )
            }

            is EffectsBasicsAction.NumberFactResponse -> {
                when {
                    action.result.isSuccess -> {
                        state
                            .copy(
                                isNumberFactRequestInFlight = false,
                                numberFact = action.result.getOrNull()
                            )
                            .withNoEffect()
                    }
                    action.result.isFailure -> {
                        state
                            .copy(
                                isNumberFactRequestInFlight = false,
                            )
                            .withNoEffect()
                    }
                    else -> state.withNoEffect()
                }
            }
        }
    }

@Composable
fun EffectsBasicsView(
    title: String,
    store: ComposableStore<EffectsBasicsState, EffectsBasicsAction>
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = title) }) },
        backgroundColor = Color(0xF0F0F0FF)
    ) {
        WithViewStore(store) { viewStore ->

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

                        Column {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    // .height(ButtonDefaults.MinHeight)
                                    .padding(ButtonDefaults.ContentPadding),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Button(onClick = { viewStore.send(EffectsBasicsAction.DecrementButtonTapped) }) {
                                    Text("-")
                                }

                                Text("${viewStore.state.count}", modifier = Modifier.padding(8.dp))

                                Button(onClick = { viewStore.send(EffectsBasicsAction.IncrementButtonTapped) }) {
                                    Text("-")
                                }
                            }

                            Divider(color = Color.LightGray, thickness = 0.5.dp)

                            Button(
                                onClick = { viewStore.send(EffectsBasicsAction.NumberFactButtonTapped) },
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text("Number fact")
                            }

                            Divider(color = Color.LightGray, thickness = 0.5.dp)

                            if (viewStore.state.isNumberFactRequestInFlight) {
                                CircularProgressIndicator()
                            }

                            viewStore.state.numberFact?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.body1,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    val context = LocalContext.current

                    TextButton(onClick = {
                        val openURL = Intent(Intent.ACTION_VIEW)
                        openURL.data = Uri.parse("http://numbersapi.com/")
                        startActivity(context, openURL, null)
                    }) {
                        Text(
                            "Number facts provided by numbersapi.com",
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }
    }
}
