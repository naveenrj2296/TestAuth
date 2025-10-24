package com.example.firebaseauthtest

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class UnifiedAuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val authMethod: AuthMethod = AuthMethod.NONE,
    val user: com.google.firebase.auth.FirebaseUser? = null,
    val errorMessage: String = "",
    val phoneNumber: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val firstName: String = "",
    val lastName: String = ""
)

enum class AuthMethod {
    NONE,
    PHONE,
    GOOGLE,
    TRUECALLER
}

class UnifiedAuthViewModel : ViewModel() {
    private val phoneAuthViewModel = PhoneAuthViewModel()
    private val googleAuthService = GoogleAuthService(android.app.Application())
    private val truecallerAuthService = TruecallerAuthService(android.app.Application())
    
    private val _state = MutableStateFlow(UnifiedAuthState())
    val state: StateFlow<UnifiedAuthState> = _state.asStateFlow()
    
    init {
        // Initialize authentication services
        googleAuthService.initializeGoogleSignIn()
        truecallerAuthService.initializeTruecallerSDK()
        
        // Combine all authentication states
        viewModelScope.launch {
            combine(
                phoneAuthViewModel.state,
                googleAuthService.state,
                truecallerAuthService.state
            ) { phoneState, googleState, truecallerState ->
                updateUnifiedState(phoneState, googleState, truecallerState)
            }.collect { }
        }
    }
    
    private fun updateUnifiedState(
        phoneState: PhoneAuthState,
        googleState: GoogleAuthState,
        truecallerState: TruecallerAuthState
    ) {
        val isLoading = phoneState.isLoading || googleState.isLoading || truecallerState.isLoading
        val isAuthenticated = phoneState.isVerified || googleState.isAuthenticated || truecallerState.isAuthenticated
        
        val authMethod = when {
            phoneState.isVerified -> AuthMethod.PHONE
            googleState.isAuthenticated -> AuthMethod.GOOGLE
            truecallerState.isAuthenticated -> AuthMethod.TRUECALLER
            else -> AuthMethod.NONE
        }
        
        val errorMessage = when {
            phoneState.errorMessage.isNotEmpty() -> phoneState.errorMessage
            googleState.errorMessage.isNotEmpty() -> googleState.errorMessage
            truecallerState.errorMessage.isNotEmpty() -> truecallerState.errorMessage
            else -> ""
        }
        
        _state.value = _state.value.copy(
            isLoading = isLoading,
            isAuthenticated = isAuthenticated,
            authMethod = authMethod,
            user = googleState.user,
            errorMessage = errorMessage,
            phoneNumber = phoneState.phoneNumber.ifEmpty { truecallerState.phoneNumber },
            displayName = googleState.displayName,
            email = googleState.email,
            photoUrl = googleState.photoUrl,
            firstName = truecallerState.firstName,
            lastName = truecallerState.lastName
        )
    }
    
    // Phone Authentication Methods
    fun updatePhoneNumber(phoneNumber: String) {
        phoneAuthViewModel.updatePhoneNumber(phoneNumber)
    }
    
    fun updateOtpCode(otpCode: String) {
        phoneAuthViewModel.updateOtpCode(otpCode)
    }
    
    fun sendOtp(activity: Activity) {
        phoneAuthViewModel.sendOtp(activity)
    }
    
    fun verifyOtp() {
        phoneAuthViewModel.verifyOtp()
    }
    
    // Google Authentication Methods
    fun setGoogleActivityResultLauncher(launcher: androidx.activity.result.ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>) {
        googleAuthService.setActivityResultLauncher(launcher)
    }
    
    fun signInWithGoogle(activity: Activity) {
        googleAuthService.signInWithGoogle(activity)
    }
    
    fun handleGoogleSignInResult(data: Intent?) {
        googleAuthService.handleGoogleSignInResult(data)
    }
    
    // Truecaller Authentication Methods
    fun setTruecallerActivityResultLauncher(launcher: androidx.activity.result.ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>) {
        truecallerAuthService.setActivityResultLauncher(launcher)
    }
    
    fun signInWithTruecaller(activity: Activity) {
        truecallerAuthService.authenticateWithTruecaller(activity)
    }
    
    // General Methods
    fun signOut() {
        phoneAuthViewModel.clearError()
        googleAuthService.signOut()
        truecallerAuthService.signOut()
        _state.value = UnifiedAuthState()
    }
    
    fun clearError() {
        phoneAuthViewModel.clearError()
        googleAuthService.clearError()
        truecallerAuthService.clearError()
    }
}
