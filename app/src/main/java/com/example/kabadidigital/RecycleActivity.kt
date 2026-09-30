package com.example.kabadidigital

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf

import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import java.util.Locale


/* =========================================================
   RECYCLER LANGUAGE
   Uses the same preference as the collector side:
   KabadiDigitalLanguage -> "en" or "mr"
========================================================= */

val LocalRecyclerLanguage = compositionLocalOf { "en" }

fun getRecyclerLanguage(context: Context): String {
    return context
        .getSharedPreferences(
            "KabadiDigitalLanguage",
            Context.MODE_PRIVATE
        )
        .getString("language", "en") ?: "en"
}

@Composable
fun recyclerText(
    english: String,
    marathi: String
): String {
    return if (LocalRecyclerLanguage.current == "mr") {
        marathi
    } else {
        english
    }
}


class RecycleActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val language = getRecyclerLanguage(newBase)
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)

        super.attachBaseContext(
            newBase.createConfigurationContext(configuration)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            RecycleDashboard(
                onSignOut = {

                    val intent = Intent(
                        this@RecycleActivity,
                        SplashActivity::class.java
                    )

                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)

                    finish()
                }
            )
        }
    }
}


/* =========================================================
   RECYCLER DASHBOARD
   ========================================================= */

@Composable
fun RecycleDashboard(
    onSignOut: () -> Unit
) {

    val context = LocalContext.current

    var selectedPage by remember {
        mutableStateOf("Home")
    }

    var language by remember {
        mutableStateOf(getRecyclerLanguage(context))
    }

    CompositionLocalProvider(
        LocalRecyclerLanguage provides language
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F6))
        ) {

            /*
             * TOP NAVIGATION BAR
             *
             * Previously this was on the LEFT.
             * Now it is at the TOP.
             */

            RecyclerSideBar(
                selectedPage = selectedPage,

                onPageSelected = {
                    selectedPage = it
                },

                onSignOut = onSignOut,

                language = language,

                onLanguageChanged = { newLanguage ->
                    language = newLanguage
                    context
                        .getSharedPreferences(
                            "KabadiDigitalLanguage",
                            Context.MODE_PRIVATE
                        )
                        .edit()
                        .putString("language", newLanguage)
                        .apply()
                }
            )


            /*
             * MAIN CONTENT
             */

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                when (selectedPage) {

                    "Home" -> {

                        RecyclerHome(
                            onScrapClick = {
                                selectedPage = "Available Scrap"
                            },

                            onPickupClick = {
                                selectedPage = "Pickup Requests"
                            },

                            onPaymentClick = {
                                selectedPage = "Payments"
                            },

                            onOrdersClick = {
                                selectedPage = "Orders"
                            }
                        )
                    }


                    /* =================================================
                       AVAILABLE SCRAP
                       ================================================= */

                    "Available Scrap" -> {

                        AvailableScrapPage()
                    }


                    /* ADDED: collector-only scrap pickup requests */
                    "Collector Requests" -> {

                        CollectorRequestsPage()
                    }


                    "Pickup Requests" -> {

                        CollectorRequestsPage()
                    }


                    "Payments" -> {

                        RecyclerPage(
                            title = recyclerText("Payments", "पेमेंट्स"),
                            subtitle = recyclerText("View payment history", "पेमेंट इतिहास पहा")
                        )
                    }


                    "Orders" -> {

                        RecyclerPage(
                            title = recyclerText("Orders & History", "ऑर्डर्स आणि इतिहास"),
                            subtitle = "View recycling orders"
                        )
                    }


                    "Notifications" -> {

                        RecyclerPage(
                            title = recyclerText("Notifications", "सूचना"),
                            subtitle = recyclerText("View your notifications", "तुमच्या सूचना पहा")
                        )
                    }


                    "Profile" -> {

                        RecyclerPage(
                            title = recyclerText("Profile", "प्रोफाइल"),
                            subtitle = recyclerText("Manage your profile", "तुमचे प्रोफाइल व्यवस्थापित करा")
                        )
                    }


                    "Settings" -> {

                        RecyclerPage(
                            title = recyclerText("Settings", "सेटिंग्ज"),
                            subtitle = recyclerText("Manage application settings", "अॅपच्या सेटिंग्ज व्यवस्थापित करा")
                        )
                    }
                }
            }
        }
    }
}


