package me.onebone.parvenu

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@Immutable
class ParvenuEditorValue(
	val parvenuString: ParvenuAnnotatedString,
	val selection: TextRange,
	val composition: TextRange?
) {
	fun copy(
		parvenuString: ParvenuAnnotatedString = this.parvenuString,
		selection: TextRange = this.selection,
		composition: TextRange? = this.composition
	) = ParvenuEditorValue(
		parvenuString = parvenuString,
		selection = selection,
		composition = composition
	)
}

fun ParvenuEditorValue.toTextFieldValue() = TextFieldValue(
	annotatedString = parvenuString.toAnnotatedString(),
	selection = selection,
	composition = composition
)

fun ParvenuEditorValue.plusSpanStyle(
	spanStyle: ParvenuAnnotatedString.Range<SpanStyle>
) = ParvenuEditorValue(
	parvenuString = parvenuString.copy(
		spanStyles = parvenuString.spanStyles + spanStyle
	),
	selection = selection,
	composition = composition
)

fun ParvenuEditorValue.plusParagraphStyle(
	paragraphStyle: ParvenuAnnotatedString.Range<ParagraphStyle>
) = ParvenuEditorValue(
	parvenuString = parvenuString.copy(
		paragraphStyles = parvenuString.paragraphStyles + paragraphStyle
	),
	selection = selection,
	composition = composition
)
