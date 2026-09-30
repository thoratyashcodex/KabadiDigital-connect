package com.example.kabadidigital

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            SplashScreen(

                onCollectorClick = {

                    val intent = Intent(
                        this@SplashActivity,
                        CollectorLoginActivity::class.java
                    )

                    startActivity(intent)
                    finish()
                },

                onRecyclerClick = {

                    val intent = Intent(
                        this@SplashActivity,
                        RecyclerLoginActivity::class.java
                    )

                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}


@Composable
fun SplashScreen(
    onCollectorClick: () -> Unit,
    onRecyclerClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF14743D),
                        Color(0xFF0D5C30)
                    )
                )
            )
            .padding(horizontal = 20.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center
    ) {

        // ---------------------------------------------------------
        // LOGO
        // ---------------------------------------------------------

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = Color(0x33FFFFFF),
                    shape = RoundedCornerShape(24.dp)
                ),

            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.kabadi_logo
                ),

                contentDescription = "Kabadi Digital Logo",

                modifier = Modifier
                    .size(95.dp),

                contentScale = ContentScale.Fit
            )
        }


        Spacer(
            modifier = Modifier.height(18.dp)
        )


        // ---------------------------------------------------------
        // APP NAME
        // ---------------------------------------------------------

        Text(
            text = "कबाडी डिजिटल",

            color = Color.White,

            fontSize = 32.sp,

            fontWeight = FontWeight.Bold
        )


        Spacer(
            modifier = Modifier.height(4.dp)
        )


        Text(
            text = "Smart Scrap Management",

            color = Color(0xFFD6EBDD),

            fontSize = 15.sp
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        Text(
            text = "पर्यावरणासाठी, नफ्यासाठी 🌱",

            color = Color(0xFFAFE0C1),

            fontSize = 12.sp
        )


        Spacer(
            modifier = Modifier.height(35.dp)
        )


        // ---------------------------------------------------------
        // WHO ARE YOU?
        // ---------------------------------------------------------

        Text(
            text = "Who are you?",

            color = Color.White,

            fontSize = 21.sp,

            fontWeight = FontWeight.Bold
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        Text(
            text = "Select your role to continue",

            color = Color(0xFFD6EBDD),

            fontSize = 13.sp
        )


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // ---------------------------------------------------------
        // ROLE CARDS
        // ---------------------------------------------------------

        Row(
            modifier = Modifier
                .fillMaxWidth(),

            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // =====================================================
            // COLLECTOR CARD
            // =====================================================

            RoleCard(
                modifier = Modifier
                    .weight(1f),

                emoji = "♻️",

                title = "Collector",

                subtitle = "Collect & manage scrap",

                onClick = onCollectorClick
            )


            // =====================================================
            // RECYCLER CARD
            // =====================================================

            RoleCard(
                modifier = Modifier
                    .weight(1f),

                emoji = "🏭",

                title = "Recycler",

                subtitle = "Manage recycling",

                onClick = onRecyclerClick
            )
        }


        Spacer(
            modifier = Modifier.height(28.dp)
        )


        Text(
            text = "Choose your role to enter Kabadi Digital",

            color = Color(0xFFBFE6CE),

            fontSize = 11.sp
        )
    }
}


// =================================================================
// ROLE CARD
// =================================================================

@Composable
fun RoleCard(
    modifier: Modifier = Modifier,

    emoji: String,

    title: String,

    subtitle: String,

    onClick: () -> Unit
) {

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSource.collectIsPressedAsState()


    // -------------------------------------------------------------
    // CARD COLORS
    // -------------------------------------------------------------

    val cardColor = if (isPressed) {

        Color(0xFFE8FFF0)

    } else {

        Color.White
    }


    val iconBackground = if (isPressed) {

        Color(0xFFB7E8C7)

    } else {

        Color(0xFFE7F5EC)
    }


    // -------------------------------------------------------------
    // RECTANGULAR CARD
    // -------------------------------------------------------------

    Card(

        modifier = modifier
            .height(175.dp)
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
            defaultElevation = if (isPressed) {
                2.dp
            } else {
                8.dp
            },

            pressedElevation = 2.dp
        )
    ) {

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.Center
        ) {


            // -----------------------------------------------------
            // ICON
            // -----------------------------------------------------

            Box(

                modifier = Modifier
                    .size(58.dp)
                    .background(
                        color = iconBackground,
                        shape = RoundedCornerShape(16.dp)
                    ),

                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = emoji,

                    fontSize = 28.sp
                )
            }


            Spacer(
                modifier = Modifier.height(10.dp)
            )


            // -----------------------------------------------------
            // TITLE
            // -----------------------------------------------------

            Text(
                text = title,

                color = Color(0xFF126B3A),

                fontSize = 17.sp,

                fontWeight = FontWeight.Bold
            )


            Spacer(
                modifier = Modifier.height(5.dp)
            )


            // -----------------------------------------------------
            // SUBTITLE
            // -----------------------------------------------------

            Text(
                text = subtitle,

                color = Color(0xFF66756B),

                fontSize = 11.sp
            )


            Spacer(
                modifier = Modifier.height(8.dp)
            )


            // -----------------------------------------------------
            // CLICK TEXT
            // -----------------------------------------------------

            Text(
                text = "Tap to continue →",

                color = Color(0xFF14743D),

                fontSize = 11.sp,

                fontWeight = FontWeight.SemiBold
            )
        }
    }
}