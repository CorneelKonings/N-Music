package moe.rukamori.archivetune.ui.player.player_0

import android.graphics.drawable.Drawable
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
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
        
        Crossfade(
            targetState = currentData,
            animationSpec = tween(500),
            label = "CoverCrossfade"
        ) { targetData ->
            if (targetData == null) {
                Image(
                    painter = painterResource(id = placeholderResId),
                    contentDescription = "Album Art Large",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                val request = remember(targetData) {
                    ImageRequest.Builder(context)
                        .data(targetData)
                        .crossfade(500)
                        .build()
                }
                AsyncImage(
                    model = request,
                    contentDescription = "Album Art Large",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = placeholderResId)
                )
            }
        }
    }
}
