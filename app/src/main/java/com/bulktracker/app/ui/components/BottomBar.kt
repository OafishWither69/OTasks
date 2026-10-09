package com.bulktracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.Tokens

data class BottomBarItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun CustomBottomBar(
    items: List<BottomBarItem>,
    currentRoute: String?,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BulkTrackerTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
    ) {
        // 1dp top divider in border colour (Block C)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Tokens.BorderThin)
                .background(colors.border)
        )

        // Main navigation bar row: height 64dp, handles navigation bars padding seamlessly
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = item.route == currentRoute
                val interactionSource = remember { MutableInteractionSource() }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(bounded = false, radius = 28.dp),
                            role = Role.Tab,
                            onClick = { onItemSelected(item.route) }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Squircle indicator for icon (Block C & Block E V5)
                    Box(
                        modifier = Modifier
                            .size(width = 48.dp, height = 32.dp)
                            .clip(RoundedCornerShape(Tokens.RadiusLg))
                            .background(if (selected) colors.border else androidx.compose.ui.graphics.Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (selected) colors.accent else colors.textTertiary,
                            modifier = Modifier.size(Tokens.IconMd)
                        )
                    }

                    Text(
                        text = item.label,
                        fontSize = Tokens.LabelSize,
                        lineHeight = Tokens.LabelLineHeight,
                        color = if (selected) colors.textPrimary else colors.textTertiary,
                        maxLines = 1,
                        modifier = Modifier.padding(top = Tokens.SpaceXxs)
                    )
                }
            }
        }

        // Window insets spacer for 3-button navigation / gesture bar with matching background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        )
    }
}
