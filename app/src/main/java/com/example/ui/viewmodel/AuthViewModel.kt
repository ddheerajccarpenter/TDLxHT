package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.HabitTaskApplication
import com.example.data.repository.AuthRepository
import com.example.data.service.SyncService
import com.example.reminder.ReminderScheduler
import com.example.util.ErrorMapper
import com.example.util.SafeAppError
import com.example.util.SafeLog
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AuthScreen { LOGIN, REGISTER, FORGOT_PASSWORD }

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: SafeAppError? = null,
    val successMessage: String? = null,
    val isResendingConfirmation: Boolean = false,
    val pendingEmail: String? = null
)

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val syncService by lazy {
        SyncService(HabitTaskApplication.instance.database)
    }

    val currentUser: StateFlow<UserInfo?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _authScreen = MutableStateFlow(AuthScreen.LOGIN)
    val authScreen: StateFlow<AuthScreen> = _authScreen.asStateFlow()

    private val _syncState = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncState: StateFlow<SyncStatus> = _syncState.asStateFlow()

    fun switchScreen(screen: AuthScreen) {
        _authScreen.value = screen
        clearMessages()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }

    fun signIn(email: String, password: String) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = SafeAppError.Validation("Enter a valid email address", "Please fill in all fields")
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                pendingEmail = trimmedEmail
            )
            authRepository.signIn(trimmedEmail, trimmedPassword).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    // Download cloud data after sign-in
                    syncAfterLogin()
                },
                onFailure = { e ->
                    SafeLog.w("AuthViewModel", "Sign-in failed", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = ErrorMapper.mapSignIn(e, trimmedEmail)
                    )
                }
            )
        }
    }

    fun signUp(email: String, password: String, displayName: String, confirmPassword: String) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        val trimmedName = displayName.trim()

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = SafeAppError.Validation("Enter a valid email address", "Please fill in all fields")
            )
            return
        }
        if (trimmedPassword != confirmPassword.trim()) {
            _uiState.value = _uiState.value.copy(
                error = SafeAppError.PasswordMismatch
            )
            return
        }
        if (trimmedPassword.length < 6) {
            _uiState.value = _uiState.value.copy(
                error = SafeAppError.WeakPassword
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                pendingEmail = trimmedEmail
            )
            authRepository.signUp(trimmedEmail, trimmedPassword, trimmedName).fold(
                onSuccess = {
                    if (authRepository.isSignedIn()) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = null,
                            successMessage = "Account created successfully!"
                        )
                        syncAfterLogin()
                    } else {
                        // Attempt immediate sign-in in case auto-confirm is enabled in Supabase
                        viewModelScope.launch {
                            authRepository.signIn(trimmedEmail, trimmedPassword).fold(
                                onSuccess = {
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        error = null,
                                        successMessage = "Account created successfully!"
                                    )
                                    syncAfterLogin()
                                },
                                onFailure = { signInErr ->
                                    val mappedErr = ErrorMapper.mapSignIn(signInErr, trimmedEmail)
                                    if (mappedErr is SafeAppError.EmailNotConfirmed) {
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            error = mappedErr,
                                            successMessage = null
                                        )
                                        _authScreen.value = AuthScreen.LOGIN
                                    } else {
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            error = null,
                                            successMessage = "Account created! Please sign in."
                                        )
                                        _authScreen.value = AuthScreen.LOGIN
                                    }
                                }
                            )
                        }
                    }
                },
                onFailure = { e ->
                    SafeLog.w("AuthViewModel", "Sign-up failed", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = ErrorMapper.mapSignUp(e)
                    )
                }
            )
        }
    }

    fun resendConfirmationEmail(email: String? = null) {
        val targetEmail = email?.trim() ?: _uiState.value.pendingEmail?.trim()
        if (targetEmail.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                error = SafeAppError.Validation("Enter a valid email address", "Please enter your email to resend confirmation.")
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isResendingConfirmation = true)
            authRepository.resendConfirmationEmail(targetEmail).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isResendingConfirmation = false,
                        error = null,
                        successMessage = "Confirmation email sent"
                    )
                },
                onFailure = { e ->
                    SafeLog.w("AuthViewModel", "Resend confirmation failed", e)
                    _uiState.value = _uiState.value.copy(
                        isResendingConfirmation = false,
                        error = ErrorMapper.mapResendConfirmation(e)
                    )
                }
            )
        }
    }

    fun signOut(context: Context? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val db = HabitTaskApplication.instance.database
                val taskIds = db.taskDao().getAllTasksSync().map { it.id }
                val habitIds = db.habitDao().getAllHabitsSync().map { it.id }
                val ctx = context ?: HabitTaskApplication.instance
                ReminderScheduler.cancelAllUserReminders(ctx, taskIds, habitIds)

                // Wipe local Room tables to guarantee zero cross-user data leakage
                db.taskDao().clearAllTasks()
                db.subtaskDao().clearAllSubtasks()
                db.habitDao().clearAllHabits()
                db.habitCompletionRecordDao().clearAllRecords()

                // Wipe cached AI productivity insights
                com.example.data.service.AiIntelligenceEngine.instance.clearCache()

                authRepository.signOut()
            } catch (e: Exception) {
                SafeLog.e("AuthViewModel", "Sign out cleanup failed", e)
                authRepository.signOut()
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun deleteAccount(context: Context? = null, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                authRepository.deleteAccount()
            } catch (e: Exception) {
                SafeLog.w("AuthViewModel", "Remote deleteAccount failed (continuing local wipe)", e)
            }
            try {
                val db = HabitTaskApplication.instance.database
                val taskIds = db.taskDao().getAllTasksSync().map { it.id }
                val habitIds = db.habitDao().getAllHabitsSync().map { it.id }
                val ctx = context ?: HabitTaskApplication.instance
                ReminderScheduler.cancelAllUserReminders(ctx, taskIds, habitIds)

                db.taskDao().clearAllTasks()
                db.subtaskDao().clearAllSubtasks()
                db.habitDao().clearAllHabits()
                db.habitCompletionRecordDao().clearAllRecords()

                // Wipe cached AI productivity insights
                com.example.data.service.AiIntelligenceEngine.instance.clearCache()

                authRepository.signOut()
                onComplete()
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun resetPassword(email: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = SafeAppError.Validation("Enter a valid email address", "Please enter your email address.")
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.resetPassword(trimmedEmail).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        successMessage = "Password reset email sent"
                    )
                    _authScreen.value = AuthScreen.LOGIN
                },
                onFailure = { e ->
                    SafeLog.w("AuthViewModel", "Reset password failed", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = ErrorMapper.mapPasswordReset(e)
                    )
                }
            )
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _syncState.value = SyncStatus.Syncing
            syncService.uploadAll().fold(
                onSuccess = { _syncState.value = SyncStatus.Success("Data synced to cloud") },
                onFailure = { e ->
                    SafeLog.w("AuthViewModel", "Sync failed", e)
                    val safeErr = ErrorMapper.mapSync(e)
                    _syncState.value = SyncStatus.Error(safeErr.title)
                }
            )
        }
    }

    private fun syncAfterLogin() {
        viewModelScope.launch {
            _syncState.value = SyncStatus.Syncing
            syncService.downloadAll().fold(
                onSuccess = { _syncState.value = SyncStatus.Success("Data loaded from cloud") },
                onFailure = { _syncState.value = SyncStatus.Idle }
            )
        }
    }
}

sealed class SyncStatus {
    data object Idle : SyncStatus()
    data object Syncing : SyncStatus()
    data class Success(val message: String) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}
