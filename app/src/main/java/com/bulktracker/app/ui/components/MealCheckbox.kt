package com.bulktracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bulktracker.app.ui.theme.Tokens

@Composable
fun MealCheckbox(
    checked: Boolean,
    sectionColor: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Tokens.RadiusSm)
    Box(
        modifier = modifier
            .size(Tokens.Checkbox)
            .then(
                if (checked) {
                    Modifier.background(sectionColor, shape)
                } else {
                    Modifier
                        .border(Tokens.BorderFocus, sectionColor, shape)
                        .background(Color.Transparent, shape)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
