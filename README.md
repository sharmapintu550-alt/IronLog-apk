# IronPulse - Advanced Gym Tracker & Workout Analytics

An AMOLED-optimized workout tracker and progressive overload logger built with Jetpack Compose, Room Database, and Firebase Cloud Sync.

---

## 🚀 Export & Local Build Instructions

### 1. Export Project from Google AI Studio
1. In Google AI Studio Build, open the top-right Settings menu (three dots or gear icon).
2. Click **Download ZIP** or **Export to GitHub**.
3. Unzip the project folder on your local machine:
   ```bash
   unzip IronPulse.zip -d IronPulse
   cd IronPulse
   ```

---

### 2. Generate a Production Keystore
Generate a dedicated release keystore using the standard `keytool` utility bundled with JDK:

```bash
keytool -genkey -v \
  -keystore release-upload-key.jks \
  -alias ironpulse-upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass YourSecurePassword123 \
  -keypass YourSecurePassword123 \
  -dname "CN=IronPulse Developer, OU=Mobile, O=AIStudio, L=San Francisco, ST=CA, C=US"
```

---

### 3. Build Production APK & Android App Bundle (AAB)

#### Option A: Quick Command-Line Build with Environment Variables
Set your release keystore parameters in your shell session:

```bash
export KEYSTORE_PATH="$(pwd)/release-upload-key.jks"
export STORE_PASSWORD="YourSecurePassword123"
export KEY_ALIAS="ironpulse-upload"
export KEY_PASSWORD="YourSecurePassword123"

# Build Signed Production Release APK
./gradlew assembleRelease

# Build Google Play App Bundle (AAB)
./gradlew bundleRelease
```

#### Option B: Output Artifacts
Once compilation finishes:
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`
- **Google Play Bundle**: `app/build/outputs/bundle/release/app-release.aab`
- **Debug APK (for immediate local testing)**: `app/build/outputs/apk/debug/app-debug.apk`

---

## 🔒 Security & Optimization Highlights
- **R8 / ProGuard Optimization**: Automatically strips `Log.d`, `Log.v`, `Log.i` calls from production binaries and preserves Room DAOs, Entities, and JSON models.
- **AMOLED Dark Theme**: Pure `#000000` dark architecture saving power on OLED displays.
- **Encrypted Local Storage**: `android:allowBackup="false"` prevents unauthorized adb backups of local database logs.
- **Secure File Sharing**: AndroidX `FileProvider` configured for sharing CSV logs and PNG summary cards.
