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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

class CollectorRegisterActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setContent {

            KabadiDigitalTheme {

                CollectorRegisterScreen(

                    onRegister = {
                            name,
                            email,
                            phone,
                            address,
                            password,
                            confirmPassword ->

                        val cleanName = name.trim()
                        val cleanEmail = email.trim()
                        val cleanPhone = phone.trim()
                        val cleanAddress = address.trim()

                        // =========================
                        // VALIDATION
                        // =========================

                        if (
                            cleanName.isBlank() ||
                            cleanEmail.isBlank() ||
                            cleanPhone.isBlank() ||
                            cleanAddress.isBlank() ||
                            password.isBlank() ||
                            confirmPassword.isBlank()
                        ) {

                            showToast(
                                "Please fill all fields"
                            )

                        } else if (cleanName.length < 2) {

                            showToast(
                                "Please enter a valid name"
                            )

                        } else if (
                            !Patterns.EMAIL_ADDRESS
                                .matcher(cleanEmail)
                                .matches()
                        ) {

                            showToast(
                                "Please enter a valid email"
                            )

                        } else if (
                            !cleanPhone.matches(
                                Regex("^[0-9]{10}$")
                            )
                        ) {

                            showToast(
                                "Please enter a valid 10-digit phone number"
                            )

                        } else if (password.length < 6) {

                            showToast(
                                "Password must be at least 6 characters"
                            )

                        } else if (password != confirmPassword) {

                            showToast(
                                "Passwords do not match"
                            )

                        } else {

                            // =========================
                            // FIREBASE AUTHENTICATION
                            // =========================

                            auth.createUserWithEmailAndPassword(
                                cleanEmail,
                                password
                            )
                                .addOnCompleteListener { task ->

                                    if (task.isSuccessful) {

                                        val user = auth.currentUser

                                        if (user == null) {

                                            showToast(
                                                "Registration failed"
                                            )

                                            return@addOnCompleteListener
                                        }

                                        val uid = user.uid

                                        // =========================
                                        // COLLECTOR DATA
                                        // =========================

                                        val collectorData =
                                            hashMapOf(
                                                "name" to cleanName,
                                                "email" to cleanEmail,
                                                "phone" to cleanPhone,
                                                "address" to cleanAddress,
                                                "role" to "collector"
                                            )

                                        // =========================
                                        // SAVE TO FIRESTORE
                                        // =========================

                                        firestore
                                            .collection("collectors")
                                            .document(uid)
                                            .set(collectorData)
                                            .addOnSuccessListener {

                                                /*
                                                 * Firebase automatically
                                                 * logs the user in after
                                                 * createUser...
                                                 *
                                                 * We sign out here because
                                                 * we want the user to login
                                                 * from Collector Login.
                                                 */

                                                auth.signOut()

                                                showToast(
                                                    "Account created successfully"
                                                )

                                                // Go to Login
                                                startActivity(
                                                    Intent(
                                                        this,
                                                        CollectorLoginActivity::class.java
                                                    )
                                                )

                                                finish()
                                            }
                                            .addOnFailureListener { error ->

                                                showToast(
                                                    "Firestore error: ${error.message}"
                                                )
                                            }

                                    } else {

                                        val errorMessage =
                                            task.exception?.message
                                                ?: "Registration failed"

                                        showToast(
                                            errorMessage
                                        )
                                    }
                                }
                        }
                    },

                    onLogin = {

                        startActivity(
                            Intent(
                                this,
                                CollectorLoginActivity::class.java
                            )
                        )
                    }
                )
            }
        }
    }

    private fun showToast(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}


// =====================================================
// COLLECTOR REGISTER SCREEN
// =====================================================

