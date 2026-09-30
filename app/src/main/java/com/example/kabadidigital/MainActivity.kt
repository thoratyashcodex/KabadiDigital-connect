package com.example.kabadidigital

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.os.Build
import android.os.CancellationSignal
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.kabadidigital.ui.theme.KabadiDigitalTheme

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


/* =========================================================
   HISTORY DATA CLASS
========================================================= */

data class ScrapHistory(
    val icon: String,
    val title: String,
    val lotNumber: String,
    val weight: String,
    val rate: String,
    val price: String,
    val time: String,
    val photo: String
)


/* =========================================================
   INPUT DATA CLASS
   Stores all input information together for processing.
========================================================= */

data class ScrapData(
    val photo: String,
    val material: String,
    val weight: Double,
    val location: String,
    val date: String
)


/* =========================================================
   COLLECTOR -> RECYCLER PICKUP REQUEST
   Stored locally so the recycler screen can see collector
   requests without mixing them with recycler-created scrap.
========================================================= */

data class NearbyRecycler(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

data class RecyclerPickupRequest(
    val id: String,
    val collectorId: String,
    val recyclerId: String,
    val recyclerName: String,
    val material: String,
    val weight: String,
    val estimatedPrice: String,
    val location: String,
    val photo: String,
    val time: String,
    val status: String
)

val nearbyRecyclerCenters = listOf(
    NearbyRecycler("R001", "Green Recycler Center", 19.8762, 75.3433),
    NearbyRecycler("R002", "Eco Scrap Recycler", 19.8826, 75.3390),
    NearbyRecycler("R003", "City E-Waste Recycler", 19.8675, 75.3520)
)


/* =========================================================
   MAIN ACTIVITY
========================================================= */

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val language = newBase
            .getSharedPreferences("KabadiDigitalLanguage", Context.MODE_PRIVATE)
            .getString("language", "en") ?: "en"

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

            KabadiDigitalTheme {

                KabadiApp()
            }
        }
    }
}


/* =========================================================
   MAIN APP
========================================================= */

@Composable
fun KabadiApp() {

    var selectedTab by remember {

        mutableIntStateOf(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F3EA))
    ) {

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {

            when (selectedTab) {

                0 -> KabadiHomeScreen(
                    onNavigate = { selectedTab = it },
                    onViewEarnings = { selectedTab = 4 }
                )

                1 -> ScanScreen()

                2 -> PriceScreen()

                3 -> HistoryScreen()

                4 -> EarningsScreen(onBack = { selectedTab = 0 })

                5 -> CollectorRequestStatusScreen()
            }
        }

        BottomNavigation(
            selectedTab = selectedTab,
            onTabSelected = {

                selectedTab = it
            }
        )
    }
}


/* =========================================================
   HOME SCREEN
========================================================= */

@Composable
fun KabadiHomeScreen(
    onNavigate: (Int) -> Unit,
    onViewEarnings: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F3EA))
    ) {

        TopHeader()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(18.dp)
        ) {

            BalanceCard(onViewEarnings = onViewEarnings)

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            FeatureCards(onNavigate = onNavigate)

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            PriceAlert()

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            RecentLots(onNavigate = onNavigate)
        }
    }
}


/* =========================================================
   TOP HEADER
========================================================= */

@Composable
fun TopHeader() {

    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }

    // Added only for the collector logo menu.
    // Existing header, language and online UI remain unchanged.
    var showCollectorMenu by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF14743D))
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 22.dp,
                bottom = 20.dp
            )
    ) {

        Text(
            text = stringResource(R.string.good_evening),
            color = Color(0xFFD6EBDD),
            fontSize = 13.sp
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = stringResource(R.string.app_title),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            TextButton(
                onClick = {
                    showLanguageDialog = true
                }
            ) {
                Text(
                    text = "🌐",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(20.dp)
                    )
                    .background(
                        Color(0xFF39865C)
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    )
            ) {

                Text(
                    text = stringResource(R.string.online),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Box {

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(
                            RoundedCornerShape(50)
                        )
                        .background(
                            Color(0xFFE96800)
                        )
                        .clickable {
                            showCollectorMenu = true
                        },
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "₹",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                DropdownMenu(
                    expanded = showCollectorMenu,
                    onDismissRequest = {
                        showCollectorMenu = false
                    }
                ) {

                    DropdownMenuItem(
                        text = {
                            Text("👤 Profile")
                        },
                        onClick = {
                            showCollectorMenu = false
                            showProfileDialog = true
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text("⚙️ Setting")
                        },
                        onClick = {
                            showCollectorMenu = false
                            showSettingsDialog = true
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text("🚪 Logout")
                        },
                        onClick = {
                            showCollectorMenu = false

                            val intent = Intent(
                                context,
                                SplashActivity::class.java
                            )

                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK

                            context.startActivity(intent)

                            (context as? ComponentActivity)?.finish()
                        }
                    )
                }
            }
        }
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = {
                showProfileDialog = false
            },
            title = {
                Text(
                    text = "👤 Profile",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Collector Profile")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Manage your collector account information here.")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showProfileDialog = false
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = {
                showSettingsDialog = false
            },
            title = {
                Text(
                    text = "⚙️ Setting",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Collector Settings")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Your existing language setting is available from the language button.")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSettingsDialog = false
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = {
                showLanguageDialog = false
            },
            title = {
                Text(
                    text = stringResource(R.string.select_language),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            saveAppLanguage(context, "en")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.english),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    TextButton(
                        onClick = {
                            saveAppLanguage(context, "mr")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.marathi),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }
}


/* =========================================================
   LANGUAGE
========================================================= */

fun saveAppLanguage(
    context: Context,
    languageCode: String
) {
    context
        .getSharedPreferences(
            "KabadiDigitalLanguage",
            Context.MODE_PRIVATE
        )
        .edit()
        .putString("language", languageCode)
        .apply()

    (context as? ComponentActivity)?.recreate()
}


/* =========================================================
   BALANCE CARD
========================================================= */

@Composable
fun BalanceCard(onViewEarnings: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(
                Color(0xFF39865C)
            )
            .padding(16.dp)
    ) {

        Text(
            text = stringResource(R.string.today_earning),
            color = Color(0xFFD7E9DE),
            fontSize = 12.sp
        )

        Spacer(
            modifier = Modifier.height(3.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "₹1,870",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(18.dp)
                    )
                    .background(
                        Color(0xFFE96800)
                    )
                    .clickable {
                        onViewEarnings()
                    }
                    .padding(
                        horizontal = 15.dp,
                        vertical = 9.dp
                    )
            ) {

                Text(
                    text = stringResource(R.string.view),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = stringResource(R.string.more_than_last_week),
            color = Color(0xFFD7E9DE),
            fontSize = 11.sp
        )
    }
}


/* =========================================================
   FEATURE CARDS
========================================================= */

@Composable
fun FeatureCards(onNavigate: (Int) -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(Color.White)
            .padding(10.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {

            FeatureCard(
                modifier = Modifier.weight(1f),
                icon = "📷",
                title = stringResource(R.string.submit_ewaste),
                subtitle = stringResource(R.string.photo_and_weight),
                background = Color(0xFFE6F3ED),
                iconBackground = Color(0xFF16733E),
                onClick = { onNavigate(1) }
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            FeatureCard(
                modifier = Modifier.weight(1f),
                icon = "📊",
                title = stringResource(R.string.today_price),
                subtitle = stringResource(R.string.live_market_price),
                background = Color(0xFFFFF4D9),
                iconBackground = Color(0xFFF0A900),
                onClick = { onNavigate(2) }
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {

            FeatureCard(
                modifier = Modifier.weight(1f),
                icon = "🧾",
                title = stringResource(R.string.recycler_service),
                subtitle = stringResource(R.string.available_centers),
                background = Color(0xFFF6E5CF),
                iconBackground = Color(0xFFE96800),
                onClick = { onNavigate(2) }
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            FeatureCard(
                modifier = Modifier.weight(1f),
                icon = "📦",
                title = stringResource(R.string.my_lots),
                subtitle = stringResource(R.string.tracking_history),
                background = Color(0xFFE9EDFF),
                iconBackground = Color(0xFF5148E5),
                onClick = { onNavigate(3) }
            )
        }
    }
}


/* =========================================================
   FEATURE CARD
========================================================= */

@Composable
fun FeatureCard(
    modifier: Modifier,
    icon: String,
    title: String,
    subtitle: String,
    background: Color,
    iconBackground: Color,
    onClick: () -> Unit
) {

    Column(
        modifier = modifier
            .height(90.dp)
            .clip(
                RoundedCornerShape(12.dp)
            )
            .background(background)
            .clickable { onClick() }
            .padding(10.dp)
    ) {

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(
                    RoundedCornerShape(10.dp)
                )
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = icon,
                fontSize = 17.sp
            )
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF222222)
        )

        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = Color(0xFF777777)
        )
    }
}


/* =========================================================
   PRICE ALERT
========================================================= */

@Composable
fun PriceAlert() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(12.dp)
            )
            .background(
                Color(0xFFFFF5DD)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = "⚡",
            fontSize = 20.sp
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = stringResource(R.string.price_alert_title),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )

            Text(
                text = stringResource(R.string.price_alert_detail),
                fontSize = 10.sp,
                color = Color(0xFF555555)
            )
        }

        Text(
            text = "›",
            fontSize = 22.sp,
            color = Color(0xFF333333)
        )
    }
}


/* =========================================================
   RECENT LOTS
========================================================= */

@Composable
fun RecentLots(onNavigate: (Int) -> Unit) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = stringResource(R.string.recent_lots),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF222222),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = stringResource(R.string.view_all),
            fontSize = 12.sp,
            color = Color(0xFF14743D),
            modifier = Modifier.clickable { onNavigate(3) }
        )
    }

    Spacer(
        modifier = Modifier.height(10.dp)
    )

    LotCard(
        icon = "📱",
        title = stringResource(R.string.mobile),
        details = "LOT-2847 • 2.4 kg",
        time = "Today, 11:30",
        price = "₹312"
    )

    Spacer(
        modifier = Modifier.height(10.dp)
    )

    LotCard(
        icon = "💻",
        title = stringResource(R.string.laptop),
        details = "LOT-2846 • 5.1 kg",
        time = "Today, 10:20",
        price = "₹918"
    )
}


