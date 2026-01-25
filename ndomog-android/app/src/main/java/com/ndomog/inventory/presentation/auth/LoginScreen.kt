package com.ndomog.inventory.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndomog.inventory.presentation.theme.NdomogTheme
import com.ndomog.inventory.presentation.theme.NdomogColors

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val loginState by authViewModel.loginState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLogin by remember { mutableStateOf(true) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onLoginSuccess()
        }
    }

    NdomogTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(NdomogColors.DarkBackground),
            color = NdomogColors.DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                
                // Logo - Gear icon matching the favicon design
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = NdomogColors.Primary
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Ndomog Logo",
                            tint = NdomogColors.TextOnPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    "Ndomog Investment",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 28.sp,
                        color = NdomogColors.TextLight,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Sign in to access your inventory",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = NdomogColors.TextMuted
                    )
                )
                
                Spacer(modifier = Modifier.height(40.dp))

                // Email Label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        "Email",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = NdomogColors.TextLight,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Enter your email", color = NdomogColors.TextMuted.copy(alpha = 0.5f)) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = "Email",
                            tint = NdomogColors.TextMuted
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = NdomogColors.InputBorder,
                        focusedBorderColor = NdomogColors.Primary,
                        unfocusedContainerColor = NdomogColors.InputBackground.copy(alpha = 0.5f),
                        focusedContainerColor = NdomogColors.InputBackground.copy(alpha = 0.5f),
                        unfocusedTextColor = NdomogColors.TextLight,
                        focusedTextColor = NdomogColors.TextLight,
                        cursorColor = NdomogColors.Primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                // Password Label with Forgot Password
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Password",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = NdomogColors.TextLight,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    if (isLogin) {
                        Text(
                            "Forgot password?",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = NdomogColors.Primary,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.clickable {
                                // TODO: Implement forgot password
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Enter your password", color = NdomogColors.TextMuted.copy(alpha = 0.5f)) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = "Password",
                            tint = NdomogColors.TextMuted
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { showPassword = !showPassword }
                        ) {
                            Icon(
                                if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle password",
                                tint = NdomogColors.TextMuted
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = NdomogColors.InputBorder,
                        focusedBorderColor = NdomogColors.Primary,
                        unfocusedContainerColor = NdomogColors.InputBackground.copy(alpha = 0.5f),
                        focusedContainerColor = NdomogColors.InputBackground.copy(alpha = 0.5f),
                        unfocusedTextColor = NdomogColors.TextLight,
                        focusedTextColor = NdomogColors.TextLight,
                        cursorColor = NdomogColors.Primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(28.dp))

                // Login/Signup Button
                if (loginState is LoginState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = NdomogColors.Primary
                    )
                } else {
                    Button(
                        onClick = {
                            if (isLogin) {
                                authViewModel.login(email, password)
                            } else {
                                authViewModel.signup(email, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NdomogColors.Primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            if (isLogin) "Sign In" else "Sign Up",
                            color = NdomogColors.TextOnPrimary,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 16.sp
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Sign Up/In Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isLogin) "Don't have an account? " else "Already have an account? ",
                        color = NdomogColors.TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        if (isLogin) "Sign up" else "Sign in",
                        color = NdomogColors.Primary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { isLogin = !isLogin }
                    )
                }

                // Error Message
                if (loginState is LoginState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = NdomogColors.ErrorBackground
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = (loginState as LoginState.Error).message,
                            color = NdomogColors.ErrorText,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
