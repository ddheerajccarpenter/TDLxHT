package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import com.example.ui.theme.SFProFontFamily
import com.example.data.service.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import com.example.util.AppPreferences
import com.example.util.ShakeDetector
import com.example.util.VibrationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.mutableFloatStateOf
import com.example.ui.theme.MobbinInk
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircleOutline
import com.example.ui.theme.PrimaryAccentLime
import com.example.ui.theme.PrimaryDarkCanvas
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppSettings
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.ui.components.AnimatedDialogContent
import com.example.ui.components.CircularProgressGauge
import com.example.ui.components.SketchCard
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassSheet
import com.example.ui.components.fluidLensedGlass
import com.example.ui.components.bounceClick
import com.example.ui.components.elasticStretchPress
import com.example.ui.screens.auth.AuthenticationScreen
import com.example.ui.screens.calendar.CalendarScreen
import com.example.ui.screens.habits.HabitDetailEditScreen
import com.example.ui.screens.habits.HabitListScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.productivity.ProductivityScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.tasks.TaskDetailEditScreen
import com.example.ui.screens.tasks.TaskListScreen
import com.example.ui.screens.tools.ToolsScreen
import com.example.ui.theme.AccentTerracotta
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PaperBackground
import com.example.ui.theme.BrandCyprus
import com.example.ui.theme.BrandSand
import com.example.ui.theme.PillBackgroundDark
import com.example.ui.theme.PillTextDark
import com.example.ui.theme.PaletteAntiqueWhite
import com.example.ui.theme.GrapeCanvasDark
import com.example.ui.theme.InkWashCharcoal
import com.example.ui.theme.InkWashMist
import com.example.ui.theme.InkWashIvory
import com.example.ui.theme.InkWashSlate
import com.example.ui.theme.InkWashCharcoalDeep
import com.example.ui.theme.InkWashCharcoalSurface
import com.example.ui.theme.InkWashCharcoalElevated
import com.example.ui.theme.InkWashCharcoalBorder
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MainViewModel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.annotation.RawRes
import com.example.ui.components.AnimatedIcon
import com.example.ui.navigation.AppNavDestination
import com.example.ui.navigation.AppNavOverlay
import com.example.ui.navigation.PredictiveNavController
import com.example.ui.navigation.PredictiveNavigationHost
import com.example.ui.navigation.rememberPredictiveNavController
import com.example.ui.screens.settings.DiagnosticsScreen
import com.example.ui.screens.tools.ToolRunnerModal
import kotlinx.coroutines.launch

enum class AppDestination(
  val label: String,
  val iconFilled: ImageVector,
  val iconOutlined: ImageVector,
  @param:RawRes val animRes: Int
) {
  TODAY("Home", Icons.Filled.Home, Icons.Outlined.Home, R.raw.anim_today),
  PRODUCTIVITY("Productivity", Icons.AutoMirrored.Filled.FormatListBulleted, Icons.AutoMirrored.Outlined.FormatListBulleted, R.raw.anim_productivity),
  TOOLS("Tools", Icons.Filled.Build, Icons.Outlined.Build, R.raw.anim_tools),
  INSIGHTS("Insights", Icons.AutoMirrored.Filled.ShowChart, Icons.AutoMirrored.Outlined.ShowChart, R.raw.anim_insights)
}

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels {
    MainViewModel.provideFactory(application as HabitTaskApplication)
  }

  private val authViewModel: AuthViewModel by viewModels()

  private val shakeTriggerFlow = MutableStateFlow<Long>(0L)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    try {
      SupabaseClient.client.handleDeeplinks(intent)
    } catch (_: Exception) {}

    if (intent?.getBooleanExtra(EXTRA_SHAKE_TRIGGERED, false) == true) {
      shakeTriggerFlow.value = System.currentTimeMillis()
    }

    setContent {
      val settings by viewModel.settings.collectAsStateWithLifecycle()
      val prefTheme by AppPreferences.themeMode.collectAsStateWithLifecycle()
      val isSystemDark = isSystemInDarkTheme()

      val effectiveThemeMode = remember(settings.themeMode, prefTheme) {
        if (prefTheme != "SYSTEM") prefTheme else settings.themeMode
      }

      val isDark = when (effectiveThemeMode.uppercase()) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemDark
      }

      val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
      val shakeTrigger by shakeTriggerFlow.collectAsStateWithLifecycle()

      MyApplicationTheme(darkTheme = isDark) {
        if (currentUser != null) {
          MainApp(
            viewModel = viewModel,
            authViewModel = authViewModel,
            settings = settings,
            shakeTrigger = shakeTrigger
          )
        } else {
          AuthenticationScreen(viewModel = authViewModel)
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    try {
      SupabaseClient.client.handleDeeplinks(intent)
    } catch (_: Exception) {}
    if (intent.getBooleanExtra(EXTRA_SHAKE_TRIGGERED, false)) {
      shakeTriggerFlow.value = System.currentTimeMillis()
    }
  }

  override fun onResume() {
    super.onResume()
    if (AppPreferences.isShakeToOpenEnabled(this)) {
      com.example.service.ShakeDetectionService.start(this)
    }
  }

  override fun onPause() {
    super.onPause()
  }

  companion object {
    const val EXTRA_SHAKE_TRIGGERED = "extra_shake_triggered"
  }
}

