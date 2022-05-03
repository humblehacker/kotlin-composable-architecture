package composablearchitecture.example.casestudies.jetpackcompose

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import composablearchitecture.android.ComposableStore

@Composable
fun CaseStudiesApp(
    navController: NavHostController,
    store: ComposableStore<RootState, RootAction>
) {
    RootView(navController, store)
}
