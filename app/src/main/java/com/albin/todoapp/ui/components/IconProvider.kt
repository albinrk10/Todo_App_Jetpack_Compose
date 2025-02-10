package com.albin.todoapp.ui.components
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class IconProvider : PreviewParameterProvider<IconContainer> {
    override  val values: Sequence<IconContainer>
        get() = sequenceOf(
            IconContainer(icon = Icons.Default.Add),
            IconContainer(icon = Icons.Default.MoreVert),
            IconContainer(icon = Icons.Default.Star),
            IconContainer(icon = Icons.Default.Search),

        )
}