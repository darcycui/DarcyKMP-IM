package com.darcy.kmpdemo.ui.screen.phone.dynamic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import darcykmp_im.composeapp.generated.resources.Res
import darcykmp_im.composeapp.generated.resources.page_dynamic
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhoneDynamicScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Text(stringResource(Res.string.page_dynamic))
        }
    }
}