package com.example.rinconsur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rinconsur.R
import com.example.rinconsur.ui.theme.Dimens

@Composable
fun BannerNotificacion(
    mensaje: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.cornerMedium))
            .background(Color(0xFFE8F5E9))
            .padding(horizontal = Dimens.espacioMedio, vertical = Dimens.espacioChico),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            tint = Color(0xFF2E7D32),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(Dimens.espacioChico))
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF2E7D32),
            fontWeight = FontWeight.SemiBold
        )
    }
}