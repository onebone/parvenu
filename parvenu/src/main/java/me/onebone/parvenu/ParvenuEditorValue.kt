package me.onebone.parvenu

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@Immutable
public class ParvenuEditorValue(
	public val parvenuString: ParvenuString,
	public val selection: TextRange,
	public val composition: TextRange?
) {
	public fun copy(
		parvenuString: ParvenuString = this.parvenuString,
		selection: TextRange = this.selection,
		composition: TextRange? = this.composition
	): ParvenuEditorValue = ParvenuEditorValue(
		parvenuString = parvenuString,
		selection = selection,
		composition = composition
	)
}

public fun ParvenuEditorValue.toTextFieldValue(): TextFieldValue = TextFieldValue(
	annotatedString = parvenuString.toAnnotatedString(),
	selection = selection,
	composition = composition
)

public fun ParvenuEditorValue.plusSpanStyle(
	spanStyle: ParvenuString.Range<SpanStyle>
): ParvenuEditorValue = ParvenuEditorValue(
	parvenuString = parvenuString.copy(
		spanStyles = parvenuString.spanStyles + spanStyle
	),
	selection = selection,
	composition = composition
)

public fun ParvenuEditorValue.plusParagraphStyle(
	paragraphStyle: ParvenuString.Range<ParagraphStyle>
): ParvenuEditorValue = ParvenuEditorValue(
	parvenuString = parvenuString.copy(
		paragraphStyles = parvenuString.paragraphStyles + paragraphStyle
	),
	selection = selection,
	composition = composition
)
