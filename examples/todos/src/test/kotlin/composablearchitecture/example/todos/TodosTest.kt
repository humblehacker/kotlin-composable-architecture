package composablearchitecture.example.todos

import composablearchitecture.android.Alert.ButtonAction.Companion.send
import composablearchitecture.test.TestStore
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.UUID

class TodosTest {

    @Test
    fun `Store scoped test`() = runTest {
        val testDispatcher = TestCoroutineDispatcher()

        val todo = Todo(id = UUID.fromString("DEADBEEF-DEAD-BEEF-DEAD-BEEDDEADBEEF"))
        val appState = AppState(todos = listOf(todo))

        val store = TestStore(
            appState,
            appReducer,
            AppEnvironment(uuid = { UUID.randomUUID() }),
            testDispatcher
        )

        val scopedStore = store.scope(
            stateMap = AppState.todoState,
            actionMap = AppAction.todoAction
        )

        scopedStore.assert {
            send(todo.id to TodoAction.TextFieldChanged("Buy milk")) {
                listOf(todo.copy(description = "Buy milk"))
            }
        }
    }

    @Test
    fun `When add todo is tapped new item is showing up`() = runTest {
        val testDispatcher = TestCoroutineDispatcher()

        val store = TestStore(
            AppState(),
            appReducer,
            AppEnvironment(uuid = { UUID.randomUUID() }),
            testDispatcher
        )

        fun appStateFromTodos(vararg todos: Todo): AppState =
            AppState(todos = todos.toList())

        store.assert {
            environment {
                it.uuid = { UUID.fromString("DEADBEEF-DEAD-BEEF-DEAD-BEEDDEADBEEF") }
            }
            send(AppAction.AddTodoButtonTapped) {
                appStateFromTodos(
                    Todo(id = UUID.fromString("DEADBEEF-DEAD-BEEF-DEAD-BEEDDEADBEEF"))
                )
            }
            environment {
                it.uuid = { UUID.fromString("00000000-0000-0000-0000-000000000000") }
            }
            send(AppAction.AddTodoButtonTapped) {
                appStateFromTodos(
                    Todo(id = UUID.fromString("DEADBEEF-DEAD-BEEF-DEAD-BEEDDEADBEEF")),
                    Todo(id = UUID.fromString("00000000-0000-0000-0000-000000000000"))
                )
            }
            send(
                AppAction.Todo(
                    UUID.fromString("DEADBEEF-DEAD-BEEF-DEAD-BEEDDEADBEEF"),
                    TodoAction.CheckBoxToggled(true)
                )
            ) {
                appStateFromTodos(
                    Todo(
                        id = UUID.fromString("DEADBEEF-DEAD-BEEF-DEAD-BEEDDEADBEEF"),
                        isComplete = true
                    ),
                    Todo(id = UUID.fromString("00000000-0000-0000-0000-000000000000"))
                )
            }
            doBlock {
                testDispatcher.advanceTimeBy(1000L)
            }
            receive(AppAction.SortCompletedTodos) {
                appStateFromTodos(
                    Todo(id = UUID.fromString("00000000-0000-0000-0000-000000000000")),
                    Todo(
                        id = UUID.fromString("DEADBEEF-DEAD-BEEF-DEAD-BEEDDEADBEEF"),
                        isComplete = true
                    )
                )
            }
        }
    }
}