/* =========================================================
   TOP NAVIGATION BAR
   ========================================================= */

@Composable
fun RecyclerSideBar(
    selectedPage: String,
    onPageSelected: (String) -> Unit,
    onSignOut: () -> Unit,
    language: String,
    onLanguageChanged: (String) -> Unit
) {

    /*
     * Horizontal top navigation.
     *
     * The function name is kept as RecyclerSideBar
     * so you do not need to change anything elsewhere.
     */

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .background(Color(0xFF116B3B))
            .horizontalScroll(
                rememberScrollState()
            )
            .padding(horizontal = 10.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        /* AVATAR / RECYCLER LOGO */

        var showRecyclerMenu by remember {
            mutableStateOf(false)
        }

        Box {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color.White,
                        CircleShape
                    )
                    .clickable {
                        showRecyclerMenu = !showRecyclerMenu
                    },

                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "♻️",
                    fontSize = 23.sp
                )
            }


            DropdownMenu(
                expanded = showRecyclerMenu,

                onDismissRequest = {
                    showRecyclerMenu = false
                }
            ) {

                DropdownMenuItem(
                    text = {
                        Text(recyclerText("Profile", "प्रोफाइल"))
                    },

                    onClick = {
                        showRecyclerMenu = false
                        onPageSelected("Profile")
                    }
                )


                DropdownMenuItem(
                    text = {
                        Text(recyclerText("Setting", "सेटिंग्ज"))
                    },

                    onClick = {
                        showRecyclerMenu = false
                        onPageSelected("Settings")
                    }
                )


                DropdownMenuItem(
                    text = {
                        Text(recyclerText("Logout", "लॉगआउट"))
                    },

                    onClick = {
                        showRecyclerMenu = false
                        onSignOut()
                    }
                )
            }
        }


        Spacer(
            modifier = Modifier.width(12.dp)
        )


        /* HOME */

        NavItem(
            icon = "⌂",
            title = recyclerText("Home", "मुख्यपृष्ठ"),
            selected = selectedPage == "Home"
        ) {

            onPageSelected("Home")
        }


        /* SCRAP */

        NavItem(
            icon = "♻",
            title = recyclerText("Scrap", "भंगार"),
            selected = selectedPage == "Available Scrap"
        ) {

            onPageSelected("Available Scrap")
        }


        /* COLLECTOR REQUESTS - ADDED */

        NavItem(
            icon = "📩",
            title = recyclerText("Requests", "विनंत्या"),
            selected = selectedPage == "Collector Requests"
        ) {

            onPageSelected("Collector Requests")
        }


        /* PICKUP */

        NavItem(
            icon = "🚚",
            title = recyclerText("Pickup", "पिकअप"),
            selected = selectedPage == "Pickup Requests"
        ) {

            onPageSelected("Pickup Requests")
        }


        /* PAYMENT */

        NavItem(
            icon = "₹",
            title = recyclerText("Pay", "पेमेंट"),
            selected = selectedPage == "Payments"
        ) {

            onPageSelected("Payments")
        }


        /* ORDERS */

        NavItem(
            icon = "📦",
            title = recyclerText("Orders", "ऑर्डर्स"),
            selected = selectedPage == "Orders"
        ) {

            onPageSelected("Orders")
        }


        /* ALERT */

        NavItem(
            icon = "🔔",
            title = recyclerText("Alert", "सूचना"),
            selected = selectedPage == "Notifications"
        ) {

            onPageSelected("Notifications")
        }


        /* PROFILE */

        NavItem(
            icon = "👤",
            title = recyclerText("Profile", "प्रोफाइल"),
            selected = selectedPage == "Profile"
        ) {

            onPageSelected("Profile")
        }


        /* SETTINGS */

        NavItem(
            icon = "⚙",
            title = recyclerText("Setting", "सेटिंग्ज"),
            selected = selectedPage == "Settings"
        ) {

            onPageSelected("Settings")
        }


        /* LANGUAGE */

        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.18f))
                .clickable {
                    val nextLanguage = if (language == "en") "mr" else "en"
                    onLanguageChanged(nextLanguage)
                }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (language == "en") "मराठी" else "English",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }


        /* LOGOUT */

        NavItem(
            icon = "↪",
            title = recyclerText("Logout", "लॉगआउट"),
            selected = false,
            onClick = onSignOut
        )
    }
}


