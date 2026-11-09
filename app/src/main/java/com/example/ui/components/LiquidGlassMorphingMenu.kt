package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.glass.liquidGlassRim
import com.example.ui.navigation.PredictiveBackGestureState
import com.example.ui.navigation.PredictiveBackTarget
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.StrongFontFamily
import com.example.util.VibrationHelper
import dev.chrisbanes.haze.hazeEffect

/**
 * Authentic iOS 26 Liquid Glass Morphing Menu Container.
 *
 * Adheres to `/liquid-glass-design` patterns:
 * - In-layout morphing container that seamlessly surfaces from the action button.
 * - Directional specular rim bevels & authentic frosted glass translucency.
 * - Predictive Back gesture integration: smoothly translates downward and fades out as user swipes back.
 */
@Composable
fun LiquidGlassQuickCreateMenu(
  visible: Boolean,
  onDismissRequest: () -> Unit,
  onNewTask: () -> Unit,
  onNewHabit: () -> Unit,
  onShortTermTask: () -> Unit,
  modifier: Modifier = Modifier,
  gestureState: PredictiveBackGestureState? = null,
  hazeState: dev.chrisbanes.haze.HazeState? = null
) {
  val context = LocalContext.current
  val density = LocalDensity.current
  val colors = LocalAppColors.current
  val isDark = colors.isDark

  val menuShape = RoundedCornerShape(24.dp)
  val isPredictingBack = gestureState?.isPredictingBack == true && gestureState.targetKind == PredictiveBackTarget.OVERLAY
  val gestureProgress = gestureState?.animProgress?.value ?: 0f

  val scrimAlpha by animateFloatAsState(
    targetValue = if (visible && !isPredictingBack) 0.38f else if (isPredictingBack) (0.38f * (1f - gestureProgress)).coerceIn(0f, 0.38f) else 0f,
    animationSpec = tween(180),
    label = "scrimAlpha"
  )

  if (visible || isPredictingBack || scrimAlpha > 0.01f) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .zIndex(100f),
      contentAlignment = Alignment.Center
    ) {
      // 1. Ambient Backdrop Scrim
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = scrimAlpha))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) {
            onDismissRequest()
          }
      )

      // 2. Liquid Glass Card Container
      val translationYPx = with(density) { (220.dp * gestureProgress).toPx() }
      val activeScale = if (isPredictingBack) (1f - gestureProgress * 0.12f).coerceIn(0.88f, 1f) else 1f
      val activeAlpha = if (isPredictingBack) ((1f - gestureProgress * 1.3f)).coerceIn(0f, 1f) else 1f

      AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
          initialScale = 0.82f,
          animationSpec = spring(dampingRatio = 0.76f, stiffness = 460f)
        ) + fadeIn(tween(160)),
        exit = scaleOut(
          targetScale = 0.88f,
          animationSpec = tween(140)
        ) + fadeOut(tween(120))
      ) {
        val glassBrush = if (isDark) {
          Brush.verticalGradient(
            listOf(
              Color(0xD0242834),
              Color(0xD0181A22)
            )
          )
        } else {
          Brush.verticalGradient(
            listOf(
              Color(0xF0FFFFFF),
              Color(0xE6F8F6F2)
            )
          )
        }

        val cardModifier = if (hazeState != null) {
          Modifier.clip(menuShape).hazeEffect(state = hazeState) {
            blurRadius = 20.dp
          }
        } else Modifier

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .graphicsLayer {
              translationY = translationYPx
              scaleX = activeScale
              scaleY = activeScale
              alpha = activeAlpha
              shadowElevation = (if (isDark) 24.dp else 16.dp).toPx()
              spotShadowColor = if (isDark) Color.Black.copy(alpha = 0.60f) else Color.Black.copy(alpha = 0.14f)
              ambientShadowColor = if (isDark) Color.Black.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.06f)
              shape = menuShape
              clip = false
            }
            .then(cardModifier)
            .background(glassBrush, menuShape)
            .liquidGlassRim(menuShape, isDark, 1.dp)
            .padding(22.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Drag Indicator Handle
            Box(
              modifier = Modifier
                .size(width = 36.dp, height = 4.dp)
                .clip(CircleShape)
                .background(if (isDark) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.18f))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Header Row: Title & Close Button
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "QUICK CREATE",
                fontFamily = StrongFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = if (isDark) Color.White else Color(0xFF1C1C1E)
              )

              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.05f))
                  .liquidGlassRim(CircleShape, isDark, 0.75.dp)
                  .bounceClick {
                    VibrationHelper.click(context)
                    onDismissRequest()
                  },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close",
                  tint = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF1C1C1E),
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action 1: New Task
            LiquidGlassMenuItem(
              icon = Icons.Rounded.Checklist,
              title = "NEW TASK",
              subtitle = "Actionable to-do with due dates & subtasks",
              isDark = isDark,
              onClick = {
                VibrationHelper.tick(context)
                onNewTask()
              }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action 2: New Habit
            LiquidGlassMenuItem(
              icon = Icons.Default.CalendarToday,
              title = "NEW HABIT",
              subtitle = "Track daily routines & maintain momentum",
              isDark = isDark,
              onClick = {
                VibrationHelper.tick(context)
                onNewHabit()
              }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action 3: Short-Term Task
            LiquidGlassMenuItem(
              icon = Icons.Default.Check,
              title = "SHORT-TERM TASK",
              subtitle = "Stored locally • Auto-clears when complete",
              isDark = isDark,
              onClick = {
                VibrationHelper.tick(context)
                onShortTermTask()
              }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun LiquidGlassMenuItem(
  icon: ImageVector,
  title: String,
  subtitle: String,
  isDark: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val itemShape = RoundedCornerShape(16.dp)
  val itemBg = if (isDark) {
    Color.White.copy(alpha = 0.07f)
  } else {
    Color.White.copy(alpha = 0.70f)
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .bounceClick { onClick() }
      .background(itemBg, itemShape)
      .liquidGlassRim(itemShape, isDark, 0.75.dp)
      .padding(horizontal = 16.dp, vertical = 14.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.05f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (isDark) Color.White else Color(0xFF1C1C1E),
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontFamily = StrongFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = if (isDark) Color.White else Color(0xFF1C1C1E)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          fontSize = 12.sp,
          color = if (isDark) Color.White.copy(alpha = 0.55f) else Color(0xFF6B6E76),
          lineHeight = 16.sp
        )
      }

      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = null,
        tint = if (isDark) Color.White.copy(alpha = 0.35f) else Color(0xFF8E8E93),
        modifier = Modifier.size(18.dp)
      )
    }
  }
}
