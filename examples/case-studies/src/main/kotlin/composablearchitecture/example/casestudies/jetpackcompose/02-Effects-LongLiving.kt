@file:OptIn(ExperimentalPermissionsApi::class)

package composablearchitecture.example.casestudies.jetpackcompose

import android.Manifest
import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import arrow.optics.optics
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import composablearchitecture.*
import composablearchitecture.android.ComposableStore
import composablearchitecture.android.WithViewStore
import composablearchitecture.example.casestudies.jetpackcompose.extras.SimpleTextView
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.parcelize.Parcelize

private val readMe = """
This application demonstrates how to handle long-living effects, for example notifications from \
Notification Center.

Run this application on the device (unfortunately this will not work in the Android emulator), \
and take a few screenshots (usually by pressing power and volume down buttons simultaneously), \
and observe that the UI counts the number of times that happens.

Then, navigate to another screen and take screenshots there, and observe that this screen does \
*not* count those screenshots.
""".replace("\\\n", "")

@optics
@Parcelize
@Immutable
data class LongLivingEffectsState(
    val screenshotCount: Int = 0
) : Parcelable {
    companion object
}

sealed class LongLivingEffectsAction {
    object UserDidTakeScreenshotNotification : LongLivingEffectsAction()
    object OnAppear : LongLivingEffectsAction()
    object OnDisappear : LongLivingEffectsAction()

    override fun toString(): String {
        return when (this) {
            UserDidTakeScreenshotNotification -> "LongLivingEffectsAction.UserDidTakeScreenshotNotification"
            OnAppear -> "LongLivingEffectsAction.OnAppear"
            OnDisappear -> "LongLivingEffectsAction.OnDisappear"
        }
    }
}

data class LongLivingEffectsEnvironment(
    val userDidTakeScreenshot: Effect<Unit>
)

object UserDidTakeScreenshotNotificationId

val longLivingEffectsReducer =
    Reducer<LongLivingEffectsState, LongLivingEffectsAction, LongLivingEffectsEnvironment> { state, action, environment ->

        when (action) {
            LongLivingEffectsAction.UserDidTakeScreenshotNotification -> {
                state
                    .copy(screenshotCount = state.screenshotCount + 1)
                    .withNoEffect()
            }

            LongLivingEffectsAction.OnAppear -> {
                state
                    .withEffect<LongLivingEffectsState, LongLivingEffectsAction>(
                        environment
                            .userDidTakeScreenshot
                            .map { LongLivingEffectsAction.UserDidTakeScreenshotNotification }
                    )
                    .cancellable(UserDidTakeScreenshotNotificationId)
            }

            LongLivingEffectsAction.OnDisappear -> {
                state
                    .cancel(UserDidTakeScreenshotNotificationId)
            }
        }
    }

fun NavGraphBuilder.longLivingEffectsGraph() {
    navigation(startDestination = "detail-view", route = "long-living-effects") {
        composable("detail-view") {
            SimpleTextView(
                text = """
Take a screenshot of this screen a few times, and then go back to the previous screen to see \
that those screenshots were not counted.
""".replace("\\\n", "")
            )
        }
    }
}

@Composable
fun LongLivingEffectsView(
    title: String,
    store: ComposableStore<LongLivingEffectsState, LongLivingEffectsAction>,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = title) }) },
        backgroundColor = Color(0xF0F0F0FF)
    ) {
        WithReadExternalStoragePermission {
            WithViewStore(store) { viewStore ->
                LaunchedEffect(lifecycleOwner) {
                    viewStore.send(LongLivingEffectsAction.OnAppear)
                }
                DisposableEffect(lifecycleOwner) {
                    onDispose {
                        viewStore.send(LongLivingEffectsAction.OnDisappear)
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

                            Column {

                                Text(
                                    "A screenshot of this screen has been taken ${viewStore.state.screenshotCount} times",
                                    style = MaterialTheme.typography.body1,
                                    modifier = Modifier.padding(8.dp)
                                )

                                Divider(color = Color.LightGray, thickness = 0.5.dp)

                                Button(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    onClick = {
                                        viewStore.navigateTo("detail-view", onDismiss = {})
                                    }
                                ) {
                                    Text("Navigate to another screen")
                                }

                                Divider(color = Color.LightGray, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("PermissionLaunchedDuringComposition")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WithReadExternalStoragePermission(content: @Composable () -> Unit) {
    val permissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    permissionState.launchPermissionRequest()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    })

    when {
        permissionState.hasPermission -> {
            content()
        }
        permissionState.shouldShowRationale -> {
            Column {
                Text(text = "Reading external permission is required by this app")
            }
        }
        !permissionState.hasPermission && !permissionState.shouldShowRationale -> {
            Text(text = "Permission fully denied. Go to settings to enable")
        }
    }
}
