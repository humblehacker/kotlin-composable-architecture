package composablearchitecture.example.casestudies.jetpackcompose

import composablearchitecture.Effect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.URL
import kotlin.time.Duration.Companion.seconds

data class FactClient(
    var fetch: (number: Int) -> Effect<String>
) {
    companion object
}

fun FactClient.Companion.live(): FactClient {
    return FactClient(
        fetch = { number ->
            Effect(
                flow {
                    try {
                        val json = URL("http://numbersapi.com/$number/trivia").readText()
                        emit(json)
                    } catch (e: Exception) {
                        println(e)
                        delay(1.seconds)
                        emit("$number is a good number Brent")
                    }
                }.flowOn(Dispatchers.Default)
            )
        }
    )
}