@Composable
fun MainApp(
  viewModel: MainViewModel,
  authViewModel: AuthViewModel,
  settings: AppSettings,
  shakeTrigger: Long = 0L
) {
    val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  val destinations = remember {
    listOf(
      AppDestination.TODAY,
      AppDestination.PRODUCTIVITY,
      AppDestination.TOOLS,
      AppDestination.INSIGHTS
    )
  }

  val pagerState = rememberPagerState(initialPage = 0) { destinations.size }
  val currentDestination = destinations[pagerState.currentPage]

  val navController = rememberPredictiveNavController(
    initialDestination = AppNavDestination.Main(0)
  )

  // Keep PagerState and navController root tab index synchronized
  LaunchedEffect(navController.currentDestination) {
    val cur = navController.currentDestination
    if (cur is AppNavDestination.Main && cur.tabIndex != pagerState.currentPage) {
      pagerState.scrollToPage(cur.tabIndex)
    }
  }

  LaunchedEffect(pagerState.currentPage) {
    val cur = navController.currentDestination
    if (cur is AppNavDestination.Main && cur.tabIndex != pagerState.currentPage) {
      navController.backStack[0] = AppNavDestination.Main(pagerState.currentPage)
    }
  }

  // Remote update check state
  var availableUpdate by remember { mutableStateOf<com.example.util.AppUpdateInfo?>(null) }

  LaunchedEffect(Unit) {
    com.example.util.AppUpdateManager.checkForUpdates(
      context = context,
      notifySystem = true,
      forceShowDismissed = false
    ) { info, isNew ->
      if (isNew && info != null) {
        availableUpdate = info
      }
    }
  }

  // Initial single-time permission prompt state
  var showInitialPermissionPrompt by remember { mutableStateOf(false) }

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { _ ->
    viewModel.updateSettings(settings.copy(hasPromptedPermissions = true))
  }

  LaunchedEffect(settings.hasPromptedPermissions) {
    if (!settings.hasPromptedPermissions) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val hasPerm = ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPerm) {
          showInitialPermissionPrompt = true
        } else {
          viewModel.updateSettings(settings.copy(hasPromptedPermissions = true))
        }
      } else {
        viewModel.updateSettings(settings.copy(hasPromptedPermissions = true))
      }
    }
  }

  fun dispatchShakeAction() {
    com.example.util.VibrationHelper.shakeDetected(context)
    when (com.example.util.AppPreferences.getShakeAction(context)) {
      "CREATE_TASK" -> {
        navController.navigate(AppNavDestination.TaskDetail())
        Toast.makeText(context, "Shake: Quick Add Task", Toast.LENGTH_SHORT).show()
      }
      "START_TIMER" -> {
        val duration = com.example.util.AppPreferences.getShakeTimerDuration(context)
        navController.navigate(AppNavDestination.FocusTimer(duration))
        Toast.makeText(context, "Shake: Focus Timer (${duration}m)", Toast.LENGTH_SHORT).show()
      }
      "QUICK_CHOOSER" -> {
        navController.navigate(AppNavDestination.QuickCreate)
        Toast.makeText(context, "Shake: Quick Actions", Toast.LENGTH_SHORT).show()
      }
      "DISABLED" -> {
        // Shake gesture is disabled in settings
      }
      else -> {
        navController.navigate(AppNavDestination.TaskDetail())
      }
    }
  }

  // React to shake event: dispatch configured shake action
  LaunchedEffect(shakeTrigger) {
    if (shakeTrigger > 0L) {
      dispatchShakeAction()
    }
  }

  LaunchedEffect(Unit) {
    com.example.util.ShakeEventBus.events.collect {
      dispatchShakeAction()
    }
  }

  // Tactile swipe vibration when swiping between pages
  var lastSettledPage by remember { mutableIntStateOf(pagerState.currentPage) }
  LaunchedEffect(pagerState.currentPage) {
    if (pagerState.currentPage != lastSettledPage) {
      lastSettledPage = pagerState.currentPage
      VibrationHelper.tick(context)
    }
  }

  // Centralized physical back handling: guards against unexpected application exit
  BackHandler(
    enabled = navController.activeOverlay != null ||
      navController.backStack.size > 1 ||
      pagerState.currentPage != 0
  ) {
    when {
      navController.activeOverlay != null -> {
        navController.dismissOverlay()
      }
      navController.backStack.size > 1 -> {
        navController.navigateBack()
      }
      pagerState.currentPage != 0 -> {
        coroutineScope.launch {
          pagerState.animateScrollToPage(0)
        }
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(PaperBackground)
  ) {
    PredictiveNavigationHost(
      navController = navController,
      modifier = Modifier.fillMaxSize()
    ) { destination ->
      when (destination) {
        is AppNavDestination.Main -> {
          Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = PaperBackground,
            bottomBar = {
              SketchBottomNavigationBar(
                destinations = destinations,
                pagerState = pagerState,
                isQuickAddOpen = navController.activeOverlay is AppNavOverlay.QuickCreate,
                onDestinationSelected = { dest ->
                  val targetIndex = destinations.indexOf(dest)
                  if (targetIndex >= 0) {
                    coroutineScope.launch {
                      pagerState.animateScrollToPage(targetIndex)
                    }
                  }
                },
                onQuickAdd = { navController.navigate(AppNavDestination.QuickCreate) }
              )
            }
          ) { innerPadding ->
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
            ) {
              HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = navController.backStack.size == 1 && navController.activeOverlay == null
              ) { page ->
                when (destinations[page]) {
                  AppDestination.TODAY -> {
                    HomeScreen(
                      viewModel = viewModel,
                      onNavigateToAddTask = { navController.navigate(AppNavDestination.TaskDetail()) },
                      onNavigateToAddHabit = { navController.navigate(AppNavDestination.HabitDetail()) },
                      onTaskClick = { navController.navigate(AppNavDestination.TaskDetail(task = it)) },
                      onHabitClick = { navController.navigate(AppNavDestination.HabitDetail(habit = it)) },
                      onNavigateToSettings = { navController.navigate(AppNavDestination.Settings) },
                      onNavigateToSearch = { navController.navigate(AppNavDestination.Search) }
                    )
                  }
                  AppDestination.PRODUCTIVITY -> {
                    ProductivityScreen(
                      viewModel = viewModel,
                      onNavigateToAddTask = { navController.navigate(AppNavDestination.TaskDetail()) },
                      onNavigateToAddHabit = { navController.navigate(AppNavDestination.HabitDetail()) },
                      onTaskClick = { navController.navigate(AppNavDestination.TaskDetail(task = it)) },
                      onHabitClick = { navController.navigate(AppNavDestination.HabitDetail(habit = it)) },
                      onBackToHome = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                    )
                  }
                  AppDestination.TOOLS -> {
                    ToolsScreen(
                      onBack = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                      onToolClick = { tool -> navController.navigate(AppNavDestination.ToolRunner(tool)) },
                      onCreateTaskFromResult = { title, notes ->
                        navController.navigate(AppNavDestination.TaskDetail(initialTitle = title, initialNotes = notes))
                      }
                    )
                  }
                  AppDestination.INSIGHTS -> {
                    ReportsScreen(
                      onBack = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                      onNavigateToAddTask = { navController.navigate(AppNavDestination.TaskDetail()) }
                    )
                  }
                }
              }
            }
          }
        }

        is AppNavDestination.TaskDetail -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .windowInsetsPadding(WindowInsets.statusBars)
              .windowInsetsPadding(WindowInsets.navigationBars)
          ) {
            TaskDetailEditScreen(
              viewModel = viewModel,
              existingTask = destination.task,
              initialTitle = destination.initialTitle,
              initialNotes = destination.initialNotes,
              initialIsShortTerm = destination.initialIsShortTerm,
              onDismiss = { navController.navigateBack() }
            )
          }
        }

        is AppNavDestination.HabitDetail -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .windowInsetsPadding(WindowInsets.statusBars)
              .windowInsetsPadding(WindowInsets.navigationBars)
          ) {
            HabitDetailEditScreen(
              viewModel = viewModel,
              existingHabit = destination.habit,
              onDismiss = { navController.navigateBack() }
            )
          }
        }

        is AppNavDestination.Settings -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .windowInsetsPadding(WindowInsets.statusBars)
              .windowInsetsPadding(WindowInsets.navigationBars)
          ) {
            SettingsScreen(
              viewModel = viewModel,
              authViewModel = authViewModel,
              onBack = { navController.navigateBack() },
              onNavigateToDiagnostics = { navController.navigate(AppNavDestination.Diagnostics) }
            )
          }
        }

        is AppNavDestination.Diagnostics -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .windowInsetsPadding(WindowInsets.statusBars)
              .windowInsetsPadding(WindowInsets.navigationBars)
          ) {
            DiagnosticsScreen(
              taskRepository = HabitTaskApplication.instance.taskRepository,
              habitRepository = HabitTaskApplication.instance.habitRepository,
              onNavigateBack = { navController.navigateBack() }
            )
          }
        }

        is AppNavDestination.Search -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .windowInsetsPadding(WindowInsets.statusBars)
              .windowInsetsPadding(WindowInsets.navigationBars)
          ) {
            SearchScreen(
              viewModel = viewModel,
              onTaskClick = { navController.replaceTop(AppNavDestination.TaskDetail(task = it)) },
              onHabitClick = { navController.replaceTop(AppNavDestination.HabitDetail(habit = it)) },
              onBack = { navController.navigateBack() }
            )
          }
        }

        is AppNavDestination.ToolRunner -> {
          ToolRunnerModal(
            tool = destination.tool,
            onCreateTaskFromResult = { title, notes ->
              navController.replaceTop(
                AppNavDestination.TaskDetail(initialTitle = title, initialNotes = notes)
              )
            },
            onDismiss = { navController.navigateBack() }
          )
        }

        is AppNavDestination.QuickCreate -> {}
        is AppNavDestination.FocusTimer -> {}
      }
    }

    // OVERLAY LAYER: Renders modals/sheets over whatever screen is currently active
    when (val overlay = navController.activeOverlay) {
      is AppNavOverlay.QuickCreate -> {
        GlassSheet(
          visible = true,
          onDismissRequest = { navController.dismissOverlay() }
        ) {
          val isDark = com.example.ui.theme.LocalAppColors.current.isDark
          val optionIconAccent = if (isDark) PrimaryAccentLime else AccentTerracotta

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Quick Create",
              style = TextStyle(
                fontFamily = SFProFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
              )
            )

            GlassButton(
              onClick = { navController.dismissOverlay() },
              contentDescription = "Close",
              size = 34.dp
            ) {
              Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = InkPrimary,
                modifier = Modifier.size(16.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Option 1: New Task
          GlassCard(
            modifier = Modifier
              .fillMaxWidth()
              .bounceClick {
                navController.dismissOverlay()
                navController.navigate(AppNavDestination.TaskDetail())
              },
            cornerRadius = 16.dp,
            elevation = 2.dp
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(optionIconAccent.copy(alpha = 0.14f))
                  .border(1.dp, optionIconAccent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                AnimatedIcon(
                  resId = R.raw.anim_productivity,
                  isSelected = true,
                  tint = optionIconAccent,
                  size = 24.dp
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Text(
                text = "New Task",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = InkPrimary
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Option 2: New Habit
          GlassCard(
            modifier = Modifier
              .fillMaxWidth()
              .bounceClick {
                navController.dismissOverlay()
                navController.navigate(AppNavDestination.HabitDetail())
              },
            cornerRadius = 16.dp,
            elevation = 2.dp
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(optionIconAccent.copy(alpha = 0.14f))
                  .border(1.dp, optionIconAccent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                AnimatedIcon(
                  resId = R.raw.anim_today,
                  isSelected = true,
                  tint = optionIconAccent,
                  size = 24.dp
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Text(
                text = "New Habit",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = InkPrimary
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Option 3: Short-term Task (Auto-removes upon completion, local record only)
          GlassCard(
            modifier = Modifier
              .fillMaxWidth()
              .bounceClick {
                navController.dismissOverlay()
                navController.navigate(
                  AppNavDestination.TaskDetail(initialIsShortTerm = true)
                )
              },
            cornerRadius = 16.dp,
            elevation = 2.dp
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(optionIconAccent.copy(alpha = 0.14f))
                  .border(1.dp, optionIconAccent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = optionIconAccent,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(
                  text = "Short-term Task",
                  style = TextStyle(
                    fontFamily = SFProFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                  )
                )
                Text(
                  text = "Auto-removes when done • Local record only",
                  style = TextStyle(fontSize = 12.sp, color = InkSecondary)
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
        }
      }

      is AppNavOverlay.FocusTimer -> {
        Dialog(onDismissRequest = { navController.dismissOverlay() }) {
          AnimatedDialogContent {
            FocusTimerDialog(
              initialMinutes = overlay.initialMinutes,
              onClose = { navController.dismissOverlay() }
            )
          }
        }
      }

      is AppNavOverlay.ToolRunner -> {
        ToolRunnerModal(
          tool = overlay.tool,
          onCreateTaskFromResult = { title, notes ->
            navController.dismissOverlay()
            navController.navigate(
              AppNavDestination.TaskDetail(initialTitle = title, initialNotes = notes)
            )
          },
          onDismiss = { navController.dismissOverlay() }
        )
      }

      null -> {}
    }
  }

  // Single-time first launch permission dialog
  if (showInitialPermissionPrompt) {
    Dialog(onDismissRequest = {
      showInitialPermissionPrompt = false
      viewModel.updateSettings(settings.copy(hasPromptedPermissions = true))
    }) {
      AnimatedDialogContent {
        SketchCard(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        cornerRadius = 20.dp,
        borderWidth = 2.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(56.dp)
              .border(1.8.dp, AccentTerracotta, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Notifications,
              contentDescription = null,
              tint = AccentTerracotta,
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Stay Consistent",
            style = TextStyle(
              fontFamily = SFProFontFamily,
              fontSize = 26.sp,
              fontWeight = FontWeight.Bold,
              color = InkPrimary
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "TDlxHT needs notification permission to remind you of due tasks and keep your daily habit streaks unbroken.",
            style = TextStyle(
              fontSize = 14.sp,
              color = InkSecondary,
              textAlign = TextAlign.Center,
              lineHeight = 20.sp
            )
          )

          Spacer(modifier = Modifier.height(24.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .border(1.5.dp, CardBorder, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .bounceClick {
                  showInitialPermissionPrompt = false
                  viewModel.updateSettings(settings.copy(hasPromptedPermissions = true))
                },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Later",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 17.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = InkPrimary
                )
              )
            }

            Box(
              modifier = Modifier
                .weight(1.3f)
                .height(46.dp)
                .background(PillBackgroundDark, RoundedCornerShape(10.dp))
                .border(1.5.dp, CardBorder, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .bounceClick {
                  showInitialPermissionPrompt = false
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  } else {
                    viewModel.updateSettings(settings.copy(hasPromptedPermissions = true))
                  }
                },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Enable Alerts",
                style = TextStyle(
                  fontFamily = SFProFontFamily,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = PillTextDark
                )
              )
            }
          }
        }
      }
    }
  }
  }

  // In-App New Version Release Update Notification Dialog with Real-Time Download Progress
  if (availableUpdate != null) {
    val update = availableUpdate!!
    val downloadState by com.example.util.ApkDownloadManager.downloadState.collectAsStateWithLifecycle()

    androidx.compose.ui.window.Dialog(onDismissRequest = {
      if (downloadState is com.example.util.ApkDownloadState.Downloading) {
        com.example.util.ApkDownloadManager.cancelDownload()
      }
      com.example.util.AppPreferences.setDismissedUpdateVersionCode(context, update.versionCode)
      com.example.util.ApkDownloadManager.reset()
      availableUpdate = null
    }) {
      AnimatedDialogContent {
        SketchCard(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          cornerRadius = 20.dp,
          borderWidth = 1.5.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(54.dp)
                .background(PillBackgroundDark, CircleShape)
                .border(1.2.dp, CardBorder, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "New Release",
                tint = PillTextDark,
                modifier = Modifier.size(26.dp)
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (val state = downloadState) {
              is com.example.util.ApkDownloadState.Idle -> {
                Text(
                  text = "Update Available!",
                  style = TextStyle(
                    fontFamily = SFProFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                  )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = "A new version of TDlxHT (v${update.version}) is ready with two-color liquid glass styling, multi-session habit discipline, and performance safeguards.",
                  style = TextStyle(
                    fontSize = 13.sp,
                    color = InkSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                  )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(PillBackgroundDark, RoundedCornerShape(12.dp))
                    .border(1.2.dp, CardBorder, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .bounceClick {
                      com.example.util.ApkDownloadManager.startDownload(context, update)
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "Download & Install Update",
                    style = TextStyle(
                      fontFamily = SFProFontFamily,
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold,
                      color = PillTextDark
                    )
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .bounceClick {
                      com.example.util.AppPreferences.setDismissedUpdateVersionCode(context, update.versionCode)
                      availableUpdate = null
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "Remind Me Later",
                    style = TextStyle(
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Medium,
                      color = InkSecondary
                    )
                  )
                }
              }

              is com.example.util.ApkDownloadState.Downloading -> {
                Text(
                  text = "Downloading v${update.version}...",
                  style = TextStyle(
                    fontFamily = SFProFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                  )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = "${state.formattedSize}${if (state.percentage >= 0) " (${state.percentage}%)" else ""}",
                  style = TextStyle(
                    fontSize = 13.sp,
                    color = InkSecondary
                  )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Real-time horizontal progress bar
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(PillBackgroundDark.copy(alpha = 0.12f), RoundedCornerShape(5.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(5.dp))
                    .clip(RoundedCornerShape(5.dp))
                ) {
                  val fraction = if (state.percentage >= 0) (state.percentage / 100f).coerceIn(0.03f, 1f) else 0.5f
                  Box(
                    modifier = Modifier
                      .fillMaxWidth(fraction)
                      .height(10.dp)
                      .background(PillBackgroundDark)
                  )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .border(1.2.dp, CardBorder, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .bounceClick {
                      com.example.util.ApkDownloadManager.cancelDownload()
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "Cancel Download",
                    style = TextStyle(
                      fontSize = 13.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = InkPrimary
                    )
                  )
                }
              }

              is com.example.util.ApkDownloadState.Verifying -> {
                Text(
                  text = "Verifying APK...",
                  style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Checking package signature and cryptographic integrity...",
                  style = TextStyle(fontSize = 13.sp, color = InkSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                )
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.CircularProgressIndicator(color = PillBackgroundDark, strokeWidth = 2.5.dp)
              }

              is com.example.util.ApkDownloadState.ReadyToInstall -> {
                Text(
                  text = "Ready to Install!",
                  style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "TDlxHT v${update.version} has been downloaded and verified.",
                  style = TextStyle(fontSize = 13.sp, color = InkSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(PillBackgroundDark, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .bounceClick {
                      com.example.util.ApkDownloadManager.installApk(context, state.apkFile)
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "Install Now",
                    style = TextStyle(fontFamily = SFProFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PillTextDark)
                  )
                }
              }

              is com.example.util.ApkDownloadState.Failed -> {
                Text(
                  text = "Download Failed",
                  style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = state.error,
                  style = TextStyle(fontSize = 13.sp, color = InkSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(PillBackgroundDark, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .bounceClick {
                      com.example.util.ApkDownloadManager.startDownload(context, update)
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Text("Retry Download", style = TextStyle(fontWeight = FontWeight.Bold, color = PillTextDark))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .bounceClick {
                      com.example.util.ApkDownloadManager.reset()
                      availableUpdate = null
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Text("Close", style = TextStyle(color = InkSecondary))
                }
              }

              is com.example.util.ApkDownloadState.Cancelled -> {
                Text(
                  text = "Download Cancelled",
                  style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(PillBackgroundDark, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .bounceClick {
                      com.example.util.ApkDownloadManager.startDownload(context, update)
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Text("Start Again", style = TextStyle(fontWeight = FontWeight.Bold, color = PillTextDark))
                }
              }
            }
          }
        }
      }
    }
  }
}

private fun interpolateColor(start: Color, end: Color, fraction: Float): Color {
  val f = fraction.coerceIn(0f, 1f)
  return Color(
    red = start.red + (end.red - start.red) * f,
    green = start.green + (end.green - start.green) * f,
    blue = start.blue + (end.blue - start.blue) * f,
    alpha = start.alpha + (end.alpha - start.alpha) * f
  )
}

/**
 * Custom Scooped Shape for "06 Center FAB" design format.
 * Gently scoops downward in the center to embrace the elevated Center FAB button.
 */
class CenterFabScoopedShape(
  private val cornerRadius: androidx.compose.ui.unit.Dp = 26.dp,
  private val cradleRadius: androidx.compose.ui.unit.Dp = 33.dp,
  private val cradleDepth: androidx.compose.ui.unit.Dp = 18.dp
) : androidx.compose.ui.graphics.Shape {
  override fun createOutline(
    size: androidx.compose.ui.geometry.Size,
    layoutDirection: androidx.compose.ui.unit.LayoutDirection,
    density: androidx.compose.ui.unit.Density
  ): androidx.compose.ui.graphics.Outline {
    val cr = with(density) { cornerRadius.toPx() }
    val r = with(density) { cradleRadius.toPx() }
    val depth = with(density) { cradleDepth.toPx() }
    val w = size.width
    val h = size.height
    val cx = w / 2f

    val path = androidx.compose.ui.graphics.Path().apply {
      moveTo(0f, cr)
      quadraticTo(0f, 0f, cr, 0f)

      val scoopLeft = cx - r * 1.35f
      val scoopRight = cx + r * 1.35f
      lineTo(scoopLeft, 0f)

      // Smooth cradle dip
      cubicTo(
        x1 = cx - r * 0.75f, y1 = 0f,
        x2 = cx - r * 0.50f, y2 = depth,
        x3 = cx, y3 = depth
      )
      cubicTo(
        x1 = cx + r * 0.50f, y1 = depth,
        x2 = cx + r * 0.75f, y2 = 0f,
        x3 = scoopRight, y3 = 0f
      )

      lineTo(w - cr, 0f)
      quadraticTo(w, 0f, w, cr)

      lineTo(w, h - cr)
      quadraticTo(w, h, w - cr, h)

      lineTo(cr, h)
      quadraticTo(0f, h, 0f, h - cr)

      close()
    }
    return androidx.compose.ui.graphics.Outline.Generic(path)
  }
}

@Composable
fun SketchBottomNavigationBar(
  destinations: List<AppDestination> = AppDestination.entries,
  pagerState: PagerState,
  isQuickAddOpen: Boolean = false,
  onDestinationSelected: (AppDestination) -> Unit,
  onQuickAdd: () -> Unit
) {
  val context = LocalContext.current
  val isDark = com.example.ui.theme.LocalAppColors.current.isDark
  val coroutineScope = rememberCoroutineScope()

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.navigationBars)
      .padding(horizontal = 16.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    // Liquid Glass Floating Stadium Pill Navigation Dock (Apple-Style Optical Material)
    // Layout: Home (0) -> Productivity (1) -> Tools (2) -> Insights (3) -> Plus (Far Right), Icons Only
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .fluidLensedGlass(
          shape = CircleShape,
          elevation = if (isDark) 8.dp else 5.dp,
          borderWidth = 0.75.dp,
          isElevated = true
        ),
      contentAlignment = Alignment.Center
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Tab 0: Home (weight 1f)
        LiquidGlassNavItem(
          modifier = Modifier.weight(1f),
          destination = destinations[0],
          pageIndex = 0,
          currentPage = pagerState.currentPage,
          pageOffset = pagerState.currentPageOffsetFraction,
          isDark = isDark,
          onClick = {
            VibrationHelper.tick(context)
            if (pagerState.currentPage != 0) {
              coroutineScope.launch {
                pagerState.animateScrollToPage(
                  page = 0,
                  animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f)
                )
              }
              onDestinationSelected(destinations[0])
            }
          }
        )

        // Tab 1: Productivity (weight 1f)
        LiquidGlassNavItem(
          modifier = Modifier.weight(1f),
          destination = destinations[1],
          pageIndex = 1,
          currentPage = pagerState.currentPage,
          pageOffset = pagerState.currentPageOffsetFraction,
          isDark = isDark,
          onClick = {
            VibrationHelper.tick(context)
            if (pagerState.currentPage != 1) {
              coroutineScope.launch {
                pagerState.animateScrollToPage(
                  page = 1,
                  animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f)
                )
              }
              onDestinationSelected(destinations[1])
            }
          }
        )

        // Tab 2: Tools (weight 1f)
        LiquidGlassNavItem(
          modifier = Modifier.weight(1f),
          destination = destinations[2],
          pageIndex = 2,
          currentPage = pagerState.currentPage,
          pageOffset = pagerState.currentPageOffsetFraction,
          isDark = isDark,
          onClick = {
            VibrationHelper.tick(context)
            if (pagerState.currentPage != 2) {
              coroutineScope.launch {
                pagerState.animateScrollToPage(
                  page = 2,
                  animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f)
                )
              }
              onDestinationSelected(destinations[2])
            }
          }
        )

        // Tab 3: Insights (weight 1f)
        LiquidGlassNavItem(
          modifier = Modifier.weight(1f),
          destination = destinations[3],
          pageIndex = 3,
          currentPage = pagerState.currentPage,
          pageOffset = pagerState.currentPageOffsetFraction,
          isDark = isDark,
          onClick = {
            VibrationHelper.tick(context)
            if (pagerState.currentPage != 3) {
              coroutineScope.launch {
                pagerState.animateScrollToPage(
                  page = 3,
                  animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f)
                )
              }
              onDestinationSelected(destinations[3])
            }
          }
        )

        // Far-Right Quick Add (+) Button — Apple Liquid Glass Circular Button with Kern Elastic Rotation
        val fabRotation by animateFloatAsState(
          targetValue = if (isQuickAddOpen) 45f else 0f,
          animationSpec = spring(
            dampingRatio = 0.54f, // Kern Society snappy elastic rebound
            stiffness = 450f
          ),
          label = "fabRotation"
        )

        GlassButton(
          onClick = {
            VibrationHelper.click(context)
            onQuickAdd()
          },
          contentDescription = if (isQuickAddOpen) "Close create sheet" else "Quick Create",
          size = 46.dp,
          modifier = Modifier
            .padding(start = 4.dp, end = 2.dp)
            .testTag("nav_center_fab")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = if (isDark) InkWashIvory else InkWashCharcoal,
            modifier = Modifier
              .size(24.dp)
              .graphicsLayer(rotationZ = fabRotation)
          )
        }
      }
    }
  }
}

@Composable
private fun LiquidGlassNavItem(
  modifier: Modifier = Modifier,
  destination: AppDestination,
  pageIndex: Int,
  currentPage: Int,
  pageOffset: Float,
  isDark: Boolean,
  onClick: () -> Unit
) {
  val continuousPosition = (currentPage + pageOffset).coerceIn(0f, 3f)
  val distance = kotlin.math.abs(continuousPosition - pageIndex).coerceIn(0f, 1f)
  val proximity = (1f - distance).coerceIn(0f, 1f)
  val isSelected = proximity > 0.50f

  val activeColor = if (isDark) InkWashIvory else InkWashCharcoal
  val inactiveColor = if (isDark) InkWashMist.copy(alpha = 0.50f) else InkWashCharcoal.copy(alpha = 0.45f)

  // Liquid capsule animated presence using pure GPU RenderNode transforms (zero layout remeasurement!)
  val liquidAlpha by animateFloatAsState(
    targetValue = if (isSelected) 1f else 0f,
    animationSpec = spring(dampingRatio = 0.80f, stiffness = 450f),
    label = "liquidAlpha"
  )
  val liquidScale by animateFloatAsState(
    targetValue = if (isSelected) 1f else 0.84f,
    animationSpec = spring(dampingRatio = 0.65f, stiffness = 420f),
    label = "liquidScale"
  )
  val iconScale by animateFloatAsState(
    targetValue = if (isSelected) 1.10f else 1.0f,
    animationSpec = spring(dampingRatio = 0.58f, stiffness = 450f),
    label = "iconScale"
  )

  Box(
    modifier = modifier
      .height(48.dp)
      .clip(CircleShape)
      .elasticStretchPress(
        horizontalStretch = 1.06f,
        verticalSquash = 0.93f,
        onClick = onClick
      )
      .testTag("nav_${destination.name.lowercase()}"),
    contentAlignment = Alignment.Center
  ) {
    // 1. Sliding Liquid Glass Capsule (RenderNode GPU-only, no remeasure)
    if (liquidAlpha > 0.01f) {
      Box(
        modifier = Modifier
          .fillMaxWidth(0.92f)
          .height(42.dp)
          .graphicsLayer {
            alpha = liquidAlpha
            scaleX = liquidScale
            scaleY = liquidScale
          }
          .fluidLensedGlass(
            shape = CircleShape,
            elevation = if (isDark) 2.dp else 1.dp,
            borderWidth = 0.6.dp,
            isElevated = true
          )
      )
    }

    // 2. Icon Only (Centered, No text labels, accessible contentDescription)
    Icon(
      imageVector = if (isSelected) destination.iconFilled else destination.iconOutlined,
      contentDescription = destination.label,
      tint = if (isSelected) activeColor else inactiveColor,
      modifier = Modifier
        .size(23.dp)
        .graphicsLayer {
          scaleX = iconScale
          scaleY = iconScale
        }
    )
  }
}

@Composable
fun FocusTimerDialog(
  initialMinutes: Int = 25,
  onClose: () -> Unit
) {
  val context = LocalContext.current
  var selectedMinutes by remember { mutableIntStateOf(initialMinutes) }
  var remainingSeconds by remember { mutableIntStateOf(initialMinutes * 60) }
  var isRunning by remember { mutableStateOf(false) }

  LaunchedEffect(isRunning, remainingSeconds) {
    if (isRunning && remainingSeconds > 0) {
      kotlinx.coroutines.delay(1000L)
      remainingSeconds--
    } else if (isRunning && remainingSeconds == 0) {
      isRunning = false
      com.example.util.VibrationHelper.shakeDetected(context)
      android.widget.Toast.makeText(context, "Focus Session Complete!", android.widget.Toast.LENGTH_LONG).show()
    }
  }

  val minutes = remainingSeconds / 60
  val seconds = remainingSeconds % 60
  val formattedTime = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)

  SketchCard(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    cornerRadius = 24.dp,
    borderWidth = 1.5.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(22.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Focus Timer",
          style = TextStyle(
            fontFamily = SFProFontFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InkPrimary
          )
        )

        Box(
          modifier = Modifier
            .size(32.dp)
            .border(1.dp, CardBorder, CircleShape)
            .clip(CircleShape)
            .bounceClick { onClose() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = InkSecondary,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Timer Display with OKLCH Focus Ring Gauge
      val totalSeconds = (selectedMinutes * 60).coerceAtLeast(1)
      val timerProgress = (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)

      CircularProgressGauge(
        progress = timerProgress,
        modifier = Modifier.size(170.dp),
        strokeWidth = 9.dp,
        primaryColor = InkWashSlate,
        secondaryColor = InkWashIvory
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = formattedTime,
            style = TextStyle(
              fontFamily = SFProFontFamily,
              fontSize = 36.sp,
              fontWeight = FontWeight.Bold,
              color = InkPrimary
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = if (isRunning) "FOCUSING" else if (remainingSeconds == 0) "COMPLETED" else "PAUSED",
            style = TextStyle(
              fontFamily = SFProFontFamily,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = InkWashSlate
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Preset pills
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
      ) {
        listOf(5, 15, 25, 45, 60).forEach { preset ->
          val isSel = selectedMinutes == preset
          Box(
            modifier = Modifier
              .background(if (isSel) PillBackgroundDark else PillBackgroundDark.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
              .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
              .clip(RoundedCornerShape(12.dp))
              .bounceClick {
                selectedMinutes = preset
                remainingSeconds = preset * 60
                isRunning = false
              }
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = "${preset}m",
              style = TextStyle(
                fontFamily = SFProFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSel) PillTextDark else InkPrimary
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(22.dp))

      // Action controls: Start/Pause and Reset
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .height(46.dp)
            .background(PillBackgroundDark, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .bounceClick {
              if (remainingSeconds == 0) {
                remainingSeconds = selectedMinutes * 60
              }
              isRunning = !isRunning
            },
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (isRunning) "Pause" else "Start Focus",
            style = TextStyle(
              fontFamily = SFProFontFamily,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = PillTextDark
            )
          )
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .height(46.dp)
            .border(1.2.dp, CardBorder, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .bounceClick {
              isRunning = false
              remainingSeconds = selectedMinutes * 60
            },
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Reset",
            style = TextStyle(
              fontFamily = SFProFontFamily,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold,
              color = InkPrimary
            )
          )
        }
      }
    }
  }
}