/* =========================================================
   NAV ITEM
   ========================================================= */

@Composable
fun NavItem(
    icon: String,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    val interactionSource = remember {
        MutableInteractionSource()
    }


    val isPressed by interactionSource.collectIsPressedAsState()


    val backgroundColor = when {

        selected ->
            Color.White.copy(alpha = 0.22f)

        isPressed ->
            Color.White.copy(alpha = 0.14f)

        else ->
            Color.Transparent
    }


    Column(
        modifier = Modifier
            .width(70.dp)
            .padding(horizontal = 2.dp)
            .background(
                backgroundColor,
                RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(
                vertical = 8.dp
            ),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = icon,
            color = Color.White,
            fontSize = 19.sp
        )


        Text(
            text = title,
            color = Color.White,
            fontSize = 9.sp
        )
    }
}


/* =========================================================
   RECYCLER HOME
   ========================================================= */

@Composable
fun RecyclerHome(
    onScrapClick: () -> Unit,
    onPickupClick: () -> Unit,
    onPaymentClick: () -> Unit,
    onOrdersClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(22.dp)
    ) {

        Text(
            text = recyclerText("Good Evening 👋", "शुभ संध्याकाळ 👋"),
            color = Color.Gray,
            fontSize = 13.sp
        )


        Spacer(
            modifier = Modifier.height(5.dp)
        )


        Text(
            text = recyclerText("Recycler Dashboard", "रीसायकलर डॅशबोर्ड"),
            color = Color(0xFF174D32),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )


        Text(
            text = recyclerText("Manage your recycling business", "तुमचा रिसायकलिंग व्यवसाय व्यवस्थापित करा"),
            color = Color.Gray,
            fontSize = 14.sp
        )


        Spacer(
            modifier = Modifier.height(22.dp)
        )


        EarningsCard()


        Spacer(
            modifier = Modifier.height(22.dp)
        )


        Text(
            text = recyclerText("Quick Actions", "जलद कृती"),
            color = Color(0xFF174D32),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            RecyclerCard(
                modifier = Modifier.weight(1f),
                icon = "♻️",
                title = recyclerText("Available Scrap", "उपलब्ध भंगार"),
                subtitle = recyclerText("View scrap", "भंगार पहा"),
                onClick = onScrapClick
            )


            RecyclerCard(
                modifier = Modifier.weight(1f),
                icon = "🚚",
                title = recyclerText("Pickup Requests", "पिकअप विनंत्या"),
                subtitle = recyclerText("Manage pickups", "पिकअप व्यवस्थापित करा"),
                onClick = onPickupClick
            )
        }


        Spacer(
            modifier = Modifier.height(12.dp)
        )

        /* ADDED: collector-only request dashboard card */
        RecyclerCard(
            modifier = Modifier.fillMaxWidth(),
            icon = "📩",
            title = recyclerText("Collector Scrap Requests", "कलेक्टरच्या भंगार विनंत्या"),
            subtitle = recyclerText("View and accept/reject collector pickup requests", "कलेक्टरच्या पिकअप विनंत्या पहा आणि स्वीकारा/नाकारा"),
            onClick = onPickupClick
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            RecyclerCard(
                modifier = Modifier.weight(1f),
                icon = "₹",
                title = recyclerText("Payments", "पेमेंट्स"),
                subtitle = recyclerText("View payments", "पेमेंट्स पहा"),
                onClick = onPaymentClick
            )


            RecyclerCard(
                modifier = Modifier.weight(1f),
                icon = "📦",
                title = recyclerText("Orders & History", "ऑर्डर्स आणि इतिहास"),
                subtitle = recyclerText("View orders", "ऑर्डर्स पहा"),
                onClick = onOrdersClick
            )
        }


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        Text(
            text = recyclerText("Recent Activity", "अलीकडील हालचाली"),
            color = Color(0xFF174D32),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        ActivityItem(
            icon = "♻️",
            title = "E-Waste received",
            subtitle = "Mobile phones and laptops",
            value = "₹2,450"
        )


        ActivityItem(
            icon = "🚚",
            title = "Pickup completed",
            subtitle = "Order #KD1024",
            value = "Completed"
        )


        ActivityItem(
            icon = "₹",
            title = "Payment received",
            subtitle = "Today's transaction",
            value = "₹1,870"
        )
    }
}