/* =========================================================
   LOT CARD
========================================================= */

@Composable
fun LotCard(
    icon: String,
    title: String,
    details: String,
    time: String,
    price: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(15.dp)
            )
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(
                    RoundedCornerShape(12.dp)
                )
                .background(
                    Color(0xFFF0EDE5)
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = icon,
                fontSize = 22.sp
            )
        }

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )

            Text(
                text = details,
                fontSize = 11.sp,
                color = Color(0xFF999999)
            )

            Text(
                text = time,
                fontSize = 10.sp,
                color = Color(0xFF999999)
            )
        }

        Text(
            text = price,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF14743D)
        )
    }
}


/* =========================================================
   SCAN SCREEN
========================================================= */

@Composable
fun ScanScreen() {

    val context = LocalContext.current

    // Resolve localized strings here, inside the composable context.
    // These values can then safely be used from callbacks and LaunchedEffect.
    val photoSavedMessage = stringResource(R.string.photo_saved)
    val photoSaveFailedMessage = stringResource(R.string.photo_save_failed)
    val photoNotTakenMessage = stringResource(R.string.photo_not_taken)
    val cameraPermissionRequiredMessage = stringResource(R.string.camera_permission_required)
    val historyAutoSavedMessage = stringResource(R.string.history_auto_saved)


    /* -----------------------------------------------------
       SAVED PHOTOS
    ----------------------------------------------------- */

    val savedPhotos = remember {

        mutableStateListOf<String>().apply {

            addAll(
                loadSavedPhotos(context)
            )
        }
    }


    var selectedPhoto by remember {

        mutableStateOf<String?>(null)
    }


    /* -----------------------------------------------------
       WEIGHT
    ----------------------------------------------------- */

    var weightText by remember {

        mutableStateOf("")
    }


    /* -----------------------------------------------------
       SELECTED SCRAP
    ----------------------------------------------------- */

    var selectedScrap by remember {

        mutableStateOf("mobile")
    }


    /* -----------------------------------------------------
       CURRENT PHOTO
    ----------------------------------------------------- */

    var currentPhotoFilename by remember {

        mutableStateOf<String?>(null)
    }


    /* -----------------------------------------------------
       GPS LOCATION
       Ready for GPS integration.
    ----------------------------------------------------- */

    var gpsLocation by remember {

        mutableStateOf("Not captured")
    }


    /* -----------------------------------------------------
       CURRENT SCRAP DATA
       All input values are stored together in one object.
    ----------------------------------------------------- */

    var scrapData by remember {

        mutableStateOf<ScrapData?>(null)
    }


    /* -----------------------------------------------------
       RECYCLER PICKUP REQUEST
       ----------------------------------------------------- */
    var showRecyclerRequestDialog by remember {
        mutableStateOf(false)
    }

    /* -----------------------------------------------------
       REQUEST-TIME GPS
       A fresh location is captured when the collector taps
       Find Nearby Recycler, so the request contains the
       collector's current location rather than an old fix.
    ----------------------------------------------------- */

    var isFetchingRecyclerLocation by remember {
        mutableStateOf(false)
    }

    var openRecyclerAfterLocation by remember {
        mutableStateOf(false)
    }


    /* -----------------------------------------------------
       LAST SAVED SCAN
    ----------------------------------------------------- */

    var lastSavedKey by remember {

        mutableStateOf("")
    }


    /* -----------------------------------------------------
       SCRAP RATE
    ----------------------------------------------------- */

    val scrapRate = when (selectedScrap) {

        "mobile" -> 130.0

        "laptop" -> 180.0

        "copper_wire" -> 520.0

        "iron" -> 45.0

        "battery" -> 95.0

        "tv" -> 85.0

        "computer" -> 150.0

        else -> 0.0
    }

    val selectedScrapName = when (selectedScrap) {
        "mobile" -> stringResource(R.string.mobile)
        "laptop" -> stringResource(R.string.laptop)
        "copper_wire" -> stringResource(R.string.copper_wire)
        "iron" -> stringResource(R.string.iron)
        "battery" -> stringResource(R.string.battery)
        "tv" -> stringResource(R.string.tv)
        "computer" -> stringResource(R.string.computer)
        else -> ""
    }


    /* -----------------------------------------------------
       WEIGHT CALCULATION
    ----------------------------------------------------- */

    val weight =
        weightText.toDoubleOrNull() ?: 0.0


    /* -----------------------------------------------------
       TOTAL PRICE
    ----------------------------------------------------- */

    val totalPrice =
        weight * scrapRate


    /* =====================================================
       CAMERA
    ===================================================== */

    val cameraLauncher =
        rememberLauncherForActivityResult(

            contract =
            ActivityResultContracts.TakePicturePreview()

        ) { bitmap: Bitmap? ->

            if (bitmap != null) {

                val filename =
                    savePhotoInsideApp(
                        bitmap = bitmap,
                        context = context
                    )

                if (filename != null) {

                    savedPhotos.add(
                        0,
                        filename
                    )

                    savePhotoList(
                        context = context,
                        photos = savedPhotos
                    )

                    currentPhotoFilename =
                        filename

                    /*
                     * New photo means a new scan.
                     */
                    lastSavedKey = ""

                    Toast.makeText(
                        context,
                        photoSavedMessage,
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    Toast.makeText(
                        context,
                        photoSaveFailedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } else {

                Toast.makeText(
                    context,
                    photoNotTakenMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    /* =====================================================
       CAMERA PERMISSION
    ===================================================== */

    val permissionLauncher =
        rememberLauncherForActivityResult(

            contract =
            ActivityResultContracts.RequestPermission()

        ) { isGranted ->

            if (isGranted) {

                cameraLauncher.launch(null)

            } else {

                Toast.makeText(
                    context,
                    cameraPermissionRequiredMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    /* =====================================================
       GPS LOCATION PERMISSION
       Added without changing the existing camera flow.
       GPS capture starts automatically after a photo is taken.
    ===================================================== */

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(

            contract =
            ActivityResultContracts.RequestMultiplePermissions()

        ) { permissions ->

            val fineGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

            val coarseGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineGranted || coarseGranted) {

                captureFreshGpsLocation(
                    context = context,
                    onLocationCaptured = { location ->

                        gpsLocation =
                            "${location.latitude}, ${location.longitude}"

                        isFetchingRecyclerLocation = false

                        if (openRecyclerAfterLocation) {
                            openRecyclerAfterLocation = false
                            showRecyclerRequestDialog = true
                        }
                    },
                    onFailed = {

                        isFetchingRecyclerLocation = false
                        openRecyclerAfterLocation = false
                        gpsLocation = "Location unavailable"

                        Toast.makeText(
                            context,
                            "GPS/location is enabled, but the current location fix is not ready. Please wait a moment and try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )

            } else {

                isFetchingRecyclerLocation = false
                openRecyclerAfterLocation = false
                gpsLocation = "Location permission denied"

                Toast.makeText(
                    context,
                    "Location permission is required to find nearby recyclers.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    /* =====================================================
       AUTOMATIC GPS CAPTURE
       Starts after the existing photo is saved.
    ===================================================== */

    LaunchedEffect(currentPhotoFilename) {

        if (currentPhotoFilename != null) {

            val finePermission =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

            val coarsePermission =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

            if (finePermission || coarsePermission) {

                captureGpsLocation(
                    context = context,
                    onLocationCaptured = { location ->

                        gpsLocation =
                            "${location.latitude}, ${location.longitude}"
                    },
                    onFailed = {

                        gpsLocation = "Location unavailable"
                    }
                )

            } else {

                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }


    /* =====================================================
       UPDATE SCRAP DATA WITH GPS
       Keeps the existing history logic unchanged.
    ===================================================== */

    LaunchedEffect(
        currentPhotoFilename,
        selectedScrap,
        weightText,
        gpsLocation
    ) {

        if (
            currentPhotoFilename != null &&
            weight > 0
        ) {

            scrapData = ScrapData(
                photo = currentPhotoFilename!!,
                material = selectedScrapName,
                weight = weight,
                location = gpsLocation,
                date = getCurrentDateTime()
            )
        }
    }


    /* =====================================================
       AUTOMATIC HISTORY SAVE
    ===================================================== */

    LaunchedEffect(
        currentPhotoFilename,
        selectedScrap,
        weightText
    ) {

        if (
            currentPhotoFilename != null &&
            weight > 0 &&
            scrapRate > 0
        ) {

            val photo =
                currentPhotoFilename!!

            val saveKey =
                "$photo|$selectedScrap|$weightText"

            /*
             * Prevent duplicate history records
             * for the same completed scan.
             */
            if (saveKey != lastSavedKey) {

                val icon =
                    getScrapIcon(selectedScrap)

                val lotNumber =
                    generateLotNumber(context)

                val currentTime =
                    getCurrentDateTime()

                /*
                 * INPUT LAYER:
                 * Store photo, material, weight, location and date
                 * together as one ScrapData object.
                 */
                scrapData = ScrapData(
                    photo = photo,
                    material = selectedScrapName,
                    weight = weight,
                    location = gpsLocation,
                    date = currentTime
                )

                val history =
                    ScrapHistory(

                        icon = icon,

                        title = selectedScrapName,

                        lotNumber = lotNumber,

                        weight =
                        formatWeight(weight) + " kg",

                        rate =
                        "₹${formatMoney(scrapRate)}/kg",

                        price =
                        "₹${formatMoney(totalPrice)}",

                        time = currentTime,

                        photo = photo
                    )


                saveHistory(
                    context = context,
                    history = history
                )


                lastSavedKey = saveKey


                Toast.makeText(
                    context,
                    historyAutoSavedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    /* =====================================================
       SCAN UI
    ===================================================== */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFF7F3EA)
            )
    ) {

        /* -------------------------------------------------
           HEADER
        ------------------------------------------------- */

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF14743D)
                )
                .padding(20.dp)
        ) {

            Column {

                Text(
                    text = stringResource(R.string.scan),
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.scan_subtitle),
                    color = Color(0xFFD6EBDD),
                    fontSize = 13.sp
                )
            }
        }


        /* -------------------------------------------------
           CONTENT
        ------------------------------------------------- */

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {

            /* =============================================
               USER TIPS
            ============================================= */

            item {

                Text(
                    text = stringResource(R.string.how_it_works),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222)
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )


                ScanStep(
                    number = "1",
                    title = stringResource(R.string.take_photo),
                    description =
                    stringResource(R.string.take_scrap_photo)
                )


                ScanStep(
                    number = "2",
                    title = stringResource(R.string.select_scrap_type),
                    description =
                    stringResource(R.string.select_scrap_description)
                )


                ScanStep(
                    number = "3",
                    title = stringResource(R.string.enter_weight),
                    description =
                    stringResource(R.string.enter_weight_description)
                )


                ScanStep(
                    number = "4",
                    title = stringResource(R.string.view_price),
                    description =
                    stringResource(R.string.view_price_description)
                )


                ScanStep(
                    number = "5",
                    title = stringResource(R.string.total_price),
                    description =
                    stringResource(R.string.total_price_description)
                )


                Spacer(
                    modifier = Modifier.height(18.dp)
                )
            }


            /* =============================================
               CAMERA BOX
            ============================================= */

            item {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(
                            RoundedCornerShape(20.dp)
                        )
                        .background(Color.White),
                    contentAlignment =
                    Alignment.Center
                ) {

                    Column(
                        horizontalAlignment =
                        Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "📷",
                            fontSize = 60.sp
                        )

                        Spacer(
                            modifier =
                            Modifier.height(12.dp)
                        )

                        Text(
                            text =
                            stringResource(R.string.scan_photo_title),
                            fontSize = 17.sp,
                            fontWeight =
                            FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                            Modifier.height(6.dp)
                        )

                        Text(
                            text =
                            stringResource(R.string.scan_photo_subtitle),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Spacer(
                            modifier =
                            Modifier.height(20.dp)
                        )


                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(25.dp)
                                )
                                .background(
                                    Color(0xFF14743D)
                                )
                                .clickable {

                                    val permissionStatus =
                                        ContextCompat
                                            .checkSelfPermission(
                                                context,
                                                Manifest.permission.CAMERA
                                            )

                                    if (
                                        permissionStatus ==
                                        PackageManager.PERMISSION_GRANTED
                                    ) {

                                        cameraLauncher
                                            .launch(null)

                                    } else {

                                        permissionLauncher
                                            .launch(
                                                Manifest.permission.CAMERA
                                            )
                                    }
                                }
                                .padding(
                                    horizontal = 30.dp,
                                    vertical = 13.dp
                                )
                        ) {

                            Text(
                                text = "📷 " + stringResource(R.string.take_photo),
                                color = Color.White,
                                fontWeight =
                                FontWeight.Bold
                            )
                        }
                    }
                }


                Spacer(
                    modifier =
                    Modifier.height(22.dp)
                )
            }


            /* =============================================
               SELECT SCRAP
            ============================================= */

            item {

                Text(
                    text = stringResource(R.string.select_scrap_type),
                    fontSize = 18.sp,
                    fontWeight =
                    FontWeight.Bold,
                    color =
                    Color(0xFF222222)
                )

                Spacer(
                    modifier =
                    Modifier.height(10.dp)
                )


                ScrapTypeButton(
                    text =
                    "📱 " + stringResource(R.string.mobile),
                    selected =
                    selectedScrap == "mobile",
                    onClick = {
                        selectedScrap =
                            "mobile"
                    }
                )


                ScrapTypeButton(
                    text =
                    "💻 " + stringResource(R.string.laptop),
                    selected =
                    selectedScrap == "laptop",
                    onClick = {
                        selectedScrap =
                            "laptop"
                    }
                )


                ScrapTypeButton(
                    text =
                    "🔌 " + stringResource(R.string.copper_wire),
                    selected =
                    selectedScrap == "copper_wire",
                    onClick = {
                        selectedScrap =
                            "copper_wire"
                    }
                )


                ScrapTypeButton(
                    text =
                    "⚙️ " + stringResource(R.string.iron),
                    selected =
                    selectedScrap == "iron",
                    onClick = {
                        selectedScrap =
                            "iron"
                    }
                )


                ScrapTypeButton(
                    text =
                    "🔋 " + stringResource(R.string.battery),
                    selected =
                    selectedScrap == "battery",
                    onClick = {
                        selectedScrap =
                            "battery"
                    }
                )


                ScrapTypeButton(
                    text =
                    "📺 " + stringResource(R.string.tv),
                    selected =
                    selectedScrap == "tv",
                    onClick = {
                        selectedScrap =
                            "tv"
                    }
                )


                ScrapTypeButton(
                    text =
                    "🖥️ " + stringResource(R.string.computer),
                    selected =
                    selectedScrap == "computer",
                    onClick = {
                        selectedScrap =
                            "computer"
                    }
                )


                Spacer(
                    modifier =
                    Modifier.height(15.dp)
                )
            }


            /* =============================================
               WEIGHT
            ============================================= */

            item {

                Text(
                    text = stringResource(R.string.enter_weight),
                    fontSize = 18.sp,
                    fontWeight =
                    FontWeight.Bold,
                    color =
                    Color(0xFF222222)
                )

                Spacer(
                    modifier =
                    Modifier.height(8.dp)
                )


                OutlinedTextField(
                    value =
                    weightText,

                    onValueChange = {

                        weightText = it
                    },

                    modifier =
                    Modifier.fillMaxWidth(),

                    label = {
                        Text(stringResource(R.string.weight_kg))
                    },

                    placeholder = {
                        Text(stringResource(R.string.weight_example))
                    },

                    singleLine = true,

                    keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                        KeyboardType.Decimal
                    )
                )


                Spacer(
                    modifier =
                    Modifier.height(18.dp)
                )
            }


            /* =============================================
               PRICE CARD
            ============================================= */

            item {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(18.dp)
                        )
                        .background(
                            Color(0xFFE6F3ED)
                        )
                        .padding(18.dp)
                ) {

                    Text(
                        text = stringResource(R.string.price_estimate),
                        fontSize = 18.sp,
                        fontWeight =
                        FontWeight.Bold,
                        color =
                        Color(0xFF222222)
                    )

                    Spacer(
                        modifier =
                        Modifier.height(12.dp)
                    )


                    Row(
                        modifier =
                        Modifier.fillMaxWidth(),
                        verticalAlignment =
                        Alignment.CenterVertically
                    ) {

                        Text(
                            text =
                            stringResource(R.string.scrap_type),
                            fontSize = 13.sp,
                            color =
                            Color.Gray,
                            modifier =
                            Modifier.weight(1f)
                        )

                        Text(
                            text =
                            selectedScrapName,
                            fontSize = 14.sp,
                            fontWeight =
                            FontWeight.Bold
                        )
                    }


                    Spacer(
                        modifier =
                        Modifier.height(8.dp)
                    )


                    Row(
                        modifier =
                        Modifier.fillMaxWidth(),
                        verticalAlignment =
                        Alignment.CenterVertically
                    ) {

                        Text(
                            text = stringResource(R.string.weight),
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier =
                            Modifier.weight(1f)
                        )

                        Text(
                            text =
                            if (weight > 0) {
                                "${formatWeight(weight)} kg"
                            } else {
                                "0 kg"
                            },
                            fontSize = 14.sp,
                            fontWeight =
                            FontWeight.Bold
                        )
                    }


                    Spacer(
                        modifier =
                        Modifier.height(8.dp)
                    )


                    Row(
                        modifier =
                        Modifier.fillMaxWidth(),
                        verticalAlignment =
                        Alignment.CenterVertically
                    ) {

                        Text(
                            text =
                            stringResource(R.string.today_rate),
                            fontSize = 13.sp,
                            color =
                            Color.Gray,
                            modifier =
                            Modifier.weight(1f)
                        )

                        Text(
                            text =
                            "₹${formatMoney(scrapRate)} / kg",
                            fontSize = 14.sp,
                            fontWeight =
                            FontWeight.Bold,
                            color =
                            Color(0xFF14743D)
                        )
                    }


                    Spacer(
                        modifier =
                        Modifier.height(12.dp)
                    )


                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(14.dp)
                            )
                            .background(
                                Color(0xFF14743D)
                            )
                            .padding(16.dp)
                    ) {

                        Row(
                            modifier =
                            Modifier.fillMaxWidth(),
                            verticalAlignment =
                            Alignment.CenterVertically
                        ) {

                            Text(
                                text =
                                stringResource(R.string.total_estimated_price),
                                color =
                                Color.White,
                                fontSize = 14.sp,
                                fontWeight =
                                FontWeight.Bold,
                                modifier =
                                Modifier.weight(1f)
                            )

                            Text(
                                text =
                                "₹${formatMoney(totalPrice)}",
                                color =
                                Color.White,
                                fontSize = 20.sp,
                                fontWeight =
                                FontWeight.Bold
                            )
                        }
                    }


                    Spacer(
                        modifier =
                        Modifier.height(10.dp)
                    )


                    if (
                        currentPhotoFilename != null &&
                        weight > 0
                    ) {

                        Text(
                            text =
                            stringResource(R.string.auto_history_saved),
                            fontSize = 11.sp,
                            fontWeight =
                            FontWeight.Bold,
                            color =
                            Color(0xFF14743D)
                        )

                    } else {

                        Text(
                            text =
                            stringResource(R.string.auto_history_instruction),
                            fontSize = 10.sp,
                            color =
                            Color.Gray
                        )
                    }


                    Spacer(
                        modifier =
                        Modifier.height(6.dp)
                    )


                    Text(
                        text =
                        stringResource(R.string.price_note),
                        fontSize = 10.sp,
                        color =
                        Color.Gray
                    )
                }


                Spacer(
                    modifier =
                    Modifier.height(22.dp)
                )
            }


            /* =============================================
               REQUEST NEARBY RECYCLER
               This is an ADDITION. Existing scan/history
               logic remains unchanged.
            ============================================= */

            item {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE96800))
                        .clickable {
                            if (currentPhotoFilename == null || weight <= 0) {
                                Toast.makeText(
                                    context,
                                    "Take a scrap photo and enter weight first.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {

                                val finePermission =
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED

                                val coarsePermission =
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED

                                isFetchingRecyclerLocation = true

                                if (finePermission || coarsePermission) {

                                    captureFreshGpsLocation(
                                        context = context,
                                        onLocationCaptured = { location ->

                                            gpsLocation =
                                                "${location.latitude}, ${location.longitude}"

                                            isFetchingRecyclerLocation = false
                                            showRecyclerRequestDialog = true
                                        },
                                        onFailed = {

                                            isFetchingRecyclerLocation = false

                                            Toast.makeText(
                                                context,
                                                "GPS/location is enabled, but the current location fix is not ready. Please wait a moment and try again.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    )

                                } else {

                                    openRecyclerAfterLocation = true

                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            }
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isFetchingRecyclerLocation) {
                            "📍 Getting your current GPS location..."
                        } else {
                            "🚚 Find Nearby Recycler & Request Pickup"
                        },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            /* =============================================
               MY PHOTOS
            ============================================= */

            item {

                Text(
                    text = stringResource(R.string.my_photos),
                    fontSize = 20.sp,
                    fontWeight =
                    FontWeight.Bold,
                    color =
                    Color(0xFF222222)
                )

                Spacer(
                    modifier =
                    Modifier.height(10.dp)
                )
            }


            if (savedPhotos.isEmpty()) {

                item {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(16.dp)
                            )
                            .background(
                                Color.White
                            )
                            .padding(25.dp),
                        contentAlignment =
                        Alignment.Center
                    ) {

                        Text(
                            text =
                            stringResource(R.string.no_photos),
                            color =
                            Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }

            } else {

                items(
                    items = savedPhotos,
                    key = {
                        it
                    }
                ) { filename ->

                    PhotoCard(
                        filename =
                        filename,
                        context =
                        context,
                        onClick = {

                            selectedPhoto =
                                filename
                        }
                    )

                    Spacer(
                        modifier =
                        Modifier.height(12.dp)
                    )
                }
            }
        }
    }


    /* =====================================================
       PHOTO DIALOG
    ===================================================== */

    selectedPhoto?.let { filename ->

        val bitmap =
            loadPhoto(
                context = context,
                filename = filename
            )

        if (bitmap != null) {

            AlertDialog(

                onDismissRequest = {

                    selectedPhoto = null
                },

                title = {

                    Text(
                        text =
                        stringResource(R.string.scrap_photo),
                        fontWeight =
                        FontWeight.Bold
                    )
                },

                text = {

                    Image(
                        bitmap =
                        bitmap.asImageBitmap(),

                        contentDescription =
                        stringResource(R.string.saved_scrap_photo),

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .clip(
                                RoundedCornerShape(12.dp)
                            ),

                        contentScale =
                        ContentScale.Fit
                    )
                },

                confirmButton = {

                    TextButton(
                        onClick = {

                            selectedPhoto = null
                        }
                    ) {

                        Text(
                            text = stringResource(R.string.close)
                        )
                    }
                }
            )
        }
    }

    /* =====================================================
       RECYCLER REQUEST DIALOG
    ===================================================== */

    if (showRecyclerRequestDialog) {

        val sortedRecyclers = nearbyRecyclerCenters.sortedBy {
            recyclerDistanceKm(gpsLocation, it.latitude, it.longitude)
        }

        AlertDialog(
            onDismissRequest = {
                showRecyclerRequestDialog = false
            },
            title = {
                Text(
                    text = "🚚 Nearby Recycler",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Your GPS: $gpsLocation",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sortedRecyclers) { recycler ->

                            val distance =
                                recyclerDistanceKm(
                                    gpsLocation,
                                    recycler.latitude,
                                    recycler.longitude
                                )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE6F3ED))
                                    .clickable {

                                        val request =
                                            RecyclerPickupRequest(
                                                id = "REQ-${System.currentTimeMillis()}",
                                                collectorId = getCurrentCollectorId(context),
                                                recyclerId = recycler.id,
                                                recyclerName = recycler.name,
                                                material = selectedScrapName,
                                                weight = "${formatWeight(weight)} kg",
                                                estimatedPrice =
                                                "₹${formatMoney(totalPrice)}",
                                                location = gpsLocation,
                                                photo = currentPhotoFilename ?: "",
                                                time = getCurrentDateTime(),
                                                status = "Pending"
                                            )

                                        saveRecyclerPickupRequest(
                                            context = context,
                                            request = request
                                        )

                                        showRecyclerRequestDialog = false

                                        Toast.makeText(
                                            context,
                                            "Pickup request sent to ${recycler.name}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    text = "♻️",
                                    fontSize = 25.sp
                                )

                                Spacer(
                                    modifier = Modifier.width(10.dp)
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = recycler.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF174D32)
                                    )

                                    Text(
                                        text =
                                        if (distance != null) {
                                            "${String.format(Locale.US, "%.1f", distance)} km away"
                                        } else {
                                            "Distance unavailable"
                                        },
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )

                                    Text(
                                        text = "Tap to request pickup",
                                        fontSize = 10.sp,
                                        color = Color(0xFFE96800)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRecyclerRequestDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/* =========================================================
   RECYCLER PICKUP REQUEST STORAGE
========================================================= */

fun getCurrentCollectorId(context: Context): String {

    val preferences = context.getSharedPreferences(
        "KabadiDigitalCollectorIdentity",
        Context.MODE_PRIVATE
    )

    val existingId = preferences.getString("collector_id", null)

    if (!existingId.isNullOrBlank()) {
        return existingId
    }

    val newId = "COL-${UUID.randomUUID()}"

    preferences.edit()
        .putString("collector_id", newId)
        .apply()

    return newId
}

fun saveRecyclerPickupRequest(
    context: Context,
    request: RecyclerPickupRequest
) {
    val preferences = context.getSharedPreferences(
        "KabadiDigitalRecyclerRequests",
        Context.MODE_PRIVATE
    )

    val oldRequests = preferences.getString("requests", "") ?: ""

    val newRecord = listOf(
        request.id,
        request.collectorId,
        request.recyclerId,
        request.recyclerName,
        request.material,
        request.weight,
        request.estimatedPrice,
        request.location,
        request.photo,
        request.time,
        request.status
    ).joinToString("|")

    val updated = if (oldRequests.isEmpty()) {
        newRecord
    } else {
        newRecord + "\n" + oldRequests
    }

    preferences.edit()
        .putString("requests", updated)
        .apply()
}

fun loadRecyclerPickupRequests(
    context: Context
): List<RecyclerPickupRequest> {

    val preferences = context.getSharedPreferences(
        "KabadiDigitalRecyclerRequests",
        Context.MODE_PRIVATE
    )

    val requestString = preferences.getString("requests", "") ?: ""

    if (requestString.isEmpty()) {
        return emptyList()
    }

    val currentCollectorId = getCurrentCollectorId(context)

    return requestString
        .split("\n")
        .mapNotNull { record ->

            val parts = record.split("|")

            when {
                parts.size >= 11 -> {
                    RecyclerPickupRequest(
                        id = parts[0],
                        collectorId = parts[1],
                        recyclerId = parts[2],
                        recyclerName = parts[3],
                        material = parts[4],
                        weight = parts[5],
                        estimatedPrice = parts[6],
                        location = parts[7],
                        photo = parts[8],
                        time = parts[9],
                        status = parts[10]
                    )
                }

                // Backward compatibility with requests created before collectorId was added.
                parts.size >= 10 -> {
                    RecyclerPickupRequest(
                        id = parts[0],
                        collectorId = currentCollectorId,
                        recyclerId = parts[1],
                        recyclerName = parts[2],
                        material = parts[3],
                        weight = parts[4],
                        estimatedPrice = parts[5],
                        location = parts[6],
                        photo = parts[7],
                        time = parts[8],
                        status = parts[9]
                    )
                }

                else -> null
            }
        }
}

fun updateRecyclerPickupRequestStatus(
    context: Context,
    requestId: String,
    newStatus: String
) {
    val requests = loadRecyclerPickupRequests(context)

    val updated = requests.map { request ->
        if (request.id == requestId) {
            request.copy(status = newStatus)
        } else {
            request
        }
    }

    val preferences = context.getSharedPreferences(
        "KabadiDigitalRecyclerRequests",
        Context.MODE_PRIVATE
    )

    val requestString = updated.joinToString("\n") { request ->
        listOf(
            request.id,
            request.collectorId,
            request.recyclerId,
            request.recyclerName,
            request.material,
            request.weight,
            request.estimatedPrice,
            request.location,
            request.photo,
            request.time,
            request.status
        ).joinToString("|")
    }

    preferences.edit()
        .putString("requests", requestString)
        .apply()
}

fun recyclerDistanceKm(
    gpsLocation: String,
    latitude: Double,
    longitude: Double
): Double? {

    return try {
        val parts = gpsLocation.split(",")

        if (parts.size < 2) {
            null
        } else {
            val collectorLat = parts[0].trim().toDouble()
            val collectorLon = parts[1].trim().toDouble()

            val results = FloatArray(1)

            Location.distanceBetween(
                collectorLat,
                collectorLon,
                latitude,
                longitude,
                results
            )

            results[0].toDouble() / 1000.0
        }
    } catch (e: Exception) {
        null
    }
}


/* =========================================================
   COLLECTOR REQUEST STATUS
   Shows only requests created by this collector/device.
   Recycler-created scrap is never shown here.
========================================================= */

@Composable
fun CollectorRequestStatusScreen() {

    val context = LocalContext.current

    var requestList by remember {
        mutableStateOf(
            loadRecyclerPickupRequests(context)
                .filter { it.collectorId == getCurrentCollectorId(context) }
        )
    }

    LaunchedEffect(Unit) {
        requestList = loadRecyclerPickupRequests(context)
            .filter { it.collectorId == getCurrentCollectorId(context) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F3EA))
            .padding(18.dp)
    ) {

        Text(
            text = "Recycler Request Status",
            color = Color(0xFF14743D),
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "Only your requests and recycler responses are shown here.",
            color = Color.Gray,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(15.dp))

        if (requestList.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📩", fontSize = 52.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "No pickup requests yet",
                        color = Color(0xFF14743D),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "Send a pickup request to a nearby recycler first.",
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

                    val message = when (request.status) {
                        "Accepted" -> "✅ Recycler accepted your pickup request."
                        "Rejected" -> "❌ Recycler rejected your pickup request."
                        else -> "⏳ Waiting for recycler to accept or reject your request."
                    }

                    val statusColor = when (request.status) {
                        "Accepted" -> Color(0xFF14743D)
                        "Rejected" -> Color(0xFFB3261E)
                        else -> Color(0xFFE96800)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(15.dp)
                        ) {

                            Text(
                                text = message,
                                color = statusColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Recycler: ${request.recyclerName}",
                                color = Color(0xFF222222),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(5.dp))

                            Text(
                                text = "Scrap: ${request.material}",
                                color = Color.DarkGray,
                                fontSize = 12.sp
                            )

                            Text(
                                text = "Weight: ${request.weight}",
                                color = Color.DarkGray,
                                fontSize = 12.sp
                            )

                            Text(
                                text = "Estimated price: ${request.estimatedPrice}",
                                color = Color.DarkGray,
                                fontSize = 12.sp
                            )

                            Text(
                                text = "Request ID: ${request.id}",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )

                            Text(
                                text = "Sent: ${request.time}",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Status: ${request.status}",
                                color = statusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


/* =========================================================
   SCAN STEP
========================================================= */
@Composable
fun ScanStep(
    number: String,
    title: String,
    description: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(
                RoundedCornerShape(14.dp)
            )
            .background(Color.White)
            .padding(12.dp),

        verticalAlignment =
        Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(
                    RoundedCornerShape(50)
                )
                .background(
                    Color(0xFF14743D)
                ),

            contentAlignment =
            Alignment.Center
        ) {

            Text(
                text = number,
                color = Color.White,
                fontWeight =
                FontWeight.Bold
            )
        }


        Spacer(
            modifier =
            Modifier.width(12.dp)
        )


        Column {

            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight =
                FontWeight.Bold,
                color =
                Color(0xFF222222)
            )

            Text(
                text = description,
                fontSize = 11.sp,
                color =
                Color.Gray
            )
        }
    }
}



@Composable
fun localizedScrapName(scrap: String): String {
    return when (scrap) {
        "mobile" -> stringResource(R.string.mobile)
        "laptop" -> stringResource(R.string.laptop)
        "copper_wire" -> stringResource(R.string.copper_wire)
        "iron" -> stringResource(R.string.iron)
        "battery" -> stringResource(R.string.battery)
        "tv" -> stringResource(R.string.tv)
        "computer" -> stringResource(R.string.computer)
        else -> scrap
    }
}

/* =========================================================
   SCRAP TYPE BUTTON
========================================================= */

@Composable
fun ScrapTypeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    val backgroundColor =
        if (selected) {

            Color(0xFF14743D)

        } else {

            Color.White
        }


    val textColor =
        if (selected) {

            Color.White

        } else {

            Color(0xFF222222)
        }


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(
                RoundedCornerShape(14.dp)
            )
            .background(
                backgroundColor
            )
            .clickable {

                onClick()
            }
            .padding(15.dp)
    ) {

        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontWeight =
            FontWeight.Bold
        )
    }
}


/* =========================================================
   PHOTO CARD
========================================================= */

@Composable
fun PhotoCard(
    filename: String,
    context: Context,
    onClick: () -> Unit
) {

    val bitmap =
        loadPhoto(
            context = context,
            filename = filename
        )

    if (bitmap != null) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(16.dp)
                )
                .background(Color.White)
                .clickable {

                    onClick()
                }
                .padding(10.dp),

            verticalAlignment =
            Alignment.CenterVertically
        ) {

            Image(
                bitmap =
                bitmap.asImageBitmap(),

                contentDescription =
                stringResource(R.string.saved_scrap_photo),

                modifier = Modifier
                    .size(90.dp)
                    .clip(
                        RoundedCornerShape(12.dp)
                    ),

                contentScale =
                ContentScale.Crop
            )


            Spacer(
                modifier =
                Modifier.width(12.dp)
            )


            Column(
                modifier =
                Modifier.weight(1f)
            ) {

                Text(
                    text =
                    stringResource(R.string.scrap_photo),
                    fontSize = 15.sp,
                    fontWeight =
                    FontWeight.Bold,
                    color =
                    Color(0xFF222222)
                )

                Spacer(
                    modifier =
                    Modifier.height(4.dp)
                )

                Text(
                    text =
                    stringResource(R.string.storage),
                    fontSize = 11.sp,
                    color =
                    Color.Gray
                )

                Spacer(
                    modifier =
                    Modifier.height(5.dp)
                )

                Text(
                    text =
                    stringResource(R.string.tap_to_view),
                    fontSize = 11.sp,
                    color =
                    Color(0xFF14743D)
                )
            }


            Text(
                text = "›",
                fontSize = 28.sp,
                color =
                Color(0xFF14743D)
            )
        }
    }
}


/* =========================================================
   SAVE PHOTO
========================================================= */

fun savePhotoInsideApp(
    bitmap: Bitmap,
    context: Context
): String? {

    val filename =
        "scrap_${System.currentTimeMillis()}.jpg"

    return try {

        context.openFileOutput(
            filename,
            Context.MODE_PRIVATE
        ).use { outputStream ->

            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                95,
                outputStream
            )
        }

        filename

    } catch (e: Exception) {

        e.printStackTrace()

        null
    }
}


/* =========================================================
   LOAD PHOTO
========================================================= */

fun loadPhoto(
    context: Context,
    filename: String
): Bitmap? {

    return try {

        context.openFileInput(
            filename
        ).use { inputStream ->

            BitmapFactory.decodeStream(
                inputStream
            )
        }

    } catch (e: Exception) {

        e.printStackTrace()

        null
    }
}


/* =========================================================
   SAVE PHOTO LIST
========================================================= */

fun savePhotoList(
    context: Context,
    photos: List<String>
) {

    val preferences =
        context.getSharedPreferences(
            "KabadiDigitalPhotos",
            Context.MODE_PRIVATE
        )

    val photoString =
        photos.joinToString("\n")

    preferences.edit()
        .putString(
            "photos",
            photoString
        )
        .apply()
}


/* =========================================================
   LOAD PHOTO LIST
========================================================= */

fun loadSavedPhotos(
    context: Context
): List<String> {

    val preferences =
        context.getSharedPreferences(
            "KabadiDigitalPhotos",
            Context.MODE_PRIVATE
        )

    val photoString =
        preferences.getString(
            "photos",
            ""
        ) ?: ""


    if (photoString.isEmpty()) {

        return emptyList()
    }


    return photoString
        .split("\n")
        .filter {

            it.isNotBlank()
        }
}


/* =========================================================
   HISTORY - SAVE
========================================================= */

fun saveHistory(
    context: Context,
    history: ScrapHistory
) {

    val preferences =
        context.getSharedPreferences(
            "KabadiDigitalHistory",
            Context.MODE_PRIVATE
        )


    val oldHistory =
        preferences.getString(
            "history",
            ""
        ) ?: ""


    /*
     * Store one history item per line.
     *
     * Format:
     *
     * icon | title | lot | weight | rate |
     * price | time | photo
     */

    val newRecord =
        listOf(
            history.icon,
            history.title,
            history.lotNumber,
            history.weight,
            history.rate,
            history.price,
            history.time,
            history.photo
        ).joinToString("|")


    val updatedHistory =
        if (oldHistory.isEmpty()) {

            newRecord

        } else {

            newRecord +
                    "\n" +
                    oldHistory
        }


    preferences.edit()
        .putString(
            "history",
            updatedHistory
        )
        .apply()
}


/* =========================================================
   HISTORY - LOAD
========================================================= */

fun loadHistory(
    context: Context
): List<ScrapHistory> {

    val preferences =
        context.getSharedPreferences(
            "KabadiDigitalHistory",
            Context.MODE_PRIVATE
        )


    val historyString =
        preferences.getString(
            "history",
            ""
        ) ?: ""


    if (historyString.isEmpty()) {

        return emptyList()
    }


    return historyString
        .split("\n")
        .mapNotNull { record ->

            try {

                val parts =
                    record.split("|")


                if (parts.size >= 8) {

                    ScrapHistory(

                        icon = parts[0],

                        title = parts[1],

                        lotNumber = parts[2],

                        weight = parts[3],

                        rate = parts[4],

                        price = parts[5],

                        time = parts[6],

                        photo = parts[7]
                    )

                } else {

                    null
                }

            } catch (e: Exception) {

                null
            }
        }
}


/* =========================================================
   GENERATE LOT NUMBER
========================================================= */

fun generateLotNumber(
    context: Context
): String {

    val preferences =
        context.getSharedPreferences(
            "KabadiDigitalHistory",
            Context.MODE_PRIVATE
        )


    val lastNumber =
        preferences.getInt(
            "lastLotNumber",
            2847
        )


    val newNumber =
        lastNumber + 1


    preferences.edit()
        .putInt(
            "lastLotNumber",
            newNumber
        )
        .apply()


    return "LOT-$newNumber"
}


/* =========================================================
   CURRENT DATE + TIME
========================================================= */

fun getCurrentDateTime(): String {

    val formatter =
        SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        )


    return formatter.format(
        Date()
    )
}


/* =========================================================
   SCRAP ICON
========================================================= */

fun getScrapIcon(
    scrap: String
): String {

    return when (scrap) {

        "mobile" -> "📱"
        "laptop" -> "💻"
        "copper_wire" -> "🔌"
        "iron" -> "⚙️"
        "battery" -> "🔋"
        "tv" -> "📺"
        "computer" -> "🖥️"

        else -> "♻️"
    }
}


/* =========================================================
   FORMAT WEIGHT
========================================================= */

fun formatWeight(
    value: Double
): String {

    return if (value % 1.0 == 0.0) {

        value.toInt().toString()

    } else {

        String.format(
            Locale.getDefault(),
            "%.2f",
            value
        )
    }
}


/* =========================================================
   FORMAT MONEY
========================================================= */

fun formatMoney(
    value: Double
): String {

    return if (value % 1.0 == 0.0) {

        value.toInt().toString()

    } else {

        String.format(
            Locale.getDefault(),
            "%.2f",
            value
        )
    }
}


/* =========================================================
   EARNINGS SCREEN
========================================================= */

@Composable
fun EarningsScreen(onBack: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F3EA))
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF14743D))
                .padding(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "‹",
                    color = Color.White,
                    fontSize = 34.sp,
                    modifier = Modifier
                        .clickable { onBack() }
                        .padding(end = 12.dp)
                )

                Column {
                    Text(
                        text = stringResource(R.string.today_earning),
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(R.string.more_than_last_week),
                        color = Color(0xFFD6EBDD),
                        fontSize = 13.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF39865C))
                    .padding(22.dp)
            ) {

                Text(
                    text = stringResource(R.string.today_earning),
                    color = Color(0xFFD7E9DE),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "₹1,870",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.more_than_last_week),
                    color = Color(0xFFD7E9DE),
                    fontSize = 12.sp
                )
            }
        }
    }
}


