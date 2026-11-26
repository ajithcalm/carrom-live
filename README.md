# Carrom AI Floating – cloud build (no JDK on your phone)

This project builds the Android APK on GitHub Actions. Your phone only needs a browser and a GitHub account; JDK/Gradle are installed on GitHub's cloud runner, not on your phone.

## Build from your Android phone
1. Sign in or create an account at https://github.com.
2. Create a new repository, e.g. `carrom-ai-floating`.
3. Upload the contents of this ZIP to the repository root. Make sure `.github/workflows/android.yml` is included. If GitHub's mobile uploader hides dot-folders, create `.github/workflows/android.yml` using the GitHub website's Add file > Create new file flow and paste the workflow from this project.
4. Open the repository's Actions tab.
5. Select **Build Carrom AI APK** and tap **Run workflow** (or wait for the first push build).
6. When the run completes successfully, open it and download the `CarromAI-debug-apk` artifact.
7. Extract the artifact ZIP on your phone, tap `app-debug.apk`, and install it. Allow installs from your browser/files app only if you trust the repository you uploaded.

## What the floating app does
- Starts a draggable AI bubble using Android overlay permission.
- The bubble opens quick strategy buttons for direct shots, cushion shots, blocked paths and end-game planning.
- The separate main app also includes screenshot import and manual striker/target/pocket marking with approximate angle/power guidance.

## Important limits
- This is a prototype. The floating panel does not capture or analyse the live Carrom Pool screen automatically.
- It does not automate taps, control gameplay, modify game files or bypass anti-cheat.
- Overlay permission must be enabled explicitly by the user.
- Cloud builds require a GitHub account and internet access.
