/*
 * YumaPlayer (2026) | Modified work by MuwMix
 * ArchiveTune (2026) | Original work by © Rukamori
 * GPL-3.0 License | Contributors: see git history
 */

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package moe.rukamori.archivetune.ui.screens.onboarding

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.onboarding.OnboardingCommunityActionUiModel
import moe.rukamori.archivetune.onboarding.OnboardingEvent
import moe.rukamori.archivetune.onboarding.OnboardingPageId
import moe.rukamori.archivetune.onboarding.OnboardingPermissionAction
import moe.rukamori.archivetune.onboarding.OnboardingPermissionStatus
import moe.rukamori.archivetune.onboarding.OnboardingPermissionUiModel
import moe.rukamori.archivetune.onboarding.OnboardingScreenState
import moe.rukamori.archivetune.onboarding.OnboardingUiState
import moe.rukamori.archivetune.onboarding.OnboardingViewModel
import moe.rukamori.archivetune.ui.theme.LocalYumaColors
import moe.rukamori.archivetune.ui.theme.yumaClickable
import moe.rukamori.archivetune.ui.theme.yumaGlassCard

private val OnboardingContentMaxWidth = 540.dp
private val OnboardingPagePadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)

private val GoogleSansFont = FontFamily(
    Font(R.font.google_sans_regular, FontWeight.Normal),
    Font(R.font.google_sans_bold, FontWeight.Bold)
)

@Composable
private fun getM3ExpressiveShape(index: Int) =
    when (index % 4) {
        0 -> MaterialShapes.Cookie4Sided.toShape()
        1 -> MaterialShapes.Clover4Leaf.toShape()
        2 -> MaterialShapes.Ghostish.toShape()
        else -> MaterialShapes.Sunny.toShape()
    }

@Composable
fun OnboardingRoute(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            viewModel.onPermissionResult()
        }
    val settingsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.onPermissionResult()
        }

    LaunchedEffect(context, viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is OnboardingEvent.RequestPermission -> {
                    permissionLauncher.launch(event.permission)
                }

                OnboardingEvent.OpenInstallPackagesSettings -> {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        val intent =
                            Intent(
                                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                "package:${context.packageName}".toUri(),
                            )
                        settingsLauncher.launch(intent)
                    }
                }

                is OnboardingEvent.OpenUri -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                    context.startActivity(intent)
                }
            }
        }
    }

    OnboardingScreen(
        state = state,
        onNext = viewModel::onNext,
        onBack = viewModel::onBack,
        onComplete = viewModel::complete,
        onPermissionAction = viewModel::onPermissionAction,
        onCommunityAction = viewModel::onCommunityAction,
        modifier = modifier,
    )
}