@Composable
fun CollectorRegisterScreen(
    onRegister: (
        String,
        String,
        String,
        String,
        String,
        String
    ) -> Unit,

    onLogin: () -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var phone by remember {
        mutableStateOf("")
    }

    var address by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var visible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        delay(150)

        visible = true
    }

    AnimatedVisibility(

        visible = visible,

        enter =
        fadeIn(
            animationSpec = tween(600)
        ) +
                slideInVertically(
                    initialOffsetY = { 120 },
                    animationSpec = tween(600)
                )
    ) {

        Column(

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
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(22.dp),

            horizontalAlignment =
            Alignment.CenterHorizontally
        ) {

            // =========================
            // LOGO
            // =========================

            Text(
                text = "🚛",
                fontSize = 55.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // =========================
            // TITLE
            // =========================

            Text(
                text = "Collector Registration",

                fontSize = 28.sp,

                fontWeight =
                FontWeight.Bold,

                color =
                Color(0xFF14743D)
            )

            Text(
                text = "Create your Collector account",

                color =
                Color.Gray,

                fontSize = 14.sp
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // =========================
            // CARD
            // =========================

            Card(

                modifier =
                Modifier.fillMaxWidth(),

                shape =
                RoundedCornerShape(24.dp),

                elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 7.dp
                )
            ) {

                Column(

                    modifier =
                    Modifier.padding(20.dp)
                ) {

                    // NAME

                    OutlinedTextField(

                        value = name,

                        onValueChange = {
                            name = it
                        },

                        label = {
                            Text("Full Name")
                        },

                        singleLine = true,

                        modifier =
                        Modifier.fillMaxWidth(),

                        shape =
                        RoundedCornerShape(14.dp)
                    )

                    Spacer(
                        modifier =
                        Modifier.height(12.dp)
                    )

                    // EMAIL

                    OutlinedTextField(

                        value = email,

                        onValueChange = {
                            email = it
                        },

                        label = {
                            Text("Email")
                        },

                        singleLine = true,

                        modifier =
                        Modifier.fillMaxWidth(),

                        shape =
                        RoundedCornerShape(14.dp)
                    )

                    Spacer(
                        modifier =
                        Modifier.height(12.dp)
                    )

                    // PHONE

                    OutlinedTextField(

                        value = phone,

                        onValueChange = {

                            // Only allow numbers
                            if (
                                it.length <= 10 &&
                                it.all { char ->
                                    char.isDigit()
                                }
                            ) {
                                phone = it
                            }
                        },

                        label = {
                            Text("Phone Number")
                        },

                        singleLine = true,

                        modifier =
                        Modifier.fillMaxWidth(),

                        shape =
                        RoundedCornerShape(14.dp)
                    )

                    Spacer(
                        modifier =
                        Modifier.height(12.dp)
                    )

                    // ADDRESS

                    OutlinedTextField(

                        value = address,

                        onValueChange = {
                            address = it
                        },

                        label = {
                            Text("Address")
                        },

                        modifier =
                        Modifier.fillMaxWidth(),

                        shape =
                        RoundedCornerShape(14.dp)
                    )

                    Spacer(
                        modifier =
                        Modifier.height(12.dp)
                    )

                    // PASSWORD

                    OutlinedTextField(

                        value = password,

                        onValueChange = {
                            password = it
                        },

                        label = {
                            Text("Password")
                        },

                        singleLine = true,

                        visualTransformation =
                        PasswordVisualTransformation(),

                        modifier =
                        Modifier.fillMaxWidth(),

                        shape =
                        RoundedCornerShape(14.dp)
                    )

                    Spacer(
                        modifier =
                        Modifier.height(12.dp)
                    )

                    // CONFIRM PASSWORD

                    OutlinedTextField(

                        value = confirmPassword,

                        onValueChange = {
                            confirmPassword = it
                        },

                        label = {
                            Text("Confirm Password")
                        },

                        singleLine = true,

                        visualTransformation =
                        PasswordVisualTransformation(),

                        modifier =
                        Modifier.fillMaxWidth(),

                        shape =
                        RoundedCornerShape(14.dp)
                    )

                    Spacer(
                        modifier =
                        Modifier.height(22.dp)
                    )

                    // =========================
                    // CREATE ACCOUNT
                    // =========================

                    Button(

                        onClick = {

                            onRegister(
                                name,
                                email,
                                phone,
                                address,
                                password,
                                confirmPassword
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
                            "CREATE ACCOUNT",

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

                    // =========================
                    // LOGIN
                    // =========================

                    TextButton(

                        onClick = onLogin,

                        modifier =
                        Modifier.fillMaxWidth()
                    ) {

                        Text(

                            text =
                            "Already have an account? Login",

                            color =
                            Color(0xFF14743D)
                        )
                    }
                }
            }
        }
    }
}