/* =========================================================
   PRICE SCREEN
========================================================= */

@Composable
fun PriceScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFF7F3EA)
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF14743D)
                )
                .padding(20.dp)
        ) {

            Column {

                Text(
                    text = stringResource(R.string.today_price),
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight =
                    FontWeight.Bold
                )

                Text(
                    text =
                    stringResource(R.string.today_scrap_prices),
                    color =
                    Color(0xFFD6EBDD),
                    fontSize = 13.sp
                )
            }
        }


        Column(
            modifier = Modifier
                .padding(18.dp)
                .verticalScroll(
                    rememberScrollState()
                )
        ) {

            PriceRow(
                "📱",
                stringResource(R.string.mobile),
                "₹130 / kg"
            )

            PriceRow(
                "💻",
                stringResource(R.string.laptop),
                "₹180 / kg"
            )

            PriceRow(
                "🔌",
                stringResource(R.string.copper_wire),
                "₹520 / kg"
            )

            PriceRow(
                "⚙️",
                stringResource(R.string.iron),
                "₹45 / kg"
            )

            PriceRow(
                "🔋",
                stringResource(R.string.battery),
                "₹95 / kg"
            )

            PriceRow(
                "📺",
                stringResource(R.string.tv),
                "₹85 / kg"
            )

            PriceRow(
                "🖥️",
                stringResource(R.string.computer),
                "₹150 / kg"
            )
        }
    }
}