@Composable
fun OnboardingScreen(
    state: OnboardingScreenState,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onPermissionAction: (OnboardingPermissionAction) -> Unit,
    onCommunityAction: (OnboardingCommunityActionUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when (state) {
            OnboardingScreenState.Loading -> {
                LoadingContent(contentPadding = padding)
            }

            OnboardingScreenState.Empty -> {
                MessageContent(
                    title = stringResource(R.string.onboarding_empty_title),
                    subtitle = stringResource(R.string.onboarding_empty_subtitle),
                    actionLabel = stringResource(R.string.onboarding_finish),
                    onAction = onComplete,
                    contentPadding = padding,
                )
            }

            is OnboardingScreenState.Error -> {
                MessageContent(
                    title = stringResource(state.messageResId),
                    subtitle = stringResource(R.string.onboarding_empty_subtitle),
                    actionLabel = stringResource(R.string.onboarding_finish),
                    onAction = onComplete,
                    contentPadding = padding,
                )
            }

            is OnboardingScreenState.Success -> {
                OnboardingSuccessContent(
                    uiState = state.uiState,
                    onNext = onNext,
                    onBack = onBack,
                    onPermissionAction = onPermissionAction,
                    onCommunityAction = onCommunityAction,
                    contentPadding = padding,
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(contentPadding: PaddingValues) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator()
    }
}

@Composable
private fun MessageContent(
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit,
    contentPadding: PaddingValues,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = OnboardingContentMaxWidth)
                    .yumaGlassCard(shape = RoundedCornerShape(28.dp))
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = GoogleSansFont,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = GoogleSansFont,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onAction,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.yumaClickable(onClick = onAction),
            ) {
                Text(
                    text = actionLabel,
                    fontFamily = GoogleSansFont,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun OnboardingSuccessContent(
    uiState: OnboardingUiState,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onPermissionAction: (OnboardingPermissionAction) -> Unit,
    onCommunityAction: (OnboardingCommunityActionUiModel) -> Unit,
    contentPadding: PaddingValues,
) {
    val pagerState =
        rememberPagerState(
            initialPage = uiState.currentPage,
            pageCount = { uiState.pages.size },
        )

    LaunchedEffect(uiState.currentPage, uiState.pages.size) {
        val targetPage = uiState.currentPage.coerceIn(0, uiState.pages.lastIndex)
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(contentPadding),
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.weight(1f),
        ) { pageIndex ->
            when (uiState.pages[pageIndex].id) {
                OnboardingPageId.WELCOME -> {
                    WelcomePage(
                        uiState = uiState,
                        pageIndex = pageIndex,
                    )
                }

                OnboardingPageId.PERMISSIONS -> {
                    PermissionsPage(
                        uiState = uiState,
                        pageIndex = pageIndex,
                        onPermissionAction = onPermissionAction,
                    )
                }

                OnboardingPageId.COMMUNITY -> {
                    CommunityPage(
                        uiState = uiState,
                        pageIndex = pageIndex,
                        onCommunityAction = onCommunityAction,
                    )
                }
            }
        }

        GlassBottomNavigation(
            currentPage = pagerState.currentPage,
            pageCount = uiState.pages.size,
            onBack = onBack,
            onNext = onNext,
        )
    }
}

@Composable
private fun GlassBottomNavigation(
    currentPage: Int,
    pageCount: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val showBack = currentPage > 0
    val isLastPage = currentPage >= pageCount - 1
    val nextLabel =
        if (currentPage == 0) {
            stringResource(R.string.onboarding_lets_go)
        } else if (isLastPage) {
            stringResource(R.string.onboarding_finish)
        } else {
            stringResource(R.string.next)
        }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            repeat(pageCount) { index ->
                val selected = index == currentPage
                val dotWidth by animateDpAsState(
                    targetValue = if (selected) 26.dp else 10.dp,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
                    label = "dotWidth",
                )
                Surface(
                    shape = CircleShape,
                    color =
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        },
                    modifier =
                        Modifier
                            .height(10.dp)
                            .width(dotWidth),
                ) {}
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(
                visible = showBack,
                enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut(),
            ) {
                Surface(
                    modifier =
                        Modifier
                            .yumaClickable(onClick = onBack)
                            .border(1.dp, LocalYumaColors.current.glassBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = LocalYumaColors.current.glassBackground,
                ) {
                    Text(
                        text = stringResource(R.string.back_button_desc),
                        fontFamily = GoogleSansFont,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    )
                }
            }

            Surface(
                modifier =
                    Modifier
                        .weight(1f)
                        .yumaClickable(onClick = onNext),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = nextLabel,
                        fontFamily = GoogleSansFont,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 15.sp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_right),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomePage(
    uiState: OnboardingUiState,
    pageIndex: Int,
) {
    val page = uiState.pages[pageIndex]
    val avatarShape = MaterialShapes.Cookie4Sided.toShape()

    val infiniteTransition = rememberInfiniteTransition(label = "avatarEffects")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = OnboardingPagePadding,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        item(key = "welcome_content", contentType = "welcome") {
            Column(
                modifier = Modifier
                    .widthIn(max = OnboardingContentMaxWidth)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(260.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .graphicsLayer { rotationZ = rotationAngle }
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        Color.Transparent,
                                    ),
                                ),
                                shape = avatarShape,
                            )
                    )

                    Surface(
                        modifier = Modifier
                            .size(210.dp)
                            .graphicsLayer { rotationZ = rotationAngle },
                        shape = avatarShape,
                        color = LocalYumaColors.current.glassBackground,
                        border = BorderStroke(
                            width = 4.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary,
                                    MaterialTheme.colorScheme.primary,
                                )
                            )
                        ),
                    ) {}

                    Icon(
                        painter = painterResource(id = R.mipmap.ic_launcher_monochrome),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(200.dp)
                            .graphicsLayer {
                                scaleX = 1.25f
                                scaleY = 1.25f
                            }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(page.titleResId),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSansFont,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(page.subtitleResId),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp,
                    fontFamily = GoogleSansFont,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                )

                Spacer(modifier = Modifier.height(20.dp))

                OnboardingMetadataPills(uiState = uiState)
            }
        }
    }
}

