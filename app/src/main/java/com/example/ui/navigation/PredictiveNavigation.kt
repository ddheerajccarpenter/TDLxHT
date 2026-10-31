package com.example.ui.navigation

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.data.tools.ToolDefinition
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Type-safe representation of every navigation destination and modal across the TDlxHT application.
 */
sealed interface AppNavDestination {
  // 1. Primary Bottom Navigation (Home, Productivity, Tools, Insights)
  data class Main(val tabIndex: Int = 0) : AppNavDestination

  // 2. Full-Screen Detail & Utility Screens
  data class TaskDetail(
    val task: Task? = null,
    val initialTitle: String = "",
    val initialNotes: String = "",
    val initialIsShortTerm: Boolean = false
  ) : AppNavDestination

  data class HabitDetail(
    val habit: Habit? = null
  ) : AppNavDestination

  data object Settings : AppNavDestination
  data object Diagnostics : AppNavDestination
  data object Search : AppNavDestination
  data class ToolRunner(val tool: ToolDefinition) : AppNavDestination

  // 3. Modal Surfaces & Overlays
  data object QuickCreate : AppNavDestination
  data class FocusTimer(val initialMinutes: Int = 25) : AppNavDestination
}

/**
 * Encapsulates live predictive gesture telemetry (progress 0..1, edge, touch coordinates).
 */
@Stable
class PredictiveBackGestureState {
  var isPredictingBack by mutableStateOf(false)
  var progress by mutableFloatStateOf(0f)
  var swipeEdge by mutableIntStateOf(BackEventCompat.EDGE_LEFT)
  var touchX by mutableFloatStateOf(0f)
  var touchY by mutableFloatStateOf(0f)
  val animProgress = Animatable(0f)
}

/**
 * Centralized stateful navigation controller managing the global backstack and transitions.
 */
@Stable
class PredictiveNavController(
  val initialDestination: AppNavDestination = AppNavDestination.Main(0)
) {
  val backStack = mutableStateListOf<AppNavDestination>(initialDestination)
  val gestureState = PredictiveBackGestureState()

  // Tracks navigation direction for AnimatedContent transitions (true = forward, false = backward)
  var isForwardNavigation by mutableStateOf(true)
    internal set

  val currentDestination: AppNavDestination
    get() = backStack.lastOrNull() ?: initialDestination

  val previousDestination: AppNavDestination?
    get() = if (backStack.size > 1) backStack[backStack.size - 2] else null

  val canNavigateBack: Boolean
    get() = backStack.size > 1 || (currentDestination is AppNavDestination.Main && (currentDestination as AppNavDestination.Main).tabIndex != 0)

  /**
   * Navigate forward to a new destination.
   */
  fun navigate(destination: AppNavDestination) {
    if (backStack.lastOrNull() == destination) return
    isForwardNavigation = true
    backStack.add(destination)
  }

  /**
   * Pop the current destination from the stack.
   */
  fun navigateBack(): Boolean {
    if (backStack.size > 1) {
      isForwardNavigation = false
      backStack.removeAt(backStack.size - 1)
      return true
    } else if (currentDestination is AppNavDestination.Main && (currentDestination as AppNavDestination.Main).tabIndex != 0) {
      isForwardNavigation = false
      backStack[0] = AppNavDestination.Main(0)
      return true
    }
    return false
  }

  /**
   * Replace the top destination without expanding stack depth.
   */
  fun replaceTop(destination: AppNavDestination) {
    if (backStack.isNotEmpty()) {
      backStack[backStack.size - 1] = destination
    } else {
      backStack.add(destination)
    }
  }

  /**
   * Dismiss an active modal or pop to root Main(0).
   */
  fun popToRoot() {
    isForwardNavigation = false
    backStack.clear()
    backStack.add(AppNavDestination.Main(0))
  }
}

@Composable
fun rememberPredictiveNavController(
  initialDestination: AppNavDestination = AppNavDestination.Main(0)
): PredictiveNavController {
  return remember { PredictiveNavController(initialDestination) }
}

/**
 * Universal, global Predictive Navigation Host.
 *
 * Provides:
 * 1. Live gesture-progress tracking (scale, translationX, corner squircle, depth dimming)
 * 2. Real previous-screen live rendering underneath during back gesture
 * 3. Matching smooth forward and backward AnimatedContent transitions
 * 4. Android 13–15 system PredictiveBackHandler integration with older OS fallback
 */
