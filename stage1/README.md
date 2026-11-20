# American Life English — Android Source (4,400-word dataset)

This is the active Android project source.

## Included vocabulary
- 4,400 unique vocabulary records are included in `app/src/main/assets/`.
- The main dataset is `vocabulary_1000.json` (currently 2,997 records; filename retained for compatibility).
- Additional vocabulary is stored in `vocabulary_additional_001.json` through `vocabulary_additional_009.json`.
- The Android loader in `VocabularyData.kt` reads the main dataset and automatically loads every matching additional JSON asset.
- CEFR overrides are stored in `cefr_levels.json`.

The 10 vocabulary JSON files were checked together: 4,400 records and 4,400 unique headwords; no duplicate headwords were found.

## Android project
Open the `stage1` directory as the Gradle project. It uses Java 17, Android Gradle Plugin 8.7.3, Kotlin 2.0.21, and Android SDK 35.

## APK
No APK was built as part of this source update.
