package com.example.ui.components

import com.skydoves.cloudy.cloudy

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
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import com.example.data.model.Task
import com.example.util.NaturalLanguageTaskParser
import java.time.format.DateTimeFormatter
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
  onOpenTools: (() -> Unit)? = null,
  onQuickCaptureTask: ((Task) -> Unit)? = null,
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

  val animatedScrimAlpha by animateFloatAsState(
    targetValue = if (visible) 0.38f else 0f,
    animationSpec = tween(180),
    label = "scrimAlpha"
  )
  val scrimAlpha = if (isPredictingBack) {
    (0.38f * (1f - gestureProgress)).coerceIn(0f, 0.38f)
  } else {
    animatedScrimAlpha
  }

  var quickText by remember { mutableStateOf("") }
  val parsedTask = remember(quickText) { NaturalLanguageTaskParser.parse(quickText) }

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
        } else {
          Modifier.clip(menuShape).cloudy(radius = 20)
        }

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

            // Header Row: Clean Title
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Start,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Quick Create",
                fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = if (isDark) Color.White else Color(0xFF1C1C1E)
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Smart Natural Language Quick Capture Box
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.85f),
                  RoundedCornerShape(16.dp)
                )
                .liquidGlassRim(RoundedCornerShape(16.dp), isDark, 0.75.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(
                  imageVector = Icons.Rounded.AutoAwesome,
                  contentDescription = null,
                  tint = if (isDark) Color(0xFF64D2FF) else Color(0xFF007AFF),
                  modifier = Modifier.size(20.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                  if (quickText.isEmpty()) {
                    Text(
                      text = "Try 'Gym tomorrow 7am !high'...",
                      style = TextStyle(
                        fontSize = 14.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.38f) else Color.Black.copy(alpha = 0.38f)
                      )
                    )
                  }
                  BasicTextField(
                    value = quickText,
                    onValueChange = { quickText = it },
                    singleLine = true,
                    textStyle = TextStyle(
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Medium,
                      color = if (isDark) Color.White else Color(0xFF1C1C1E)
                    ),
                    cursorBrush = SolidColor(if (isDark) Color.White else Color.Black),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                      onDone = {
                        if (quickText.isNotBlank() && onQuickCaptureTask != null) {
                          VibrationHelper.tick(context)
                          val task = parsedTask.toTask()
                          onQuickCaptureTask(task)
                          quickText = ""
                        }
                      }
                    ),
                    modifier = Modifier.fillMaxWidth()
                  )
                }

                if (quickText.isNotBlank()) {
                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(CircleShape)
                      .background(if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF))
                      .clickable {
                        if (onQuickCaptureTask != null) {
                          VibrationHelper.tick(context)
                          val task = parsedTask.toTask()
                          onQuickCaptureTask(task)
                          quickText = ""
                        }
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.ArrowUpward,
                      contentDescription = "Create Task",
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }

            // Live parsed token pills
            if (quickText.isNotBlank()) {
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                if (parsedTask.hasExplicitDate) {
                  QuickTokenPill(
                    text = "📅 " + parsedTask.dueDate.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                    isDark = isDark
                  )
                }
                if (parsedTask.hasExplicitTime) {
                  QuickTokenPill(
                    text = "⏰ " + (parsedTask.dueTime?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: ""),
                    isDark = isDark
                  )
                }
                if (parsedTask.hasExplicitCategory) {
                  QuickTokenPill(
                    text = "🏷️ " + parsedTask.category,
                    isDark = isDark
                  )
                }
                if (parsedTask.hasExplicitPriority) {
                  QuickTokenPill(
                    text = "🚩 " + parsedTask.priority.displayName,
                    isDark = isDark
                  )
                }
                if (parsedTask.isShortTerm) {
                  QuickTokenPill(
                    text = "⚡ Scratch",
                    isDark = isDark
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtle divider
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(modifier = Modifier.weight(1f).height(0.5.dp).background(if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)))
              Text(
                text = "OR CHOOSE FORM",
                style = TextStyle(
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.35f),
                  letterSpacing = 0.8.sp
                )
              )
              Box(modifier = Modifier.weight(1f).height(0.5.dp).background(if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action 1: New Task
            LiquidGlassMenuItem(
              icon = Icons.Rounded.Checklist,
              title = "New Task",
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
              title = "New Habit",
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
              title = "Short-term Task",
              subtitle = "Stored locally • Auto-clears when complete",
              isDark = isDark,
              onClick = {
                VibrationHelper.tick(context)
                onShortTermTask()
              }
            )

            if (onOpenTools != null) {
              Spacer(modifier = Modifier.height(10.dp))

              // Action 4: Productivity Utilities
              LiquidGlassMenuItem(
                icon = Icons.Rounded.Handyman,
                title = "Productivity Calculators",
                subtitle = "15 practical offline tools & finance helpers",
                isDark = isDark,
                onClick = {
                  VibrationHelper.tick(context)
                  onOpenTools()
                }
              )
            }
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
          fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
          fontWeight = FontWeight.SemiBold,
          fontSize = 15.sp,
          color = if (isDark) Color.White else Color(0xFF1C1C1E)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
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

@Composable
private fun QuickTokenPill(
  text: String,
  isDark: Boolean,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .background(
        if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.07f),
        RoundedCornerShape(8.dp)
      )
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Text(
      text = text,
      style = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (isDark) Color(0xFF64D2FF) else Color(0xFF007AFF)
      )
    )
  }
}
