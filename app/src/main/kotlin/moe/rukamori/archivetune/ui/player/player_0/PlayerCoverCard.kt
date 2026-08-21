package moe.rukamori.archivetune.ui.player.player_0

import android.graphics.drawable.Drawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerCoverCard(
    coverDrawable: Drawable? = null,
    coverUrl: String? = null,
    modifier: Modifier = Modifier,
    placeholderResId: Int,
    isAlbumCoverGlowEnabled: Boolean = false,
    vibrantColor: Color = Color.Transparent
) {
    val shadowColor = MaterialTheme.colorScheme.scrim
    val outlineColor: Color = MaterialTheme.colorScheme.outlineVariant
    
    Box(
        modifier = modifier
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .then(
                if (isAlbumCoverGlowEnabled) {
                    Modifier.shadow(
                        elevation = 48.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = false,
                        ambientColor = vibrantColor.copy(alpha = 0.8f),
                        spotColor = vibrantColor
                    )
                } else {
                    Modifier.shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = false,
                        ambientColor = shadowColor,
                        spotColor = shadowColor.copy(alpha = 0.6f)
                    )
                }
            )
            .clip(RoundedCornerShape(24.dp))
            .border(BorderStroke(1.dp, outlineColor), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        val context = LocalContext.current
        val currentData = coverDrawable ?: coverUrl.takeIf { !it.isNullOrEmpty() }
        val placeholderPainter = painterResource(id = placeholderResId)
        
        var previousPainter by remember { mutableStateOf<Painter?>(null) }
        var currentPainter by remember { mutableStateOf<Painter?>(null) }
        
        val request = remember(currentData) {
            ImageRequest.Builder(context)
                .data(currentData)
                .crossfade(400)
                .build()
        }
        
        val painter = coil3.compose.rememberAsyncImagePainter(
            model = request,
            error = placeholderPainter,
            fallback = placeholderPainter
        )
        
        val painterState by painter.state.collectAsState()
        
        LaunchedEffect(painterState, currentData) {
            val state = painterState
            if (currentData == null) {
                previousPainter = currentPainter
                currentPainter = placeholderPainter
            } else if (state is AsyncImagePainter.State.Success) {
                previousPainter = currentPainter
                currentPainter = state.painter
            } else if (state is AsyncImagePainter.State.Error) {
                previousPainter = currentPainter
                currentPainter = placeholderPainter
            }
        }
        
        val basePainter = previousPainter ?: currentPainter ?: placeholderPainter
        Image(
            painter = basePainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        val activePainter = currentPainter ?: painter
        Image(
            painter = activePainter,
            contentDescription = "Album Art Large",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
