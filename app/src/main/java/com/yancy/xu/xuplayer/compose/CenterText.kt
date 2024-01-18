package com.yancy.xu.xuplayer.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/**
 *
 * @date: 2024/1/18
 * @author: XuYanjun
 */

@Composable
fun CenterText(
    text: String,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    style: TextStyle = LocalTextStyle.current
) {
    Box(contentAlignment = contentAlignment, modifier = modifier) {
        Text(
            text = text,
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

}