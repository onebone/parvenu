package me.onebone.parvenu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
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
                            )
                        )
                    ),
                    selection = TextRange.Zero,
                    composition = null
                )
            ) }

            Column(modifier = Modifier.fillMaxSize()) {
                Button(
                    onClick = {
                        val selection = editorValue.selection

                        if (editorValue.parvenuString.spanStyles.any {
                            it.item.fontStyle == FontStyle.Italic
                                    && (selection.start in it || selection.end in it)
                        }) {
                            editorValue = ParvenuEditorValue(
                                parvenuString = ParvenuAnnotatedString(
                                    text = editorValue.parvenuString.text,
                                    spanStyles = editorValue.parvenuString.spanStyles.flatMap { range ->
                                        if (range.item.fontStyle != FontStyle.Italic) return@flatMap listOf(range)

                                        if (selection.start == range.start && selection.end == range.end) {
                                            println("remove")
                                            listOf()
                                        } else if (selection.start in range && selection.end in range) {
                                            listOf(
                                                range.copy(end = selection.start),
                                                range.copy(start = selection.end)
                                            )
                                        } else if (selection.start in range) {
                                            // SELECTION:     ---------
                                            // RANGE    : --------
                                            // REMAINDER: ____    _____
                                            listOf(
                                                range.copy(end = selection.start),
                                                range.copy(start = range.end, end = selection.end)
                                            )
                                        } else if (selection.end in range) {
                                            // SELECTION: ---------
                                            // RANGE    :      --------
                                            // REMAINDER: ____     ____
                                            listOf(
                                                range.copy(start = selection.start, end = range.start),
                                                range.copy(start = range.start)
                                            )
                                        } else {
                                            listOf(range)
                                        }
                                    }
                                ),
                                selection = selection,
                                composition = editorValue.composition
                            )
                        } else {
                            editorValue = ParvenuEditorValue(
                                parvenuString = ParvenuAnnotatedString(
                                    text = editorValue.parvenuString.text,
                                    spanStyles = editorValue.parvenuString.spanStyles + ParvenuAnnotatedString.Range(
                                        item = SpanStyle(fontStyle = FontStyle.Italic),
                                        start = selection.start, end = selection.end,
                                        startInclusive = false, endInclusive = true
                                    )
                                ),
                                selection = editorValue.selection,
                                composition = editorValue.composition
                            )
                        }
                    }
                ) {
                    Text("italic")
                }

                ParvenuEditor(
                    value = editorValue,
                    onValueChange = {
                        editorValue = it
                    }
                ) { value, onValueChange ->
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = TextStyle(fontSize = 21.sp)
                    )
                }
            }
        }
    }
}