/* =========================================================
   PRICE ROW
========================================================= */

@Composable
fun PriceRow(
    icon: String,
    name: String,
    price: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(
                RoundedCornerShape(15.dp)
            )
            .background(Color.White)
            .padding(15.dp),

        verticalAlignment =
        Alignment.CenterVertically
    ) {

        Text(
            text = icon,
            fontSize = 28.sp
        )

        Spacer(
            modifier =
            Modifier.width(14.dp)
        )

        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight =
            FontWeight.Bold,
            modifier =
            Modifier.weight(1f)
        )

        Text(
            text = price,
            fontSize = 14.sp,
            fontWeight =
            FontWeight.Bold,
            color =
            Color(0xFF14743D)
        )
    }
}


/* =========================================================
   HISTORY SCREEN
========================================================= */

@Composable
fun HistoryScreen() {

    val context =
        LocalContext.current


    /*
     * Load saved history whenever
     * History screen is opened.
     */

    var historyList by remember {

        mutableStateOf(
            loadHistory(context)
        )
    }


    /*
     * Reload when this screen enters composition.
     */

    LaunchedEffect(Unit) {

        historyList =
            loadHistory(context)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFF7F3EA)
            )
    ) {

        /* -------------------------------------------------
           HEADER
        ------------------------------------------------- */

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF14743D)
                )
                .padding(20.dp)
        ) {

            Column {

                Text(
                    text = stringResource(R.string.history),
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight =
                    FontWeight.Bold
                )

                Text(
                    text =
                    stringResource(R.string.all_scrap_lots),
                    color =
                    Color(0xFFD6EBDD),
                    fontSize = 13.sp
                )
            }
        }


        /* -------------------------------------------------
           HISTORY CONTENT
        ------------------------------------------------- */

        if (historyList.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment =
                Alignment.Center
            ) {

                Column(
                    horizontalAlignment =
                    Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "📋",
                        fontSize = 55.sp
                    )

                    Spacer(
                        modifier =
                        Modifier.height(12.dp)
                    )

                    Text(
                        text =
                        stringResource(R.string.history_empty),
                        fontSize = 17.sp,
                        fontWeight =
                        FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                        Modifier.height(6.dp)
                    )

                    Text(
                        text =
                        stringResource(R.string.history_empty_description),
                        fontSize = 12.sp,
                        color =
                        Color.Gray
                    )
                }
            }

        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(18.dp)
            ) {

                Text(
                    text =
                    stringResource(R.string.total_lots, historyList.size),
                    fontSize = 16.sp,
                    fontWeight =
                    FontWeight.Bold,
                    color =
                    Color(0xFF222222)
                )


                Spacer(
                    modifier =
                    Modifier.height(10.dp)
                )


                historyList.forEach { history ->

                    HistoryCard(
                        icon =
                        history.icon,

                        title =
                        history.title,

                        lotNumber =
                        history.lotNumber,

                        weight =
                        history.weight,

                        rate =
                        history.rate,

                        price =
                        history.price,

                        time =
                        history.time
                    )


                    Spacer(
                        modifier =
                        Modifier.height(10.dp)
                    )
                }
            }
        }
    }
}


