package com.example.chap.Screens.PostTimeline

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.chap.components.ui.AppBottomBar
import com.example.chap.components.ui.BottomDestination

@Composable
fun PostTimelineScreen(){
	Scaffold(
		bottomBar = {
			AppBottomBar { dest ->
				when(dest){
					BottomDestination.Home -> { /* TODO */ }
					BottomDestination.Map -> { /* TODO navigate map */ }
					BottomDestination.Event -> { /* TODO */ }
					BottomDestination.Thread -> { /* Already here or TODO */ }
				}
			}
		}
	){ innerPadding ->
		Surface(modifier = Modifier
			.fillMaxSize()
			.padding(innerPadding)
		){
			// TODO: タイムライン本体
		}
	}
}

