package composablearchitecture

/*
In order to store Exception objects in state and to be able to compare them in tests, they
must be equatable. But by default Exceptions, being reference types, only support
referential equality.  `EquatableException` can be used as a base class for Exceptions
that are equatable by type and `message`.
 */

open class EquatableException(message: String = "") : Exception(message) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return this.message == (other as EquatableException).message
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
