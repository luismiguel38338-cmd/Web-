package com.example.searchpro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.searchpro.ui.theme.StatusActive
import com.example.searchpro.ui.theme.StatusArchived
import com.example.searchpro.ui.theme.StatusDraft
import com.example.searchpro.ui.theme.StatusPending

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val (color, localizedText) = when (status.lowercase()) {
        "activo", "active" -> StatusActive to stringResource(R.string.filters_status_active)
        "borrador", "draft" -> StatusDraft to stringResource(R.string.filters_status_draft)
        "pendiente", "pending" -> StatusPending to stringResource(R.string.filters_status_pending)
        "archivado", "archived" -> StatusArchived to stringResource(R.string.filters_status_archived)
        else -> MaterialTheme.colorScheme.secondary to status
    }

    Box(
        modifier = modifier
            .testTag("status_chip_${status.lowercase()}")
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = localizedText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        )
    }
}
