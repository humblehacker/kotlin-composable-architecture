package composablearchitecture.example.casestudies.jetpackcompose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import composablearchitecture.android.ComposableStore

@Composable
fun RootView(store: ComposableStore<RootState, RootAction>) {

    val navController = rememberNavController()
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "Case Studies") })
    }) {
        NavHost(navController, startDestination = "case-studies") {

            composable("case-studies") {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text("Getting started", style = MaterialTheme.typography.subtitle1)

                    Card(shape = RoundedCornerShape(10.dp)) {

                        Column(Modifier.verticalScroll(rememberScrollState())) {

                            TextButton(onClick = { }, enabled = false) {
                                Text("Basics", style = MaterialTheme.typography.subtitle2)
                            }

                            Divider(color = Color.LightGray, thickness = 0.5.dp)

                            TextButton(onClick = { navController.navigate("01.getting-started.optional-state") }) {
                                Text(
                                    "Optional state",
                                    style = MaterialTheme.typography.subtitle2
                                )
                            }
                        }
                    }
                }
            }

            composable("01.getting-started.optional-state") {
                OptionalBasicsView(
                    store.scope(
                        state = RootState.optionalBasics,
                        action = RootAction.optionalBasicsAction
                    )
                )
            }
        }
    }
}
