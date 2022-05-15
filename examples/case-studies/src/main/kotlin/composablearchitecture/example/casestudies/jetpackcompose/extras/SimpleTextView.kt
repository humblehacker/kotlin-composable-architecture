package composablearchitecture.example.casestudies.jetpackcompose.extras

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import composablearchitecture.example.casestudies.jetpackcompose.backgroundColor

@Composable
fun SimpleTextView(title: String = "", text: String) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = title) })
    }, backgroundColor = backgroundColor) {
        Text(
            text,
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .wrapContentHeight()
        )
    }
}

