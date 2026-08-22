
## 2026-08-22: LyricsEntity Schema Verification (Wave 0)
- **Diff**: `LyricsEntity.kt` in YumaPlayer is identical to `ArchiveTune`. Both have the same fields (`id`, `lyrics`, `source`, `updatedAt`), types, and annotations.
- **Database Version**: `MusicDatabase.kt` in YumaPlayer is at version 35.
- **Decision**: No schema changes or migrations are needed for `LyricsEntity`. The baseline is fully compatible.

## 2026-08-22: WAVE1-T1 - PlayerUiState Migration
- **Changes**: 
  - `PlayerUiState.kt`: Changed `lyricsList` from `List<LyricLine>` to `List<LyricsEntry>`. Added `lyricsRomanizationPrefs: LyricsRomanizationPreferences? = null`.
  - `PlayerViewModel.kt`: Updated `parseLyrics` to return `List<LyricsEntry>`. Replaced `LyricLine` with `LyricsEntry`. Replaced `timeMs` with `time` for `LyricsEntry`.
  - `LyricsContentCard.kt`: Replaced `LyricLine` with `LyricsEntry`. Replaced `timeMs` with `time`.
  - `PlayerUiStateTest.kt`: Created test to verify `PlayerUiState` default values and `copy` with `lyricsList`. Used `mockkStatic(android.graphics.Color::class)` to mock `Color.parseColor` for unit tests.
- **Diff**: `git diff --stat` shows changes in `PlayerUiState.kt`, `PlayerViewModel.kt`, `LyricsContentCard.kt`, and `PlayerUiStateTest.kt`.
- **Status**: `compileFossMobileArm64DebugKotlin` and `testFossMobileArm64DebugUnitTest` are green.

## 2026-08-22: WAVE1-T2 - LyricLine Removal
- **Changes**:
  - Deleted `app/src/main/kotlin/moe/rukamori/archivetune/ui/state/LyricLine.kt` completely.
  - Verified with `grep -r LyricLine app/src/main` that no usages remain (except for `PlainLyricLine` which is a different class in `LyricsEnhanced.kt`).
- **Status**: `compileFossMobileArm64DebugKotlin` is green.

## 2026-08-22: WAVE1-T3 - LyricsRomanizationPreferences DataStore Wiring
- **Changes**:
  - `LyricsUtils.kt`: Added default values (`= true`) to `LyricsRomanizationPreferences` data class fields.
  - `SettingsRepository.kt`: Added `val lyricsRomanizationPrefsFlow: Flow<LyricsRomanizationPreferences>`.
  - `SettingsRepositoryImpl.kt`: Implemented `lyricsRomanizationPrefsFlow` using `context.dataStore.data.map` and existing `LyricsRomanize*Key` constants.
  - `PlayerViewModel.kt`: Added `viewModelScope.launch` in `init` block to collect `lyricsRomanizationPrefsFlow` and update `_uiState`.
  - `LyricsPrefsTest.kt`: Created unit tests to verify default values and DataStore persistence using a temporary `PreferenceDataStoreFactory`.
- **Status**: `compileFossMobileArm64DebugKotlin` and `testFossMobileArm64DebugUnitTest` are green.

## 2026-08-22: WAVE2-T1 - PlayerViewModel parseLyrics Delegation
- **Changes**:
  - `PlayerViewModel.kt`: Replaced custom regex-based `parseLyrics` with delegation to `LyricsUtils`.
  - Added support for TTML (`LyricsUtils.parseTtml`), LRC/YRC (`LyricsUtils.parseLyrics`), and plain text fallback (mapping lines to `time = -1L`).
  - Added `LyricsUtils.insertInstrumentalBreaks` to automatically insert instrumental pauses based on `audioPlayer?.duration`.
  - `isSynced` logic remains `parsedLines.any { line -> line.time > 0 }` but now works with the canonical parser.
  - `LyricsUtilsTest.kt`: Created unit tests for `parseLyrics` (LRC, YRC, duplicate timestamps), `parseTtml`, and `insertInstrumentalBreaks`.