/* =========================================================
   HISTORY CARD
========================================================= */

@Composable
fun HistoryCard(
    icon: String,
    title: String,
    lotNumber: String,
    weight: String,
    rate: String,
    price: String,
    time: String
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(15.dp)
            )
            .background(Color.White)
            .padding(14.dp)
    ) {

        Row(
            verticalAlignment =
            Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(
                        RoundedCornerShape(12.dp)
                    )
                    .background(
                        Color(0xFFE6F3ED)
                    ),

                contentAlignment =
                Alignment.Center
            ) {

                Text(
                    text = icon,
                    fontSize = 24.sp
                )
            }


            Spacer(
                modifier =
                Modifier.width(12.dp)
            )


            Column(
                modifier =
                Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    fontWeight =
                    FontWeight.Bold,
                    fontSize = 14.sp
                )

                Text(
                    text = lotNumber,
                    fontSize = 11.sp,
                    color =
                    Color.Gray
                )

                Text(
                    text = time,
                    fontSize = 10.sp,
                    color =
                    Color.Gray
                )
            }


            Column(
                horizontalAlignment =
                Alignment.End
            ) {

                Text(
                    text = price,
                    fontSize = 17.sp,
                    fontWeight =
                    FontWeight.Bold,
                    color =
                    Color(0xFF14743D)
                )

                Text(
                    text = stringResource(R.string.total_label),
                    fontSize = 9.sp,
                    color =
                    Color.Gray
                )
            }
        }


        Spacer(
            modifier =
            Modifier.height(12.dp)
        )


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(10.dp)
                )
                .background(
                    Color(0xFFF7F3EA)
                )
                .padding(10.dp)
        ) {

            Row(
                modifier =
                Modifier.fillMaxWidth(),
                verticalAlignment =
                Alignment.CenterVertically
            ) {

                Column(
                    modifier =
                    Modifier.weight(1f)
                ) {

                    Text(
                        text = stringResource(R.string.weight),
                        fontSize = 10.sp,
                        color =
                        Color.Gray
                    )

                    Text(
                        text = weight,
                        fontSize = 13.sp,
                        fontWeight =
                        FontWeight.Bold
                    )
                }


                Column(
                    modifier =
                    Modifier.weight(1f)
                ) {

                    Text(
                        text = stringResource(R.string.rate_label),
                        fontSize = 10.sp,
                        color =
                        Color.Gray
                    )

                    Text(
                        text = rate,
                        fontSize = 13.sp,
                        fontWeight =
                        FontWeight.Bold
                    )
                }
            }
        }
    }
}


