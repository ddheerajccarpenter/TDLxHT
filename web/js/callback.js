import { supabase } from './supabase-config.js';
import { initBottomNav } from './bottom-nav.js';

/**
 * TDlxHT Auth Callback & Confirmation Handler
 * Handles Supabase email confirmation, password recovery, and automatic deep linking to Android app.
 */
document.addEventListener("DOMContentLoaded", async () => {
  // Initialize unified bottom navigation
  initBottomNav();
  const loadingCard = document.getElementById("callback-loading");
  const successCard = document.getElementById("callback-success");
  const recoveryCard = document.getElementById("callback-recovery");
  const errorCard = document.getElementById("callback-error");
  const errorMessageEl = document.getElementById("callback-error-message");
  const userEmailEl = document.getElementById("confirmed-user-email");
  const openAppBtn = document.getElementById("callback-open-app-btn");
  const countdownEl = document.getElementById("redirect-countdown");
  const recoveryForm = document.getElementById("recovery-form");
  const recoveryAlert = document.getElementById("recovery-alert");

  // 1. Extract params from Hash Fragment and Query String
  const hash = window.location.hash.substring(1);
  const search = window.location.search.substring(1);
  
  const hashParams = new URLSearchParams(hash);
  const searchParams = new URLSearchParams(search);

  // Check for error parameters
  const error = hashParams.get("error") || searchParams.get("error");
  const errorDescription = hashParams.get("error_description") || searchParams.get("error_description");

  if (error || errorDescription) {
    showError(errorDescription || error || "Failed to process authentication link.");
    return;
  }

  const accessToken = hashParams.get("access_token");
  const refreshToken = hashParams.get("refresh_token");
  const type = hashParams.get("type") || searchParams.get("type") || "";
  const code = searchParams.get("code");
  const tokenHash = searchParams.get("token_hash");

  try {
    let verifiedEmail = "Your Account";

    // 2. Exchange PKCE code, OTP token_hash, or establish session from tokens
    if (supabase) {
      if (tokenHash && type) {
        const { data, error: otpErr } = await supabase.auth.verifyOtp({
          token_hash: tokenHash,
          type: type
        });
        if (otpErr) throw otpErr;
        if (data && data.user) {
          verifiedEmail = data.user.email || verifiedEmail;
        }
      } else if (code) {
        const { data, error: exchangeErr } = await supabase.auth.exchangeCodeForSession(code);
        if (exchangeErr) throw exchangeErr;
        if (data && data.user) {
          verifiedEmail = data.user.email || verifiedEmail;
        }
      } else if (accessToken && refreshToken) {
        const { data, error: sessionErr } = await supabase.auth.setSession({
          access_token: accessToken,
          refresh_token: refreshToken
        });
        if (sessionErr) throw sessionErr;
        if (data && data.user) {
          verifiedEmail = data.user.email || verifiedEmail;
        }
      } else {
        const { data: { session } } = await supabase.auth.getSession();
        if (session && session.user) {
          verifiedEmail = session.user.email || verifiedEmail;
        }
      }
    }

    // 3. Check if this is a Password Recovery Link
    if (type === "recovery") {
      loadingCard.style.display = "none";
      if (recoveryCard) recoveryCard.style.display = "block";

      if (recoveryForm) {
        recoveryForm.addEventListener("submit", async (e) => {
          e.preventDefault();
          const p1 = document.getElementById("new-recovery-password").value;
          const p2 = document.getElementById("confirm-recovery-password").value;
          const submitBtn = document.getElementById("recovery-submit-btn");

          if (p1 !== p2) {
            showRecoveryAlert("Passwords do not match.", true);
            return;
          }
          if (p1.length < 6) {
            showRecoveryAlert("Password must be at least 6 characters.", true);
            return;
          }

          submitBtn.disabled = true;
          submitBtn.textContent = "Updating Password...";

          try {
            const { error: updateErr } = await supabase.auth.updateUser({ password: p1 });
            if (updateErr) throw updateErr;

            showRecoveryAlert("Password updated successfully! Launching TDlxHT app...", false);
            setTimeout(() => {
              window.location.href = "tdlxht://login";
            }, 1200);
          } catch (err) {
            showRecoveryAlert(err.message || "Failed to update password.", true);
            submitBtn.disabled = false;
            submitBtn.textContent = "Save Password & Launch App";
          }
        });
      }
      return;
    }

    // 4. Regular Email Verification Success
    if (userEmailEl) userEmailEl.textContent = verifiedEmail;
    
    loadingCard.style.display = "none";
    if (recoveryCard) recoveryCard.style.display = "none";
    if (errorCard) errorCard.style.display = "none";
    successCard.style.display = "block";

    // 5. Construct Mobile App Deep Link URL
    const scheme = (window.TDLXHT_CONFIG && window.TDLXHT_CONFIG.appScheme) || "tdlxht";
    const deepLinkUrl = `${scheme}://auth/callback${window.location.hash ? window.location.hash : (window.location.search || '')}`;

    if (openAppBtn) {
      openAppBtn.href = deepLinkUrl;
      openAppBtn.addEventListener("click", () => {
        window.location.href = deepLinkUrl;
      });
    }

    // 6. Automatic App Launch with 3-second countdown
    let secondsLeft = 3;
    const interval = setInterval(() => {
      secondsLeft -= 1;
      if (countdownEl) {
        countdownEl.textContent = `Launching TDlxHT App in ${secondsLeft}s...`;
      }
      if (secondsLeft <= 0) {
        clearInterval(interval);
        if (countdownEl) {
          countdownEl.textContent = "Opening TDlxHT App...";
        }
        window.location.href = deepLinkUrl;
      }
    }, 1000);

  } catch (err) {
    console.error("Auth callback verification error:", err);
    showError(err.message || "An unexpected error occurred while processing authentication.");
  }

  function showRecoveryAlert(msg, isError) {
    if (!recoveryAlert) return;
    recoveryAlert.textContent = msg;
    recoveryAlert.className = `alert-box ${isError ? 'error' : 'success'}`;
    recoveryAlert.style.display = "block";
  }

  function showError(msg) {
    if (loadingCard) loadingCard.style.display = "none";
    if (successCard) successCard.style.display = "none";
    if (recoveryCard) recoveryCard.style.display = "none";
    if (errorCard) {
      errorCard.style.display = "block";
      if (errorMessageEl) errorMessageEl.textContent = msg;
    }
  }
});
