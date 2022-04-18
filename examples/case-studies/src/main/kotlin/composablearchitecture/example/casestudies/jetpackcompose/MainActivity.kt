package composablearchitecture.example.casestudies.jetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import composablearchitecture.android.StoreViewModel
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {

    private val viewModel: StoreViewModel<RootState, RootAction, RootEnvironment> by viewModels {
        StoreViewModel.Factory(
            initialState = RootState(),
            reducer = rootReducer,
            environment = RootEnvironment(),
            mainDispatcher = Dispatchers.Main,
            owner = this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CaseStudiesApp(this, viewModel)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveInstanceState()
    }
}
