# Better XCloud Android Client

This module contains an experimental Android application that wraps the Better xCloud user script inside a hardened Chromium-based WebView. The client is designed for handheld consoles and Android phones where the focus is a single immersive tab that always loads the Xbox Cloud Gaming portal with the Better xCloud enhancements applied.

## Feature overview

* **Single purpose browser** – launches directly into `https://www.xbox.com/en-US/play` in fullscreen landscape mode and injects the bundled `better-xcloud.user.js` script automatically.
* **Controller kernel switcher** – lets you move between the native Android controller kernel (low latency events dispatched by the app) and the stock web kernel exposed by the site. Switching is immediate and does not require restarting the stream.
* **Customizable mapping** – remap every button of the standard Xbox layout to any detected Android key code. The mapping is persisted across sessions and is pushed to the JavaScript bridge every time the page loads.
* **Gamepad polyfill** – when the native kernel is active the bridge overrides `navigator.getGamepads()` so the Better xCloud script and the xCloud player receive up-to-date button and axis data without requiring additional browser extensions.
* **Immersive UX additions** – keeps the screen awake, hides system bars, tweaks the user agent to force the desktop streaming UI, and exposes a compact launcher with quick access to Options.

## Project structure

```
android/
├── app/                     # Android application module
│   ├── src/main/assets/     # Bundled Better xCloud script + Android bridge
│   ├── src/main/java/       # Kotlin sources
│   ├── src/main/res/        # Layouts, drawables, strings, preferences
│   └── build.gradle.kts     # Module build configuration
├── build.gradle.kts         # Top-level Gradle configuration
├── gradle.properties        # Global Gradle settings
└── settings.gradle.kts      # Project settings
```

Key Kotlin classes:

* `BetterXcloudApp` – Initializes default preferences.
* `LauncherActivity` – First screen with **Start XCloud** and **Options** actions.
* `XCloudActivity` – Hosts the immersive WebView and wires controller events.
* `BetterXcloudWebView` – Custom WebView that injects scripts and exposes the bridge.
* `ControllerEventDispatcher` – Converts Android `KeyEvent` / `MotionEvent` data into the WebView bridge protocol.
* `OptionsFragment` – Preference screen that controls kernel selection and button mapping.

## Building the client

1. Install the **Android SDK 35** (Android 15) platform and at least **Build Tools 35.0.0** through Android Studio or the command-line tools.
2. From the repository root run:

   ```bash
   cd android
   ./gradlew assembleDebug
   ```

   The first run will download the Gradle wrapper bootstrap JAR (via `curl`, `wget`, or PowerShell) along with the Android Gradle Plugin and all dependencies. The resulting APK can be found at `app/build/outputs/apk/debug/app-debug.apk`.

> **Tip:** Importing the `android` folder as an existing project inside Android Studio gives you access to layout previews, emulator deployment and logcat.

## Using the app

1. Launch the app on your device. The launcher shows **Start XCloud** and **Options**.
2. Open **Options** to choose the controller kernel and remap any button.
   * **Native (Android)** – Uses the built-in bridge. Controller events are injected directly into the page and `navigator.getGamepads()` is polyfilled. Recommended for the lowest latency.
   * **Web (Site default)** – Disables the bridge so the Xbox site handles the controller through the Chromium gamepad API.
3. Press **Start XCloud** to open the immersive player. The Better xCloud script is injected automatically once the page finishes loading.
4. While playing you can long-press the **menu** button to reveal Android system UI (standard gesture for immersive apps).

## Controller remapping notes

* The mapping stores Android key codes for each logical Xbox button. If your controller reports unexpected codes you can inspect them via logcat while pressing the button – the dispatcher logs unknown keys in debug builds.
* Trigger values and analog sticks are normalized between -1 and 1 before being forwarded to JavaScript. The Better xCloud script reads them through the Gamepad API override provided by the bridge.
* When switching to the web kernel the native dispatcher is bypassed so remapping is ignored (the browser handles the controller directly).

## Extending the bridge

The Android bridge script lives in `app/src/main/assets/bx-android-bridge.js`. It exposes the following methods to Kotlin:

* `setKernel(mode)` – Updates the active kernel (`native` or `web`).
* `updateMapping(mapping)` – Receives the persisted key mapping.
* `onNativeEvent(event)` – Handles button/axis events from Android.

Additional telemetry or diagnostics can be implemented by adding new functions to the bridge and wiring them through `BetterXcloudWebView`.

## License

The Android client is distributed under the same MIT license as the rest of the Better xCloud project. The bundled `better-xcloud.user.js` is sourced from the local `dist/` folder during development to ensure parity with the upstream script.
