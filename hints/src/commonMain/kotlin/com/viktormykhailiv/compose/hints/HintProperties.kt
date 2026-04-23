package com.viktormykhailiv.compose.hints

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlin.time.Duration

/**
 * Alignment of the hint relative to its anchor.
 */
enum class HintAlignment {
    /**
     * Placed above the anchor, centered horizontally.
     * Smart positioning: if it doesn't fit on top, it flips to the bottom.
     */
    Top,

    /**
     * Placed below the anchor, centered horizontally.
     * Smart positioning: if it doesn't fit at the bottom, it flips to the top.
     */
    Bottom,

    /**
     * Placed at the start of the anchor (left in LTR, right in RTL), centered vertically.
     * Smart positioning: if it doesn't fit at the start, it flips to the end.
     */
    Start,

    /**
     * Placed at the end of the anchor (right in LTR, left in RTL), centered vertically.
     * Smart positioning: if it doesn't fit at the end, it flips to the start.
     */
    End,

    /**
     * Perfectly centered over the anchor on both X and Y axes.
     */
    Center,

    /**
     * Aligns the top-start corner of the hint with the top-start corner of the anchor.
     * Useful as a base for custom manual offsets relative to the anchor's origin.
     */
    Overlap,

    // Specific alignments with smart flipping

    /**
     * Above the anchor, aligned to the start edge. Flips to [BottomStart] if no space.
     */
    TopStart,

    /**
     * Above the anchor, aligned to the end edge. Flips to [BottomEnd] if no space.
     */
    TopEnd,

    /**
     * Below the anchor, aligned to the start edge. Flips to [TopStart] if no space.
     */
    BottomStart,

    /**
     * Below the anchor, aligned to the end edge. Flips to [TopEnd] if no space.
     */
    BottomEnd,

    /**
     * At the start of the anchor, aligned to the top edge. Flips to [EndTop] if no space.
     */
    StartTop,

    /**
     * At the start of the anchor, aligned to the bottom edge. Flips to [EndBottom] if no space.
     */
    StartBottom,

    /**
     * At the end of the anchor, aligned to the top edge. Flips to [StartTop] if no space.
     */
    EndTop,

    /**
     * At the end of the anchor, aligned to the bottom edge. Flips to [StartBottom] if no space.
     */
    EndBottom,
}

/**
 * Properties used to customize the behavior of a [Hint].
 *
 * @property dismissOnBackPress whether the hint can be dismissed by pressing the back or escape
 *   buttons on Android or the escape key on desktop. If true, pressing the back button will call
 *   onDismissRequest.
 * @property dismissOnClickOutside whether the hint can be dismissed by clicking outside the
 *   hint's bounds.
 * @property alignment the alignment of the hint relative to its anchor.
 * @property padding the gap between the hint content and its anchor (or the hole).
 * @property holePadding the padding to apply to the hole (the highlighted area) around the
 *   anchor. This expands the cut-out area without affecting the actual UI layout.
 * @property autoAdvanceDuration the duration after which the hint will automatically move to
 *   the next step in the sequence (or dismiss if it's the last step). If null, the hint
 *   will stay until manually dismissed.
 * @property offset additional offset to apply to the hint's position.
 */
@Immutable
data class HintProperties(
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
    val alignment: HintAlignment = HintAlignment.Bottom,
    val padding: Dp = 0.dp,
    val holePadding: Dp = 0.dp,
    val autoAdvanceDuration: Duration? = null,
    val offset: DpOffset = DpOffset.Zero,
)
