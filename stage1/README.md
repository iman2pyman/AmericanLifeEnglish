# American English Vocabulary — Stage 1 UI

This is the active Android project for the first UI review stage.

## Current scope
- Persian, right-to-left home screen
- Three main actions: vocabulary search, vocabulary learning, and grammar
- Help and About dialogs
- Placeholder destination screens only

The vocabulary database is deliberately not connected in this stage. The grammar screen is intentionally a placeholder until the UI is approved.

## Cloud APK build
The repository workflow at `.github/workflows/build-apk.yml` builds this project with GitHub Actions. Open the repository's Actions tab, select the latest successful "Build American Life English APK" run, and download the `AmericanLifeEnglish-Stage1-APK` artifact.

## Android project
Open this `stage1` directory as the Gradle project. It uses Java 17, Gradle 8.9, Android Gradle Plugin 8.7.3, Kotlin 2.0.21, and Android SDK 35.

This source has been committed to GitHub. An APK is only considered ready after the cloud workflow completes successfully.
