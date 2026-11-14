package com.example.ui.components

/** Convert a physical pointer coordinate to the logical tab order used by the pager. */
internal fun tabPositionAt(x: Float, slotWidth: Float, count: Int, isRtl: Boolean): Float {
  if (count <= 1 || slotWidth <= 0f) return 0f
  val physical = (x / slotWidth - 0.5f).coerceIn(0f, (count - 1).toFloat())
  return visualTabPosition(physical, count, isRtl)
}

/** Mirroring is its own inverse; keep animation state in logical order. */
internal fun visualTabPosition(position: Float, count: Int, isRtl: Boolean): Float =
  if (isRtl) count - 1f - position else position
