package com.example.chap.components.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chap.ui.theme.BrandBlue

@Composable
public fun SearchBar(
    modifier: Modifier = Modifier,
    color: Color = BrandBlue,
    searchTextColor: Color = Color.White,
    searchTarget: String = "message"
) {
    var keyword by remember { mutableStateOf("") }
    TextField(
        value = keyword,
        onValueChange = { keyword = it },
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp)
            .clip(RoundedCornerShape(24.dp)),
        placeholder = { Text("Search for the $searchTarget", color = searchTextColor) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF7D8790)) },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = color,
            unfocusedContainerColor = color,
            disabledContainerColor = color,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}