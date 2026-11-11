package com.movdash.app.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movdash.app.R
import kotlinx.coroutines.delay
import kotlin.random.Random

private enum class AuthStep {
    PROFILE_INPUT,
    OTP_VERIFICATION,
    PASSWORD_CREATE,
    TASTE_PREFERENCES
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthSetupScreen(
    onFinished: (name: String, email: String, isVerified: Boolean, password: String?, genres: Set<String>, audio: String) -> Unit,
    onSkip: () -> Unit
) {
    var step by remember { mutableStateOf(AuthStep.PROFILE_INPUT) }

    // Step 1 State: Profile & Email
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    // Step 2 State: OTP
    var otpInput by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf("742916") }
    var otpError by remember { mutableStateOf<String?>(null) }
    var resendSeconds by remember { mutableIntStateOf(60) }
    var showOtpBanner by remember { mutableStateOf(false) }

    // Step 3 State: Password
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Step 4 State: Viewing Taste
    val availableGenres = listOf(
        "Action" to "💥",
        "Sci-Fi" to "🚀",
        "Bollywood" to "🇮🇳",
        "South Cinema" to "⚔️",
        "Crime Thriller" to "🕵️",
        "Comedy" to "😂",
        "Romance" to "❤️",
        "Anime" to "⛩️",
        "Horror" to "👻"
    )
    var selectedGenres by remember {
        mutableStateOf(setOf("Action", "Sci-Fi", "Bollywood"))
    }

    val availableAudio = listOf(
        "Hindi Dubbed (Dual Audio)",
        "Original English",
        "Telugu Dubbed",
        "Tamil Dubbed",
        "Multi-Audio & Subs"
    )
    var selectedAudio by remember { mutableStateOf("Hindi Dubbed (Dual Audio)") }

    // Resend countdown timer
    LaunchedEffect(step, resendSeconds) {
        if (step == AuthStep.OTP_VERIFICATION && resendSeconds > 0) {
            delay(1000)
            resendSeconds--
        }
    }

