package me.onebone.parvenu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var editorValue by remember { mutableStateOf(
                ParvenuEditorValue(
                    parvenuString = ParvenuAnnotatedString(
                        text = "normal bold",
                        spanStyles = listOf(
                            ParvenuAnnotatedString.Range(
                                item = SpanStyle(fontWeight = FontWeight.Bold),
                                start = 7, end = 11,
                                startInclusive = false,
                                endInclusive = true
                            ),
                        )
                    ),
                    selection = ParvenuTextRange(
                        range = TextRange.Zero,
                        startInclusive = false,
                        endInclusive = true
                    ),
                    composition = null
                )
            ) }

            ParvenuEditor(
                value = editorValue,
                onValueChange = {
                    editorValue = it
                }
            ) { value, onValueChange ->
                BasicTextField(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(fontSize = 21.sp)
                )
            }
        }
    }
}
