package composablearchitecture.example.casestudies.jetpackcompose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import composablearchitecture.android.ComposableStore
import composablearchitecture.example.casestudies.jetpackcompose.extras.NotYetImplementedView

val backgroundColor = Color(0xF0F0F0FF)

@Composable
fun RootView(navController: NavHostController, store: ComposableStore<RootState, RootAction>) {

    NavHost(navController, startDestination = "case-studies") {
        composable("case-studies") { CaseStudiesView { navController.navigate(it) } }
        composable(gettingStartedCaseStudies, store)
        composable(effectsCaseStudies, store)
        composable(navigationCaseStudies, store)
        composable(higherOrderReducersCaseStudies, store)

        loadThenNavigateGraph(
            store = store.scope(
                state = RootState.loadThenNavigate,
                action = RootAction.loadThenNavigateAction
            )
        )

        longLivingEffectsGraph()
    }
}

private fun NavGraphBuilder.composable(
    caseStudies: List<CaseStudy>,
    store: ComposableStore<RootState, RootAction>
) {
    caseStudies.forEach { caseStudy ->
        composable(caseStudy.route) {
            caseStudy.composable(store)
        }
    }
}

@Composable
private fun CaseStudiesView(navigateTo: (route: String) -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "Case Studies") })
    }, backgroundColor = backgroundColor) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CaseStudiesGroup("Getting started", gettingStartedCaseStudies, navigateTo)
            CaseStudiesGroup("Effects", effectsCaseStudies, navigateTo)
            CaseStudiesGroup("Navigation", navigationCaseStudies, navigateTo)
            CaseStudiesGroup("Higher-order reducers", higherOrderReducersCaseStudies, navigateTo)
        }
    }
}

@Composable
private fun CaseStudiesGroup(
    title: String,
    caseStudies: List<CaseStudy>,
    navigateTo: (route: String) -> Unit
) {
    Text(title, style = MaterialTheme.typography.subtitle1)

    Card(shape = RoundedCornerShape(10.dp)) {

        Column {

            caseStudies.forEachIndexed { index, caseStudy ->

                CaseStudyItem(caseStudy, navigateTo)

                if (index < caseStudies.lastIndex)
                    Divider(color = Color.LightGray, thickness = 0.5.dp)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun CaseStudyItem(caseStudy: CaseStudy, navigateTo: (route: String) -> Unit) {
    TextButton(onClick = { navigateTo(caseStudy.route) }) {
        Text(
            caseStudy.navTitle,
            style = MaterialTheme.typography.subtitle2,
            textAlign = TextAlign.Left,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

val gettingStartedCaseStudies: List<CaseStudy> = listOf(
    CaseStudy(
        navTitle = "Basics",
        route = "01.getting-started.counter",
        composable = { store ->
            CounterDemoView(
                title = "Counter Demo",
                store.scope(
                    state = RootState.counter,
                    action = RootAction.counterAction
                )
            )
        }
    ),
    CaseStudy(
        navTitle = "Pullback and combine",
        route = "01.getting-started.composition.two-counters",
        composable = { store ->
            TwoCountersView(
                title = "Two counter Demo",
                store.scope(
                    state = RootState.twoCounters,
                    action = RootAction.twoCountersAction
                )
            )
        }
    ),
    CaseStudy(
        navTitle = "Pullback and combine - lists",
        route = "01.getting-started.composition.list-basics",
        composable = { store ->
            ListBasicsView(
                title = "List Basics Demo",
                store.scope(
                    state = RootState.listBasics,
                    action = RootAction.listBasicsAction
                )
            )
        }
    ),
    CaseStudy(
        navTitle = "Bindings",
        route = "01.getting-started.bindings-basics",
        composable = { store ->
            BindingBasicsView(
                title = "Bindings basics",
                store.scope(
                    state = RootState.bindingBasics,
                    action = RootAction.bindingBasicsAction
                )
            )
        }
    ),
    CaseStudy(
        navTitle = "Form Bindings",
        route = "01.getting-started.bindings.forms",
        composable = { NotYetImplementedView(title = "Bindings form") }
    ),
    CaseStudy(
        navTitle = "Optional State",
        route = "01.getting-started.optional-state",
        composable = { store ->
            OptionalBasicsView(
                title = "Optional state",
                store.scope(
                    state = RootState.optionalBasics,
                    action = RootAction.optionalBasicsAction
                )
            )
        }
    ),
    CaseStudy(
        navTitle = "Alerts and Confirmation Dialogs",
        route = "01.getting-started.alerts-and-confirmation-dialogs",
        composable = { store ->
            AlertAndConfirmationDialogView(
                title = "Alerts & Confirmation Dialogs",
                store.scope(
                    state = RootState.alertAndConfirmationDialog,
                    action = RootAction.alertAndConfirmationDialogAction
                )
            )
        }
    ),
)

val effectsCaseStudies: List<CaseStudy> = listOf(
    CaseStudy(
        navTitle = "Basics",
        route = "01.effects.basic",
        composable = { store ->
            EffectsBasicsView(
                title = "Basics",
                store = store.scope(
                    state = RootState.effectsBasics,
                    action = RootAction.effectsBasicsAction
                )
            )
        }
    ),
    CaseStudy(
        navTitle = "Cancellation",
        route = "01.effects.cancellation",
        composable = { store ->
            EffectsCancellationView(
                title = "Effect cancellation",
                store = store.scope(
                    state = RootState.effectsCancellation,
                    action = RootAction.effectsCancellationAction
                )
            )
        }
    ),
    CaseStudy(
        navTitle = "Long-living effects",
        route = "01.effects.long_living",
        composable = { store ->
            LongLivingEffectsView(
                title = "Long-living effects",
                store = store.scope(
                    state = RootState.longLivingEffects,
                    action = RootAction.longLivingEffectsAction
                )
            )
        }
    )
)

val navigationCaseStudies: List<CaseStudy> = listOf(
    CaseStudy(
        navTitle = "Load data then navigate",
        route = "03.navigation.load-then-navigate",
        composable = { store ->
            LoadThenNavigateView(
                title = "Load then navigate",
                store = store.scope(
                    state = RootState.loadThenNavigate,
                    action = RootAction.loadThenNavigateAction
                )
            )
        }
    )
)

val higherOrderReducersCaseStudies: List<CaseStudy> = listOf(
    CaseStudy(
        navTitle = "Reusable favoriting component",
        route = "01.higher-order-reducers.reusable-favoriting",
        composable = { NotYetImplementedView(title = "Favoriting") }
    )
)

data class CaseStudy(
    val navTitle: String,
    val route: String,
    val composable: @Composable (store: ComposableStore<RootState, RootAction>) -> Unit
)
