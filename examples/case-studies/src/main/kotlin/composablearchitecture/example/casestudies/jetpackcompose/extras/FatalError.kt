package composablearchitecture.example.casestudies.jetpackcompose

fun fatalError(): Nothing = throw NotImplementedError("Fatal Error")
fun fatalError(reason: String): Nothing = throw NotImplementedError("Fatal Error: $reason")

