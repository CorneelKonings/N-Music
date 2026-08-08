package moe.rukamori.archivetune.home.effects

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.rukamori.archivetune.constants.HomeBackgroundStyle

val LocalHomeBackgroundStyle = staticCompositionLocalOf { HomeBackgroundStyle.TONAL }

@Composable
fun ScreenBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        when (LocalHomeBackgroundStyle.current) {
            HomeBackgroundStyle.TONAL -> {
                HomePremiumBackground(
                    blobColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(430.dp)
                        .align(Alignment.TopCenter)
                )
            }
            HomeBackgroundStyle.CIRCLES -> {
                CirclesBackground()
            }
        }
    }
}

@Composable
fun CirclesBackground() {
    // Stub for CirclesBackground
}
