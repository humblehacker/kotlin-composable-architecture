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
import composablearchitecture.*
import composablearchitecture.android.ComposableStore
import composablearchitecture.android.ViewStore
import composablearchitecture.android.WithViewStore
import composablearchitecture.example.casestudies.jetpackcompose.extras.Stepper
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.parcelize.Parcelize
import kotlin.Result

private val readMe = """
This screen demonstrates how one can cancel in-flight effects in the Composable Architecture.

Use the stepper to count to a number, and then tap the "Number fact" button to fetch \
a random fact about that number using an API.

While the API request is in-flight, you can tap "Cancel" to cancel the effect and prevent \
it from feeding data back into the application. Interacting with the stepper while a \
request is in-flight will also cancel it.
""".replace("\\\n", "")

@Parcelize
@Immutable
data class EffectsCancellationState(
    val count: Int = 0,
    val currentTrivia: String? = null,
    val isTriviaRequestInFlight: Boolean = false,
) : Parcelable {
    companion object
}

sealed class EffectsCancellationAction {
    object CancelButtonTapped : EffectsCancellationAction()
    data class StepperChanged(val value: Int) : EffectsCancellationAction()
    object TriviaButtonTapped : EffectsCancellationAction()
    data class TriviaResponse(val result: Result<String>) : EffectsCancellationAction()

    override fun toString(): String {
        return when (this) {
            CancelButtonTapped -> "EffectsCancellationAction.DecrementButtonTapped"
            is StepperChanged -> "EffectsCancellationAction.is StepperChanged(value: $value)"
            TriviaButtonTapped -> "EffectsCancellationAction.TriviaButtonTapped"
            is TriviaResponse -> "EffectsCancellationAction.TriviaResponse(result: $result)"
        }
    }
}

data class EffectsCancellationEnvironment(
    val fact: FactClient
)

val effectsCancellationReducer =
    Reducer<EffectsCancellationState, EffectsCancellationAction, EffectsCancellationEnvironment> { state, action, environment ->
        val triviaRequestId = "triviaRequestId"
        when (action) {
            EffectsCancellationAction.CancelButtonTapped -> {
                state
                    .copy(
                        isTriviaRequestInFlight = false
                    )
                    .cancel(triviaRequestId)
            }

            is EffectsCancellationAction.StepperChanged -> {
                state
                    .copy(
                        count = action.value,
                        currentTrivia = null,
                        isTriviaRequestInFlight = false
                    )
                    .cancel(triviaRequestId)
            }

            EffectsCancellationAction.TriviaButtonTapped -> {
                state
                    .copy(
                        currentTrivia = null,
                        isTriviaRequestInFlight = true
                    )
                    .withEffect<EffectsCancellationState, EffectsCancellationAction>(
                        environment.fact.fetch(state.count)
                            .map { EffectsCancellationAction.TriviaResponse(Result.success(it)) }
                            .catch { emit(EffectsCancellationAction.TriviaResponse(Result.failure(it))) }
                    )
                    .cancellable(triviaRequestId)
            }

            is EffectsCancellationAction.TriviaResponse -> {
                when {
                    action.result.isSuccess -> {
                        state
                            .copy(
                                isTriviaRequestInFlight = false,
                                currentTrivia = action.result.getOrNull()
                            )
                            .withNoEffect()
                    }
                    action.result.isFailure -> {
                        state
                            .copy(
                                isTriviaRequestInFlight = false,
                            )
                            .withNoEffect()
                    }
                    else -> state.withNoEffect()
                }
            }
        }
    }

@Composable
fun EffectsCancellationView(
    title: String,
    store: ComposableStore<EffectsCancellationState, EffectsCancellationAction>
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

                            StepperRow(viewStore)

                            Divider(color = Color.LightGray, thickness = 0.5.dp)

                            if (viewStore.state.isTriviaRequestInFlight) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Button(
                                        onClick = { viewStore.send(EffectsCancellationAction.CancelButtonTapped) },
                                    ) {
                                        Text("Cancel")
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                }
                            } else {
                                Button(
                                    onClick = { viewStore.send(EffectsCancellationAction.TriviaButtonTapped) },
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    enabled = !viewStore.state.isTriviaRequestInFlight
                                ) {
                                    Text("Number fact")
                                }
                            }

                            viewStore.state.currentTrivia?.let {
                                Divider(color = Color.LightGray, thickness = 0.5.dp)

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

@Composable
private fun StepperRow(viewStore: ViewStore<EffectsCancellationState, EffectsCancellationAction>) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        Text("${viewStore.state.count}")
        Stepper(
            viewStore.state.count,
            0..100,
            onValueChange = { viewStore.send(EffectsCancellationAction.StepperChanged(it)) }
        )
    }
}