@Composable
private fun OnboardingMetadataPills(uiState: OnboardingUiState) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PassivePill(text = stringResource(uiState.variantLabelResId))
        PassivePill(
            text =
                stringResource(
                    R.string.onboarding_version_label,
                    uiState.versionName,
                ),
        )
    }
}

@Composable
private fun PassivePill(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = LocalYumaColors.current.glassBackground,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.border(1.dp, LocalYumaColors.current.glassBorder, RoundedCornerShape(12.dp)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            fontFamily = GoogleSansFont,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PermissionsPage(
    uiState: OnboardingUiState,
    pageIndex: Int,
    onPermissionAction: (OnboardingPermissionAction) -> Unit,
) {
    val page = uiState.pages[pageIndex]

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = OnboardingPagePadding,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = page.id.name, contentType = "header") {
            ExpressivePageHeader(
                iconResId = page.iconResId,
                titleResId = page.titleResId,
                subtitleResId = page.subtitleResId,
            )
        }
        item(key = "permissions_group", contentType = "permissions_group") {
            Column(
                modifier =
                    Modifier
                        .widthIn(max = OnboardingContentMaxWidth)
                        .fillMaxWidth()
                        .yumaGlassCard(shape = RoundedCornerShape(24.dp))
                        .padding(8.dp),
            ) {
                uiState.permissions.forEachIndexed { index, item ->
                    GlassPermissionRow(
                        permission = item,
                        index = index,
                        onPermissionAction = onPermissionAction,
                    )
                    if (index < uiState.permissions.lastIndex) {
                        HorizontalDivider(
                            color = LocalYumaColors.current.glassBorder.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassPermissionRow(
    permission: OnboardingPermissionUiModel,
    index: Int,
    onPermissionAction: (OnboardingPermissionAction) -> Unit,
) {
    val action = permission.action
    val onClick =
        remember(action, onPermissionAction) {
            {
                if (action != null) {
                    onPermissionAction(action)
                }
            }
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(enabled = action != null, onClick = onClick)
                .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PermissionIcon(permission = permission, index = index)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(permission.titleResId),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = GoogleSansFont,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = stringResource(permission.descriptionResId),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = GoogleSansFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        PermissionStatusAction(
            permission = permission,
            onPermissionAction = onPermissionAction,
        )
    }
}

@Composable
private fun PermissionIcon(
    permission: OnboardingPermissionUiModel,
    index: Int,
) {
    val containerColor =
        when (permission.status) {
            OnboardingPermissionStatus.ALLOWED -> MaterialTheme.colorScheme.primary
            OnboardingPermissionStatus.NEEDS_ACTION -> MaterialTheme.colorScheme.tertiary
            OnboardingPermissionStatus.ALLOWED_BY_INSTALL -> MaterialTheme.colorScheme.secondary
            OnboardingPermissionStatus.UNAVAILABLE -> LocalYumaColors.current.glassBackground
        }

    val iconTint =
        when (permission.status) {
            OnboardingPermissionStatus.ALLOWED -> MaterialTheme.colorScheme.onPrimary
            OnboardingPermissionStatus.NEEDS_ACTION -> MaterialTheme.colorScheme.onTertiary
            OnboardingPermissionStatus.ALLOWED_BY_INSTALL -> MaterialTheme.colorScheme.onSecondary
            OnboardingPermissionStatus.UNAVAILABLE -> MaterialTheme.colorScheme.onSurfaceVariant
        }

    Surface(
        shape = getM3ExpressiveShape(index),
        color = containerColor,
        modifier = Modifier.size(46.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(permission.iconResId),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun PermissionStatusAction(
    permission: OnboardingPermissionUiModel,
    onPermissionAction: (OnboardingPermissionAction) -> Unit,
) {
    val action = permission.action

    if (action != null) {
        Surface(
            modifier =
                Modifier
                    .yumaClickable { onPermissionAction(action) }
                    .clip(RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.primary,
        ) {
            Text(
                text = stringResource(R.string.allow),
                fontFamily = GoogleSansFont,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    } else {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = LocalYumaColors.current.glassBackground,
            border = BorderStroke(1.dp, LocalYumaColors.current.glassBorder),
        ) {
            Text(
                text = stringResource(permission.status.labelResId()),
                fontFamily = GoogleSansFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun CommunityPage(
    uiState: OnboardingUiState,
    pageIndex: Int,
    onCommunityAction: (OnboardingCommunityActionUiModel) -> Unit,
) {
    val page = uiState.pages[pageIndex]

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = OnboardingPagePadding,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = page.id.name, contentType = "header") {
            ExpressivePageHeader(
                iconResId = page.iconResId,
                titleResId = page.titleResId,
                subtitleResId = page.subtitleResId,
            )
        }
        item(key = "community_group", contentType = "community_group") {
            Column(
                modifier =
                    Modifier
                        .widthIn(max = OnboardingContentMaxWidth)
                        .fillMaxWidth()
                        .yumaGlassCard(shape = RoundedCornerShape(24.dp))
                        .padding(8.dp),
            ) {
                uiState.communityActions.forEachIndexed { index, item ->
                    GlassCommunityRow(
                        action = item,
                        index = index,
                        onCommunityAction = onCommunityAction,
                    )
                    if (index < uiState.communityActions.lastIndex) {
                        HorizontalDivider(
                            color = LocalYumaColors.current.glassBorder.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassCommunityRow(
    action: OnboardingCommunityActionUiModel,
    index: Int,
    onCommunityAction: (OnboardingCommunityActionUiModel) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onCommunityAction(action) }
                .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(46.dp),
            shape = getM3ExpressiveShape(index),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(action.iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(action.titleResId),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = GoogleSansFont,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = stringResource(action.descriptionResId),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = GoogleSansFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Icon(
            painter = painterResource(R.drawable.arrow_forward),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExpressivePageHeader(
    iconResId: Int,
    titleResId: Int,
    subtitleResId: Int,
) {
    Column(
        modifier =
            Modifier
                .widthIn(max = OnboardingContentMaxWidth)
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = MaterialShapes.Cookie9Sided.toShape(),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        Text(
            text = stringResource(titleResId),
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = GoogleSansFont,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(subtitleResId),
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = GoogleSansFont,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private fun OnboardingPermissionStatus.labelResId(): Int =
    when (this) {
        OnboardingPermissionStatus.ALLOWED -> R.string.permission_status_allowed
        OnboardingPermissionStatus.NEEDS_ACTION -> R.string.allow
        OnboardingPermissionStatus.ALLOWED_BY_INSTALL -> R.string.onboarding_permission_allowed_by_install
        OnboardingPermissionStatus.UNAVAILABLE -> R.string.onboarding_permission_unavailable
    }
