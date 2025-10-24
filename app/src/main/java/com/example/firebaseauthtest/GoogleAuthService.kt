package com.example.firebaseauthtest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class GoogleAuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: FirebaseUser? = null,
    val errorMessage: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = ""
)

class GoogleAuthService(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var oneTapClient: SignInClient
    
    private val _state = MutableStateFlow(GoogleAuthState())
    val state: StateFlow<GoogleAuthState> = _state.asStateFlow()
    
    private var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    
    fun initializeGoogleSignIn() {
        try {
            // Configure Google Sign-In
            val webClientId = context.getString(R.string.default_web_client_id)
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
            
            googleSignInClient = GoogleSignIn.getClient(context, gso)
            
            // Initialize One Tap client
            oneTapClient = Identity.getSignInClient(context)
            
            Log.d("GoogleAuth", "Google Sign-In initialized successfully with client ID: $webClientId")
        } catch (e: Exception) {
            Log.e("GoogleAuth", "Failed to initialize Google Sign-In", e)
            _state.value = _state.value.copy(
                errorMessage = "Failed to initialize Google Sign-In: ${e.message}"
            )
        }
    }
    
    fun setActivityResultLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        this.activityResultLauncher = launcher
    }
    
    fun handleActivityResult(result: androidx.activity.result.ActivityResult) {
        Log.d("GoogleAuth", "Handling activity result: ${result.resultCode}")
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            handleGoogleSignInResult(result.data)
        } else {
            Log.d("GoogleAuth", "Google Sign-In cancelled or failed")
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = "Google Sign-In cancelled or failed"
            )
        }
    }
    
    fun signInWithGoogle(activity: Activity) {
        Log.d("GoogleAuth", "🔵 Starting Google Sign-In process...")
        _state.value = _state.value.copy(isLoading = true, errorMessage = "")
        
        try {
            val clientId = context.getString(R.string.default_web_client_id)
            Log.d("GoogleAuth", "🔵 Using client ID: $clientId")
            
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(clientId)
                        .build()
                )
                .build()
            
            Log.d("GoogleAuth", "🔵 Launching Google Sign-In request...")
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener { result ->
                    Log.d("GoogleAuth", "✅ Google Sign-In request successful")
                    try {
                        val intentSender = result.pendingIntent.intentSender
                        val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                        Log.d("GoogleAuth", "🔵 Launching intent sender...")
                        activityResultLauncher?.launch(intentSenderRequest)
                    } catch (e: Exception) {
                        Log.e("GoogleAuth", "❌ Error launching Google Sign-In", e)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = "Error launching Google Sign-In: ${e.message}"
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("GoogleAuth", "❌ Google Sign-In failed", exception)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Google Sign-In failed: ${exception.message}"
                    )
                }
        } catch (e: Exception) {
            Log.e("GoogleAuth", "❌ Exception during Google Sign-In", e)
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = "Sign-In failed: ${e.message}"
            )
        }
    }
    
    // This method will be called when the Google Sign-In result is received
    fun handleSignInResult(result: androidx.activity.result.ActivityResult) {
        Log.d("GoogleAuth", "Handling sign-in result: ${result.resultCode}")
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            handleGoogleSignInResult(result.data)
        } else {
            Log.d("GoogleAuth", "Google Sign-In cancelled or failed")
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = "Google Sign-In cancelled or failed"
            )
        }
    }
    
    fun handleGoogleSignInResult(data: Intent?) {
        try {
            Log.d("GoogleAuth", "🔵 Handling Google Sign-In result: $data")
            if (data != null) {
                Log.d("GoogleAuth", "🔵 Extracting credential from intent...")
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                
                Log.d("GoogleAuth", "🔵 ID Token received: ${idToken != null}")
                Log.d("GoogleAuth", "🔵 ID Token length: ${idToken?.length ?: 0}")
                
                if (idToken != null) {
                    Log.d("GoogleAuth", "🔵 Creating Firebase credential...")
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    Log.d("GoogleAuth", "🔵 Signing in with Firebase...")
                    signInWithFirebaseCredential(firebaseCredential)
                } else {
                    Log.e("GoogleAuth", "❌ No ID token received from Google")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "No ID token received from Google"
                    )
                }
            } else {
                Log.e("GoogleAuth", "❌ No data received from Google Sign-In")
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "No data received from Google Sign-In"
                )
            }
        } catch (e: ApiException) {
            Log.e("GoogleAuth", "❌ Google Sign-In API exception", e)
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = "Google Sign-In failed: ${e.message}"
            )
        } catch (e: Exception) {
            Log.e("GoogleAuth", "❌ General exception during Google Sign-In", e)
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = "Google Sign-In failed: ${e.message}"
            )
        }
    }
    
    private fun signInWithFirebaseCredential(credential: com.google.firebase.auth.AuthCredential) {
        Log.d("GoogleAuth", "🔵 Starting Firebase authentication...")
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("GoogleAuth", "✅ Firebase authentication successful!")
                    Log.d("GoogleAuth", "🔵 User ID: ${user?.uid}")
                    Log.d("GoogleAuth", "🔵 User email: ${user?.email}")
                    Log.d("GoogleAuth", "🔵 User name: ${user?.displayName}")
                    
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = user,
                        displayName = user?.displayName ?: "",
                        email = user?.email ?: "",
                        photoUrl = user?.photoUrl?.toString() ?: "",
                        errorMessage = ""
                    )
                    Log.d("GoogleAuth", "✅ State updated with user information")
                } else {
                    Log.e("GoogleAuth", "❌ Firebase authentication failed", task.exception)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Firebase authentication failed: ${task.exception?.message}"
                    )
                }
            }
    }
    
    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
        _state.value = GoogleAuthState()
        Log.d("GoogleAuth", "User signed out")
    }
    
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = "")
    }
}
