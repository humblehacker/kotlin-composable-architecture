package composablearchitecture.example.casestudies.jetpackcompose

import androidx.compose.runtime.Composable
import composablearchitecture.android.ComposableStore

@Composable
fun CaseStudiesApp(
    store: ComposableStore<RootState, RootAction>
) {
    RootView(store)
}
