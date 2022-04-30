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
    NavHost(navController, startDestination = "case-studies") {

        composable("case-studies") {
            CaseStudiesView { navController.navigate(it) }
        }

        composable(CaseStudy.Basics.route) {
            CounterDemoView(
                store.scope(
                    state = RootState.counter,
                    action = RootAction.counterAction
                )
            )
        }

        composable(CaseStudy.TwoCounters.route) {
            TwoCountersView(
                store.scope(
                    state = RootState.twoCounters,
                    action = RootAction.twoCountersAction
                )
            )
        }

        composable(CaseStudy.BindingsBasics.route) {
            BindingBasicsView(
                store.scope(
                    state = RootState.bindingBasics,
                    action = RootAction.bindingBasicsAction
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
private fun CaseStudiesView(navigateTo: (route: String) -> Unit) {
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

                    CaseStudyItem(CaseStudy.Basics, navigateTo)

                    Divider(color = Color.LightGray, thickness = 0.5.dp)

                    CaseStudyItem(CaseStudy.TwoCounters, navigateTo)

                    Divider(color = Color.LightGray, thickness = 0.5.dp)

                    CaseStudyItem(CaseStudy.BindingsBasics, navigateTo)

                    Divider(color = Color.LightGray, thickness = 0.5.dp)

                    CaseStudyItem(CaseStudy.OptionalState, navigateTo)
                }
            }
        }
    }
}

@Composable
fun CaseStudyItem(caseStudy: CaseStudy, navigateTo: (route: String) -> Unit) {
    TextButton(onClick = { navigateTo(caseStudy.route) }) {
        Text(caseStudy.navTitle, style = MaterialTheme.typography.subtitle2)
    }
}

sealed class CaseStudy(val navTitle: String, val viewTitle: String, val route: String) {
    object Basics : CaseStudy("Basics", "Counter Demo", "01.getting-started.counter")
    object TwoCounters : CaseStudy("Pullback and combine", "Two counter Demo", "01.getting-started.composition.two-counters")
    object OptionalState : CaseStudy("Optional State", "Optional state", "01.getting-started.optional-state")
    object BindingsBasics : CaseStudy("Bindings", "Bindings basics", "01.getting-started.bindings-basics")
}
