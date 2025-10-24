package com.example.firebaseauthtest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firebaseauthtest.ui.theme.FirebaseAuthTestTheme

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInLauncher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>
    private lateinit var truecallerSignInLauncher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>
    private var unifiedAuthViewModel: UnifiedAuthViewModel? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize activity result launchers
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            Log.d("MainActivity", "🔵 Google Sign-In result received: ${result.resultCode}")
            Log.d("MainActivity", "🔵 Result data: ${result.data}")
            Log.d("MainActivity", "🔵 Result extras: ${result.data?.extras}")
            
            // Handle the result directly
            if (result.resultCode == RESULT_OK) {
                Log.d("MainActivity", "✅ Google Sign-In successful - result code: ${result.resultCode}")
                Log.d("MainActivity", "🔵 Processing Google Sign-In result...")
                // Pass the result to the ViewModel
                unifiedAuthViewModel?.handleGoogleSignInResult(result)
            } else {
                Log.d("MainActivity", "❌ Google Sign-In failed or cancelled - result code: ${result.resultCode}")
            }
        }
        
        truecallerSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                // Handle Truecaller Sign-In result
                // This will be handled by the ViewModel
            }
        }
        
        setContent {
            FirebaseAuthTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UnifiedAuthScreen(
                        modifier = Modifier.padding(innerPadding),
                        activity = this@MainActivity,
                        googleSignInLauncher = googleSignInLauncher,
                        truecallerSignInLauncher = truecallerSignInLauncher,
                        onViewModelCreated = { viewModel -> unifiedAuthViewModel = viewModel }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedAuthScreen(
    modifier: Modifier = Modifier,
    viewModel: UnifiedAuthViewModel = viewModel(),
    activity: ComponentActivity,
    googleSignInLauncher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>,
    truecallerSignInLauncher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>,
    onViewModelCreated: (UnifiedAuthViewModel) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    
    // Set up activity result launchers
    LaunchedEffect(Unit) {
        Log.d("UnifiedAuthScreen", "🚀 Initializing authentication services...")
        viewModel.initializeServices(activity)
        viewModel.setGoogleActivityResultLauncher(googleSignInLauncher)
        viewModel.setTruecallerActivityResultLauncher(truecallerSignInLauncher)
        onViewModelCreated(viewModel)
        Log.d("UnifiedAuthScreen", "✅ Authentication services initialized")
    }
    
    // Handle Google Sign-In result
    LaunchedEffect(googleSignInLauncher) {
        Log.d("UnifiedAuthScreen", "🔵 Google Sign-In launcher ready")
    }
    
    // Monitor for Google Sign-In completion
    LaunchedEffect(googleSignInLauncher) {
        Log.d("UnifiedAuthScreen", "🔵 Monitoring Google Sign-In results")
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Multi-Auth Test App",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        if (state.isAuthenticated) {
            // Success screen
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✅ Authentication Successful!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Method: ${state.authMethod.name}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    if (state.phoneNumber.isNotEmpty()) {
                        Text(
                            text = "Phone: ${state.phoneNumber}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (state.email.isNotEmpty()) {
                        Text(
                            text = "Email: ${state.email}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (state.displayName.isNotEmpty()) {
                        Text(
                            text = "Name: ${state.displayName}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { viewModel.signOut() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign Out")
                    }
                }
            }
        } else {
            // Authentication options screen
            Text(
                text = "Choose your authentication method",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Google Sign-In Button
            Button(
                onClick = { 
                    Log.d("UnifiedAuthScreen", "🔵 Google Sign-In button clicked")
                    viewModel.signInWithGoogle(activity) 
                },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4285F4)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Google Sign-In",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign in with Google")
            }
            
            // Truecaller Sign-In Button
            Button(
                onClick = { viewModel.signInWithTruecaller(activity) },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00A86B)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Truecaller Sign-In",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign in with Truecaller")
            }
            
            // Phone Authentication Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Or use Phone Authentication",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    OutlinedTextField(
                        value = state.phoneNumber,
                        onValueChange = viewModel::updatePhoneNumber,
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { viewModel.sendOtp(activity) },
                        enabled = !state.isLoading && state.phoneNumber.length == 10,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Send OTP")
                        }
                    }
                }
            }
        }
        
        // Error message
        if (state.errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}