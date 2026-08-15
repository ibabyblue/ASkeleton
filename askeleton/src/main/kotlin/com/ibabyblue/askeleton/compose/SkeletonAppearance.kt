package com.ibabyblue.askeleton.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.ibabyblue.askeleton.SkeletonConfiguration

/** Appearance inherited by Compose skeleton modifiers in the current subtree. */
public val LocalSkeletonAppearance = staticCompositionLocalOf { SkeletonConfiguration.Default }

/** Supplies [appearance] to skeleton modifiers in [content], with normal nested override semantics. */
@Composable
public fun SkeletonAppearance(
    appearance: SkeletonConfiguration,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalSkeletonAppearance provides appearance, content = content)
}
