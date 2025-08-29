package com.example.chap.Screens.PostTimeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.chap.Screens.PostTimeline.bindingmodel.PostBindingModel

@Composable
fun PostRow(
    postBindingModel: PostBindingModel,
    modifier: Modifier = Modifier,
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = buildAnnotatedString {
                    append(postBindingModel.username)
                    withStyle(
                        style = SpanStyle(
                            // ⽂字⾊を薄くするために、ContentAlpha.mediumを指定
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        )
                    ) {
                        append(" @${postBindingModel.username}")
                    }
                },
                maxLines = 1, // ⽂字列が複数⾏にならないように指定
                overflow = TextOverflow.Ellipsis, // はみ出した分を「...」で表現
                fontWeight = FontWeight.Bold, // ⽂字を太字に
            )
            Text(text = postBindingModel.content)
            LazyRow {
            }
        }
    }
}
