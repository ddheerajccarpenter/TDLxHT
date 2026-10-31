package com.example.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.PriceChange
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.AppPreferences
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.tools.ToolCategory
import com.example.data.tools.ToolDefinition
import com.example.data.tools.ToolRegistry
import com.example.data.tools.ToolResult
import com.example.ui.components.AppEmptyState
import com.example.ui.components.AppHeader
import com.example.ui.components.SketchCard
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassButton
import com.example.ui.components.fluidLensedGlass
import com.example.ui.components.GlassMenu
import com.example.ui.components.GlassMenuItem
import com.example.ui.components.SketchFilterChip
import com.example.ui.components.SketchInputField
import com.example.ui.components.SketchSearchBar
import com.example.ui.components.bounceClick
import com.example.ui.components.staggeredEntrance
import com.example.ui.components.paperDotBackground
import com.example.ui.theme.AccentTerracotta
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PillBackgroundDark
import com.example.ui.theme.PillTextDark
import com.example.ui.theme.SFProFontFamily
import com.example.util.VibrationHelper
import com.example.ui.components.FloatingNavContentBottomPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.KeyboardType
import java.time.LocalDate

@Composable
fun ToolsScreen(
  onBack: (() -> Unit)? = null,
  onToolModalStateChanged: ((Boolean) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val favoriteToolIds by AppPreferences.favoriteToolIds.collectAsStateWithLifecycle()
  val customToolOrder by AppPreferences.customToolOrder.collectAsStateWithLifecycle()
  var isReorderMode by remember { mutableStateOf(false) }
  var sortCriterion by rememberSaveable { mutableStateOf("Name") }
  var showSortMenu by remember { mutableStateOf(false) }

  var selectedCategory by remember { mutableStateOf(ToolCategory.ALL) }
  var searchQuery by remember { mutableStateOf("") }
  var activeToolId by rememberSaveable { mutableStateOf<String?>(null) }
  val activeTool = remember(activeToolId) {
    activeToolId?.let { ToolRegistry.getToolById(it) }
  }
  var displayedTool by remember { mutableStateOf<ToolDefinition?>(null) }
  if (activeTool != null) {
    displayedTool = activeTool
  }

  LaunchedEffect(activeTool != null) {
    onToolModalStateChanged?.invoke(activeTool != null)
  }

  val filteredTools = remember(selectedCategory, searchQuery, favoriteToolIds, customToolOrder, sortCriterion) {
    val q = searchQuery.trim().lowercase()

    // 1. Order all tools according to customToolOrder (or natural index)
    val orderedAll = ToolRegistry.tools.sortedBy { tool ->
      val idx = customToolOrder.indexOf(tool.id)
      if (idx >= 0) idx else (1000 + ToolRegistry.tools.indexOf(tool))
    }

    // 2. Partition into favorites and others so favorites ALWAYS move to the front
    val (favs, others) = orderedAll.partition { favoriteToolIds.contains(it.id) }
    val fullOrdered = favs + others

    // 3. Filter by category & search query
    val matching = fullOrdered.filter { tool ->
      val matchesCategory = (selectedCategory == ToolCategory.ALL) || (tool.category == selectedCategory)
      val matchesQuery = q.isEmpty() ||
        tool.name.lowercase().contains(q) ||
        tool.description.lowercase().contains(q) ||
        tool.tags.any { it.lowercase().contains(q) }
      matchesCategory && matchesQuery
    }

    // 4. Apply selected sort criteria (GlassMenu items: Date, Name, Size)
    when (sortCriterion) {
      "Name" -> matching.sortedBy { it.name }
      "Size" -> matching.sortedByDescending { it.description.length }
      "Date" -> matching.reversed()
      else -> matching
    }
  }

  BackHandler(enabled = activeTool != null) {
    activeToolId = null
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .paperDotBackground()
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = FloatingNavContentBottomPadding + 48.dp)
    ) {
      // Header with Apple Liquid Glass Controls
      item {
        AppHeader(
          title = "Tools",
          onBack = onBack,
          rightContent = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // GlassMenu Trigger Button (<GlassMenu icon={<SortIcon />} aria-label="Sort by" />)
              GlassButton(
                onClick = { showSortMenu = true },
                contentDescription = "Sort by",
                size = 36.dp
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.Sort,
                  contentDescription = null,
                  tint = InkPrimary,
                  modifier = Modifier.size(18.dp)
                )
              }

              GlassButton(
                onClick = { isReorderMode = !isReorderMode },
                modifier = Modifier.padding(horizontal = 2.dp)
              ) {
                Text(
                  text = if (isReorderMode) "Done" else "Reorder",
                  style = TextStyle(
                    fontFamily = SFProFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                  )
                )
              }
            }
          }
        )

        // GlassMenu Context Dropdown
        GlassMenu(
          expanded = showSortMenu,
          onDismissRequest = { showSortMenu = false }
        ) {
          listOf("Date", "Name", "Size").forEach { sort ->
            GlassMenuItem(
              label = sort,
              checked = sort == sortCriterion,
              onSelect = {
                sortCriterion = sort
                showSortMenu = false
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
      }

      // Search Bar
      item {
        SketchSearchBar(
          query = searchQuery,
          onQueryChange = { searchQuery = it },
          placeholder = "Search tools...",
          modifier = Modifier.padding(bottom = 12.dp)
        )
      }

      // Category Chips
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ToolCategory.entries.forEach { category ->
            val isSelected = category == selectedCategory
            SketchFilterChip(
              selected = isSelected,
              onClick = { selectedCategory = category },
              label = category.displayName
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
      }

      // Tool Cards List
      if (filteredTools.isEmpty()) {
        item {
          AppEmptyState(
            icon = Icons.Default.Close,
            title = if (searchQuery.isNotBlank()) "No tools found" else "No tools",
            subtitle = if (searchQuery.isNotBlank()) "\"$searchQuery\"" else ""
          )
        }
      } else {
        items(filteredTools, key = { it.id }) { tool ->
          val isFav = favoriteToolIds.contains(tool.id)
          val toolIndex = filteredTools.indexOf(tool)
          Box(modifier = Modifier.animateItem().staggeredEntrance(index = toolIndex)) {
            GlassCard(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .bounceClick {
                  if (!isReorderMode) {
                    activeToolId = tool.id
                  }
                }
                .testTag("tool_card_${tool.id}"),
              borderColor = if (isFav) AccentTerracotta.copy(alpha = 0.6f) else CardBorder,
              cornerRadius = 18.dp
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Box(
                    modifier = Modifier
                      .size(38.dp)
                      .fluidLensedGlass(
                        shape = RoundedCornerShape(10.dp),
                        elevation = 1.dp,
                        borderWidth = 0.6.dp,
                        isElevated = isFav
                      ),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = getToolIcon(tool.id),
                      contentDescription = null,
                      tint = if (isFav) AccentTerracotta else InkPrimary,
                      modifier = Modifier.size(20.dp)
                    )
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = tool.name,
                        style = TextStyle(
                          fontFamily = SFProFontFamily,
                          fontSize = 16.sp,
                          fontWeight = FontWeight.Bold,
                          color = InkPrimary
                        )
                      )
                      if (isFav) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                          modifier = Modifier
                            .background(AccentTerracotta.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                          Text(
                            text = "FAVORITE",
                            style = TextStyle(
                              fontSize = 9.sp,
                              fontWeight = FontWeight.Bold,
                              letterSpacing = 0.5.sp,
                              color = AccentTerracotta
                            )
                          )
                        }
                      }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = tool.description,
                      style = TextStyle(
                        fontSize = 12.sp,
                        color = InkSecondary,
                        lineHeight = 16.sp
                      ),
                      maxLines = 2
                    )
                  }
                }

                if (isReorderMode) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(start = 6.dp)
                  ) {
                    val currentIndex = filteredTools.indexOf(tool)
                    Box(
                      modifier = Modifier
                        .size(32.dp)
                        .border(1.dp, CardBorder, CircleShape)
                        .clip(CircleShape)
                        .bounceClick(enabled = currentIndex > 0) {
                          if (currentIndex > 0) {
                            val targetTool = filteredTools[currentIndex - 1]
                            val currentOrder = if (customToolOrder.isNotEmpty()) customToolOrder.toMutableList() else ToolRegistry.tools.map { it.id }.toMutableList()
                            val idx1 = currentOrder.indexOf(tool.id)
                            val idx2 = currentOrder.indexOf(targetTool.id)
                            if (idx1 >= 0 && idx2 >= 0) {
                              java.util.Collections.swap(currentOrder, idx1, idx2)
                              AppPreferences.setCustomToolOrder(context, currentOrder)
                            }
                          }
                        },
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Move Up",
                        tint = if (currentIndex > 0) InkPrimary else InkMuted,
                        modifier = Modifier.size(18.dp)
                      )
                    }

                    Box(
                      modifier = Modifier
                        .size(32.dp)
                        .border(1.dp, CardBorder, CircleShape)
                        .clip(CircleShape)
                        .bounceClick(enabled = currentIndex < filteredTools.size - 1) {
                          if (currentIndex < filteredTools.size - 1) {
                            val targetTool = filteredTools[currentIndex + 1]
                            val currentOrder = if (customToolOrder.isNotEmpty()) customToolOrder.toMutableList() else ToolRegistry.tools.map { it.id }.toMutableList()
                            val idx1 = currentOrder.indexOf(tool.id)
                            val idx2 = currentOrder.indexOf(targetTool.id)
                            if (idx1 >= 0 && idx2 >= 0) {
                              java.util.Collections.swap(currentOrder, idx1, idx2)
                              AppPreferences.setCustomToolOrder(context, currentOrder)
                            }
                          }
                        },
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Move Down",
                        tint = if (currentIndex < filteredTools.size - 1) InkPrimary else InkMuted,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                } else {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .bounceClick {
                        AppPreferences.toggleToolFavorite(context, tool.id)
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = if (isFav) Icons.Filled.Star else Icons.Outlined.StarOutline,
                      contentDescription = if (isFav) "Favorited" else "Favorite",
                      tint = if (isFav) AccentTerracotta else InkMuted,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Interactive Tool Runner Modal
    AnimatedVisibility(
      visible = activeTool != null,
      enter = fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)),
      exit = fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
    ) {
      displayedTool?.let { tool ->
        key(tool.id) {
          ToolRunnerModal(
            tool = tool,
            onDismiss = { activeToolId = null }
          )
        }
      }
    }
  }
}