/* =========================================================
   BOTTOM NAVIGATION
========================================================= */

@Composable
fun BottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .background(Color.White),

        horizontalArrangement =
        Arrangement.SpaceEvenly,

        verticalAlignment =
        Alignment.CenterVertically
    ) {

        BottomItem(
            icon = "⌂",
            text = stringResource(R.string.home),
            selected =
            selectedTab == 0,
            onClick = {

                onTabSelected(0)
            }
        )


        BottomItem(
            icon = "▣",
            text = stringResource(R.string.scan_nav),
            selected =
            selectedTab == 1,
            onClick = {

                onTabSelected(1)
            }
        )


        BottomItem(
            icon = "◉",
            text = stringResource(R.string.price),
            selected =
            selectedTab == 2,
            onClick = {

                onTabSelected(2)
            }
        )


        BottomItem(
            icon = "▤",
            text = stringResource(R.string.history_nav),
            selected =
            selectedTab == 3,
            onClick = {

                onTabSelected(3)
            }
        )


        BottomItem(
            icon = "📩",
            text = "Requests",
            selected =
            selectedTab == 5,
            onClick = {

                onTabSelected(5)
            }
        )
    }
}


/* =========================================================
   BOTTOM ITEM
========================================================= */

@Composable
fun BottomItem(
    icon: String,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    val color =
        if (selected) {

            Color(0xFF14743D)

        } else {

            Color(0xFF888888)
        }


    Column(
        modifier = Modifier
            .clip(
                RoundedCornerShape(12.dp)
            )
            .clickable {

                onClick()
            }
            .padding(
                horizontal = 18.dp,
                vertical = 5.dp
            ),

        horizontalAlignment =
        Alignment.CenterHorizontally
    ) {

        Text(
            text = icon,
            fontSize = 22.sp,
            color = color
        )

        Text(
            text = text,
            fontSize = 10.sp,
            color = color
        )
    }
}


