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
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id)) // You'll need to add this to strings.xml
                .requestEmail()
                .build()
            
            googleSignInClient = GoogleSignIn.getClient(context, gso)
            
            // Initialize One Tap client
            oneTapClient = Identity.getSignInClient(context)
            
            Log.d("GoogleAuth", "Google Sign-In initialized successfully")
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
    
    fun signInWithGoogle(activity: Activity) {
        _state.value = _state.value.copy(isLoading = true, errorMessage = "")
        
        try {
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(context.getString(R.string.default_web_client_id))
                        .build()
                )
                .build()
            
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener { result ->
                    try {
                        val intentSender = result.pendingIntent.intentSender
                        val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                        activityResultLauncher?.launch(intentSenderRequest)
                    } catch (e: Exception) {
                        Log.e("GoogleAuth", "Error launching Google Sign-In", e)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = "Error launching Google Sign-In: ${e.message}"
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("GoogleAuth", "Google Sign-In failed", exception)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Google Sign-In failed: ${exception.message}"
                    )
                }
        } catch (e: Exception) {
            Log.e("GoogleAuth", "Exception during Google Sign-In", e)
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = "Sign-In failed: ${e.message}"
            )
        }
    }
    
    fun handleGoogleSignInResult(data: Intent?) {
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            
            if (idToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                signInWithFirebaseCredential(firebaseCredential)
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "No ID token received from Google"
                )
            }
        } catch (e: ApiException) {
            Log.e("GoogleAuth", "Google Sign-In API exception", e)
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = "Google Sign-In failed: ${e.message}"
            )
        }
    }
    
    private fun signInWithFirebaseCredential(credential: com.google.firebase.auth.AuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("GoogleAuth", "Firebase authentication successful")
                    
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = user,
                        displayName = user?.displayName ?: "",
                        email = user?.email ?: "",
                        photoUrl = user?.photoUrl?.toString() ?: "",
                        errorMessage = ""
                    )
                } else {
                    Log.e("GoogleAuth", "Firebase authentication failed", task.exception)
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
