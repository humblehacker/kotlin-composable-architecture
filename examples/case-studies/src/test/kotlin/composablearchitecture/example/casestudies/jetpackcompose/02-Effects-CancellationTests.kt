package composablearchitecture.example.casestudies.jetpackcompose

import composablearchitecture.EquatableException
import composablearchitecture.asEffect
import composablearchitecture.test.TestStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class EffectsCancellationTests {

    @Test
    fun trivia_SuccessfulRequest() = runTest {
        val store = TestStore(
            EffectsCancellationState(),
            effectsCancellationReducer,
            EffectsCancellationEnvironment(
                fact = FactClient(
                    fetch = { number ->
                        flowOf("$number is a good number Brent")
                            .asEffect()
                    }
                )
            )
        )

        store.assert {

            send(EffectsCancellationAction.StepperChanged(1)) {
                it.copy(count = 1)
            }

            send(EffectsCancellationAction.StepperChanged(0)) {
                it.copy(count = 0)
            }

            send(EffectsCancellationAction.TriviaButtonTapped) {
                it.copy(isTriviaRequestInFlight = true)
            }

            receive(EffectsCancellationAction.TriviaResponse(Result.success("0 is a good number Brent"))) {
                it.copy(
                    currentTrivia = "0 is a good number Brent",
                    isTriviaRequestInFlight = false
                )
            }
        }
    }

    @Test
    fun trivia_FailedRequest() = runTest {
        val store = TestStore(
            EffectsCancellationState(),
            effectsCancellationReducer,
            EffectsCancellationEnvironment(
                fact = FactClient(
                    fetch = { _ ->
                        flow<String> { throw EquatableException() }
                            .asEffect()
                    }
                )
            )
        )

        store.assert {

            send(EffectsCancellationAction.TriviaButtonTapped) {
                it.copy(isTriviaRequestInFlight = true)
            }

            receive(EffectsCancellationAction.TriviaResponse(Result.failure(EquatableException()))) {
                it.copy(isTriviaRequestInFlight = false)
            }
        }
    }

    // NB: This tests that the cancel button really does cancel the in-flight API request.
    //
    // To see the real power of this test, try replacing the `.cancel` effect with a `.none` effect
    // in the `.cancelButtonTapped` action of the `effectsCancellationReducer`. This will cause the
    // test to fail, showing that we are exhaustively asserting that the effect truly is canceled and
    // will never emit.
    @Test
    fun trivia_CancelButtonCancelsRequest() = runTest {
        val store = TestStore(
            EffectsCancellationState(),
            effectsCancellationReducer,
            EffectsCancellationEnvironment(
                fact = FactClient(
                    fetch = { number ->
                        flow {
                            delay(10)
                            this.emit("$number is a good number Brent")
                        }.asEffect()
                    }
                )
            )
        )

        store.assert {

            send(EffectsCancellationAction.TriviaButtonTapped) {
                it.copy(isTriviaRequestInFlight = true)
            }

            send(EffectsCancellationAction.CancelButtonTapped) {
                it.copy(isTriviaRequestInFlight = false)
            }
        }
    }

    @Test
    fun trivia_PlusMinusButtonsCancelsRequest() = runTest {
        val store = TestStore(
            EffectsCancellationState(),
            effectsCancellationReducer,
            EffectsCancellationEnvironment(
                fact = FactClient(
                    fetch = { number ->
                        flow {
                            delay(10)
                            this.emit("$number is a good number Brent")
                        }.asEffect()
                    }
                )
            )
        )

        store.assert {

            send(EffectsCancellationAction.TriviaButtonTapped) {
                it.copy(isTriviaRequestInFlight = true)
            }

            send(EffectsCancellationAction.StepperChanged(1)) {
                it.copy(
                    count = 1,
                    isTriviaRequestInFlight = false
                )
            }
        }
    }
}
