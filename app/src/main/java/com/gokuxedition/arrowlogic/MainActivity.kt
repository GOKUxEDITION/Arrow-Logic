package com.gokuxedition.arrowlogic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gokuxedition.arrowlogic.presentation.ArrowLogicScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ArrowLogicScreen() }
    }
}
