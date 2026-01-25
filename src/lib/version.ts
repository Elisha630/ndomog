/**
 * App Version Utility
 * 
 * VERSION UPDATE INSTRUCTIONS:
 * ============================
 * 1. Update "version" in package.json (e.g., "1.0.0" → "1.1.0")
 * 2. Run: npm run build
 * 3. Run: npx cap sync android
 * 4. Build new APK in Android Studio
 * 5. Replace public/downloads/Ndomog.apk with new APK
 * 
 * SEMANTIC VERSIONING GUIDE:
 * ==========================
 * MAJOR (1.x.x → 2.0.0): Breaking changes or major redesigns
 * MINOR (1.0.x → 1.1.0): New features, backward compatible
 * PATCH (1.0.0 → 1.0.1): Bug fixes, small improvements
 */

// APP_VERSION is injected at build time from package.json via vite.config.ts
declare const APP_VERSION: string;

export const getAppVersion = (): string => {
  try {
    return APP_VERSION;
  } catch {
    return '1.0.0';
  }
};

export const getFormattedVersion = (): string => {
  return `v${getAppVersion()}`;
};