@Composable
fun ToolRunnerModal(
  tool: ToolDefinition,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  // State holders for tool inputs (Keyed per tool ID with clean zero-demo start)
  var input1 by rememberSaveable(tool.id) { mutableStateOf("") }
  var input2 by rememberSaveable(tool.id) { mutableStateOf("") }
  var input3 by rememberSaveable(tool.id) { mutableStateOf("") }
  var isOptionChecked by rememberSaveable(tool.id) { mutableStateOf(false) }

  // Calculate result dynamically
  val result: ToolResult = remember(tool.id, input1, input2, input3, isOptionChecked) {
    if (input1.isBlank() && input2.isBlank() && input3.isBlank()) {
      return@remember ToolResult(
        primaryLabel = "Result",
        primaryValue = "—",
        secondaryDetails = emptyList(),
        summaryNotes = "Enter your values below to calculate."
      )
    }
    try {
      when (tool.id) {
        "loan_emi" -> ToolRegistry.calculateLoanEmi(
          principal = input1.toDoubleOrNull() ?: 0.0,
          annualRate = input2.toDoubleOrNull() ?: 0.0,
          tenureMonths = input3.toIntOrNull() ?: 0
        )
        "split_bill" -> ToolRegistry.calculateTipSplit(
          totalBill = input1.toDoubleOrNull() ?: 0.0,
          tipPercent = input2.toDoubleOrNull() ?: 0.0,
          numPeople = input3.toIntOrNull() ?: 1
        )
        "gst_tax" -> ToolRegistry.calculateGst(
          amount = input1.toDoubleOrNull() ?: 0.0,
          gstRate = input2.toDoubleOrNull() ?: 18.0,
          isPriceInclusive = isOptionChecked
        )
        "margin_markup" -> ToolRegistry.calculateMarginMarkup(
          cost = input1.toDoubleOrNull() ?: 0.0,
          revenue = input2.toDoubleOrNull() ?: 0.0
        )
        "attendance_planner" -> ToolRegistry.calculateAttendance(
          attended = input1.toIntOrNull() ?: 0,
          total = input2.toIntOrNull() ?: 0,
          targetPercent = input3.toDoubleOrNull() ?: 75.0
        )
        "cgpa_converter" -> ToolRegistry.calculateCgpa(
          cgpa = input1.toDoubleOrNull() ?: 0.0,
          scale = input2.toDoubleOrNull() ?: 10.0
        )
        "discount_savings" -> ToolRegistry.calculateDiscount(
          originalPrice = input1.toDoubleOrNull() ?: 0.0,
          discountPercent = input2.toDoubleOrNull() ?: 0.0
        )
        "break_even" -> ToolRegistry.calculateBreakEven(
          fixedCosts = input1.toDoubleOrNull() ?: 0.0,
          pricePerUnit = input2.toDoubleOrNull() ?: 0.0,
          variableCostPerUnit = input3.toDoubleOrNull() ?: 0.0
        )
        "profit_loss" -> ToolRegistry.calculateProfitLoss(
          costPrice = input1.toDoubleOrNull() ?: 0.0,
          sellingPrice = input2.toDoubleOrNull() ?: 0.0,
          quantity = input3.toIntOrNull() ?: 1
        )
        "pomodoro_planner" -> ToolRegistry.calculatePomodoro(
          sessions = input1.toIntOrNull() ?: 4,
          focusMin = input2.toIntOrNull() ?: 25,
          breakMin = input3.toIntOrNull() ?: 5
        )
        "reading_estimator" -> ToolRegistry.calculateReadingTime(
          words = input1.toIntOrNull() ?: 0,
          wpm = input2.toIntOrNull() ?: 200
        )
        "date_difference" -> {
          val days = input1.toLongOrNull() ?: 30L
          ToolRegistry.calculateDateDifference(
            start = LocalDate.now().minusDays(days),
            end = LocalDate.now()
          )
        }
        "exam_countdown" -> {
          val days = input1.toLongOrNull() ?: 14L
          ToolRegistry.calculateExamCountdown(targetDate = LocalDate.now().plusDays(days))
        }
        "percentage_change" -> ToolRegistry.calculatePercentageChange(
          initialValue = input1.toDoubleOrNull() ?: 0.0,
          finalValue = input2.toDoubleOrNull() ?: 0.0
        )
        "unit_converter" -> ToolRegistry.calculateUnitConverter(
          value = input1.toDoubleOrNull() ?: 0.0,
          categoryIndex = input2.toIntOrNull() ?: 0
        )
        else -> ToolResult("Output", "Ready")
      }
    } catch (e: Exception) {
      ToolResult("Calculation", "Invalid Input")
    }
  }

  // Semi-transparent backdrop with click-to-dismiss and touch absorption
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.55f))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) {
        onDismiss()
      }
      .windowInsetsPadding(WindowInsets.systemBars)
      .windowInsetsPadding(WindowInsets.ime),
    contentAlignment = Alignment.BottomCenter
  ) {
    AnimatedVisibility(
      visible = true,
      enter = slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(240, easing = FastOutSlowInEasing)) +
        fadeIn(animationSpec = tween(240, easing = FastOutSlowInEasing)),
      exit = slideOutVertically(targetOffsetY = { it / 3 }, animationSpec = tween(180, easing = FastOutSlowInEasing)) +
        fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
    ) {
      GlassCard(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) {
            // Consume clicks inside the card so tapping inside does not dismiss the sheet
          },
        cornerRadius = 22.dp,
        borderWidth = 1.2.dp
      ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 620.dp)
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
      ) {
        // Modal Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .background(InkPrimary.copy(alpha = 0.06f), CircleShape)
                .border(1.2.dp, CardBorder, CircleShape)
                .clip(CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = tool.name.take(2).uppercase(),
                style = TextStyle(
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = InkPrimary
                )
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = tool.name,
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  color = InkPrimary
                )
              )
              Text(
                text = tool.category.displayName,
                style = TextStyle(
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = AccentTerracotta
                )
              )
            }
          }

          GlassButton(
            onClick = { onDismiss() },
            contentDescription = "Close",
            size = 34.dp
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = null,
              tint = InkPrimary,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Dynamic Input Fields
        when (tool.id) {
          "loan_emi" -> {
            ToolInputField(label = "Loan Amount (₹)", value = input1, onValueChange = { input1 = it })
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Box(modifier = Modifier.weight(1f)) {
                ToolInputField(label = "Interest Rate (% p.a.)", value = input2, onValueChange = { input2 = it })
              }
              Box(modifier = Modifier.weight(1f)) {
                ToolInputField(label = "Tenure (Months)", value = input3, onValueChange = { input3 = it }, keyboardType = KeyboardType.Number)
              }
            }
          }
          "split_bill" -> {
            ToolInputField(label = "Total Bill Amount (₹)", value = input1, onValueChange = { input1 = it })
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Box(modifier = Modifier.weight(1f)) {
                ToolInputField(label = "Tip (%)", value = input2, onValueChange = { input2 = it })
              }
              Box(modifier = Modifier.weight(1f)) {
                ToolInputField(label = "Number of People", value = input3, onValueChange = { input3 = it }, keyboardType = KeyboardType.Number)
              }
            }
          }
          "gst_tax" -> {
            ToolInputField(label = "Amount (₹)", value = input1, onValueChange = { input1 = it })
            Spacer(modifier = Modifier.height(10.dp))
            ToolInputField(label = "GST Rate (%)", value = input2, onValueChange = { input2 = it })
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.bounceClick { isOptionChecked = !isOptionChecked }
            ) {
              Text(
                text = if (isOptionChecked) "Price already includes GST" else "Add GST on top of price",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = InkPrimary)
              )
            }
          }
          "attendance_planner" -> {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Box(modifier = Modifier.weight(1f)) {
                ToolInputField(label = "Classes Attended", value = input1, onValueChange = { input1 = it }, keyboardType = KeyboardType.Number)
              }
              Box(modifier = Modifier.weight(1f)) {
                ToolInputField(label = "Total Held", value = input2, onValueChange = { input2 = it }, keyboardType = KeyboardType.Number)
              }
            }
            Spacer(modifier = Modifier.height(10.dp))
            ToolInputField(label = "Target Requirement (%)", value = input3, onValueChange = { input3 = it })
          }
          "discount_savings" -> {
            ToolInputField(label = "Original Price (₹)", value = input1, onValueChange = { input1 = it })
            Spacer(modifier = Modifier.height(10.dp))
            ToolInputField(label = "Discount Percentage (%)", value = input2, onValueChange = { input2 = it })
          }
          "profit_loss" -> {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Box(modifier = Modifier.weight(1f)) {
                ToolInputField(label = "Cost / Unit (₹)", value = input1, onValueChange = { input1 = it })
              }
              Box(modifier = Modifier.weight(1f)) {
                ToolInputField(label = "Sale / Unit (₹)", value = input2, onValueChange = { input2 = it })
              }
            }
            Spacer(modifier = Modifier.height(10.dp))
            ToolInputField(label = "Quantity", value = input3, onValueChange = { input3 = it }, keyboardType = KeyboardType.Number)
          }
          "margin_markup" -> {
            ToolInputField(label = "Cost Price (₹)", value = input1, onValueChange = { input1 = it })
            Spacer(modifier = Modifier.height(10.dp))
            ToolInputField(label = "Selling Price / Revenue (₹)", value = input2, onValueChange = { input2 = it })
          }
          "break_even" -> {
            ToolInputField(label = "Fixed Overhead Costs (₹)", value = input1, onValueChange = { input1 = it })
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Box(modifier = Modifier.weight(1f)) {
                ToolInputField(label = "Price / Unit (₹)", value = input2, onValueChange = { input2 = it })
              }
              Box(modifier = Modifier.weight(1f)) {
                ToolInputField(label = "Variable Cost / Unit (₹)", value = input3, onValueChange = { input3 = it })
              }
            }
          }
          "pomodoro_planner" -> {
            ToolInputField(label = "Planned Focus Sessions", value = input1, onValueChange = { input1 = it }, keyboardType = KeyboardType.Number)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Box(modifier = Modifier.weight(1f)) {
                ToolInputField(label = "Focus Duration (Min)", value = input2, onValueChange = { input2 = it }, keyboardType = KeyboardType.Number)
              }
              Box(modifier = Modifier.weight(1f)) {
                ToolInputField(label = "Break Duration (Min)", value = input3, onValueChange = { input3 = it }, keyboardType = KeyboardType.Number)
              }
            }
          }
          "reading_estimator" -> {
            ToolInputField(label = "Total Word Count", value = input1, onValueChange = { input1 = it }, keyboardType = KeyboardType.Number)
            Spacer(modifier = Modifier.height(10.dp))
            ToolInputField(label = "Reading Speed (Words / Min)", value = input2, onValueChange = { input2 = it }, keyboardType = KeyboardType.Number)
          }
          "cgpa_converter" -> {
            ToolInputField(label = "Enter CGPA", value = input1, onValueChange = { input1 = it })
            Spacer(modifier = Modifier.height(10.dp))
            ToolInputField(label = "Scale (10 or 4)", value = input2, onValueChange = { input2 = it })
          }
          "exam_countdown" -> {
            ToolInputField(label = "Days Remaining to Deadline", value = input1, onValueChange = { input1 = it }, keyboardType = KeyboardType.Number)
          }
          "percentage_change" -> {
            ToolInputField(label = "Initial Value", value = input1, onValueChange = { input1 = it })
            Spacer(modifier = Modifier.height(10.dp))
            ToolInputField(label = "Final Value", value = input2, onValueChange = { input2 = it })
          }
          "unit_converter" -> {
            ToolInputField(label = "Value to Convert", value = input1, onValueChange = { input1 = it })
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf("Length (m)" to "0", "Weight (kg)" to "1", "Temp (°C)" to "2").forEach { (label, code) ->
                val isSelected = (input2 == code) || (input2.isEmpty() && code == "0")
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .border(1.2.dp, if (isSelected) PillBackgroundDark else CardBorder, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) PillBackgroundDark else Color.Transparent)
                    .bounceClick { input2 = code }
                    .padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = label,
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isSelected) PillTextDark else InkSecondary)
                  )
                }
              }
            }
          }
          "date_difference" -> {
            ToolInputField(label = "Days Difference / Days Elapsed", value = input1, onValueChange = { input1 = it }, keyboardType = KeyboardType.Number)
          }
          else -> {
            ToolInputField(label = "Primary Input", value = input1, onValueChange = { input1 = it })
            Spacer(modifier = Modifier.height(10.dp))
            ToolInputField(label = "Secondary Input", value = input2, onValueChange = { input2 = it })
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Result Card (Tactile highlight)
        SketchCard(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = AccentTerracotta.copy(alpha = 0.08f),
          borderColor = AccentTerracotta,
          borderWidth = 1.dp,
          cornerRadius = 14.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = result.primaryLabel.uppercase(),
                  style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = AccentTerracotta
                  )
                )
                Text(
                  text = result.primaryValue,
                  style = TextStyle(
                    fontFamily = SFProFontFamily,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                  )
                )
              }

              // Copy Button
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .background(AccentTerracotta, CircleShape)
                  .clip(CircleShape)
                  .bounceClick {
                    val clip = ClipData.newPlainText("Tool Result", "${result.primaryLabel}: ${result.primaryValue}")
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(clip)
                    VibrationHelper.click(context)
                    Toast.makeText(context, "Copied ${result.primaryValue}", Toast.LENGTH_SHORT).show()
                  },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = "Copy Result",
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            if (result.secondaryDetails.isNotEmpty()) {
              Spacer(modifier = Modifier.height(10.dp))
              result.secondaryDetails.forEach { (label, value) ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = label, style = TextStyle(fontSize = 13.sp, color = InkSecondary))
                  Text(text = value, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary))
                }
              }
            }

            if (result.summaryNotes != null) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = result.summaryNotes ?: "",
                style = TextStyle(
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = InkPrimary
                )
              )
            }
          }
        }
      }
    }
  }
}
}

