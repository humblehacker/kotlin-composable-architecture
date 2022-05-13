// ktlint-disable filename
package composablearchitecture.example.casestudies.jetpackcompose

import composablearchitecture.android.Alert
import composablearchitecture.android.TextState
import composablearchitecture.test.TestStore
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Test

class AlertsAndConfirmationDialogsTests {

    @Test
    fun alert() = runTest {
        val dispatcher = TestCoroutineDispatcher()
        val environment = AlertAndConfirmationDialogEnvironment()
        val store = TestStore(
            AlertAndConfirmationDialogState(),
            alertAndConfirmationDialogReducer,
            environment,
            dispatcher
        )

        store.assert {
            send(AlertAndConfirmationDialogAction.AlertButtonTapped) {
                it.copy(
                    alert = Alert.State(
                        title = TextState("Alert!"),
                        message = TextState("This is an alert"),
                        primaryButton = Alert.Button.cancel(label = TextState("Cancel")),
                        secondaryButton = Alert.Button.default(
                            label = TextState("Increment"),
                            action = Alert.ButtonAction(
                                Alert.ActionType.Send(
                                    AlertAndConfirmationDialogAction.IncrementButtonTapped
                                )
                            )
                        )
                    )
                )
            }
            send(AlertAndConfirmationDialogAction.IncrementButtonTapped) {
                it.copy(alert = Alert.State(
                    title = TextState("Incremented!")),
                    count = 1
                )
            }
            send(AlertAndConfirmationDialogAction.AlertDismissed) {
                it.copy(alert = null)
            }
        }
    }

    @Test @Ignore
    fun confirmationDialog() = runTest  {
        // TODO
    }
}
