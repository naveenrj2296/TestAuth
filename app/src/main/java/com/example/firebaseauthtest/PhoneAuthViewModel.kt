package com.example.firebaseauthtest

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.FirebaseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

data class PhoneAuthState(
    val phoneNumber: String = "",
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val isOtpSent: Boolean = false,
    val isVerified: Boolean = false,
    val errorMessage: String = "",
    val verificationId: String = ""
)

class PhoneAuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    
    private val _state = MutableStateFlow(PhoneAuthState())
    val state: StateFlow<PhoneAuthState> = _state.asStateFlow()
    
    private var currentVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    
    fun updatePhoneNumber(phoneNumber: String) {
        _state.value = _state.value.copy(phoneNumber = phoneNumber)
    }
    
    fun updateOtpCode(otpCode: String) {
        _state.value = _state.value.copy(otpCode = otpCode)
    }
    
    fun sendOtp(activity: Activity) {
        val phoneNumber = _state.value.phoneNumber
        if (phoneNumber.length != 10) {
            _state.value = _state.value.copy(errorMessage = "Enter 10-digit mobile number")
            return
        }
        
        _state.value = _state.value.copy(isLoading = true, errorMessage = "")
        
        val formattedPhone = if (phoneNumber.startsWith("+")) {
            phoneNumber
        } else {
            "+91$phoneNumber" // Assuming India (+91)
        }
        
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                println("DEBUG: Auto-verification completed")
                signInWithCredential(credential)
            }
            
            override fun onVerificationFailed(exception: FirebaseException) {
                println("DEBUG: Phone verification failed: ${exception.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to send OTP: ${exception.message}"
                )
            }
            
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                currentVerificationId = verificationId
                resendToken = token
                println("DEBUG: OTP sent successfully")
                _state.value = _state.value.copy(
                    isLoading = false,
                    isOtpSent = true,
                    verificationId = verificationId
                )
            }
        }
        
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    
    fun verifyOtp() {
        val otpCode = _state.value.otpCode
        val verificationId = currentVerificationId
        
        if (otpCode.length != 6) {
            _state.value = _state.value.copy(errorMessage = "Enter 6-digit OTP")
            return
        }
        
        if (verificationId == null) {
            _state.value = _state.value.copy(errorMessage = "No verification ID found")
            return
        }
        
        _state.value = _state.value.copy(isLoading = true, errorMessage = "")
        
        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
        signInWithCredential(credential)
    }
    
    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                val result = auth.signInWithCredential(credential).await()
                if (result.user != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isVerified = true,
                        errorMessage = ""
                    )
                    println("DEBUG: Phone authentication successful")
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Authentication failed"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Verification failed: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = "")
    }
}
