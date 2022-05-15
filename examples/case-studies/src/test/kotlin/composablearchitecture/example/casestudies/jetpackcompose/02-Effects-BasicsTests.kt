// ktlint-disable filename
package composablearchitecture.example.casestudies.jetpackcompose

import composablearchitecture.asEffect
import composablearchitecture.test.TestStore
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.Test

class EffectsBasicsTests {

    @Test
    fun countDown() = runTest {
        val store = TestStore(
            EffectsBasicsState(),
            effectsBasicsReducer,
            EffectsBasicsEnvironment(
                fact = FactClient.failing()
            )
        )

        store.assert {

            send(EffectsBasicsAction.IncrementButtonTapped) {
                it.copy(count = 1)
            }

            send(EffectsBasicsAction.DecrementButtonTapped) {
                it.copy(count = 0)
            }

            advanceTimeBy(1001)

            receive(EffectsBasicsAction.IncrementButtonTapped) {
                it.copy(count = 1)
            }
        }
    }

    @Test
    fun numberFact() = runTest {
        val store = TestStore(
            EffectsBasicsState(),
            effectsBasicsReducer,
            EffectsBasicsEnvironment(
                fact = FactClient(
                    fetch = { number ->
                        flowOf("$number is a good number Brent")
                            .asEffect()
                    }
                )
            )
        )

        store.assert {

            send(EffectsBasicsAction.IncrementButtonTapped) {
                it.copy(count = 1)
            }

            send(EffectsBasicsAction.NumberFactButtonTapped) {
                it.copy(isNumberFactRequestInFlight = true)
            }

            receive(EffectsBasicsAction.NumberFactResponse(Result.success("1 is a good number Brent"))) {
                it.copy(
                    isNumberFactRequestInFlight = false,
                    numberFact = "1 is a good number Brent"
                )
            }
        }
    }
}
