package com.darcy.kmpdemo.ui.components.atom

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import darcykmp_im.composeapp.generated.resources.Res
import darcykmp_im.composeapp.generated.resources.page_back
import org.jetbrains.compose.resources.stringResource

@Composable
fun PageBackButton(onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(modifier = Modifier.align(Alignment.CenterHorizontally), onClick = {
            onClick.invoke()
        }) {
            Text(text = stringResource(Res.string.page_back))
        }
    }
}