- **Status**: `compileFossMobileArm64DebugKotlin` and `testFossMobileArm64DebugUnitTest` are green.

## 2026-08-22: WAVE2-T2 - DB-first fetchLyrics
- **Changes**:
  - `PlayerViewModel.kt`: Modified `fetchLyrics()` to query `playerConnection?.database?.getLyricsById(trackUrl)` before calling `lyricsHelper.getLyrics(metadata)`.
  - If a valid cached lyric is found (not null and not `LYRICS_NOT_FOUND`), it is parsed and emitted to `_uiState` immediately, skipping the network call.
  - `PlayerViewModelLyricsTest.kt`: Created unit tests using Turbine and MockK to verify both DB hit (no network call) and DB miss (network call) scenarios.
  - Used `mockkStatic(android.graphics.Color::class)` to fix `RuntimeException: Method parseColor in android.graphics.Color not mocked` during tests.
- **Status**: `compileFossMobileArm64DebugKotlin` and `testFossMobileArm64DebugUnitTest` are green.

## 2026-08-22: WAVE2-T3 - Ordered Providers via DataStore
- **Changes**:
  - `LyricsHelper.kt`: `orderedProviders` was already implemented using `LyricsProviderOrderKey` and `deserializeLyricsProviderOrder`. Changed `private` to `internal` to allow testing.
  - `LyricsHelperOrderTest.kt`: Created unit test to verify that `orderedProviders` returns providers in the order specified by `LyricsProviderOrderKey` in DataStore. Used MockK to mock `Context.dataStore`.
- **Status**: `compileFossMobileArm64DebugKotlin` and `testFossMobileArm64DebugUnitTest` are green.

## 2026-08-22: WAVE1-2 Manual Device Verification
- **Build**: `./gradlew :app:assembleFossMobileArm64Debug` completed successfully.
- **Artifact**: `app/build/outputs/apk/fossMobileArm64/debug/app-foss-mobile-arm64-debug.apk`
- **Install**: `adb install -r` succeeded.
- **Execution**: App launched via `adb shell monkey`.
- **Logcat**: `adb logcat -d | grep -i lyrics` confirms the lyrics engine is active and querying providers in order:
  - `LyricsPreloadManager: Starting pre-load for 3 songs`
  - `PaxsenixLyrics: Searching Apple Music catalog...`
  - `PaxsenixLyrics: Requesting Musixmatch lyrics...`
  - `PaxsenixLyrics: Requesting YouTube lyrics...`
  - Fallbacks to LrcLib, SimpMusic, NetEase, Unison, KuGou, YouLyPlus observed.
- **Status**: Wave 1 and Wave 2 (T1-T3) integration is verified on-device. The canonical parser and provider chain are functioning as expected.

## Wave 2 - Task 4
- Verified that `handleAction` correctly routes `TranslateLyrics`, `SetLyricsSyncOffset`, `PrepareLyricsEdit`, `SaveLyrics`, `SearchLyrics`, and `ForceRefresh` to their respective private/public methods.
- Added `PlayerViewModelHandleActionTest` to verify the routing using MockK `spyk` and `verify`.
- Mocked `LruCache` to prevent `RuntimeException` during `PlayerViewModel` initialization in tests.

## 2026-08-22: Hotfix Verification (-1 Key Crash)
- **Build**: `./gradlew :app:assembleFossMobileArm64Debug` completed successfully.
- **Install**: `adb install -r` succeeded.
- **Execution**: App launched via `adb shell monkey`.
- **Logcat**: `adb logcat -d | grep -i "Key.*already used\|FATAL"` returned empty, confirming the crash is fixed.
- **Status**: Hotfix verified on-device. Plain lyrics with -1L time no longer crash the LazyColumn.
- Wave3-T2: Implemented progressMsProvider to avoid LazyColumn recomposition on every tick. Used produceState to poll the provider and derivedStateOf inside itemsIndexed to only trigger recomposition when isActive changes.
