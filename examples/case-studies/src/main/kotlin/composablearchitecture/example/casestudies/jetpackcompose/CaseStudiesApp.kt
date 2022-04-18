package composablearchitecture.example.casestudies.jetpackcompose

import android.content.Context
import androidx.compose.runtime.Composable
import composablearchitecture.android.StoreViewModel

@Composable
fun CaseStudiesApp(
    context: Context,
    viewModel: StoreViewModel<RootState, RootAction, RootEnvironment>
) {
    RootView(viewModel)
}
