package com.example.ui.screens.auth

import com.example.ui.theme.SFProFontFamily
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.bounceClick
import com.example.ui.components.heroEntrance
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.AccentTerracotta
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PaperBackground
import com.example.ui.theme.PillBackgroundDark
import com.example.ui.theme.PillTextDark
import com.example.ui.viewmodel.AuthScreen
import com.example.ui.viewmodel.AuthViewModel
import com.example.util.ErrorActionType
import com.example.util.SafeAppError

@Composable
fun AuthenticationScreen(viewModel: AuthViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authScreen by viewModel.authScreen.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperBackground),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = authScreen,
            transitionSpec = {
                (slideInVertically(tween(280)) { it / 6 } + fadeIn(tween(280))) togetherWith
                        (slideOutVertically(tween(200)) { -it / 6 } + fadeOut(tween(200)))
            },
            label = "auth_screen_transition"
        ) { screen ->
            when (screen) {
                AuthScreen.LOGIN -> LoginContent(viewModel = viewModel, uiState = uiState)
                AuthScreen.REGISTER -> RegisterContent(viewModel = viewModel, uiState = uiState)
                AuthScreen.FORGOT_PASSWORD -> ForgotPasswordContent(viewModel = viewModel, uiState = uiState)
            }
        }
    }
}

@Composable
private fun LoginContent(viewModel: AuthViewModel, uiState: com.example.ui.viewmodel.AuthUiState) {
    val focusManager = LocalFocusManager.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Clear messages when entering this screen
    LaunchedEffect(Unit) { viewModel.clearMessages() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 28.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo / Brand
        AppBrandHeader(
            subtitle = "Sign in to your account",
            modifier = Modifier.heroEntrance(delayMs = 40)
        )

        Spacer(Modifier.height(40.dp))

        // Email field
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            leadingIcon = { Icon(Icons.Default.Email, null, tint = InkMuted, modifier = Modifier.size(20.dp)) },
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
            contentDescriptionText = "Email address input",
            modifier = Modifier.staggeredEntrance(index = 1, delayPerItem = 40)
        )

        Spacer(Modifier.height(14.dp))

        // Password field
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = InkMuted, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .bounceClick(scaleDown = 0.88f) { showPassword = !showPassword },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showPassword) "Hide password" else "Show password",
                        tint = InkMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            onImeAction = {
                focusManager.clearFocus()
                viewModel.signIn(email, password)
            },
            contentDescriptionText = "Password input",
            modifier = Modifier.staggeredEntrance(index = 2, delayPerItem = 40)
        )

        // Forgot password link
        Row(
            Modifier.fillMaxWidth().staggeredEntrance(index = 3, delayPerItem = 40),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Forgot password?",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = AccentTerracotta,
                    textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .bounceClick { viewModel.switchScreen(AuthScreen.FORGOT_PASSWORD) }
            )
        }

        Spacer(Modifier.height(8.dp))

        // Error / success messages
        MessageBanner(
            error = uiState.error,
            success = uiState.successMessage,
            onActionClick = { actionType ->
                when (actionType) {
                    ErrorActionType.RESEND_CONFIRMATION -> viewModel.resendConfirmationEmail(email)
                    ErrorActionType.RETRY -> {
                        focusManager.clearFocus()
                        viewModel.signIn(email, password)
                    }
                    else -> {}
                }
            },
            isActionLoading = uiState.isResendingConfirmation
        )

        Spacer(Modifier.height(24.dp))

        // Sign in button
        AuthPrimaryButton(
            text = if (uiState.isLoading) "Signing in…" else "Sign In",
            isLoading = uiState.isLoading,
            onClick = {
                focusManager.clearFocus()
                viewModel.signIn(email, password)
            },
            modifier = Modifier.staggeredEntrance(index = 4, delayPerItem = 40)
        )

        Spacer(Modifier.height(24.dp))

        // Switch to register
        AuthSwitchRow(
            promptText = "Don't have an account?",
            actionText = "Create one",
            onClick = { viewModel.switchScreen(AuthScreen.REGISTER) },
            modifier = Modifier.staggeredEntrance(index = 5, delayPerItem = 40)
        )
    }
}

