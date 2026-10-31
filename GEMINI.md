# Project Rules & Guidelines for TDlxHT

## Mandatory APK Versioning Workflow
- **Always update the APK version to the latest every time changes, fixes, or features are implemented.**
- Whenever modifying the app:
  1. Increment `versionCode` (+1) and `versionName` (semantic patch increment, e.g. `2.4.7` -> `2.4.8`) in `app/build.gradle.kts`.
  2. Update `"version"` in `package.json` and `package-lock.json`.
  3. Update version strings and download links in `web/index.html`.
  4. Run `./gradlew syncReleaseApk` to clean old APKs and generate the new `TDLxHT.<version>.apk` and `TDlxHT.apk`, while refreshing `version.json` and `web/version.json`.
  5. Run `npm run build` to package the web distribution with the latest APK.

