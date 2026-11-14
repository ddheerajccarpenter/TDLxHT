package com.example

import com.example.ui.components.tabPositionAt
import com.example.ui.components.visualTabPosition
import org.junit.Assert.assertEquals
import org.junit.Test

class TabGeometryTest {
  @Test fun tabCentersMatchVisibleOrderInBothDirections() {
    for (count in 2..4) for (rtl in listOf(false, true)) for (index in 0 until count) {
      val physicalSlot = if (rtl) count - 1 - index else index
      assertEquals(index.toFloat(), tabPositionAt((physicalSlot + 0.5f) * 80f, 80f, count, rtl), 0.001f)
    }
  }

  @Test fun draggingOutsideTheTrackClampsToItsEnds() {
    assertEquals(0f, tabPositionAt(-100f, 80f, 4, false), 0f)
    assertEquals(3f, tabPositionAt(500f, 80f, 4, false), 0f)
    assertEquals(3f, tabPositionAt(-100f, 80f, 4, true), 0f)
    assertEquals(0f, tabPositionAt(500f, 80f, 4, true), 0f)
  }

  @Test fun fractionalPagerPositionsMirrorContinuously() {
    assertEquals(2.75f, visualTabPosition(0.25f, 4, true), 0f)
    assertEquals(0.25f, visualTabPosition(0.25f, 4, false), 0f)
    assertEquals(0f, tabPositionAt(1f, 0f, 4, false), 0f)
    assertEquals(0f, tabPositionAt(40f, 80f, 1, true), 0f)
  }
}