@Composable
private fun RegisterContent(viewModel: AuthViewModel, uiState: com.example.ui.viewmodel.AuthUiState) {
    val focusManager = LocalFocusManager.current
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.clearMessages() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 28.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppBrandHeader(
            subtitle = "Create your account",
            modifier = Modifier.heroEntrance(delayMs = 40)
        )

        Spacer(Modifier.height(36.dp))

        AuthTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = "Display Name (optional)",
            leadingIcon = { Icon(Icons.Default.Person, null, tint = InkMuted, modifier = Modifier.size(20.dp)) },
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
            contentDescriptionText = "Display name input",
            modifier = Modifier.staggeredEntrance(index = 1, delayPerItem = 40)
        )

        Spacer(Modifier.height(12.dp))

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            leadingIcon = { Icon(Icons.Default.Email, null, tint = InkMuted, modifier = Modifier.size(20.dp)) },
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
            contentDescriptionText = "Email address input",
            modifier = Modifier.staggeredEntrance(index = 2, delayPerItem = 40)
        )

        Spacer(Modifier.height(12.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password (min 6 characters)",
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = InkMuted, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .bounceClick(scaleDown = 0.88f) { showPassword = !showPassword },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showPassword) "Hide password" else "Show password",
                        tint = InkMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
            contentDescriptionText = "Password input",
            modifier = Modifier.staggeredEntrance(index = 3, delayPerItem = 40)
        )

        Spacer(Modifier.height(12.dp))

        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password",
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = InkMuted, modifier = Modifier.size(20.dp)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            onImeAction = {
                focusManager.clearFocus()
                viewModel.signUp(email, password, displayName, confirmPassword)
            },
            contentDescriptionText = "Confirm password input",
            modifier = Modifier.staggeredEntrance(index = 4, delayPerItem = 40)
        )

        Spacer(Modifier.height(8.dp))

        MessageBanner(
            error = uiState.error,
            success = uiState.successMessage,
            onActionClick = { actionType ->
                if (actionType == ErrorActionType.RETRY) {
                    focusManager.clearFocus()
                    viewModel.signUp(email, password, displayName, confirmPassword)
                }
            }
        )

        Spacer(Modifier.height(24.dp))

        AuthPrimaryButton(
            text = if (uiState.isLoading) "Creating account…" else "Create Account",
            isLoading = uiState.isLoading,
            onClick = {
                focusManager.clearFocus()
                viewModel.signUp(email, password, displayName, confirmPassword)
            },
            modifier = Modifier.staggeredEntrance(index = 5, delayPerItem = 40)
        )

        Spacer(Modifier.height(24.dp))

        AuthSwitchRow(
            promptText = "Already have an account?",
            actionText = "Sign in",
            onClick = { viewModel.switchScreen(AuthScreen.LOGIN) },
            modifier = Modifier.staggeredEntrance(index = 6, delayPerItem = 40)
        )
    }
}

@Composable
private fun ForgotPasswordContent(
    viewModel: AuthViewModel,
    uiState: com.example.ui.viewmodel.AuthUiState
) {
    val focusManager = LocalFocusManager.current
    var email by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.clearMessages() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 28.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppBrandHeader(
            subtitle = "Reset your password",
            modifier = Modifier.heroEntrance(delayMs = 40)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Enter your email and we'll send you a link to reset your password.",
            style = TextStyle(fontSize = 14.sp, color = InkSecondary, textAlign = TextAlign.Center, lineHeight = 20.sp),
            modifier = Modifier.fillMaxWidth().staggeredEntrance(index = 1, delayPerItem = 40)
        )

        Spacer(Modifier.height(32.dp))

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            leadingIcon = { Icon(Icons.Default.Email, null, tint = InkMuted, modifier = Modifier.size(20.dp)) },
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            onImeAction = {
                focusManager.clearFocus()
                viewModel.resetPassword(email)
            },
            contentDescriptionText = "Email address for password reset",
            modifier = Modifier.staggeredEntrance(index = 2, delayPerItem = 40)
        )

        Spacer(Modifier.height(8.dp))

        MessageBanner(
            error = uiState.error,
            success = uiState.successMessage,
            onActionClick = { actionType ->
                if (actionType == ErrorActionType.RETRY) {
                    focusManager.clearFocus()
                    viewModel.resetPassword(email)
                }
            }
        )

        Spacer(Modifier.height(24.dp))

        AuthPrimaryButton(
            text = if (uiState.isLoading) "Sending…" else "Send Reset Link",
            isLoading = uiState.isLoading,
            onClick = {
                focusManager.clearFocus()
                viewModel.resetPassword(email)
            },
            modifier = Modifier.staggeredEntrance(index = 3, delayPerItem = 40)
        )

        Spacer(Modifier.height(24.dp))

        AuthSwitchRow(
            promptText = "Remembered it?",
            actionText = "Back to Sign In",
            onClick = { viewModel.switchScreen(AuthScreen.LOGIN) },
            modifier = Modifier.staggeredEntrance(index = 4, delayPerItem = 40)
        )
    }
}