    // Edge-to-edge Cosmic Starfield
    val stars = remember {
        val rand = java.util.Random(42)
        List(80) {
            Triple(rand.nextFloat(), rand.nextFloat(), 0.7f + rand.nextFloat() * 1.5f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030509))
    ) {
        // Starfield Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x301E3A8A), Color(0x187C3AED), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.2f),
                    radius = w * 0.8f
                ),
                radius = w * 0.8f,
                center = Offset(w * 0.5f, h * 0.2f)
            )
            for (s in stars) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = s.third,
                    center = Offset(s.first * w, s.second * h)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top App Bar Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step != AuthStep.PROFILE_INPUT) {
                    IconButton(onClick = {
                        step = when (step) {
                            AuthStep.OTP_VERIFICATION -> AuthStep.PROFILE_INPUT
                            AuthStep.PASSWORD_CREATE -> AuthStep.OTP_VERIFICATION
                            AuthStep.TASTE_PREFERENCES -> AuthStep.PASSWORD_CREATE
                            else -> AuthStep.PROFILE_INPUT
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                } else {
                    Spacer(Modifier.size(40.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo_transparent),
                        contentDescription = "Orbit",
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "ORBIT",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                }

                TextButton(onClick = onSkip) {
                    Text("Skip", color = Color(0xFF94A3B8), fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Step Progress Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuthStep.values().forEachIndexed { index, s ->
                    val isActive = s == step
                    val isPassed = s.ordinal < step.ordinal
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(if (isActive) 36.dp else 18.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isActive -> Color.White
                                    isPassed -> Color(0xFF10B981)
                                    else -> Color(0xFF334155)
                                }
                            )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // STEP 1: PROFILE & GMAIL
            if (step == AuthStep.PROFILE_INPUT) {
                Text(
                    "Create Your Orbit ID",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Enter your name and official Gmail address to create your free, secure streaming profile.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(28.dp))

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    placeholder = { Text("e.g. Anshdeep") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFCBD5E1)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color(0xFF94A3B8),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Gmail Field (Strict @gmail.com)
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it.trim()
                        emailError = null
                    },
                    label = { Text("Gmail Address") },
                    placeholder = { Text("yourname@gmail.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFCBD5E1)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                    isError = emailError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFF334155),
                        errorBorderColor = Color(0xFFEF4444),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color(0xFF94A3B8),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (emailError != null) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(emailError!!, color = Color(0xFFEF4444), fontSize = 12.sp)
                    }
                } else {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "🔒 Only official @gmail.com addresses are supported for security.",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(30.dp))

                Button(
                    onClick = {
                        val cleanEmail = email.trim().lowercase()
                        if (name.isBlank()) {
                            emailError = "Please enter your name."
                            return@Button
                        }
                        if (!cleanEmail.endsWith("@gmail.com") || cleanEmail.length <= 10) {
                            emailError = "Only official @gmail.com addresses are accepted (e.g. name@gmail.com)."
                            return@Button
                        }
                        // Generate random 6-digit OTP
                        val code = (100000 + Random.nextInt(900000)).toString()
                        generatedOtp = code
                        showOtpBanner = true
                        resendSeconds = 45
                        otpInput = ""
                        otpError = null
                        step = AuthStep.OTP_VERIFICATION
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Send Verification Code", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            // STEP 2: OTP VERIFICATION
            else if (step == AuthStep.OTP_VERIFICATION) {
                Text(
                    "Verify Your Gmail",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Enter the 6-digit verification code sent to:",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    email,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(18.dp))

                // In-App Simulated Security Toast Card
                if (showOtpBanner) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF132238)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("ORBIT SECURITY CODE", color = Color(0xFF60A5FA), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text("Your 6-digit OTP is: $generatedOtp", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            TextButton(onClick = { otpInput = generatedOtp }) {
                                Text("Auto-fill", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // 6-digit OTP Input Field
                OutlinedTextField(
                    value = otpInput,
                    onValueChange = {
                        if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                            otpInput = it
                            otpError = null
                        }
                    },
                    label = { Text("6-Digit Code") },
                    placeholder = { Text("123456") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFCBD5E1)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    isError = otpError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFF334155),
                        errorBorderColor = Color(0xFFEF4444),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color(0xFF94A3B8),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (otpError != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(otpError!!, color = Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (resendSeconds > 0) {
                        Text("Resend code in ${resendSeconds}s", color = Color(0xFF64748B), fontSize = 12.sp)
                    } else {
                        TextButton(
                            onClick = {
                                val code = (100000 + Random.nextInt(900000)).toString()
                                generatedOtp = code
                                showOtpBanner = true
                                resendSeconds = 45
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Resend OTP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    TextButton(
                        onClick = { step = AuthStep.PROFILE_INPUT },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Edit Email", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = {
                        if (otpInput.length != 6) {
                            otpError = "Please enter the complete 6-digit code."
                            return@Button
                        }
                        if (otpInput != generatedOtp) {
                            otpError = "Incorrect code. Please verify and re-enter."
                            return@Button
                        }
                        step = AuthStep.PASSWORD_CREATE
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Verify & Continue", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            // STEP 3: CREATE PASSWORD
            else if (step == AuthStep.PASSWORD_CREATE) {
                Text(
                    "Secure Your Account",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Create a password to lock your offline vault, watchlist, and custom playback settings.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(24.dp))

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    label = { Text("Password") },
                    placeholder = { Text("Min 6 characters") },
                    leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFFCBD5E1)) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFF334155),
                        errorBorderColor = Color(0xFFEF4444),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color(0xFF94A3B8),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Confirm Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        passwordError = null
                    },
                    label = { Text("Confirm Password") },
                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFCBD5E1)) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    isError = passwordError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFF334155),
                        errorBorderColor = Color(0xFFEF4444),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color(0xFF94A3B8),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (passwordError != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(passwordError!!, color = Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
                }

                Spacer(Modifier.height(30.dp))

                Button(
                    onClick = {
                        if (password.length < 6) {
                            passwordError = "Password must be at least 6 characters."
                            return@Button
                        }
                        if (password != confirmPassword) {
                            passwordError = "Passwords do not match."
                            return@Button
                        }
                        step = AuthStep.TASTE_PREFERENCES
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save & Personalize Feed", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            // STEP 4: VIEWING TASTE & PREFERENCES
            else if (step == AuthStep.TASTE_PREFERENCES) {
                Text(
                    "What Do You Love Watching?",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Select your favorite genres so Orbit can personalize recommendations for you.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    "FAVORITE GENRES",
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                // Genres Flow
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableGenres.forEach { (genre, emoji) ->
                        val isSelected = selectedGenres.contains(genre)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedGenres = if (isSelected) {
                                    if (selectedGenres.size > 1) selectedGenres - genre else selectedGenres
                                } else {
                                    selectedGenres + genre
                                }
                            },
                            label = { Text("$emoji $genre", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White,
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF131A26),
                                labelColor = Color(0xFFE2E8F0)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color.White else Color(0xFF263347)
                            )
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "PREFERRED AUDIO LANGUAGE",
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    availableAudio.forEach { audio ->
                        val isSelected = selectedAudio == audio
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF1E2D44) else Color(0xFF0F1724),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF60A5FA) else Color(0xFF1E2837)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAudio = audio }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(audio, color = if (isSelected) Color.White else Color(0xFF94A3B8), fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(30.dp))

                Button(
                    onClick = {
                        val finalName = name.ifBlank { "Orbit Member" }
                        onFinished(finalName, email, true, password, selectedGenres, selectedAudio)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Enter Orbit Universe", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}
