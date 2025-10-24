# Multi-Authentication Setup Guide

This guide will help you set up Truecaller and Google authentication SDKs in your Android project.

## ✅ Completed Setup

### 1. Dependencies Added
- **Truecaller SDK**: `com.truecaller.android.sdk:truecaller-sdk:2.8.0`
- **Google Sign-In**: `com.google.android.gms:play-services-auth:20.7.0`
- **Firebase Auth**: Already configured with phone authentication

### 2. Permissions Added
- `READ_PHONE_STATE` - For Truecaller SDK
- `READ_PHONE_NUMBERS` - For Truecaller SDK
- `INTERNET` - For Firebase and authentication services

### 3. Package Visibility
- Added queries for Google Play Services and Truecaller
- Added HTTPS intent filter for web authentication

## 🔧 Required Configuration

### 1. Google Sign-In Setup

#### Step 1: Get Google Web Client ID
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Google Sign-In API
4. Go to "Credentials" → "Create Credentials" → "OAuth 2.0 Client IDs"
5. Create a "Web application" client ID
6. Copy the client ID

#### Step 2: Update strings.xml
Replace `YOUR_GOOGLE_WEB_CLIENT_ID_HERE` in `app/src/main/res/values/strings.xml` with your actual Google Web Client ID:

```xml
<string name="default_web_client_id">your-actual-google-web-client-id</string>
```

#### Step 3: Add SHA-1 Fingerprint
1. Get your app's SHA-1 fingerprint:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
2. Add this SHA-1 to your Google Cloud Console project
3. For release builds, also add your release keystore SHA-1

### 2. Truecaller SDK Setup

#### Step 1: Register with Truecaller
1. Go to [Truecaller Developer Portal](https://developer.truecaller.com/)
2. Register your app
3. Get your app's package name and SHA-1 fingerprint
4. Submit for approval

#### Step 2: Configure ProGuard (if using)
Add to your `proguard-rules.pro`:
```
-keep class com.truecaller.** { *; }
-dontwarn com.truecaller.**
```

### 3. Firebase Configuration

Your Firebase is already configured with:
- Phone authentication
- Google Services plugin
- `google-services.json` file

## 🚀 How to Use

### Authentication Methods Available:

1. **Google Sign-In**
   - One-tap authentication
   - Gets user's email, name, and profile picture
   - Integrates with Firebase Auth

2. **Truecaller Authentication**
   - Uses Truecaller app for verification
   - Gets verified phone number and user details
   - No OTP required

3. **Phone Authentication (Existing)**
   - SMS-based OTP verification
   - Works without third-party apps

### UI Features:
- **Unified Interface**: All authentication methods in one screen
- **Method Selection**: Choose between Google, Truecaller, or Phone
- **Success Display**: Shows authenticated user details
- **Error Handling**: Clear error messages for each method
- **Sign Out**: Unified sign-out functionality

## 🔍 Testing

### Test Google Sign-In:
1. Ensure Google Play Services is installed
2. Have a Google account signed in
3. Tap "Sign in with Google"
4. Complete the authentication flow

### Test Truecaller:
1. Install Truecaller app on device
2. Ensure Truecaller is set up with your phone number
3. Tap "Sign in with Truecaller"
4. Complete the Truecaller authentication

### Test Phone Authentication:
1. Enter a valid phone number
2. Tap "Send OTP"
3. Enter the received OTP
4. Complete verification

## 🐛 Troubleshooting

### Common Issues:

1. **Google Sign-In not working**:
   - Check SHA-1 fingerprint is added to Google Console
   - Verify Web Client ID is correct
   - Ensure Google Play Services is updated

2. **Truecaller not working**:
   - Ensure Truecaller app is installed
   - Check if Truecaller is properly set up
   - Verify app is registered with Truecaller Developer Portal

3. **Build errors**:
   - Clean and rebuild project
   - Check all dependencies are properly added
   - Verify AndroidManifest.xml permissions

## 📱 App Flow

1. **Launch App** → Shows authentication options
2. **Choose Method** → Google, Truecaller, or Phone
3. **Authenticate** → Complete the chosen authentication flow
4. **Success Screen** → Shows user details and sign-out option
5. **Sign Out** → Returns to authentication options

## 🔐 Security Notes

- All authentication methods integrate with Firebase Auth
- User data is handled securely through Firebase
- Truecaller provides verified phone numbers
- Google Sign-In provides verified email addresses
- Phone authentication uses SMS verification

## 📋 Next Steps

1. **Configure Google Console** with your app details
2. **Register with Truecaller Developer Portal**
3. **Test all authentication methods**
4. **Customize UI** as needed for your app
5. **Add additional user data handling** if required

Your multi-authentication system is now ready to use! 🎉
