# Google Sign-In Setup Instructions

## 🔑 Your SHA-1 Fingerprints

I've extracted your SHA-1 fingerprints from your keystores:

### Debug Keystore (for development/testing):
```
SHA1: 89:89:F6:FF:1D:9B:5C:3A:6A:B8:A9:32:AE:50:10:57:6C:99:A2:14
```

### Release Keystore (for production):
```
SHA1: FC:14:88:25:0B:1E:21:DD:F9:86:BF:9B:8E:24:72:CA:3E:D8:75:AC
```

## 📋 Step-by-Step Setup Guide

### Step 1: Google Cloud Console Setup

1. **Go to Google Cloud Console**
   - Visit: https://console.cloud.google.com/
   - Sign in with your Google account

2. **Create or Select Project**
   - If you don't have a project, click "Select a project" → "New Project"
   - Name it something like "FirebaseAuthTest" or use your existing Firebase project

3. **Enable Google Sign-In API**
   - Go to "APIs & Services" → "Library"
   - Search for "Google Sign-In API" and enable it
   - Also enable "Google+ API" if available

4. **Configure OAuth Consent Screen**
   - Go to "APIs & Services" → "OAuth consent screen"
   - Choose "External" (unless you have a Google Workspace)
   - Fill in required fields:
     - App name: "FirebaseAuthTest"
     - User support email: your email
     - Developer contact: your email
   - Save and continue through all steps

### Step 2: Create OAuth 2.0 Client ID

1. **Go to Credentials**
   - Navigate to "APIs & Services" → "Credentials"

2. **Create OAuth Client ID**
   - Click "Create Credentials" → "OAuth client ID"
   - Choose "Web application"
   - Name: "Android App Web Client"
   - Authorized redirect URIs: Add these (replace `your-project-id` with your Firebase project ID):
     ```
     https://your-project-id.firebaseapp.com/__/auth/handler
     https://your-project-id.web.app/__/auth/handler
     ```

3. **Copy the Client ID**
   - After creation, you'll see a popup with your Client ID
   - Copy this value (it looks like: `123456789-abcdefg.apps.googleusercontent.com`)

### Step 3: Update Your Android Project

1. **Update strings.xml**
   - Open `app/src/main/res/values/strings.xml`
   - Replace `YOUR_GOOGLE_WEB_CLIENT_ID_HERE` with your actual Client ID:

```xml
<string name="default_web_client_id">your-actual-client-id-here</string>
```

### Step 4: Add SHA-1 Fingerprints to Firebase

1. **Go to Firebase Console**
   - Visit: https://console.firebase.google.com/
   - Select your project

2. **Add SHA-1 Fingerprints**
   - Click the gear icon → "Project settings"
   - Scroll down to "Your apps" section
   - Click on your Android app
   - In "SHA certificate fingerprints" section:
     - Click "Add fingerprint"
     - Add the debug SHA-1: `89:89:F6:FF:1D:9B:5C:3A:6A:B8:A9:32:AE:50:10:57:6C:99:A2:14`
     - Click "Add fingerprint" again
     - Add the release SHA-1: `FC:14:88:25:0B:1E:21:DD:F9:86:BF:9B:8E:24:72:CA:3E:D8:75:AC`
   - Save changes

### Step 5: Add SHA-1 Fingerprints to Google Cloud Console

1. **Go back to Google Cloud Console**
   - Navigate to "APIs & Services" → "Credentials"

2. **Edit OAuth Client ID**
   - Click on your OAuth 2.0 Client ID
   - Scroll down to "Authorized JavaScript origins"
   - Add your app's package name in this format:
     ```
     android://com.example.firebaseauthtest
     ```

3. **Add SHA-1 to Android Client (if exists)**
   - If you have an Android OAuth client, add both SHA-1 fingerprints there
   - If not, create one:
     - Click "Create Credentials" → "OAuth client ID"
     - Choose "Android"
     - Package name: `com.example.firebaseauthtest`
     - Add both SHA-1 fingerprints

## 🧪 Testing Your Setup

1. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

2. **Test Google Sign-In**
   - Launch the app
   - Tap "Sign in with Google"
   - Complete the authentication flow
   - Verify you see user details on success screen

## 🔧 Troubleshooting

### Common Issues:

1. **"Sign in failed" error**
   - Check that SHA-1 fingerprints are added to both Firebase and Google Console
   - Verify the Web Client ID is correct in strings.xml
   - Ensure Google Play Services is installed on device

2. **"Invalid client" error**
   - Double-check the Web Client ID in strings.xml
   - Make sure you're using the Web application client ID, not Android client ID

3. **"App not authorized" error**
   - Add your app's package name to Google Console
   - Ensure OAuth consent screen is configured

4. **Build errors**
   - Clean and rebuild: `./gradlew clean assembleDebug`
   - Check that all dependencies are properly added

## ✅ Verification Checklist

- [ ] Google Cloud Console project created
- [ ] OAuth consent screen configured
- [ ] Web application OAuth client created
- [ ] Client ID copied to strings.xml
- [ ] SHA-1 fingerprints added to Firebase
- [ ] SHA-1 fingerprints added to Google Console
- [ ] App builds successfully
- [ ] Google Sign-In works in app

## 📱 Your App Package Details

- **Package Name**: `com.example.firebaseauthtest`
- **Debug SHA-1**: `89:89:F6:FF:1D:9B:5C:3A:6A:B8:A9:32:AE:50:10:57:6C:99:A2:14`
- **Release SHA-1**: `FC:14:88:25:0B:1E:21:DD:F9:86:BF:9B:8E:24:72:CA:3E:D8:75:AC`

Once you complete these steps, your Google Sign-In should work perfectly! 🎉
