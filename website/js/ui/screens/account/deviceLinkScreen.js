import { ScreenUtils } from "../../navigation/screen.js";
import { AuthManager } from "../../../core/auth/authManager.js";
import { QrLoginService } from "../../../core/auth/qrLoginService.js";
import { I18n } from "../../../i18n/index.js";

async function navigateTo(route, params = {}, options = {}) {
  const { Router } = await import("../../navigation/router.js");
  return Router.navigate(route, params, options);
}

function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

export const DeviceLinkScreen = {
  async mount(params = {}) {
    this.container = document.getElementById("account");
    ScreenUtils.show(this.container);

    let code = params?.code || "";
    if (!code && typeof window !== "undefined") {
      const search = new URLSearchParams(window.location.search);
      code = search.get("code") || "";
    }
    this.code = String(code || "").trim().toUpperCase();
    this.statusMessage = "";
    this.isSuccess = false;
    this.isSubmitting = false;

    this.render();

    // Auto-submit if code is pre-filled and user is authenticated
    if (this.code && AuthManager.isAuthenticated && this.code.length >= 6) {
      await this.handleApprove();
    }
  },

  render() {
    const isSignedIn = AuthManager.isAuthenticated;
    const formattedCode = this.code ? this.code.slice(0, 8) : "";

    this.container.innerHTML = `
      <div class="auth-simple-shell">
        <div class="auth-simple-hero">
          <div class="qr-brand-lockup" style="margin-bottom: 24px;">
            <img src="assets/brand/app_logo_wordmark.png" class="qr-logo" alt="ZYLO" style="max-height: 40px;" />
          </div>
          <h2 class="auth-simple-title">Link Device to ZYLO</h2>
          <p class="auth-simple-subtitle">
            ${
              isSignedIn
                ? "Enter the 6-character code shown on your TV or another screen to link your account."
                : "Please sign in to your ZYLO account first to approve and link your device."
            }
          </p>
        </div>

        <div class="auth-card-body" style="max-width: 420px; margin: 0 auto; width: 100%;">
          ${
            this.statusMessage
              ? `<div class="auth-status-message ${this.isSuccess ? "auth-status-success" : "auth-status-error"}" style="padding: 12px 16px; margin-bottom: 16px; border-radius: 8px; text-align: center; background: ${this.isSuccess ? "rgba(46, 213, 115, 0.15)" : "rgba(255, 71, 87, 0.15)"}; color: ${this.isSuccess ? "#2ed573" : "#ff4757"}; font-weight: 500;">
                  ${escapeHtml(this.statusMessage)}
                </div>`
              : ""
          }

          ${
            this.isSuccess
              ? `
                <div class="auth-simple-actions" style="margin-top: 24px;">
                  <button type="button" id="link-done-btn" class="auth-simple-card focusable" style="width: 100%; text-align: center; justify-content: center; background: #e50914; color: white;">
                    Go to Home
                  </button>
                </div>
              `
              : isSignedIn
                ? `
                <div style="margin-bottom: 20px;">
                  <label for="device-link-input" style="display: block; font-size: 13px; color: #aaa; margin-bottom: 8px;">6-Character Code</label>
                  <input
                    id="device-link-input"
                    type="text"
                    maxlength="8"
                    placeholder="e.g. UQTXPW"
                    value="${escapeHtml(formattedCode)}"
                    autocomplete="off"
                    autocapitalize="characters"
                    spellcheck="false"
                    class="settings-text-dialog-field focusable"
                    style="width: 100%; padding: 14px 16px; font-size: 20px; font-weight: bold; letter-spacing: 3px; text-align: center; text-transform: uppercase; background: #1c1c1e; border: 1px solid #333; border-radius: 8px; color: #fff;"
                    ${this.isSubmitting ? "disabled" : ""}
                  />
                </div>

                <div class="auth-simple-actions" style="display: flex; flex-direction: column; gap: 12px;">
                  <button
                    type="button"
                    id="link-submit-btn"
                    class="auth-simple-card focusable"
                    style="width: 100%; text-align: center; justify-content: center; background: #e50914; color: white; font-weight: 600;"
                    ${this.isSubmitting ? "disabled" : ""}
                  >
                    ${this.isSubmitting ? "Approving…" : "Approve Device"}
                  </button>
                  <button
                    type="button"
                    id="link-cancel-btn"
                    class="auth-simple-card focusable"
                    style="width: 100%; text-align: center; justify-content: center; background: #2c2c2e; color: #ccc;"
                  >
                    Cancel
                  </button>
                </div>
              `
                : `
                <div class="auth-simple-actions" style="display: flex; flex-direction: column; gap: 12px; margin-top: 16px;">
                  <button
                    type="button"
                    id="link-signin-btn"
                    class="auth-simple-card focusable"
                    style="width: 100%; text-align: center; justify-content: center; background: #e50914; color: white; font-weight: 600;"
                  >
                    Sign In with Email
                  </button>
                  <button
                    type="button"
                    id="link-cancel-btn"
                    class="auth-simple-card focusable"
                    style="width: 100%; text-align: center; justify-content: center; background: #2c2c2e; color: #ccc;"
                  >
                    Cancel
                  </button>
                </div>
              `
          }
        </div>
      </div>
    `;

    this.bindEvents();
    ScreenUtils.indexFocusables(this.container);
    ScreenUtils.setInitialFocus(this.container);
  },

  bindEvents() {
    const input = this.container.querySelector("#device-link-input");
    const submitBtn = this.container.querySelector("#link-submit-btn");
    const cancelBtn = this.container.querySelector("#link-cancel-btn");
    const doneBtn = this.container.querySelector("#link-done-btn");
    const signInBtn = this.container.querySelector("#link-signin-btn");

    if (input) {
      input.oninput = (e) => {
        this.code = String(e.target.value || "").toUpperCase().replace(/[^A-Z0-9]/g, "");
        e.target.value = this.code;
      };
      input.onkeydown = (e) => {
        if (e.key === "Enter" && this.code.length >= 6) {
          this.handleApprove();
        }
      };
    }

    if (submitBtn) {
      submitBtn.onclick = () => this.handleApprove();
    }

    if (cancelBtn) {
      cancelBtn.onclick = () => {
        navigateTo("home", {}, { replaceHistory: true });
      };
    }

    if (doneBtn) {
      doneBtn.onclick = () => {
        navigateTo("home", {}, { replaceHistory: true });
      };
    }

    if (signInBtn) {
      signInBtn.onclick = () => {
        navigateTo("authSignIn", { redirectRoute: "link", redirectCode: this.code });
      };
    }
  },

  async handleApprove() {
    const code = this.code.trim();
    if (!code || code.length < 4) {
      this.statusMessage = "Please enter a valid 6-character code.";
      this.isSuccess = false;
      this.render();
      return;
    }

    this.isSubmitting = true;
    this.statusMessage = "Approving device link…";
    this.isSuccess = false;
    this.render();

    try {
      const ok = await QrLoginService.approve(code);
      if (ok) {
        this.isSuccess = true;
        this.statusMessage = "Device approved successfully! The screen will update momentarily.";
      } else {
        this.isSuccess = false;
        this.statusMessage = QrLoginService.getLastError() || "Failed to approve device. Please check the code and try again.";
      }
    } catch (err) {
      this.isSuccess = false;
      this.statusMessage = String(err?.message || "Failed to approve device.");
    } finally {
      this.isSubmitting = false;
      this.render();
    }
  },

  unmount() {
    this.isSubmitting = false;
    this.code = "";
    this.statusMessage = "";
  }
};
