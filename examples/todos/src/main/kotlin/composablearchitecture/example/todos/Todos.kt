package composablearchitecture.example.todos

import composablearchitecture.ActionMap
import composablearchitecture.Reducer
import composablearchitecture.StateMap
import composablearchitecture.cancellable
import composablearchitecture.debug
import composablearchitecture.withEffect
import composablearchitecture.withNoEffect
import kotlinx.coroutines.delay
import java.util.UUID

data class Todo(
    var description: String = "",
    val id: UUID,
    var isComplete: Boolean = false
) {
    companion object
}

sealed class TodoAction {
    class CheckBoxToggled(val checked: Boolean) : TodoAction()
    class TextFieldChanged(val text: String) : TodoAction()

}

object TodoEnvironment

val todoReducer = Reducer<Todo, TodoAction, TodoEnvironment> { todo, action, _ ->
    when (action) {
        is TodoAction.TextFieldChanged -> {
            todo
                .copy(description = action.text)
                .withNoEffect()
        }
        is TodoAction.CheckBoxToggled ->
            todo
                .copy(isComplete = action.checked)
                .withNoEffect()
    }
}

enum class EditMode {
    active, inactive
}

enum class Filter {
    All,
    Active,
    Completed
}

data class AppState(
    val editMode: EditMode = EditMode.inactive,
    val filter: Filter = Filter.All,
    val todos: List<Todo> = emptyList()
) {
    companion object {
        val todoState = StateMap<AppState, List<Todo>>(
            toLocal = { it.todos },
            fromLocal = { ls, gs -> gs.copy(todos = ls) }
        )
    }

    val filteredTodos: List<Todo>
        get() = when (filter) {
            Filter.All -> todos
            Filter.Active -> todos.filter { !it.isComplete }
            Filter.Completed -> todos.filter { it.isComplete }
        }

    fun sortCompleted(): List<Todo> =
        todos
            .withIndex()
            .sortedWith(
                compareBy<IndexedValue<Todo>> { it.value.isComplete }.thenBy { it.index }
            )
            .map { it.value }
}

sealed class AppAction : Comparable<AppAction> {
    object AddTodoButtonTapped : AppAction()
    object ClearCompletedButtonTapped : AppAction()
    object SortCompletedTodos : AppAction()
    class Todo(val id: UUID, val action: TodoAction) : AppAction()

    override fun compareTo(other: AppAction): Int = this.compareTo(other)

    companion object {
        val todoAction = ActionMap<AppAction, Pair<UUID, TodoAction>>(
            toLocal = { if (it is AppAction.Todo) (it.id to it.action) else null },
            fromLocal = { (id, action) -> AppAction.Todo(id, action) }
        )
    }
}

class AppEnvironment(
    var uuid: () -> UUID
)

val appReducer = Reducer
    .combine(
        Reducer<AppState, AppAction, AppEnvironment> { state, action, environment ->
            when (action) {
                is AppAction.AddTodoButtonTapped -> {
                    state
                        .copy(todos = state.todos.plus(Todo(id = environment.uuid())))
                        .withNoEffect()
                }
                AppAction.SortCompletedTodos -> {
                    state
                        .copy(todos = state.sortCompleted())
                        .withNoEffect()
                }
                is AppAction.Todo -> {
                    if (action.action is TodoAction.CheckBoxToggled) {
                        state
                            .withEffect<AppState, AppAction> {
                                delay(1000L)
                                emit(AppAction.SortCompletedTodos)
                            }
                            .cancellable("TodoCompletionId", cancelInFlight = true)
                    } else {
                        state.withNoEffect()
                    }
                }
                else -> state.withNoEffect()
            }
        },
        todoReducer.forEach(
            stateMap = AppState.todoState,
            actionMap = AppAction.todoAction,
            toLocalEnvironment = { TodoEnvironment },
            idGetter = { it.id }
        )
    )
    .debug()
