package composablearchitecture.example.casestudies.jetpackcompose.extras

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import composablearchitecture.example.casestudies.jetpackcompose.backgroundColor

@Composable
fun NotYetImplementedView(title: String) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = title) })
    }, backgroundColor = backgroundColor) {
        Text(
            "Not yet implemented",
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight()
        )
    }
}

