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
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import composablearchitecture.android.ComposableStore

@Composable
fun RootView(store: ComposableStore<RootState, RootAction>) {

    val navController = rememberNavController()
    NavHost(navController, startDestination = "case-studies") {

        composable("case-studies") {
            CaseStudiesView(navController)
        }

        composable(CaseStudy.Basics.route) {
            CounterDemoView(
                store.scope(
                    state = RootState.counter,
                    action = RootAction.counterAction
                )
            )
        }

        composable(CaseStudy.OptionalState.route) {
            OptionalBasicsView(
                store.scope(
                    state = RootState.optionalBasics,
                    action = RootAction.optionalBasicsAction
                )
            )
        }
    }
}

@Composable
private fun CaseStudiesView(navController: NavHostController) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "Case Studies") })
    }, backgroundColor = Color(0xF0F0F0FF)) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text("Getting started", style = MaterialTheme.typography.subtitle1)

            Card(shape = RoundedCornerShape(10.dp)) {

                Column(Modifier.verticalScroll(rememberScrollState())) {

                    CaseStudyItem(caseStudy = CaseStudy.Basics, navController = navController)

                    Divider(color = Color.LightGray, thickness = 0.5.dp)

                    CaseStudyItem(caseStudy = CaseStudy.OptionalState, navController = navController)
                }
            }
        }
    }
}

@Composable
fun CaseStudyItem(caseStudy: CaseStudy, navController: NavController) {
    TextButton(onClick = { navController.navigate(caseStudy.route) }) {
        Text(caseStudy.navTitle, style = MaterialTheme.typography.subtitle2)
    }
}

sealed class CaseStudy(val navTitle: String, val viewTitle: String, val route: String) {
    object Basics : CaseStudy("Basics", "Counter Demo", "01.getting-started.counter")
    object OptionalState : CaseStudy("Optional State", "Optional state", "01.getting_started.optional_state")
}
