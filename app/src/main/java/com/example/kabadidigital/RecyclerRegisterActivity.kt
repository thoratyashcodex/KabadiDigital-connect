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

class RecyclerRegisterActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ==========================================
        // INITIALIZE FIREBASE
        // ==========================================

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // ==========================================
        // CHECK IF USER IS ALREADY LOGGED IN
        // ==========================================

        val currentUser = auth.currentUser

        if (currentUser != null) {

            checkExistingRecycler(currentUser.uid)

        } else {

            showRegisterScreen()
        }
    }

    // ==========================================
    // SHOW REGISTER SCREEN
    // ==========================================

    private fun showRegisterScreen() {

        setContent {

            KabadiDigitalTheme {

                RecyclerRegisterScreen(

                    onRegister = {
                            name,
                            businessName,
                            email,
                            phone,
                            address,
                            password,
                            confirmPassword ->

                        registerRecycler(
                            name = name,
                            businessName = businessName,
                            email = email,
                            phone = phone,
                            address = address,
                            password = password,
                            confirmPassword = confirmPassword
                        )
                    },

                    onLogin = {

                        startActivity(
                            Intent(
                                this,
                                RecyclerLoginActivity::class.java
                            )
                        )

                        finish()
                    },

                    onBack = {

                        finish()
                    }
                )
            }
        }
    }

    // ==========================================
    // CHECK EXISTING LOGGED-IN RECYCLER
    // ==========================================

    private fun checkExistingRecycler(uid: String) {

        firestore
            .collection("recyclers")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    val role =
                        document.getString("role")

                    if (role == "recycler") {

                        // User is already a Recycler.
                        // Go directly to Recycler dashboard.

                        startActivity(
                            Intent(
                                this,
                                RecycleActivity::class.java
                            )
                        )

                        finish()

                    } else {

                        // Logged-in account is not Recycler.

                        auth.signOut()

                        showToast(
                            "This account is not a Recycler account"
                        )

                        showRegisterScreen()
                    }

                } else {

                    // Firebase account exists,
                    // but Recycler Firestore profile does not.

                    auth.signOut()

                    showToast(
                        "Recycler profile not found"
                    )

                    showRegisterScreen()
                }
            }
            .addOnFailureListener { error ->

                auth.signOut()

                showToast(
                    "Unable to verify account: ${error.message}"
                )

                showRegisterScreen()
            }
    }

    // ==========================================
    // REGISTER RECYCLER
    // ==========================================

    private fun registerRecycler(
        name: String,
        businessName: String,
        email: String,
        phone: String,
        address: String,
        password: String,
        confirmPassword: String
    ) {

        // ==========================================
        // CLEAN INPUT
        // ==========================================

        val cleanName =
            name.trim()

        val cleanBusinessName =
            businessName.trim()

        val cleanEmail =
            email.trim()

        val cleanPhone =
            phone.trim()

        val cleanAddress =
            address.trim()

        // ==========================================
        // VALIDATION 1 - EMPTY FIELDS
        // ==========================================

        if (
            cleanName.isBlank() ||
            cleanBusinessName.isBlank() ||
            cleanEmail.isBlank() ||
            cleanPhone.isBlank() ||
            cleanAddress.isBlank() ||
            password.isBlank() ||
            confirmPassword.isBlank()
        ) {

            showToast(
                "Please fill all fields"
            )

            return
        }

        // ==========================================
        // VALIDATION 2 - NAME
        // ==========================================

        if (cleanName.length < 2) {

            showToast(
                "Please enter a valid name"
            )

            return
        }

        // ==========================================
        // VALIDATION 3 - BUSINESS NAME
        // ==========================================

        if (cleanBusinessName.length < 2) {

            showToast(
                "Please enter a valid business name"
            )

            return
        }

        // ==========================================
        // VALIDATION 4 - EMAIL
        // ==========================================

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

        // ==========================================
        // VALIDATION 5 - PHONE
        // ==========================================

        if (
            !cleanPhone.matches(
                Regex("^[0-9]{10}$")
            )
        ) {

            showToast(
                "Please enter a valid 10-digit phone number"
            )

            return
        }

        // ==========================================
        // VALIDATION 6 - ADDRESS
        // ==========================================

        if (cleanAddress.length < 5) {

            showToast(
                "Please enter a valid address"
            )

            return
        }

        // ==========================================
        // VALIDATION 7 - PASSWORD
        // ==========================================

        if (password.length < 6) {

            showToast(
                "Password must be at least 6 characters"
            )

            return
        }

        // ==========================================
        // VALIDATION 8 - CONFIRM PASSWORD
        // ==========================================

        if (password != confirmPassword) {

            showToast(
                "Passwords do not match"
            )

            return
        }

        // ==========================================
        // FIREBASE AUTHENTICATION
        // ==========================================

        auth.createUserWithEmailAndPassword(
            cleanEmail,
            password
        )
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val user =
                        auth.currentUser

                    if (user == null) {

                        showToast(
                            "Registration failed"
                        )

                        return@addOnCompleteListener
                    }

                    val uid =
                        user.uid

                    // ==========================================
                    // FIRESTORE DATA
                    // ==========================================

                    val recyclerData =
                        hashMapOf(
                            "name" to cleanName,
                            "businessName" to cleanBusinessName,
                            "email" to cleanEmail,
                            "phone" to cleanPhone,
                            "address" to cleanAddress,
                            "role" to "recycler"
                        )

                    // ==========================================
                    // SAVE RECYCLER DATA
                    // ==========================================

                    firestore
                        .collection("recyclers")
                        .document(uid)
                        .set(recyclerData)
                        .addOnSuccessListener {

                            /*
                             * createUserWithEmailAndPassword()
                             * automatically logs the new user in.
                             *
                             * We sign out here because after
                             * registration we want the user to
                             * login from Recycler Login.
                             */

                            auth.signOut()

                            showToast(
                                "Recycler account created successfully"
                            )

                            // ==================================
                            // GO TO LOGIN
                            // ==================================

                            startActivity(
                                Intent(
                                    this,
                                    RecyclerLoginActivity::class.java
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

                    // ==========================================
                    // FIREBASE REGISTRATION ERROR
                    // ==========================================

                    val errorMessage =
                        task.exception?.message
                            ?: "Registration failed"

                    showToast(
                        errorMessage
                    )
                }
            }
    }

    // ==========================================
    // SHOW TOAST
    // ==========================================

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
// RECYCLER REGISTER SCREEN
// ==========================================================

@Composable
fun RecyclerRegisterScreen(
    onRegister: (
        String,
        String,
        String,
        String,
        String,
        String,
        String
    ) -> Unit,

    onLogin: () -> Unit,

    onBack: () -> Unit
) {

    // ==========================================
    // STATES
    // ==========================================

    var name by remember {
        mutableStateOf("")
    }

    var businessName by remember {
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

    // ==========================================
    // ANIMATION
    // ==========================================

    LaunchedEffect(Unit) {

        delay(150)

        visible = true
    }

    // ==========================================
    // SCREEN
    // ==========================================

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
            Alignment.CenterHorizontally,

            verticalArrangement =
            Arrangement.Top
        ) {

            Spacer(
                modifier =
                Modifier.height(20.dp)
            )

            // ==========================================
            // ICON
            // ==========================================

            Text(
                text = "♻️",
                fontSize = 60.sp
            )

            Spacer(
                modifier =
                Modifier.height(8.dp)
            )

            // ==========================================
            // TITLE
            // ==========================================

            Text(

                text =
                "Recycler Registration",

                fontSize =
                28.sp,

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
                "Create your Recycler account",

                color =
                Color.Gray,

                fontSize =
                14.sp
            )

            Spacer(
                modifier =
                Modifier.height(20.dp)
            )

            // ==========================================
            // CARD
            // ==========================================

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

                    // ==================================
                    // FULL NAME
                    // ==================================

                    OutlinedTextField(

                        value =
                        name,

                        onValueChange = {
                            name = it
                        },

                        label = {
                            Text("Full Name")
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
                        Modifier.height(12.dp)
                    )

                    // ==================================
                    // BUSINESS NAME
                    // ==================================

                    OutlinedTextField(

                        value =
                        businessName,

                        onValueChange = {
                            businessName = it
                        },

                        label = {
                            Text("Business Name")
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
                        Modifier.height(12.dp)
                    )

                    // ==================================
                    // EMAIL
                    // ==================================

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
                        Modifier.height(12.dp)
                    )

                    // ==================================
                    // PHONE
                    // ==================================

                    OutlinedTextField(

                        value =
                        phone,

                        onValueChange = { newValue ->

                            if (
                                newValue.length <= 10 &&
                                newValue.all {
                                    it.isDigit()
                                }
                            ) {

                                phone = newValue
                            }
                        },

                        label = {
                            Text("Phone Number")
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
                        Modifier.height(12.dp)
                    )

                    // ==================================
                    // ADDRESS
                    // ==================================

                    OutlinedTextField(

                        value =
                        address,

                        onValueChange = {
                            address = it
                        },

                        label = {
                            Text("Address")
                        },

                        modifier =
                        Modifier.fillMaxWidth(),

                        minLines = 2,

                        maxLines = 3,

                        shape =
                        RoundedCornerShape(14.dp)
                    )

                    Spacer(
                        modifier =
                        Modifier.height(12.dp)
                    )

                    // ==================================
                    // PASSWORD
                    // ==================================

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
                        Modifier.height(12.dp)
                    )

                    // ==================================
                    // CONFIRM PASSWORD
                    // ==================================

                    OutlinedTextField(

                        value =
                        confirmPassword,

                        onValueChange = {
                            confirmPassword = it
                        },

                        label = {
                            Text("Confirm Password")
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
                        Modifier.height(22.dp)
                    )

                    // ==================================
                    // CREATE ACCOUNT
                    // ==================================

                    Button(

                        onClick = {

                            onRegister(

                                name,
                                businessName,
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

                    // ==================================
                    // LOGIN
                    // ==================================

                    TextButton(

                        onClick =
                        onLogin,

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

            Spacer(
                modifier =
                Modifier.height(10.dp)
            )

            // ==========================================
            // BACK
            // ==========================================

            TextButton(

                onClick =
                onBack
            ) {

                Text(
                    text =
                    "← Back"
                )
            }

            Spacer(
                modifier =
                Modifier.height(20.dp)
            )
        }
    }
}