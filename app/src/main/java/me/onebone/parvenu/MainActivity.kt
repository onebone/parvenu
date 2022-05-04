package me.onebone.parvenu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
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
					parvenuString = ParvenuString(
						text = "normal bold",
						spanStyles = listOf(
							ParvenuString.Range(
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
				ParvenuSpanToggle(
					value = editorValue,
					onValueChange = {
						editorValue = it
					},
					spanFactory = { SpanStyle(fontStyle = FontStyle.Italic) },
					spanEqualPredicate = { style ->
						style.fontStyle == FontStyle.Italic
					}
				) { enabled, onToggle ->
					Text(
						modifier = Modifier
							.clickable { onToggle() }
							.alpha(if (enabled) 1f else 0.3f)
							.background(Color.Gray)
							.padding(8.dp),
						text = "italic"
					)
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
