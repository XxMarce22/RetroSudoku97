# Sudoku 97

A complete, offline, ad-free 9x9 Sudoku game for Android, built with Kotlin and Jetpack Compose. The interface is a hand-drawn Windows 95/97 tribute: teal desktop, grey beveled windows, navy title bars, pixel-style borders, and a chunky on-screen keypad.

## Features

- Classic 9x9 Sudoku with Easy, Medium and Hard difficulties.
- Backtracking puzzle generator with a unique-solution guarantee.
- Dual-direction bit-mask solver used to verify uniqueness.
- Clue ranges:
  - Easy: 36-42 clues
  - Medium: 30-35 clues
  - Hard: 24-29 clues
- Pencil / annotation mode with small candidate notes.
- Hint system:
  - Naked singles
  - Hidden singles
  - Naked pairs with candidate eliminations
  - One-cell reveal only when no simple pattern is available
- Optional red conflict highlighting for duplicate values in a row, column or 3x3 box.
- Automatic win detection and a retro completion dialog.
- Best times stored per difficulty.
- Active game, board values, pencil notes, pencil-mode state and elapsed timer saved in `SharedPreferences`.
- Portrait and landscape layouts.
- No internet permission, no ad SDK, no analytics, no trackers, no third-party runtime dependencies.

## Download and install on Android

Sudoku 97 runs on **Android 7.0 (API 24) or newer**. It is a single APK, works completely offline, and requests no permissions.

### Option 1 — Install the APK from GitHub Releases

1. On your Android phone or tablet, open the [latest release](../../releases/latest) page.
2. Download the APK asset (for example `Sudoku97-1.0.apk`).
3. Open the downloaded file. You can tap the download notification, or use **Files → Downloads**.
4. Android will ask whether to allow installs from this source:
   - Tap **Settings** in the prompt.
   - Turn on **Allow from this source**.
   - Go back and tap **Install**.
5. Open **Sudoku 97** from your app drawer.

If Play Protect shows a warning, choose **More details → Install anyway**. The APK is built from the source in this repository and declares no permissions at all.

To move the APK from a computer instead, copy it to the phone over USB or with any file-transfer app, then open it from the phone's **Files** app and follow steps 3-5.

### Option 2 — Build and install with ADB

With the phone connected and USB debugging enabled:

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or launch it directly:

```bash
adb shell am start -n com.retro.sudoku97/.MainActivity
```

### Verify it is offline

Open **Settings → Apps → Sudoku 97 → Permissions**. The list is empty because the app declares no permissions, including no `INTERNET`.

## Project structure

```
app/src/main/java/com/retro/sudoku97/
├── MainActivity.kt
├── SudokuViewModel.kt              # MVVM state, timer, persistence, hint application
├── data/GameRepository.kt          # SharedPreferences persistence
├── logic/
│   ├── SudokuSolver.kt             # bit-mask solver + dual-direction uniqueness check
│   ├── SudokuGenerator.kt          # deterministic seeded generator
│   ├── SudokuRules.kt              # units, peers, candidates, conflicts
│   └── HintEngine.kt               # naked/hidden singles, naked pairs, fallback
├── model/GameModels.kt             # difficulty, UI state, saved game, formatting
└── ui/
    ├── RetroTheme.kt               # Windows 95/97 palette and Material 3 theme
    ├── RetroSudokuApp.kt           # app root + dialog routing
    ├── components/
    │   ├── RetroComponents.kt      # bevels, buttons, title bar, menus, progress
    │   └── SudokuBoardView.kt      # 9x9 board, notes, highlights, hit testing
    └── screens/
        ├── StartScreen.kt          # difficulty selection / resume / best times
        ├── GameScreen.kt           # game window, menus, keypad, hint bar
        └── GameDialogs.kt          # new game, best times, help, about, exit, win
```

## Build

Requirements:

- JDK 17
- Android SDK Platform 35
- Android Build Tools 35.0.0

The included Gradle wrapper downloads Gradle 8.9 on first use.

```bash
cd RetroSudoku97
./gradlew :app:assembleDebug
```

The APK is written to:

```
app/build/outputs/apk/debug/app-debug.apk
```

Run unit tests with:

```bash
./gradlew :app:testDebugUnitTest
```

Run Android lint with:

```bash
./gradlew :app:lintDebug
```

### Publish an APK for download

The APK is a build artifact and is not committed to git. Attach it to a GitHub Release so the download link above works:

1. Push the repository to GitHub.
2. Open **Releases → Draft a new release**.
3. Pick a tag such as `v1.0`, add a title, and attach `app/build/outputs/apk/debug/app-debug.apk`.
4. Publish the release.

If you have the GitHub CLI installed, the same thing is one command:

```bash
gh release create v1.0 app/build/outputs/apk/debug/app-debug.apk \
  --title "Sudoku 97 v1.0" \
  --notes "First offline release. Android 7.0+."
```

If your Android SDK is not in the default location, create `local.properties`:

```properties
sdk.dir=/path/to/android-sdk
```

## Generation and uniqueness

`SudokuGenerator` fills a complete board with randomized backtracking, then removes cells in a seeded shuffled order. After each removal it calls `SudokuSolver.hasUniqueSolution`. That method counts solutions in two independent directional passes:

- forward scan with ascending candidates
- reverse scan with descending candidates

Both passes use a minimum-remaining-values search for speed. A puzzle is accepted only when both passes return exactly one solution and the solved board still matches the original complete board. The same seed always produces the same puzzle, which makes generation deterministic and testable.

## Hint behavior

Hints are based on the logical candidate mask for every empty cell, independent of whether the player has written notes yet.

1. Naked single: a cell with one legal candidate.
2. Hidden single: a digit that can only go in one cell of a unit.
3. Naked pair: two cells in a unit with the same two candidates. The affected candidates are removed from the other highlighted cells in that unit.
4. Fallback: if none of the above exists, exactly one correct cell is revealed.

Naked-pair hints seed candidate notes in affected cells when necessary, so pressing Hint repeatedly continues to advance instead of returning the same pair forever.

## Persistence

`GameRepository` stores everything locally in `sudoku97_preferences`:

- active game flag
- difficulty
- puzzle, solution, current values and candidate masks
- elapsed seconds
- pencil-mode state
- best time per difficulty
- conflict-highlighting preference

`MainActivity.onPause()` and `onStop()` both ask the ViewModel to stop its timer and save the current state. The game also saves after each move and every 15 seconds while the timer is running.

## Privacy

The source manifest contains no permission declarations at all. In particular, there is no `android.permission.INTERNET`. There are no ad networks, analytics libraries, crash reporters or external SDKs in the runtime dependency graph. All puzzle generation and hint logic run on-device.

`android:allowBackup` is disabled so game data stays on the device.