/* =========================================================
   EARNINGS CARD
   ========================================================= */

@Composable
fun EarningsCard() {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp),

        shape = RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF258451)
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            Text(
                text = recyclerText("Today's Earnings", "आजची कमाई"),
                color = Color(0xFFD7F0DF),
                fontSize = 13.sp
            )


            Spacer(
                modifier = Modifier.height(7.dp)
            )


            Text(
                text = "₹3,870",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )


            Spacer(
                modifier = Modifier.height(5.dp)
            )


            Text(
                text = "↑ 18% more than yesterday",
                color = Color(0xFFD7F0DF),
                fontSize = 12.sp
            )
        }
    }
}


/* =========================================================
   RECYCLER CARD
   ========================================================= */

@Composable
fun RecyclerCard(
    modifier: Modifier,
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {

    val interactionSource = remember {
        MutableInteractionSource()
    }


    val isPressed by interactionSource.collectIsPressedAsState()


    val cardColor = if (isPressed) {

        Color(0xFFDFF3E6)

    } else {

        Color.White
    }


    Card(
        modifier = modifier
            .height(145.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),

        shape = RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 1.dp else 4.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),

            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = icon,
                fontSize = 30.sp
            )


            Spacer(
                modifier = Modifier.height(8.dp)
            )


            Text(
                text = title,
                color = Color(0xFF174D32),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )


            Spacer(
                modifier = Modifier.height(4.dp)
            )


            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}


/* =========================================================
   ACTIVITY ITEM
   ========================================================= */

@Composable
fun ActivityItem(
    icon: String,
    title: String,
    subtitle: String,
    value: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),

        shape = RoundedCornerShape(14.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(
                        Color(0xFFE5F5EA),
                        CircleShape
                    ),

                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = icon,
                    fontSize = 20.sp
                )
            }


            Spacer(
                modifier = Modifier.width(12.dp)
            )


            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    color = Color(0xFF174D32),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )


                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }


            Text(
                text = value,
                color = Color(0xFF14743D),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}


/* =========================================================
   AVAILABLE SCRAP DATA
   ========================================================= */

data class AvailableScrap(
    val name: String,
    val category: String,
    val quantity: String,
    val price: String
)


/* =========================================================
   AVAILABLE SCRAP PAGE
   ========================================================= */

