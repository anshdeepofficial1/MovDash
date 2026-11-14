const STORAGE_KEY = "zylo_download_preferences";

const DEFAULTS = {
  preferredQuality: "ask", // "ask" | "1080p" | "720p" | "480p"
  preferredServer: "auto",
  preferredLanguage: "default",
  includeSubtitles: true,
  rememberChoice: false
};

export const DownloadPreferencesStore = {
  get() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return { ...DEFAULTS };
      const parsed = JSON.parse(raw);
      return {
        preferredQuality: parsed?.preferredQuality || DEFAULTS.preferredQuality,
        preferredServer: parsed?.preferredServer || DEFAULTS.preferredServer,
        preferredLanguage: parsed?.preferredLanguage || DEFAULTS.preferredLanguage,
        includeSubtitles: parsed?.includeSubtitles !== false,
        rememberChoice: Boolean(parsed?.rememberChoice)
      };
    } catch (_) {
      return { ...DEFAULTS };
    }
  },

  save(preferences = {}) {
    try {
      const current = this.get();
      const updated = {
        ...current,
        ...preferences
      };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
      return updated;
    } catch (_) {
      return { ...DEFAULTS };
    }
  },

  resetRememberedChoice() {
    return this.save({
      rememberChoice: false,
      preferredQuality: "ask"
    });
  }
};
