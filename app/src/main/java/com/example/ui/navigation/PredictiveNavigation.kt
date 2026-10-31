package com.example.ui.navigation

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.data.tools.ToolDefinition
import com.example.ui.theme.PaperBackground
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Type-safe representation of every full-screen stack destination across TDlxHT.
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

  // 3. Modal Surfaces & Overlays (Mapped to AppNavOverlay)
  data object QuickCreate : AppNavDestination
  data class FocusTimer(val initialMinutes: Int = 25) : AppNavDestination
}

/**
 * Modal surfaces that float above the active destination without unmounting it.
 */
sealed interface AppNavOverlay {
  data object QuickCreate : AppNavOverlay
  data class FocusTimer(val initialMinutes: Int = 25) : AppNavOverlay
  data class ToolRunner(val tool: ToolDefinition) : AppNavOverlay
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
 * Centralized stateful navigation controller managing the global backstack, overlays, and transitions.
 */
@Stable
class PredictiveNavController(
  val initialDestination: AppNavDestination = AppNavDestination.Main(0)
) {
  val backStack = mutableStateListOf<AppNavDestination>(initialDestination)
  var activeOverlay by mutableStateOf<AppNavOverlay?>(null)
  val gestureState = PredictiveBackGestureState()
  var isTransitioning by mutableStateOf(false)

  // Tracks navigation direction for AnimatedContent transitions (true = forward, false = backward)
  var isForwardNavigation by mutableStateOf(true)
    internal set

  val currentDestination: AppNavDestination
    get() = backStack.lastOrNull() ?: initialDestination

  val previousDestination: AppNavDestination?
    get() = if (backStack.size > 1) backStack[backStack.size - 2] else null

  val canNavigateBack: Boolean
    get() = activeOverlay != null || backStack.size > 1 || (currentDestination is AppNavDestination.Main && (currentDestination as AppNavDestination.Main).tabIndex != 0)

  val canPredictiveBack: Boolean
    get() = activeOverlay == null && backStack.size > 1 && !isTransitioning

  /**
   * Navigate forward to a new destination or open an overlay.
   */
  fun navigate(destination: AppNavDestination) {
    if (isTransitioning) return
    when (destination) {
      is AppNavDestination.QuickCreate -> {
        activeOverlay = AppNavOverlay.QuickCreate
      }
      is AppNavDestination.FocusTimer -> {
        activeOverlay = AppNavOverlay.FocusTimer(destination.initialMinutes)
      }
      is AppNavDestination.ToolRunner -> {
        activeOverlay = AppNavOverlay.ToolRunner(destination.tool)
      }
      else -> {
        activeOverlay = null
        if (backStack.lastOrNull() == destination) return
        isForwardNavigation = true
        backStack.add(destination)
      }
    }
  }

  /**
   * Replace the top destination without expanding stack depth.
   */
  fun replaceTop(destination: AppNavDestination) {
    if (isTransitioning) return
    activeOverlay = null
    when (destination) {
      is AppNavDestination.QuickCreate -> {
        activeOverlay = AppNavOverlay.QuickCreate
      }
      is AppNavDestination.FocusTimer -> {
        activeOverlay = AppNavOverlay.FocusTimer(destination.initialMinutes)
      }
      is AppNavDestination.ToolRunner -> {
        activeOverlay = AppNavOverlay.ToolRunner(destination.tool)
      }
      else -> {
        if (backStack.isNotEmpty()) {
          backStack[backStack.size - 1] = destination
        } else {
          backStack.add(destination)
        }
      }
    }
  }

  /**
   * Pop the current destination or dismiss the active overlay.
   */
  fun navigateBack(): Boolean {
    if (isTransitioning) return true
    if (activeOverlay != null) {
      activeOverlay = null
      return true
    }
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
   * Directly pop the top backstack entry upon gesture commit completion.
   */
  fun popBackStackDirect(): Boolean {
    if (backStack.size > 1) {
      isForwardNavigation = false
      backStack.removeAt(backStack.size - 1)
      return true
    }
    return false
  }

  /**
   * Dismiss the active overlay modal.
   */
  fun dismissOverlay() {
    activeOverlay = null
  }

  /**
   * Dismiss an active modal or pop to root Main(0).
   */
  fun popToRoot() {
    activeOverlay = null
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
 * 1. Physical two-layer navigation transition during Android Predictive Back gestures.
 * 2. Background layer: Real previous screen from the back stack in its exact natural position.
 * 3. Foreground layer: Solid, elevated active screen card moving cleanly off-screen with drop shadow.
 * 4. Zero black screens: Guaranteed continuous PaperBackground compositing.
 * 5. Safe backstack popping: Destination removed only after moving 100% off-screen.
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
  val density = LocalDensity.current

  // System PredictiveBackHandler integration with strict backstack guards
  PredictiveBackHandler(enabled = navController.canPredictiveBack) { progressFlow: Flow<BackEventCompat> ->
    gestureState.isPredictingBack = true
    navController.isTransitioning = true
    try {
      progressFlow.collect { backEvent ->
        gestureState.progress = backEvent.progress
        gestureState.swipeEdge = backEvent.swipeEdge
        gestureState.touchX = backEvent.touchX
        gestureState.touchY = backEvent.touchY
        gestureState.animProgress.snapTo(backEvent.progress)
      }
      // Gesture committed past threshold: animate foreground card smoothly off-screen
      gestureState.animProgress.animateTo(
        targetValue = 1f,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioNoBouncy,
          stiffness = Spring.StiffnessMediumLow
        )
      )
      // Pop the destination after the card has cleanly exited the viewport
      navController.popBackStackDirect()
    } catch (_: CancellationException) {
      // Gesture cancelled: spring active screen smoothly back to rest (0f)
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
      navController.isTransitioning = false
    }
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .background(PaperBackground)
  ) {
    val fullWidthPx = with(density) { maxWidth.toPx() }

    // -------------------------------------------------------------
    // LAYER 0: PREVIOUS SCREEN (UNDERNEATH PREVIEW DURING GESTURE)
    // -------------------------------------------------------------
    if (gestureState.isPredictingBack && previousDest != null) {
      val progress = gestureState.animProgress.value
      // Previous screen rises smoothly from depth: scale 0.95f -> 1.0f
      val prevScale = 0.95f + (progress * 0.05f)
      val prevCorner = ((1f - progress) * 16).dp
      val scrimAlpha = ((1f - progress) * 0.18f).coerceIn(0f, 0.18f)

      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(PaperBackground)
          .graphicsLayer {
            scaleX = prevScale
            scaleY = prevScale
            shape = RoundedCornerShape(prevCorner)
            clip = prevCorner > 0.dp
          }
      ) {
        content(previousDest)
        // Subtle ambient scrim that fades out cleanly as the previous screen arrives
        if (scrimAlpha > 0f) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = scrimAlpha))
          )
        }
      }
    }

    // -------------------------------------------------------------
    // LAYER 1: ACTIVE SCREEN (TOP / GESTURE OR ANIMATED CONTENT)
    // -------------------------------------------------------------
    if (gestureState.isPredictingBack) {
      val progress = gestureState.animProgress.value
      // Foreground card scales subtly and rounds its corners
      val activeScale = 1f - (progress * 0.08f)
      val activeCorner = (progress * 24).dp
      val direction = if (gestureState.swipeEdge == BackEventCompat.EDGE_RIGHT) -1f else 1f

      // Translation starts tactile and accelerates off-screen on commit (progress -> 1f)
      val translationPx = fullWidthPx * progress * direction
      val shadowElevationPx = with(density) { 20.dp.toPx() }
      // Card stays 100% solid and opaque while dragging; fades only near the very edge (progress > 0.7f)
      val activeAlpha = if (progress > 0.70f) {
        ((1f - progress) / 0.30f).coerceIn(0f, 1f)
      } else {
        1f
      }

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
          .background(PaperBackground)
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
              initialOffsetX = { fullWidth -> fullWidth / 4 },
              animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f)
            ) + scaleIn(
              initialScale = 0.96f,
              animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f)
            ) + fadeIn(
              animationSpec = tween(220, easing = FastOutSlowInEasing),
              initialAlpha = 0.5f
            )).togetherWith(
              slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 6 },
                animationSpec = tween(200, easing = FastOutSlowInEasing)
              ) + scaleOut(
                targetScale = 0.96f,
                animationSpec = tween(200, easing = FastOutSlowInEasing)
              ) + fadeOut(
                animationSpec = tween(180, easing = FastOutSlowInEasing),
                targetAlpha = 0.3f
              )
            )
          } else {
            // Backward Navigation: Previous destination returns from depth
            (slideInHorizontally(
              initialOffsetX = { fullWidth -> -fullWidth / 6 },
              animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f)
            ) + scaleIn(
              initialScale = 0.96f,
              animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f)
            ) + fadeIn(
              animationSpec = tween(220, easing = FastOutSlowInEasing),
              initialAlpha = 0.5f
            )).togetherWith(
              slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth / 4 },
                animationSpec = tween(200, easing = FastOutSlowInEasing)
              ) + scaleOut(
                targetScale = 0.96f,
                animationSpec = tween(200, easing = FastOutSlowInEasing)
              ) + fadeOut(
                animationSpec = tween(180, easing = FastOutSlowInEasing),
                targetAlpha = 0.3f
              )
            )
          }
        },
        label = "predictiveNavHost",
        modifier = Modifier
          .fillMaxSize()
          .background(PaperBackground)
      ) { destination ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(PaperBackground)
        ) {
          content(destination)
        }
      }
    }
  }
}
