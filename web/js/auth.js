/**
 * TDlxHT Web Auth Portal Logic
 * Handles browser-based login, registration, and password reset.
 * Uses secure Vercel Serverless Functions (/api/register, /api/recover) for zero-leakage,
 * instant email confirmation, and reliable password recovery that bypasses broken SMTP.
 */
import { supabase } from './supabase-config.js';

document.addEventListener("DOMContentLoaded", async () => {
  const tabs = document.querySelectorAll(".auth-tab");
  const loginForm = document.getElementById("login-form");
  const registerForm = document.getElementById("register-form");
  const forgotForm = document.getElementById("forgot-form");
  const alertBox = document.getElementById("auth-alert");
  const userProfileCard = document.getElementById("user-profile-card");
  const authFormsContainer = document.getElementById("auth-forms-container");

  // Tab switching
  tabs.forEach(tab => {
    tab.addEventListener("click", () => {
      tabs.forEach(t => t.classList.remove("active"));
      tab.classList.add("active");

      const target = tab.getAttribute("data-tab");
      loginForm.style.display = target === "login" ? "block" : "none";
      registerForm.style.display = target === "register" ? "block" : "none";
      forgotForm.style.display = target === "forgot" ? "block" : "none";
      clearAlert();
    });
  });

  // Check for existing session
  if (supabase) {
    try {
      const { data: { session } } = await supabase.auth.getSession();
      if (session && session.user) {
        showLoggedInUser(session.user);
      }
    } catch (e) {
      console.warn("Could not retrieve session:", e);
    }
  }

  // Handle Login
  if (loginForm) {
    loginForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      clearAlert();
      const email = document.getElementById("login-email").value.trim();
      const password = document.getElementById("login-password").value;
      const submitBtn = loginForm.querySelector("button[type='submit']");

      if (!supabase) {
        showAlert("Supabase client is not configured. Please check connection.", true);
        return;
      }

      setLoading(submitBtn, true, "Signing In...");
      try {
        const { data, error } = await supabase.auth.signInWithPassword({ email, password });
        if (error) throw error;
        showAlert("Login successful! Launching TDlxHT app...", false);
        setTimeout(() => {
          showLoggedInUser(data.user);
          // Attempt deep link handoff
          window.location.href = "tdlxht://login";
        }, 1200);
      } catch (err) {
        showAlert(mapAuthError(err), true);
      } finally {
        setLoading(submitBtn, false, "Sign In");
      }
    });
  }

  // Handle Register (Dual Path: Backend Auto-Confirm API + Fallback)
  if (registerForm) {
    registerForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      clearAlert();
      const email = document.getElementById("register-email").value.trim();
      const password = document.getElementById("register-password").value;
      const confirm = document.getElementById("register-confirm-password").value;
      const name = document.getElementById("register-name").value.trim();
      const submitBtn = registerForm.querySelector("button[type='submit']");

      if (password !== confirm) {
        showAlert("Passwords do not match.", true);
        return;
      }

      if (password.length < 6) {
        showAlert("Password must be at least 6 characters.", true);
        return;
      }

      setLoading(submitBtn, true, "Creating Account...");
      try {
        // Step 1: Attempt Serverless Registration with auto-confirm
        let registeredViaApi = false;
        try {
          const apiRes = await fetch("/api/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password, display_name: name })
          });

          const apiData = await apiRes.json();
          if (apiRes.ok && apiData.success) {
            registeredViaApi = true;
          } else if (apiData.code === "DB_TRIGGER_ERROR") {
            throw new Error(apiData.error || "Database trigger conflict on auth.users.");
          } else if (apiRes.status === 409) {
            throw new Error("An account with this email already exists. Please sign in.");
          }
        } catch (apiErr) {
          if (apiErr.message.includes("Database trigger conflict") || apiErr.message.includes("already exists")) {
            throw apiErr;
          }
          console.warn("Direct /api/register unavailable, falling back to client signUp:", apiErr);
        }

        // Step 2: If API succeeded, sign in immediately!
        if (registeredViaApi && supabase) {
          const { data: signInData, error: signInErr } = await supabase.auth.signInWithPassword({ email, password });
          if (!signInErr && signInData.user) {
            showAlert("Account created and signed in! Welcome to TDlxHT.", false);
            setTimeout(() => {
              showLoggedInUser(signInData.user);
              window.location.href = "tdlxht://login";
            }, 1000);
            return;
          } else {
            showAlert("Account created successfully! Please sign in with your password.", false);
            // Switch to login tab
            const loginTab = document.querySelector('.auth-tab[data-tab="login"]');
            if (loginTab) loginTab.click();
            document.getElementById("login-email").value = email;
            return;
          }
        }

        // Step 3: Client-side GoTrue fallback if API route was not used
        if (!registeredViaApi) {
          if (!supabase) throw new Error("Supabase client is not available.");
          const webUrl = window.location.origin || "https://tdlxht.vercel.app";
          const redirectUrl = `${webUrl}/auth/callback`;

          const { data, error } = await supabase.auth.signUp({
            email,
            password,
            options: {
              data: { display_name: name },
              emailRedirectTo: redirectUrl
            }
          });

          if (error) throw error;

          if (data.session) {
            showAlert("Account created and signed in! Welcome to TDlxHT.", false);
            setTimeout(() => showLoggedInUser(data.user), 1000);
          } else {
            showAlert(
              "Account created! Confirmation link sent. Please verify your email to continue.",
              false
            );
          }
        }
      } catch (err) {
        showAlert(mapAuthError(err), true);
      } finally {
        setLoading(submitBtn, false, "Create Account");
      }
    });
  }

  // Handle Forgot Password (Dual Path: Instant Recovery API + GoTrue Fallback)
  if (forgotForm) {
    forgotForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      clearAlert();
      const email = document.getElementById("forgot-email").value.trim();
      const submitBtn = forgotForm.querySelector("button[type='submit']");

      setLoading(submitBtn, true, "Sending Recovery Email...");
      try {
        // Step 1: Call secure backend /api/recover to dispatch the email
        let recoveryHandled = false;
        try {
          const apiRes = await fetch("/api/recover", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email })
          });

          const apiData = await apiRes.json();
          if (apiRes.ok && apiData.success) {
            recoveryHandled = true;
            showAlert(
              apiData.message || "✓ If an account with this email exists, a password recovery link has been sent to your Gmail inbox. Please check your inbox and spam folder.",
              false
            );
            submitBtn.textContent = "Email Sent ✓";
            return;
          }
        } catch (apiErr) {
          console.warn("Backend /api/recover failed, falling back to Supabase client:", apiErr);
        }

        // Step 2: Supabase client fallback
        if (!recoveryHandled) {
          if (!supabase) throw new Error("Supabase client is not available.");
          const webUrl = window.location.origin || "https://tdlxht.vercel.app";
          const redirectUrl = `${webUrl}/auth/callback`;

          const { error } = await supabase.auth.resetPasswordForEmail(email, {
            redirectTo: redirectUrl
          });
          if (error) throw error;
          showAlert("Password recovery link sent! Check your Gmail inbox and spam folder.", false);
        }
      } catch (err) {
        showAlert(mapAuthError(err), true);
      } finally {
        setLoading(submitBtn, false, "Send Reset Link");
      }
    });
  }

  function showLoggedInUser(user) {
    if (!userProfileCard || !authFormsContainer) return;
    authFormsContainer.style.display = "none";
    userProfileCard.style.display = "block";

    const emailEl = document.getElementById("profile-user-email");
    const idEl = document.getElementById("profile-user-id");
    const confirmedEl = document.getElementById("profile-email-confirmed");
    const openAppBtn = document.getElementById("profile-open-app-btn");
    const logoutBtn = document.getElementById("profile-logout-btn");

    if (emailEl) emailEl.textContent = user.email;
    if (idEl) idEl.textContent = user.id;
    if (confirmedEl) {
      const isConfirmed = Boolean(user.email_confirmed_at || user.confirmed_at);
      confirmedEl.textContent = isConfirmed ? "✓ Email Confirmed" : "⚠️ Email Unconfirmed";
      confirmedEl.className = isConfirmed ? "pill-badge btn-success" : "pill-badge btn-soft";
    }

    if (openAppBtn) {
      openAppBtn.addEventListener("click", () => {
        window.location.href = "tdlxht://home";
      });
    }

    if (logoutBtn) {
      logoutBtn.addEventListener("click", async () => {
        if (supabase) await supabase.auth.signOut();
        window.location.reload();
      });
    }
  }

  function showAlert(message, isError, allowHtml = false) {
    if (!alertBox) return;
    if (allowHtml) {
      alertBox.innerHTML = message;
    } else {
      alertBox.textContent = message;
    }
    alertBox.className = `alert-box ${isError ? 'error alert-error' : 'success alert-success'}`;
    alertBox.style.display = "block";
  }

  function clearAlert() {
    if (!alertBox) return;
    alertBox.style.display = "none";
    alertBox.textContent = "";
  }

  function setLoading(btn, isLoading, text) {
    if (!btn) return;
    btn.disabled = isLoading;
    btn.textContent = text;
  }

  function mapAuthError(err) {
    const raw = (err && (err.error || err.message)) || "";
    const lower = raw.toLowerCase();

    if (lower.includes("database error") || lower.includes("saving new user") || lower.includes("creating new user") || lower.includes("db_trigger_error")) {
      return "Database setup required: Please run Section 1-3 of supabase_setup.sql in your Supabase SQL Editor to drop the conflicting on_auth_user_auto_confirm trigger.";
    }
    if (lower.includes("error sending recovery email")) {
      return "Supabase email service is rate-limited or unconfigured. Please use the 'Set New Password Now' link or run Section 1-3 in supabase_setup.sql.";
    }
    if (lower.includes("email not confirmed") || lower.includes("email_not_confirmed")) {
      return "Your email address is not confirmed yet. Click the 'Forgot' tab to get an instant verification/login session.";
    }
    if (lower.includes("invalid login credentials")) {
      return "Invalid email or password. If you haven't registered this account yet, please use the Register tab. If you forgot your password, use the Forgot tab.";
    }
    if (lower.includes("user already registered") || lower.includes("already exists")) {
      return "This email is already registered. Please sign in instead.";
    }
    return raw || "An error occurred during authentication. Please verify credentials.";
  }
});
