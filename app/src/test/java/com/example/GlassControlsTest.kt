package com.example

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.components.AppleCircularCheckbox
import com.example.ui.components.LiquidGlassNavItem
import com.example.ui.components.LiquidGlassSegmentedControl
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@OptIn(ExperimentalTestApi::class)
class GlassControlsTest {
  @get:Rule val compose = createComposeRule()

  @Test fun navigationCanBeFocusedAndActivatedWithEnter() {
    val selected = mutableStateOf(false)
    var clicks = 0
    compose.setContent {
      val input = LocalInputModeManager.current
      SideEffect { input.requestInputMode(InputMode.Keyboard) }
      MyApplicationTheme {
        Box(Modifier.size(80.dp, 56.dp)) {
          LiquidGlassNavItem(AppDestination.INSIGHTS, selected.value, {
            selected.value = true
            clicks++
          })
        }
      }
    }
    val tab = compose.onNodeWithContentDescription("Insights")
    tab.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
    tab.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
    tab.assertIsSelected()
    compose.runOnIdle { assertEquals(1, clicks) }
  }

  @Test fun checkboxHasANameStateAndFullTouchTarget() {
    val checked = mutableStateOf(false)
    compose.setContent {
      MyApplicationTheme {
        AppleCircularCheckbox(checked.value, { checked.value = it }, contentDescription = "Read")
      }
    }
    val checkbox = compose.onNodeWithContentDescription("Read")
    checkbox.assertIsOff()
    // Outside the 22dp visual circle, inside the 48dp control.
    checkbox.performTouchInput { click(Offset(3f, 3f)) }
    checkbox.assertIsOn()
  }

  @Test fun rtlTapSelectsTheLabelUnderThePointerOnce() {
    val selected = mutableStateOf(0)
    var clicks = 0
    compose.setContent {
      CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MyApplicationTheme {
          LiquidGlassSegmentedControl(listOf("All", "Tasks", "Habits"), selected.value, {
            selected.value = it
            clicks++
          }, Modifier.size(300.dp, 56.dp).testTag("segments"))
        }
      }
    }
    compose.onNodeWithText("Habits").performTouchInput { click() }
    compose.onNodeWithText("Habits").assertIsSelected()
    compose.runOnIdle {
      assertEquals(2, selected.value)
      assertEquals(1, clicks)
    }
  }

  @Test fun rtlDragCommitsTheFinalFingerPosition() {
    val selected = mutableStateOf(0)
    compose.setContent {
      CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MyApplicationTheme {
          LiquidGlassSegmentedControl(listOf("All", "Tasks", "Habits"), selected.value,
            { selected.value = it }, Modifier.size(300.dp, 56.dp).testTag("segments"))
        }
      }
    }
    compose.onNodeWithTag("segments").performTouchInput {
      swipe(Offset(width * 0.83f, centerY), Offset(width * 0.17f, centerY), 80)
    }
    compose.onNodeWithText("Habits").assertIsSelected()
  }
}
