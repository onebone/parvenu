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

            val italic = SpanStyle(fontStyle = FontStyle.Italic)

            Column(modifier = Modifier.fillMaxSize()) {
                Button(
                    onClick = {
                        val selection = editorValue.selection

                        val fillRanges = editorValue.parvenuString.spanStyles.fillsRange(
                            selection.start, selection.end
                        ) {
                            it.fontStyle == FontStyle.Italic
                        }

                        println(editorValue.parvenuString.spanStyles.joinToString("\n") {
                            "${it.start}..${it.end} => style=${it.item.fontStyle}, weight=${it.item.fontWeight}"
                        })
                        println("fillRanges=$fillRanges")
                        println()

                        if (
                            !fillRanges
                        ) {
                            editorValue = ParvenuEditorValue(
                                parvenuString = ParvenuAnnotatedString(
                                    text = editorValue.parvenuString.text,
                                    spanStyles = editorValue.parvenuString.spanStyles + ParvenuAnnotatedString.Range(
                                        item = italic,
                                        start = selection.start, end = selection.end,
                                        startInclusive = false, endInclusive = true
                                    )
                                ),
                                selection = editorValue.selection,
                                composition = editorValue.composition
                            )
                        } else {
                            val nonEmptyPredicate: (ParvenuAnnotatedString.Range<*>) -> Boolean = {
                                it.start != it.end
                            }

                            editorValue = ParvenuEditorValue(
                                parvenuString = ParvenuAnnotatedString(
                                    text = editorValue.parvenuString.text,
                                    spanStyles = editorValue.parvenuString.spanStyles.flatMap { range ->
                                        if (range.item.fontStyle != FontStyle.Italic) return@flatMap listOf(range)

                                        if (selection.start <= range.start && range.end <= selection.end) {
                                            emptyList()
                                        } else if (range.start <= selection.start && selection.end <= range.end) {
                                            // SELECTION:      -----
                                            // RANGE    :    ----------
                                            // REMAINDER:    __     ___
                                            listOf(
                                                range.copy(end = selection.start),
                                                range.copy(start = selection.end)
                                            ).filter(nonEmptyPredicate)
                                        } else if (selection.start in range) {
                                            // SELECTION:     ---------
                                            // RANGE    : --------
                                            // REMAINDER: ____
                                            listOf(range.copy(end = selection.start))
                                                .filter(nonEmptyPredicate)
                                        } else if (selection.end in range) {
                                            // SELECTION: ---------
                                            // RANGE    :      --------
                                            // REMAINDER:          ____
                                            listOf(range.copy(start = selection.end, end = range.end))
                                                .filter(nonEmptyPredicate)
                                        } else {
                                            listOf(range)
                                        }
                                    }
                                ),
                                selection = selection,
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