@Composable
fun AvailableScrapPage() {

    /*
     * Currently these are sample available scrap items.
     *
     * Later, these can be replaced with Firebase/database
     * data without changing the UI.
     */

    val scrapList = remember {

        listOf(

            AvailableScrap(
                name = "Newspaper",
                category = "Paper",
                quantity = "50 Kg",
                price = "₹18/Kg"
            ),

            AvailableScrap(
                name = "Plastic Bottles",
                category = "Plastic",
                quantity = "100 Kg",
                price = "₹25/Kg"
            ),

            AvailableScrap(
                name = "Cardboard",
                category = "Paper",
                quantity = "75 Kg",
                price = "₹12/Kg"
            ),

            AvailableScrap(
                name = "Iron Scrap",
                category = "Metal",
                quantity = "150 Kg",
                price = "₹35/Kg"
            ),

            AvailableScrap(
                name = "Aluminium",
                category = "Metal",
                quantity = "80 Kg",
                price = "₹120/Kg"
            ),

            AvailableScrap(
                name = "E-Waste",
                category = "Electronic",
                quantity = "40 Kg",
                price = "₹70/Kg"
            )
        )
    }


    /*
     * Stores the scrap selected by the recycler.
     */

    var selectedScrap by remember {
        mutableStateOf<AvailableScrap?>(null)
    }


    /*
     * Stores the quantity entered by the recycler.
     */

    var enteredQuantity by remember {
        mutableStateOf("")
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7F6))
            .padding(22.dp)
    ) {

        Text(
            text = recyclerText("Available Scrap", "उपलब्ध भंगार"),
            color = Color(0xFF174D32),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )


        Spacer(
            modifier = Modifier.height(5.dp)
        )


        Text(
            text = recyclerText("Select available scrap and enter required quantity", "उपलब्ध भंगार निवडा आणि आवश्यक प्रमाण भरा"),
            color = Color.Gray,
            fontSize = 14.sp
        )


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        /*
         * SCRAP LIST
         */

        LazyColumn(
            modifier = Modifier.fillMaxSize(),

            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(scrapList) { scrap ->

                Card(
                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(18.dp),

                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),

                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),

                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = scrap.name,
                                    color = Color(0xFF174D32),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )


                                Spacer(
                                    modifier = Modifier.height(5.dp)
                                )


                                Text(
                                    text = "Category: ${scrap.category}",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )


                                Spacer(
                                    modifier = Modifier.height(3.dp)
                                )


                                Text(
                                    text = "Available: ${scrap.quantity}",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )


                                Spacer(
                                    modifier = Modifier.height(3.dp)
                                )


                                Text(
                                    text = "Price: ${scrap.price}",
                                    color = Color(0xFF14743D),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }


                            Spacer(
                                modifier = Modifier.width(10.dp)
                            )


                            Button(
                                onClick = {

                                    selectedScrap = scrap
                                    enteredQuantity = ""

                                },

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF258451)
                                ),

                                shape = RoundedCornerShape(12.dp)
                            ) {

                                Text(
                                    text = recyclerText("Enter Scrap", "भंगार भरा"),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    /* =====================================================
       ENTER SCRAP DIALOG
       ===================================================== */

    if (selectedScrap != null) {

        AlertDialog(

            onDismissRequest = {

                selectedScrap = null
            },


            title = {

                Text(
                    text = recyclerText("Enter Scrap", "भंगार भरा"),
                    color = Color(0xFF174D32),
                    fontWeight = FontWeight.Bold
                )
            },


            text = {

                Column {

                    Text(
                        text = "Scrap: ${selectedScrap!!.name}",
                        color = Color.DarkGray,
                        fontSize = 14.sp
                    )


                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )


                    Text(
                        text = "Available: ${selectedScrap!!.quantity}",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )


                    Spacer(
                        modifier = Modifier.height(15.dp)
                    )


                    OutlinedTextField(

                        value = enteredQuantity,

                        onValueChange = {

                            enteredQuantity = it
                        },

                        modifier = Modifier.fillMaxWidth(),

                        label = {

                            Text(
                                text = recyclerText("Enter quantity", "प्रमाण भरा")
                            )
                        },

                        placeholder = {

                            Text(
                                text = recyclerText("Example: 20", "उदाहरण: 20")
                            )
                        },

                        singleLine = true,

                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }
            },


            confirmButton = {

                Button(

                    onClick = {

                        if (enteredQuantity.isNotBlank()) {

                            /*
                             * Quantity has been entered.
                             *
                             * At this point you can later send
                             * the request to Firebase/database.
                             */

                            selectedScrap = null
                            enteredQuantity = ""
                        }
                    },

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF258451)
                    )
                ) {

                    Text(
                        text = recyclerText("Submit", "सबमिट"),
                        color = Color.White
                    )
                }
            },


            dismissButton = {

                TextButton(

                    onClick = {

                        selectedScrap = null
                    }
                ) {

                    Text(
                        text = recyclerText("Cancel", "रद्द करा"),
                        color = Color(0xFF14743D)
                    )
                }
            }
        )
    }
}


/* =========================================================
   COLLECTOR SCRAP REQUESTS
   Only collector-generated requests are shown here.
   Recycler-created AvailableScrap items are NOT included.
========================================================= */

