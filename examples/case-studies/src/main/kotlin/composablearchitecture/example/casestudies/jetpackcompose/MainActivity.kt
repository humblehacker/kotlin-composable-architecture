package composablearchitecture.example.casestudies.jetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import composablearchitecture.android.StoreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: StoreViewModel<RootState, RootAction, RootEnvironment>

    private fun viewModel(navController: NavHostController):
        StoreViewModel<RootState, RootAction, RootEnvironment> =
        viewModels<StoreViewModel<RootState, RootAction, RootEnvironment>> {
            StoreViewModel.Factory(
                initialState = RootState(),
                reducer = rootReducer,
                environment = RootEnvironment.live(),
                navController = navController,
                mainDispatcher = Dispatchers.Main,
                owner = this
            )
        }.value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            viewModel = viewModel(navController)
            CaseStudiesApp(navController, viewModel.store)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveInstanceState()
    }
}
