package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalAppColors

/**
 * Signature Apple Reminders circular checkbox.
 * Features a tactile scale spring, smooth color transition, and checkmark bloom.
 */
@Composable
fun AppleCircularCheckbox(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  size: Dp = 22.dp,
  accentColor: Color = LocalAppColors.current.pillBackgroundDark,
  contentDescription: String = "Completion"
) {
  val colors = LocalAppColors.current
  val interactionSource = remember { MutableInteractionSource() }

  val scale by animateFloatAsState(
    targetValue = if (checked) 1f else 0.88f,
    animationSpec = spring(dampingRatio = 0.65f, stiffness = 520f),
    label = "checkScale"
  )
  val bgColor by animateColorAsState(
    targetValue = if (checked) accentColor else Color.Transparent,
    animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
    label = "checkBg"
  )
  val borderColor by animateColorAsState(
    targetValue = if (checked) {
      accentColor
    } else {
      colors.inkSecondary
    },
    label = "checkBorder"
  )

  Box(
    modifier = modifier
      .size(48.dp)
      .glassFocusRing(interactionSource, CircleShape)
      .semantics { this.contentDescription = contentDescription }
      .toggleable(
        value = checked,
        role = Role.Checkbox,
        interactionSource = interactionSource,
        indication = null,
        onValueChange = onCheckedChange
      ),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier.size(size).scale(scale).clip(CircleShape)
        .border(1.6.dp, borderColor, CircleShape).background(bgColor, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      if (checked) {
        Icon(
          imageVector = Icons.Rounded.Check,
          contentDescription = null,
          tint = colors.pillTextDark,
          modifier = Modifier.size(size * 0.65f)
        )
      }
    }
  }
}

