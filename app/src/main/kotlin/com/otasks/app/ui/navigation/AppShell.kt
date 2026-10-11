// SPDX-License-Identifier: GPL-3.0-or-later

package com.otasks.app.ui.navigation
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
import com.otasks.app.R
import com.otasks.app.ui.theme.AppType
import com.otasks.app.ui.theme.LocalAppColors
import com.otasks.app.ui.theme.Tokens
import kotlinx.serialization.Serializable

@Serializable object Today

@Serializable object Schedule

@Serializable object History

@Serializable object Settings

private data class Destination(
    val route: Any,
    val label: Int,
    val icon: ImageVector,
)

private val destinations =
    listOf(
        Destination(Today, R.string.today, Icons.Outlined.CheckCircle),
        Destination(Schedule, R.string.schedule, Icons.Outlined.DateRange),
        Destination(History, R.string.history, Icons.Outlined.CalendarMonth),
        Destination(Settings, R.string.settings, Icons.Outlined.Settings),
    )

@Composable
fun AppShell() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val wide = currentWindowAdaptiveInfoV2().windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val colors = LocalAppColors.current
    val focus = LocalFocusManager.current

    fun go(d: Destination) {
        nav.navigate(d.route) {
            popUpTo(nav.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState =
                true
        }
    }
    val navigation: @Composable (Boolean) -> Unit = { rail ->
        val modifier =
            Modifier.background(colors.surface).focusGroup().onPreviewKeyEvent {
                if (it.type == KeyEventType.KeyDown && it.key in
                    listOf(
                        Key.DirectionLeft,
                        Key.DirectionRight,
                        Key.DirectionUp,
                        Key.DirectionDown,
                    )
                ) {
                    focus.moveFocus(
                        if (it.key == Key.DirectionLeft ||
                            it.key == Key.DirectionUp
                        ) {
                            FocusDirection.Previous
                        } else {
                            FocusDirection.Next
                        },
                    )
                    true
                } else {
                    false
                }
            }
        val items: @Composable (Modifier) -> Unit = { itemModifier ->
            destinations.forEach { d ->
                val selected = entry?.destination?.hasRoute(d.route::class) == true
                Column(
                    itemModifier
                        .testTag("nav-${d.label}")
                        .heightIn(min = Tokens.Size.bottomBar)
                        .clickable(role = Role.Tab) { go(d) }
                        .semantics {
                            this.selected =
                                selected
                        }.padding(vertical = Tokens.Space.xs),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        Modifier
                            .size(
                                Tokens.Size.indicatorW,
                                Tokens.Size.indicatorH,
                            ).background(if (selected) colors.border else Color.Transparent, RoundedCornerShape(Tokens.Radius.indicator)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(d.icon, null, Modifier.size(Tokens.Size.iconMd), tint = if (selected) colors.accent else colors.textTertiary)
                    }
                    Spacer(Modifier.height(Tokens.Space.xs))
                    Text(
                        stringResource(d.label),
                        textAlign = TextAlign.Center,
                        style = AppType.label,
                        color = if (selected) colors.textPrimary else colors.textTertiary,
                    )
                }
            }
        }
        if (rail) {
            Row(modifier) {
                Column(
                    Modifier
                        .windowInsetsPadding(
                            WindowInsets.systemBars.only(WindowInsetsSides.Start + WindowInsetsSides.Vertical),
                        ).windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Start))
                        .padding(top = Tokens.Space.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(Modifier.width(Tokens.Size.rail))
                }
                Box(Modifier.width(Tokens.Border.thin).fillMaxHeight().background(colors.border))
            }
        } else {
            Column(modifier) {
                HorizontalDivider(thickness = Tokens.Border.thin, color = colors.border)
                Row(Modifier.navigationBarsPadding(), verticalAlignment = Alignment.CenterVertically) { items(Modifier.weight(1f)) }
            }
        }
    }
    Scaffold(contentWindowInsets = WindowInsets(0), containerColor = colors.background, bottomBar = {
        if (!wide) navigation(false)
    }) { padding ->
        Row(Modifier.fillMaxSize().padding(padding)) {
            if (wide) navigation(true)
            NavHost(
                nav,
                Today,
                Modifier
                    .weight(
                        1f,
                    ).fillMaxHeight()
                    .windowInsetsPadding(
                        WindowInsets.displayCutout.only(WindowInsetsSides.End),
                    ).windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.End)),
            ) {
                composable<Today> { FoundationScreen(R.string.today, wide) }
                composable<Schedule> { FoundationScreen(R.string.schedule, wide) }
                composable<History> { FoundationScreen(R.string.history, wide) }
                composable<Settings> { FoundationScreen(R.string.settings, wide) }
            }
        }
    }
}

@Composable
private fun FoundationScreen(
    title: Int,
    wide: Boolean,
) {
    val colors = LocalAppColors.current
    Column(
        Modifier.fillMaxSize().statusBarsPadding().then(if (wide) Modifier.navigationBarsPadding() else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier
                .widthIn(
                    max = Tokens.Layout.maxContent,
                ).fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(if (wide) Tokens.Layout.gutterWide else Tokens.Layout.gutter),
        ) {
            Text(stringResource(title), Modifier.semantics { heading() }, style = AppType.titleLg, color = colors.textPrimary)
            Spacer(Modifier.height(Tokens.Space.xl))
            Text(stringResource(R.string.later_phase), style = AppType.body, color = colors.textSecondary)
        }
    }
}
