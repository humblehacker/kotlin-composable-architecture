package composablearchitecture.example.casestudies.jetpackcompose

import android.os.Parcelable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.unit.dp
import arrow.optics.optics
import composablearchitecture.Reducer
import composablearchitecture.android.ComposableStore
import composablearchitecture.android.ViewStore
import composablearchitecture.android.WithViewStore
import composablearchitecture.example.casestudies.jetpackcompose.extras.Stepper
import composablearchitecture.withNoEffect
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlin.math.min

private val readMe: String = """
Since Jetpack Compose does not have the concept of two-way bindings like SwiftUI, this example \
does not demonstrate bindings. It is only implemented for parity. Original readme follows:

> This file demonstrates how to handle two-way bindings in the Composable Architecture.
> 
> Two-way bindings in SwiftUI are powerful, but also go against the grain of the "unidirectional \
> data flow" of the Composable Architecture. This is because anything can mutate the value \
> whenever it wants.
> 
> On the other hand, the Composable Architecture demands that mutations can only happen by sending \
> actions to the store, and this means there is only ever one place to see how the state of our \
> feature evolves, which is the reducer.
> 
> Any SwiftUI component that requires a Binding to do its job can be used in the Composable \
> Architecture. You can derive a Binding from your ViewStore by using the `binding` method. This \
> will allow you to specify what state renders the component, and what action to send when the \
> component changes, which means you can keep using a unidirectional style for your feature.
""".replace("\\\n", "")

@optics
@Parcelize
@Immutable
data class BindingBasicsState(
    val sliderValue: Float = 5f,
    val stepCount: Int = 10,
    val text: String = "",
    val toggleIsOn: Boolean = false
) : Parcelable {
    companion object
}

sealed class BindingBasicsAction {
    class SliderValueChanged(val value: Float) : BindingBasicsAction()
    class StepCountChanged(val count: Int) : BindingBasicsAction()
    class TextChanged(val text: String) : BindingBasicsAction()
    class ToggleChanged(val isOn: Boolean) : BindingBasicsAction()

    override fun toString(): String {
        return when (this) {
            is SliderValueChanged -> "BindingBasicsAction.SliderValueChanged($value)"
            is StepCountChanged -> "BindingBasicsAction.StepCountChanged($count)"
            is TextChanged -> "BindingBasicsAction.TextChanged($text)"
            is ToggleChanged -> "BindingBasicsAction.ToggleChanged($isOn)"
        }
    }
}

class BindingBasicsEnvironment

val bindingBasicsReducer =
    Reducer<BindingBasicsState, BindingBasicsAction, BindingBasicsEnvironment> { state, action, _ ->
        when (action) {
            is BindingBasicsAction.SliderValueChanged -> {
                state
                    .copy(sliderValue = action.value)
                    .withNoEffect()
            }
            is BindingBasicsAction.StepCountChanged -> {
                state
                    .copy(
                        sliderValue = min(state.sliderValue, action.count.toFloat()),
                        stepCount = action.count
                    )
                    .withNoEffect()
            }
            is BindingBasicsAction.TextChanged -> {
                state
                    .copy(text = action.text)
                    .withNoEffect()
            }
            is BindingBasicsAction.ToggleChanged -> {
                state
                    .copy(toggleIsOn = action.isOn)
                    .withNoEffect()
            }
        }
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BindingBasicsView(store: ComposableStore<BindingBasicsState, BindingBasicsAction>) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = CaseStudy.BindingsBasics.viewTitle) }) },
        backgroundColor = Color(0xF0F0F0FF)
    ) {
        WithViewStore(store) { viewStore ->
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
            ) {

                MarkdownText(readMe, style = MaterialTheme.typography.caption)

                Spacer(modifier = Modifier.height(16.dp))

                val requester = remember { BringIntoViewRequester() }
                val coroutineScope = rememberCoroutineScope()

                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(requester)
                ) {

                    Column {

                        TextRow(
                            viewStore,
                            bringIntoView = { coroutineScope.launch { requester.bringIntoView() } }
                        )

                        Divider(color = Color.LightGray, thickness = 0.5.dp)

                        DisableSwitchRow(viewStore)

                        Divider(color = Color.LightGray, thickness = 0.5.dp)

                        StepperRow(viewStore)

                        Divider(color = Color.LightGray, thickness = 0.5.dp)

                        SliderRow(viewStore)
                    }
                }
            }
        }
    }
}

@Composable
private fun TextRow(
    viewStore: ViewStore<BindingBasicsState, BindingBasicsAction>,
    bringIntoView: () -> Unit
) {
    TextField(
        placeholder = { Text("Type here") },
        value = viewStore.state.text,
        onValueChange = { viewStore.send(BindingBasicsAction.TextChanged(it)) },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusEvent {
                if (!it.isFocused) return@onFocusEvent
                bringIntoView()
            },
        keyboardOptions = KeyboardOptions(autoCorrect = false),
        colors = TextFieldDefaults.textFieldColors(textColor = if (viewStore.state.toggleIsOn) Gray else MaterialTheme.colors.primary),
        enabled = !viewStore.state.toggleIsOn
    )
}

@Composable
private fun DisableSwitchRow(viewStore: ViewStore<BindingBasicsState, BindingBasicsAction>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Disable other controls")
        Switch(
            checked = viewStore.state.toggleIsOn,
            onCheckedChange = { viewStore.send(BindingBasicsAction.ToggleChanged(it)) }
        )
    }
}

@Composable
private fun StepperRow(viewStore: ViewStore<BindingBasicsState, BindingBasicsAction>) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        Text("Max slider value: ${viewStore.state.stepCount}")
        Stepper(
            viewStore.state.stepCount,
            0..100,
            onValueChange = { viewStore.send(BindingBasicsAction.StepCountChanged(it)) },
            enabled = !viewStore.state.toggleIsOn,
        )
    }
}

@Composable
private fun SliderRow(viewStore: ViewStore<BindingBasicsState, BindingBasicsAction>) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        Text("Slider value: ${viewStore.state.sliderValue.toInt()}")
        Slider(
            value = viewStore.state.sliderValue,
            onValueChange = { viewStore.send(BindingBasicsAction.SliderValueChanged(it)) },
            modifier = Modifier.width(130.dp),
            valueRange = 0f.rangeTo(viewStore.state.stepCount.toFloat()),
            steps = viewStore.state.stepCount - 1,
            enabled = !viewStore.state.toggleIsOn
        )
    }
}
