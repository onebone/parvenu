package me.onebone.parvenu

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@Immutable
class ParvenuEditorValue(
	val parvenuString: ParvenuAnnotatedString,
	val selection: TextRange,
	val composition: TextRange?
)

fun ParvenuEditorValue.toTextFieldValue() = TextFieldValue(
	annotatedString = parvenuString.toAnnotatedString(),
	selection = selection,
	composition = composition
)
