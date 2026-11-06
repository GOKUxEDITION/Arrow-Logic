package com.gokuxedition.arrowlogic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ArrowLogicApp() }
    }
}

@Composable
private fun ArrowLogicApp() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ARROW LOGIC", style = MaterialTheme.typography.headlineLarge)
                Text(
                    "Think ahead. Clear the path.",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Core game engine is being built in verified phases.",
                    modifier = Modifier.padding(top = 24.dp)
                )
                Button(onClick = { /* Gameplay navigation is added after generator validation. */ },
                    modifier = Modifier.padding(top = 16.dp)) {
                    Text("Coming in the next development phase")
                }
            }
        }
    }
}