@Composable
fun CollectorRequestsPage() {

    val context = LocalContext.current

    var requestList by remember {
        mutableStateOf(
            loadRecyclerPickupRequests(context)
        )
    }

    LaunchedEffect(Unit) {
        requestList = loadRecyclerPickupRequests(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7F6))
            .padding(22.dp)
    ) {

        Text(
            text = recyclerText("Collector Scrap Requests", "कलेक्टरच्या भंगार विनंत्या"),
            color = Color(0xFF174D32),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = recyclerText("Collector pickup requests only. Accept or reject each request.", "फक्त कलेक्टरच्या पिकअप विनंत्या. प्रत्येक विनंती स्वीकारा किंवा नाकारा."),
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (requestList.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📩",
                        fontSize = 55.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = recyclerText("No collector requests", "कलेक्टरच्या विनंत्या नाहीत"),
                        color = Color(0xFF174D32),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = recyclerText("New collector pickup requests will appear here.", "नवीन कलेक्टर पिकअप विनंत्या येथे दिसतील."),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(requestList) { request ->

                    CollectorRequestCard(
                        request = request,
                        onStatusChanged = {
                            requestList =
                                loadRecyclerPickupRequests(context)
                        }
                    )
                }
            }
        }
    }
}


/* =========================================================
   COLLECTOR REQUEST CARD
========================================================= */

@Composable
fun CollectorRequestCard(
    request: RecyclerPickupRequest,
    onStatusChanged: () -> Unit
) {

    val context = LocalContext.current

    val statusColor = when (request.status) {
        "Accepted" -> Color(0xFF14743D)
        "Rejected" -> Color(0xFFC62828)
        else -> Color(0xFFE96800)
    }

    val displayStatus = when (request.status) {
        "Accepted" -> recyclerText("Accepted", "स्वीकारले")
        "Rejected" -> recyclerText("Rejected", "नाकारले")
        else -> recyclerText("Pending", "प्रलंबित")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = recyclerText("📩 Collector Request", "📩 कलेक्टरची विनंती"),
                        color = Color(0xFF174D32),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = recyclerText("Request ID: ${request.id}", "विनंती क्रमांक: ${request.id}"),
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = request.status,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            RequestDetailRow(recyclerText("♻️ Scrap", "♻️ भंगार"), request.material)
            RequestDetailRow(recyclerText("⚖️ Weight", "⚖️ वजन"), request.weight)
            RequestDetailRow(recyclerText("💰 Estimate", "💰 अंदाजे किंमत"), request.estimatedPrice)
            RequestDetailRow(recyclerText("📍 GPS", "📍 GPS स्थान"), request.location)
            RequestDetailRow(recyclerText("🏭 Recycler", "🏭 रीसायकलर"), request.recyclerName)
            RequestDetailRow(recyclerText("🕒 Requested", "🕒 विनंती वेळ"), request.time)

            Spacer(modifier = Modifier.height(12.dp))

            if (request.status == "Pending") {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF14743D))
                            .clickable {

                                updateRecyclerPickupRequestStatus(
                                    context = context,
                                    requestId = request.id,
                                    newStatus = "Accepted"
                                )

                                onStatusChanged()
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = recyclerText("✓ Accept", "✓ स्वीकारा"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFC62828))
                            .clickable {

                                updateRecyclerPickupRequestStatus(
                                    context = context,
                                    requestId = request.id,
                                    newStatus = "Rejected"
                                )

                                onStatusChanged()
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = recyclerText("✕ Reject", "✕ नाकारा"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            } else {

                Text(
                    text = if (LocalRecyclerLanguage.current == "mr") {
                        "रीसायकलरचा निर्णय: ${if (request.status == "Accepted") "स्वीकारले" else "नाकारले"}"
                    } else {
                        "Recycler decision: ${request.status}"
                    },
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun RequestDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = Color.Gray,
            fontSize = 12.sp
        )

        Text(
            text = value,
            color = Color(0xFF222222),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


/* =========================================================
   OTHER PAGES
   ========================================================= */

@Composable
fun RecyclerPage(
    title: String,
    subtitle: String
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(28.dp)
    ) {

        Text(
            text = title,
            color = Color(0xFF174D32),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        Text(
            text = subtitle,
            color = Color.Gray,
            fontSize = 14.sp
        )


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),

            shape = RoundedCornerShape(18.dp),

            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Box(
                modifier = Modifier.fillMaxSize(),

                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = title,
                    color = Color(0xFF14743D),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