@Composable
fun PredictiveNavigationHost(
  navController: PredictiveNavController,
  modifier: Modifier = Modifier,
  content: @Composable (AppNavDestination) -> Unit
) {
  val currentDest = navController.currentDestination
  val previousDest = navController.previousDestination
  val gestureState = navController.gestureState
  val coroutineScope = rememberCoroutineScope()
  val density = LocalDensity.current

  // Handle system edge-swipe and back gestures via PredictiveBackHandler
  PredictiveBackHandler(enabled = navController.canNavigateBack) { progressFlow: Flow<BackEventCompat> ->
    gestureState.isPredictingBack = true
    try {
      progressFlow.collect { backEvent ->
        gestureState.progress = backEvent.progress
        gestureState.swipeEdge = backEvent.swipeEdge
        gestureState.touchX = backEvent.touchX
        gestureState.touchY = backEvent.touchY
        gestureState.animProgress.snapTo(backEvent.progress)
      }
      // Gesture completed past commit threshold: animate smoothly to 1f and pop destination
      gestureState.animProgress.animateTo(
        targetValue = 1f,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioLowBouncy,
          stiffness = Spring.StiffnessMediumLow
        )
      )
      navController.navigateBack()
    } catch (_: CancellationException) {
      // Gesture cancelled: restore current screen smoothly back to 0f
      gestureState.animProgress.animateTo(
        targetValue = 0f,
        animationSpec = spring(
          dampingRatio = 0.78f,
          stiffness = 420f
        )
      )
    } finally {
      gestureState.isPredictingBack = false
      gestureState.animProgress.snapTo(0f)
      gestureState.progress = 0f
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    // -------------------------------------------------------------
    // LAYER 0: PREVIOUS SCREEN (UNDERNEATH PREVIEW DURING GESTURE)
    // -------------------------------------------------------------
    if (gestureState.isPredictingBack && previousDest != null) {
      val progress = gestureState.animProgress.value
      // Previous screen rises from depth: scale 0.93f -> 1.0f
      val prevScale = 0.93f + (progress * 0.07f)
      val scrimAlpha = (1f - progress) * 0.28f

      Box(
        modifier = Modifier
          .fillMaxSize()
          .graphicsLayer {
            scaleX = prevScale
            scaleY = prevScale
            shape = RoundedCornerShape(((1f - progress) * 16).dp)
            clip = true
          }
      ) {
        content(previousDest)
        // Subtle ambient dimming scrim that fades out as previous screen arrives
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = scrimAlpha))
        )
      }
    }

    // -------------------------------------------------------------
    // LAYER 1: ACTIVE SCREEN (TOP / GESTURE OR ANIMATED CONTENT)
    // -------------------------------------------------------------
    if (gestureState.isPredictingBack) {
      val progress = gestureState.animProgress.value
      // Active screen scales down subtly to 0.92f and rounds corners to 24dp
      val activeScale = 1f - (progress * 0.08f)
      val activeCorner = (progress * 24).dp
      val direction = if (gestureState.swipeEdge == BackEventCompat.EDGE_RIGHT) -1f else 1f
      val maxShiftPx = with(density) { 120.dp.toPx() }
      val translationPx = maxShiftPx * progress * direction
      val shadowElevationPx = with(density) { 16.dp.toPx() }
      val activeAlpha = 1f - (progress * 0.12f)

      Box(
        modifier = Modifier
          .fillMaxSize()
          .graphicsLayer {
            scaleX = activeScale
            scaleY = activeScale
            translationX = translationPx
            shape = RoundedCornerShape(activeCorner)
            clip = activeCorner > 0.dp
            shadowElevation = shadowElevationPx
            alpha = activeAlpha
          }
      ) {
        content(currentDest)
      }
    } else {
      // Standard forward/backward transitions when not actively dragging in gesture
      AnimatedContent(
        targetState = currentDest,
        transitionSpec = {
          if (navController.isForwardNavigation) {
            // Forward Navigation: Destination enters with subtle slide & elevation scale
            (slideInHorizontally(
              initialOffsetX = { fullWidth -> fullWidth / 5 },
              animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f)
            ) + scaleIn(
              initialScale = 0.96f,
              animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f)
            ) + fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)))
              .togetherWith(
                slideOutHorizontally(
                  targetOffsetX = { fullWidth -> -fullWidth / 6 },
                  animationSpec = tween(180, easing = FastOutSlowInEasing)
                ) + scaleOut(
                  targetScale = 0.96f,
                  animationSpec = tween(180, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(160))
              )
          } else {
            // Backward Navigation: Previous destination returns from depth
            (slideInHorizontally(
              initialOffsetX = { fullWidth -> -fullWidth / 6 },
              animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f)
            ) + scaleIn(
              initialScale = 0.96f,
              animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f)
            ) + fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)))
              .togetherWith(
                slideOutHorizontally(
                  targetOffsetX = { fullWidth -> fullWidth / 5 },
                  animationSpec = tween(180, easing = FastOutSlowInEasing)
                ) + scaleOut(
                  targetScale = 0.96f,
                  animationSpec = tween(180, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(160))
              )
          }
        },
        label = "predictiveNavHost",
        modifier = Modifier.fillMaxSize()
      ) { destination ->
        content(destination)
      }
    }
  }
}