/* =========================================================
   FRESH GPS LOCATION FOR RECYCLER REQUEST
   Captures a new location fix when the collector sends a
   pickup request. This avoids sending an old/stale location.
========================================================= */

fun captureFreshGpsLocation(
    context: Context,
    onLocationCaptured: (Location) -> Unit,
    onFailed: () -> Unit
) {
    val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val hasFinePermission =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    val hasCoarsePermission =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    if (!hasFinePermission && !hasCoarsePermission) {
        onFailed()
        return
    }

    /*
     * IMPORTANT:
     * Android can have the Location/GPS switch ON while there is
     * temporarily no location fix. Do not treat "no immediate fix"
     * as "GPS is OFF".
     */
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        if (!locationManager.isLocationEnabled) {
            onFailed()
            return
        }
    }

    val enabledProviders = mutableListOf<String>()

    try {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enabledProviders.add(LocationManager.GPS_PROVIDER)
        }

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            enabledProviders.add(LocationManager.NETWORK_PROVIDER)
        }
    } catch (_: Exception) {
        onFailed()
        return
    }

    if (enabledProviders.isEmpty()) {
        onFailed()
        return
    }

    /*
     * Android 11+:
     * getCurrentLocation() asks the system for a CURRENT fix instead
     * of depending only on an old last-known location.
     */
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val executor = ContextCompat.getMainExecutor(context)
        val cancellationSignals = mutableListOf<CancellationSignal>()
        val handler = android.os.Handler(Looper.getMainLooper())
        var finished = false

        fun finishSuccess(location: Location) {
            if (finished) return
            finished = true

            cancellationSignals.forEach {
                try {
                    it.cancel()
                } catch (_: Exception) {
                }
            }

            handler.removeCallbacksAndMessages(null)
            onLocationCaptured(location)
        }

        fun finishFailure() {
            if (finished) return
            finished = true

            cancellationSignals.forEach {
                try {
                    it.cancel()
                } catch (_: Exception) {
                }
            }

            handler.removeCallbacksAndMessages(null)

            /*
             * A recent last-known fix is used only as a fallback.
             * This prevents a working GPS from incorrectly showing
             * "turn on GPS" when the phone is indoors or the current
             * satellite fix is temporarily slow.
             */
            val fallbackLocation = enabledProviders
                .asSequence()
                .mapNotNull { provider ->
                    try {
                        locationManager.getLastKnownLocation(provider)
                    } catch (_: SecurityException) {
                        null
                    } catch (_: Exception) {
                        null
                    }
                }
                .filter { location ->
                    val age = System.currentTimeMillis() - location.time
                    age in 0..120_000L
                }
                .maxByOrNull { it.time }

            if (fallbackLocation != null) {
                onLocationCaptured(fallbackLocation)
            } else {
                onFailed()
            }
        }

        /*
         * Prefer GPS when precise permission is available.
         * If approximate location is being used, network is often the
         * better first provider, but both enabled providers are tried.
         */
        val providersToTry =
            if (hasFinePermission && enabledProviders.contains(LocationManager.GPS_PROVIDER)) {
                listOf(LocationManager.GPS_PROVIDER) +
                        enabledProviders.filter { it != LocationManager.GPS_PROVIDER }
            } else {
                enabledProviders
            }.distinct()

        providersToTry.forEach { provider ->
            if (finished) return@forEach

            val signal = CancellationSignal()
            cancellationSignals.add(signal)

            try {
                locationManager.getCurrentLocation(
                    provider,
                    signal,
                    executor
                ) { location ->
                    if (location != null) {
                        finishSuccess(location)
                    }
                }
            } catch (_: SecurityException) {
                // Try the next provider.
            } catch (_: Exception) {
                // Try the next provider.
            }
        }

        /*
         * Give the system enough time for a real GPS fix.
         * 15 seconds is much safer than the previous 10-second timeout.
         */
        handler.postDelayed(
            { finishFailure() },
            15_000L
        )

        return
    }

    /*
     * Android 10 and older fallback:
     * listen for updates from every enabled provider.
     */
    val handler = android.os.Handler(Looper.getMainLooper())
    var finished = false

    lateinit var listener: LocationListener

    fun finishSuccess(location: Location) {
        if (finished) return
        finished = true

        try {
            locationManager.removeUpdates(listener)
        } catch (_: SecurityException) {
        }

        handler.removeCallbacksAndMessages(null)
        onLocationCaptured(location)
    }

    fun finishFailure() {
        if (finished) return
        finished = true

        try {
            locationManager.removeUpdates(listener)
        } catch (_: SecurityException) {
        }

        handler.removeCallbacksAndMessages(null)

        val fallbackLocation = enabledProviders
            .asSequence()
            .mapNotNull { provider ->
                try {
                    locationManager.getLastKnownLocation(provider)
                } catch (_: SecurityException) {
                    null
                } catch (_: Exception) {
                    null
                }
            }
            .filter { location ->
                val age = System.currentTimeMillis() - location.time
                age in 0..120_000L
            }
            .maxByOrNull { it.time }

        if (fallbackLocation != null) {
            onLocationCaptured(fallbackLocation)
        } else {
            onFailed()
        }
    }

    listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            finishSuccess(location)
        }
    }

    try {
        enabledProviders.forEach { provider ->
            locationManager.requestLocationUpdates(
                provider,
                0L,
                0f,
                listener,
                Looper.getMainLooper()
            )
        }

        handler.postDelayed(
            { finishFailure() },
            15_000L
        )
    } catch (_: SecurityException) {
        finishFailure()
    } catch (_: Exception) {
        finishFailure()
    }
}


/* =========================================================
   GPS LOCATION HELPER
   Uses the same robust current-location logic as the
   recycler-request flow, so photo-time GPS also works when
   the phone has approximate location enabled.
========================================================= */

fun captureGpsLocation(
    context: Context,
    onLocationCaptured: (Location) -> Unit,
    onFailed: () -> Unit
) {
    captureFreshGpsLocation(
        context = context,
        onLocationCaptured = onLocationCaptured,
        onFailed = onFailed
    )
}

