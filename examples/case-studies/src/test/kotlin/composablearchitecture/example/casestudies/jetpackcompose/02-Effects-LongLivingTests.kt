// ktlint-disable filename
package composablearchitecture.example.casestudies.jetpackcompose

import composablearchitecture.Effect
import composablearchitecture.test.TestStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LongLivingEffectsTests {

    @Test
    fun reducer() = runTest {
        val screenshotTaken = MutableStateFlow(Unit)

        val store = TestStore(
            LongLivingEffectsState(),
            longLivingEffectsReducer,
            LongLivingEffectsEnvironment(
                userDidTakeScreenshot = Effect(screenshotTaken)
            )
        )

        store.assert {
            send(LongLivingEffectsAction.OnAppear)

            // Simulate a screenshot being taken
            screenshotTaken.emit(Unit)
            receive(LongLivingEffectsAction.UserDidTakeScreenshotNotification) {
                it.copy(screenshotCount = 1)
            }

            send(LongLivingEffectsAction.OnDisappear)

            // Simulate a screenshot being taken to show no effects are executed
            screenshotTaken.emit(Unit)
        }
    }
}
