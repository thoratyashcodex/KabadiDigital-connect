package com.example.kabadidigital

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kabadidigital.ui.theme.KabadiDigitalTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

class RecyclerLoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ==========================================
        // FIREBASE INITIALIZATION
        // ==========================================

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // ==========================================
        // CHECK ALREADY LOGGED-IN USER
        // ==========================================

        val currentUser = auth.currentUser

        if (currentUser != null) {

            checkRecyclerAccount(currentUser.uid)

        } else {

            showLoginScreen()
        }
    }

    // ==========================================================
    // SHOW LOGIN SCREEN
    // ==========================================================

    private fun showLoginScreen() {

        setContent {

            KabadiDigitalTheme {

                RecyclerLoginScreen(

                    onLogin = { email, password ->

                        loginRecycler(
                            email,
                            password
                        )
                    },

                    onRegister = {

                        startActivity(
                            Intent(
                                this,
                                RecyclerRegisterActivity::class.java
                            )
                        )
                    },

                    onBack = {

                        finish()
                    }
                )
            }
        }
    }

    // ==========================================================
    // LOGIN RECYCLER
    // ==========================================================

    private fun loginRecycler(
        email: String,
        password: String
    ) {

        val cleanEmail = email.trim()

        // ======================================================
        // VALIDATION - EMPTY EMAIL
        // ======================================================

        if (cleanEmail.isBlank()) {

            showToast(
                "Please enter your email"
            )

            return
        }

        // ======================================================
        // VALIDATION - EMAIL FORMAT
        // ======================================================

        if (
            !Patterns.EMAIL_ADDRESS
                .matcher(cleanEmail)
                .matches()
        ) {

            showToast(
                "Please enter a valid email address"
            )

            return
        }

        // ======================================================
        // VALIDATION - PASSWORD EMPTY
        // ======================================================

        if (password.isBlank()) {

            showToast(
                "Please enter your password"
            )

            return
        }

        // ======================================================
        // VALIDATION - PASSWORD LENGTH
        // ======================================================

        if (password.length < 6) {

            showToast(
                "Password must be at least 6 characters"
            )

            return
        }

        // ======================================================
        // FIREBASE LOGIN
        // ======================================================

        auth.signInWithEmailAndPassword(
            cleanEmail,
            password
        )
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val user = auth.currentUser

                    if (user == null) {

                        showToast(
                            "Login failed"
                        )

                        return@addOnCompleteListener
                    }

                    // ==========================================
                    // CHECK FIRESTORE RECYCLER PROFILE
                    // ==========================================

                    checkRecyclerAccount(user.uid)

                } else {

                    val errorMessage =
                        task.exception?.message
                            ?: "Login failed"

                    // ==========================================
                    // FRIENDLY FIREBASE ERRORS
                    // ==========================================

                    when {

                        errorMessage.contains(
                            "password",
                            ignoreCase = true
                        ) -> {

                            showToast(
                                "Incorrect password"
                            )
                        }

                        errorMessage.contains(
                            "no user record",
                            ignoreCase = true
                        ) -> {

                            showToast(
                                "Account does not exist. Please register first."
                            )
                        }

                        errorMessage.contains(
                            "user-not-found",
                            ignoreCase = true
                        ) -> {

                            showToast(
                                "Account does not exist. Please register first."
                            )
                        }

                        errorMessage.contains(
                            "invalid credential",
                            ignoreCase = true
                        ) -> {

                            showToast(
                                "Invalid email or password"
                            )
                        }

                        errorMessage.contains(
                            "badly formatted",
                            ignoreCase = true
                        ) -> {

                            showToast(
                                "Invalid email address"
                            )
                        }

                        else -> {

                            showToast(
                                "Login failed: $errorMessage"
                            )
                        }
                    }
                }
            }
    }

    // ==========================================================
    // CHECK RECYCLER FIRESTORE ACCOUNT
    // ==========================================================

    private fun checkRecyclerAccount(
        uid: String
    ) {

        firestore
            .collection("recyclers")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    val role =
                        document.getString("role")

                    // ==========================================
                    // CHECK ROLE
                    // ==========================================

                    if (
                        role.equals(
                            "recycler",
                            ignoreCase = true
                        )
                    ) {

                        // ======================================
                        // VALID RECYCLER
                        // GO TO DASHBOARD
                        // ======================================

                        startActivity(
                            Intent(
                                this,
                                RecycleActivity::class.java
                            )
                        )

                        finish()

                    } else {

                        // ======================================
                        // NOT A RECYCLER
                        // ======================================

                        auth.signOut()

                        showToast(
                            "This account is not a Recycler account"
                        )

                        showLoginScreen()
                    }

                } else {

                    // ==========================================
                    // AUTH ACCOUNT EXISTS BUT PROFILE DOES NOT
                    // ==========================================

                    auth.signOut()

                    showToast(
                        "Recycler profile not found. Please register."
                    )

                    showLoginScreen()
                }
            }

            .addOnFailureListener { error ->

                auth.signOut()

                showToast(
                    "Unable to verify Recycler account: ${error.message}"
                )

                showLoginScreen()
            }
    }

    // ==========================================================
    // TOAST
    // ==========================================================

    private fun showToast(
        message: String
    ) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}


