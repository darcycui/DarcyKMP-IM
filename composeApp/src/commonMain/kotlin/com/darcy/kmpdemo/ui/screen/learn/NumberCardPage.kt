package com.darcy.kmpdemo.ui.screen.learn

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import darcykmp_im.composeapp.generated.resources.Res
import darcykmp_im.composeapp.generated.resources.background
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ShowNumberCardPage() {
    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.width(300.dp).height(200.dp)
                .background(
                    color = Color(0xB3ffffff),
                    shape = RoundedCornerShape(30.dp)
                )
//                .graphicsLayer {
//                    renderEffect = BlurEffect(3.dp.toPx(), 3.dp.toPx())
//                }
            ,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
//            colors = CardDefaults.cardColors(containerColor = Color.Cyan)
        ) {
            Image(
                painter = painterResource(Res.drawable.background),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(modifier = Modifier.width(300.dp).height(200.dp)) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "2000")
                Text(text = "NumberCard")
            }
        }
    }
}

@Preview
@Composable
fun NumberCardPagePreview() {
    ShowNumberCardPage()
}