@Composable
fun ToolInputField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  keyboardType: KeyboardType = KeyboardType.Decimal
) {
  Column {
    Text(
      text = label,
      style = TextStyle(
        fontFamily = SFProFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = InkSecondary
      ),
      modifier = Modifier.padding(bottom = 4.dp)
    )
    SketchInputField(
      value = value,
      onValueChange = onValueChange,
      placeholder = "0",
      keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
      modifier = Modifier.fillMaxWidth()
    )
  }
}

private fun getToolIcon(toolId: String): ImageVector {
  return when (toolId) {
    "loan_emi" -> Icons.Outlined.AccountBalance
    "split_bill" -> Icons.Outlined.Receipt
    "gst_tax" -> Icons.AutoMirrored.Outlined.ReceiptLong
    "margin_markup" -> Icons.Outlined.PriceChange
    "attendance_planner" -> Icons.Outlined.School
    "cgpa_converter" -> Icons.Outlined.Grade
    "exam_countdown" -> Icons.Outlined.HourglassEmpty
    "profit_loss" -> Icons.Outlined.AttachMoney
    "discount_savings" -> Icons.Outlined.LocalOffer
    "break_even" -> Icons.AutoMirrored.Outlined.TrendingUp
    "date_difference" -> Icons.Outlined.CalendarToday
    "percentage_change" -> Icons.Outlined.Percent
    "unit_converter" -> Icons.Outlined.SwapHoriz
    "pomodoro_planner" -> Icons.Outlined.Timer
    "reading_estimator" -> Icons.AutoMirrored.Outlined.MenuBook
    else -> Icons.Outlined.Calculate
  }
}