// ==========================================================
// RECYCLER LOGIN SCREEN
// ==========================================================

@Composable
fun RecyclerLoginScreen(

    onLogin: (
        String,
        String
    ) -> Unit,

    onRegister: () -> Unit,

    onBack: () -> Unit
) {

    // ==========================================================
    // STATES
    // ==========================================================

    var email by remember {

        mutableStateOf("")
    }

    var password by remember {

        mutableStateOf("")
    }

    var visible by remember {

        mutableStateOf(false)
    }

    // ==========================================================
    // ANIMATION
    // ==========================================================

    LaunchedEffect(Unit) {

        delay(150)

        visible = true
    }

    // ==========================================================
    // SCREEN
    // ==========================================================

    Box(

        modifier = Modifier
            .fillMaxSize()
            .background(

                Brush.verticalGradient(

                    colors = listOf(
                        Color(0xFFE8F5E9),
                        Color.White
                    )
                )
            )
    ) {

        AnimatedVisibility(

            visible = visible,

            enter =
            fadeIn(
                animationSpec =
                tween(600)
            ) +
                    slideInVertically(

                        initialOffsetY = {
                            120
                        },

                        animationSpec =
                        tween(600)
                    )
        ) {

            Column(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp),

                horizontalAlignment =
                Alignment.CenterHorizontally,

                verticalArrangement =
                Arrangement.Center
            ) {

                // ==================================================
                // ICON
                // ==================================================

                Text(

                    text = "♻️",

                    fontSize = 65.sp
                )

                Spacer(
                    modifier =
                    Modifier.height(8.dp)
                )

                // ==================================================
                // TITLE
                // ==================================================

                Text(

                    text =
                    "Recycler Login",

                    fontSize =
                    29.sp,

                    fontWeight =
                    FontWeight.Bold,

                    color =
                    Color(0xFF14743D)
                )

                Spacer(
                    modifier =
                    Modifier.height(4.dp)
                )

                Text(

                    text =
                    "Welcome back to Kabadi Digital",

                    color =
                    Color.Gray,

                    fontSize =
                    14.sp
                )

                Spacer(
                    modifier =
                    Modifier.height(25.dp)
                )

                // ==================================================
                // LOGIN CARD
                // ==================================================

                Card(

                    modifier =
                    Modifier.fillMaxWidth(),

                    shape =
                    RoundedCornerShape(24.dp),

                    elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
                ) {

                    Column(

                        modifier =
                        Modifier.padding(20.dp)
                    ) {

                        // ==========================================
                        // EMAIL
                        // ==========================================

                        OutlinedTextField(

                            value =
                            email,

                            onValueChange = {
                                email = it
                            },

                            label = {
                                Text("Email")
                            },

                            singleLine =
                            true,

                            modifier =
                            Modifier.fillMaxWidth(),

                            shape =
                            RoundedCornerShape(14.dp)
                        )

                        Spacer(
                            modifier =
                            Modifier.height(14.dp)
                        )

                        // ==========================================
                        // PASSWORD
                        // ==========================================

                        OutlinedTextField(

                            value =
                            password,

                            onValueChange = {
                                password = it
                            },

                            label = {
                                Text("Password")
                            },

                            singleLine =
                            true,

                            visualTransformation =
                            PasswordVisualTransformation(),

                            modifier =
                            Modifier.fillMaxWidth(),

                            shape =
                            RoundedCornerShape(14.dp)
                        )

                        Spacer(
                            modifier =
                            Modifier.height(25.dp)
                        )

                        // ==========================================
                        // LOGIN BUTTON
                        // ==========================================

                        Button(

                            onClick = {

                                onLogin(
                                    email.trim(),
                                    password
                                )
                            },

                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(55.dp),

                            shape =
                            RoundedCornerShape(15.dp),

                            colors =
                            ButtonDefaults.buttonColors(

                                containerColor =
                                Color(0xFF14743D)
                            )
                        ) {

                            Text(

                                text =
                                "LOGIN",

                                fontSize =
                                16.sp,

                                fontWeight =
                                FontWeight.Bold
                            )
                        }

                        Spacer(
                            modifier =
                            Modifier.height(8.dp)
                        )

                        // ==========================================
                        // REGISTER BUTTON
                        // ==========================================

                        TextButton(

                            onClick =
                            onRegister,

                            modifier =
                            Modifier.fillMaxWidth()
                        ) {

                            Text(

                                text =
                                "New Recycler? Create Account",

                                color =
                                Color(0xFF14743D)
                            )
                        }
                    }
                }

                Spacer(
                    modifier =
                    Modifier.height(15.dp)
                )

                // ==================================================
                // BACK
                // ==================================================

                TextButton(

                    onClick =
                    onBack
                ) {

                    Text(
                        text =
                        "← Back"
                    )
                }
            }
        }
    }
}