// ---------------------------------------------------------------------------
// Shared composables
// ---------------------------------------------------------------------------

@Composable
private fun AppBrandHeader(subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(R.drawable.app_logo),
            contentDescription = "TDlxHT Logo",
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .border(2.dp, CardBorder, CircleShape)
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = "TDlxHT",
            style = TextStyle(
                fontFamily = com.example.ui.theme.PunkGrandmotherFontFamily,
                fontSize = 40.sp,
                fontWeight = FontWeight.Normal,
                color = InkPrimary
            )
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = subtitle,
            style = TextStyle(fontSize = 14.sp, color = InkSecondary)
        )
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    contentDescriptionText: String = "",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = TextStyle(fontSize = 13.sp, color = InkMuted)) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
        ),
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = contentDescriptionText },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = InkPrimary,
            unfocusedTextColor = InkPrimary,
            focusedBorderColor = InkPrimary,
            unfocusedBorderColor = CardBorder,
            focusedContainerColor = CardBackground,
            unfocusedContainerColor = CardBackground,
            cursorColor = InkPrimary
        ),
        textStyle = TextStyle(fontSize = 15.sp, fontFamily = FontFamily.Default, color = InkPrimary)
    )
}

@Composable
private fun AuthPrimaryButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(PillBackgroundDark, RoundedCornerShape(14.dp))
            .border(1.5.dp, CardBorder, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .bounceClick(enabled = !isLoading) { onClick() }
            .semantics { contentDescription = text },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = PillTextDark
            )
        } else {
            Text(
                text = text,
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

@Composable
private fun AuthSwitchRow(
    promptText: String,
    actionText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$promptText ", style = TextStyle(fontSize = 14.sp, color = InkSecondary))
        Text(
            text = actionText,
            style = TextStyle(
                fontSize = 14.sp,
                color = AccentTerracotta,
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline
            ),
            modifier = Modifier.bounceClick { onClick() }
        )
    }
}

@Composable
private fun MessageBanner(
    error: SafeAppError?,
    success: String?,
    onActionClick: ((ErrorActionType) -> Unit)? = null,
    isActionLoading: Boolean = false
) {
    AnimatedVisibility(
        visible = error != null || success != null,
        enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { -it },
        exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { -it }
    ) {
        val isError = error != null
        val bgColor = Color(0xFF252525)
        val borderColor = Color(0xFFFFFFFF)
        val titleColor = Color(0xFFFFFFFF)
        val descColor = Color(0xFFFFFFFF)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .background(bgColor, RoundedCornerShape(10.dp))
                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column {
                if (isError && error != null) {
                    Text(
                        text = error.title,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        ),
                        modifier = Modifier.semantics { contentDescription = "Error: ${error.title}" }
                    )

                    if (!error.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = error.description,
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = descColor,
                                lineHeight = 18.sp
                            )
                        )
                    }

                    if (error.actionType != null && !error.actionLabel.isNullOrBlank() && onActionClick != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .border(1.dp, titleColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.7f))
                                .bounceClick(enabled = !isActionLoading) {
                                    onActionClick(error.actionType)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            if (isActionLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = titleColor
                                )
                            } else {
                                Text(
                                    text = error.actionLabel,
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = titleColor
                                    )
                                )
                            }
                        }
                    }
                } else if (success != null) {
                    Text(
                        text = success,
                        style = TextStyle(fontSize = 13.sp, color = titleColor, lineHeight = 18.sp),
                        modifier = Modifier.semantics { contentDescription = "Success: $success" }
                    )
                }
            }
        }
    }
}