package com.example.util

/**
 * Action type for user-initiated recovery from safe application errors.
 */
enum class ErrorActionType {
    RETRY,
    RESEND_CONFIRMATION,
    SIGN_IN,
    OPEN_SETTINGS,
    DISMISS
}

/**
 * Controlled application-level error model.
 * Guarantees that no raw exceptions, URLs, headers, status codes,
 * or API keys are ever rendered in the user interface.
 */
sealed class SafeAppError(
    val title: String,
    val description: String? = null,
    val actionType: ErrorActionType? = null,
    val actionLabel: String? = null
) {
    // ------------------------------------------------------------------------
    // Authentication Errors
    // ------------------------------------------------------------------------
    data class EmailNotConfirmed(val email: String? = null) : SafeAppError(
        title = "Account confirmation required",
        description = "Please confirm your email address before signing in.",
        actionType = ErrorActionType.RESEND_CONFIRMATION,
        actionLabel = "Resend confirmation email"
    )

    data object InvalidCredentials : SafeAppError(
        title = "Invalid email or password",
        description = "Please check your credentials and try again."
    )

    data object AccountAlreadyExists : SafeAppError(
        title = "Account already exists",
        description = "An account with this email already exists. Try signing in."
    )

    data object WeakPassword : SafeAppError(
        title = "Password does not meet the requirements",
        description = "Password must be at least 6 characters long."
    )

    data object InvalidEmail : SafeAppError(
        title = "Enter a valid email address",
        description = "Please check the format of your email address."
    )

    data object PasswordMismatch : SafeAppError(
        title = "Passwords do not match",
        description = "Please ensure both password fields are identical."
    )

    data object SessionExpired : SafeAppError(
        title = "Session expired",
        description = "Please sign in again to continue.",
        actionType = ErrorActionType.SIGN_IN,
        actionLabel = "Sign In"
    )

    data object RateLimited : SafeAppError(
        title = "Too many attempts",
        description = "Please wait a moment and try again."
    )

    data object UnableToSignIn : SafeAppError(
        title = "Unable to sign in",
        description = "Please check your connection and credentials, then try again."
    )

    data object UnableToSignUp : SafeAppError(
        title = "Unable to create account",
        description = "Please check your details and try again."
    )

    data object PasswordResetFailed : SafeAppError(
        title = "Unable to process password reset",
        description = "Please check your email address and try again."
    )

    data object UnableToSendResetEmail : SafeAppError(
        title = "Unable to send reset email",
        description = "Please check your connection and try again.",
        actionType = ErrorActionType.RETRY,
        actionLabel = "Retry"
    )

    data object ResendConfirmationFailed : SafeAppError(
        title = "Unable to resend confirmation email",
        description = "Please wait a moment before requesting another confirmation."
    )

    // ------------------------------------------------------------------------
    // Network Errors
    // ------------------------------------------------------------------------
    data object NoInternet : SafeAppError(
        title = "No internet connection",
        description = "Please check your connection and try again.",
        actionType = ErrorActionType.RETRY,
        actionLabel = "Retry"
    )

    data object ConnectionTimeout : SafeAppError(
        title = "Connection timed out",
        description = "The server took too long to respond. Please try again.",
        actionType = ErrorActionType.RETRY,
        actionLabel = "Retry"
    )

    data object ServiceUnavailable : SafeAppError(
        title = "Service temporarily unavailable",
        description = "Please check back in a few minutes.",
        actionType = ErrorActionType.RETRY,
        actionLabel = "Retry"
    )

    data object UnableToConnect : SafeAppError(
        title = "Unable to connect right now",
        description = "Please check your internet connection.",
        actionType = ErrorActionType.RETRY,
        actionLabel = "Retry"
    )

    // ------------------------------------------------------------------------
    // Database Errors
    // ------------------------------------------------------------------------
    data object SaveFailed : SafeAppError(
        title = "Unable to save changes",
        description = "Your change could not be saved right now. Please try again.",
        actionType = ErrorActionType.RETRY,
        actionLabel = "Retry"
    )

    data object LoadFailed : SafeAppError(
        title = "Unable to load your data",
        description = "Could not retrieve your latest items.",
        actionType = ErrorActionType.RETRY,
        actionLabel = "Retry"
    )

    data object DeleteFailed : SafeAppError(
        title = "Unable to delete this item",
        description = "Please try again."
    )

    data object AccessDenied : SafeAppError(
        title = "Access denied",
        description = "You do not have permission to perform this action."
    )

    // ------------------------------------------------------------------------
    // Synchronization Errors
    // ------------------------------------------------------------------------
    data object SyncPaused : SafeAppError(
        title = "Sync paused",
        description = "Changes will be synced when you're back online."
    )

    data object SyncFailed : SafeAppError(
        title = "Unable to sync changes",
        description = "Changes will be synced when connection is restored.",
        actionType = ErrorActionType.RETRY,
        actionLabel = "Retry"
    )

    // ------------------------------------------------------------------------
    // Edge Function Errors
    // ------------------------------------------------------------------------
    data object EdgeFunctionAuthRequired : SafeAppError(
        title = "Authentication required",
        description = "Please sign in to proceed."
    )

    data object EdgeFunctionInvalidRequest : SafeAppError(
        title = "Invalid request",
        description = "The requested action could not be completed."
    )

    // ------------------------------------------------------------------------
    // Notification Errors
    // ------------------------------------------------------------------------
    data object NotificationsDisabled : SafeAppError(
        title = "Notifications are disabled",
        description = "Enable notifications in device settings to receive timely reminders."
    )

    data object NotificationScheduleFailed : SafeAppError(
        title = "Unable to schedule reminder",
        description = "Please check notification permissions in Settings."
    )

    data object NotificationUnavailable : SafeAppError(
        title = "Reminders are currently unavailable",
        description = "Please check your device notification settings."
    )

    // ------------------------------------------------------------------------
    // Napkin / Visual Generation Errors
    // ------------------------------------------------------------------------
    data object NapkinGenerationFailed : SafeAppError(
        title = "Unable to generate report visual",
        description = "Productivity metrics are still fully available.",
        actionType = ErrorActionType.RETRY,
        actionLabel = "Try Again"
    )

    // ------------------------------------------------------------------------
    // Validation Errors
    // ------------------------------------------------------------------------
    data class Validation(val customTitle: String, val customDetail: String? = null) : SafeAppError(
        title = customTitle,
        description = customDetail
    )

    // ------------------------------------------------------------------------
    // Generic / Fallback Safe Errors
    // ------------------------------------------------------------------------
    data class General(
        val customTitle: String = "Something went wrong",
        val customDetail: String? = null,
        val customActionType: ErrorActionType? = null,
        val customActionLabel: String? = null
    ) : SafeAppError(
        title = customTitle,
        description = customDetail,
        actionType = customActionType,
        actionLabel = customActionLabel
    )
}
