package composablearchitecture.example.casestudies.jetpackcompose.extras

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

@Composable
fun Stepper(
    value: Int,
    valueRange: ClosedRange<Int>,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    step: Int = 1,
    enabled: Boolean = true
) {
    val cornerRadius = 5.dp
    val iconSize = 16.dp
    Row(modifier = modifier) {
        Button(
            onClick = { onValueChange(max(valueRange.start, value - step)) },
            shape = RoundedCornerShape(topStart = cornerRadius, bottomStart = cornerRadius),
            enabled = enabled
        ) {
            Icon(
                Icons.Filled.Remove,
                contentDescription = "Step down",
                modifier = Modifier.size(iconSize)
            )
        }
        Spacer(modifier = Modifier.width(0.5.dp))
        Button(
            onClick = { onValueChange(min(valueRange.endInclusive, value + step)) },
            shape = RoundedCornerShape(topEnd = cornerRadius, bottomEnd = cornerRadius),
            enabled = enabled
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Step up",
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
