package composablearchitecture

@Suppress("unused")
fun <State, Action, Environment> Reducer<State, Action, Environment>.debug(): Reducer<State, Action, Environment> =
    Reducer { state, action, environment ->
        val result = run(state, action, environment)
        val stateString = result.state.toString()
            .replace("\n", "\\n")
            .replace("\t", "\\t")
        println("state=${stateString}, action=$action")
        result
    }
