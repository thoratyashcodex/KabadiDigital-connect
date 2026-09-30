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

class CollectorLoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // =========================================
        // CHECK IF ALREADY LOGGED IN
        // =========================================

        val currentUser = auth.currentUser

        if (currentUser != null) {

            checkCollectorProfile(
                currentUser.uid
            )

            return
        }

        // =========================================
        // SHOW LOGIN SCREEN
        // =========================================

        setContent {

            KabadiDigitalTheme {

                CollectorLoginScreen(

                    onLogin = { email, password ->

                        val cleanEmail = email.trim()

                        // =================================
                        // VALIDATION
                        // =================================

                        if (cleanEmail.isBlank()) {

                            showToast(
                                "Please enter your email"
                            )

                        } else if (
                            !Patterns.EMAIL_ADDRESS
                                .matcher(cleanEmail)
                                .matches()
                        ) {

                            showToast(
                                "Please enter a valid email address"
                            )

                        } else if (password.isBlank()) {

                            showToast(
                                "Please enter your password"
                            )

                        } else if (password.length < 6) {

                            showToast(
                                "Password must be at least 6 characters"
                            )

                        } else {

                            // =================================
                            // FIREBASE LOGIN
                            // =================================

                            auth.signInWithEmailAndPassword(
                                cleanEmail,
                                password
                            )
                                .addOnCompleteListener { task ->

                                    if (task.isSuccessful) {

                                        val user =
                                            auth.currentUser

                                        if (user != null) {

                                            checkCollectorProfile(
                                                user.uid
                                            )

                                        } else {

                                            showToast(
                                                "Login failed. User not found."
                                            )
                                        }

                                    } else {

                                        val errorMessage =
                                            task.exception?.message
                                                ?: "Invalid email or password"

                                        showToast(
                                            errorMessage
                                        )
                                    }
                                }
                        }
                    },

                    // =================================
                    // REGISTER BUTTON
                    // =================================

                    onRegister = {

                        startActivity(
                            Intent(
                                this,
                                CollectorRegisterActivity::class.java
                            )
                        )
                    },

                    // =================================
                    // BACK BUTTON
                    // =================================

                    onBack = {

                        finish()
                    }
                )
            }
        }
    }

    // =================================================
    // CHECK FIRESTORE COLLECTOR PROFILE
    // =================================================

    private fun checkCollectorProfile(uid: String) {

        firestore
            .collection("collectors")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    val role =
                        document.getString("role")

                    if (role == "collector") {

                        // =================================
                        // VALID COLLECTOR
                        // =================================

                        showToast(
                            "Login successful"
                        )

                        startActivity(
                            Intent(
                                this,
                                MainActivity::class.java
                            )
                        )

                        finish()

                    } else {

                        // User exists but role is not collector

                        auth.signOut()

                        showToast(
                            "This account is not a Collector account"
                        )
                    }

                } else {

                    // Firebase Authentication account exists
                    // but Firestore collector profile doesn't exist.

                    auth.signOut()

                    showToast(
                        "Collector profile not found"
                    )
                }
            }
            .addOnFailureListener { error ->

                showToast(
                    "Unable to verify profile: ${error.message}"
                )
            }
    }

    // =================================================
    // TOAST
    // =================================================

    private fun showToast(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}


// =====================================================
// COLLECTOR LOGIN SCREEN
// =====================================================

@Composable
fun CollectorLoginScreen(
    onLogin: (String, String) -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit
) {

    var email by remember {

        mutableStateOf("")
    }

    var password by remember {

        mutableStateOf("")
    }

    var visible by remember {

        mutableStateOf(false)
    }

    // =========================================
    // ANIMATION
    // =========================================

    LaunchedEffect(Unit) {

        delay(150)

        visible = true
    }

    // =========================================
    // MAIN SCREEN
    // =========================================

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

                // =================================
                // ICON
                // =================================

                Text(
                    text = "🚛",
                    fontSize = 65.sp
                )

                Spacer(
                    modifier =
                    Modifier.height(8.dp)
                )

                // =================================
                // TITLE
                // =================================

                Text(

                    text =
                    "Collector Login",

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

                // =================================
                // LOGIN CARD
                // =================================

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

                        // =================================
                        // EMAIL
                        // =================================

                        OutlinedTextField(

                            value =
                            email,

                            onValueChange = {

                                email = it
                            },

                            label = {

                                Text(
                                    "Email"
                                )
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

                        // =================================
                        // PASSWORD
                        // =================================

                        OutlinedTextField(

                            value =
                            password,

                            onValueChange = {

                                password = it
                            },

                            label = {

                                Text(
                                    "Password"
                                )
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

                        // =================================
                        // LOGIN BUTTON
                        // =================================

                        Button(

                            onClick = {

                                onLogin(
                                    email,
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

                        // =================================
                        // REGISTER BUTTON
                        // =================================

                        TextButton(

                            onClick =
                            onRegister,

                            modifier =
                            Modifier.fillMaxWidth()
                        ) {

                            Text(

                                text =
                                "New Collector? Create Account",

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

                // =================================
                // BACK BUTTON
                // =================================

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