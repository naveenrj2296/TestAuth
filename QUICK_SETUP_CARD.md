# 🚀 Quick Setup Card - Google Sign-In

## Your SHA-1 Fingerprints (Copy These)

### Debug (for testing):
```
89:89:F6:FF:1D:9B:5C:3A:6A:B8:A9:32:AE:50:10:57:6C:99:A2:14
```

### Release (for production):
```
FC:14:88:25:0B:1E:21:DD:F9:86:BF:9B:8E:24:72:CA:3E:D8:75:AC
```

## 📋 Quick Steps (Do These Now)

### 1. Google Cloud Console (5 minutes)
1. Go to: https://console.cloud.google.com/
2. Create/select project
3. Go to "APIs & Services" → "Credentials"
4. Click "Create Credentials" → "OAuth client ID"
5. Choose "Web application"
6. Name: "Android App Web Client"
7. **Copy the Client ID** (save it!)

### 2. Update Your App (1 minute)
1. Open `app/src/main/res/values/strings.xml`
2. Replace `YOUR_GOOGLE_WEB_CLIENT_ID_HERE` with your copied Client ID
3. Save the file

### 3. Firebase Console (3 minutes)
1. Go to: https://console.firebase.google.com/
2. Select your project
3. Click gear icon → "Project settings"
4. Scroll to "Your apps" → Click your Android app
5. In "SHA certificate fingerprints":
   - Click "Add fingerprint"
   - Paste: `89:89:F6:FF:1D:9B:5C:3A:6A:B8:A9:32:AE:50:10:57:6C:99:A2:14`
   - Click "Add fingerprint" again
   - Paste: `FC:14:88:25:0B:1E:21:DD:F9:86:BF:9B:8E:24:72:CA:3E:D8:75:AC`
6. Save

### 4. Test (2 minutes)
1. Build and run your app
2. Tap "Sign in with Google"
3. Complete authentication
4. ✅ Success!

## 🆘 If Something Goes Wrong

**Error: "Sign in failed"**
- Check SHA-1 fingerprints are added to Firebase
- Verify Client ID in strings.xml

**Error: "Invalid client"**
- Make sure you're using Web application Client ID
- Not Android application Client ID

**Error: "App not authorized"**
- Add package name to Google Console
- Configure OAuth consent screen

## 📱 Your App Info
- **Package**: `com.example.firebaseauthtest`
- **Debug SHA-1**: `89:89:F6:FF:1D:9B:5C:3A:6A:B8:A9:32:AE:50:10:57:6C:99:A2:14`
- **Release SHA-1**: `FC:14:88:25:0B:1E:21:DD:F9:86:BF:9B:8E:24:72:CA:3E:D8:75:AC`

**Total time needed: ~10 minutes** ⏱️
