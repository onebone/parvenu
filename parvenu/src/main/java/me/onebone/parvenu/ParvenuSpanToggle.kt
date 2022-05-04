package me.onebone.parvenu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.SpanStyle

@Composable
fun ParvenuSpanToggle(
	value: ParvenuEditorValue,
	onValueChange: (ParvenuEditorValue) -> Unit,
	spanFactory: () -> SpanStyle,
	spanEqualPredicate: (SpanStyle) -> Boolean,
	block: @Composable (
		enabled: Boolean,
		onToggle: () -> Unit
	) -> Unit
) {
	val enabled by derivedStateOf {
		if (value.selection.collapsed) {
			value.parvenuString.spanStyles.any { range ->
				shouldExpandSpanOnTextAddition(range, value.selection.start)
			}
		} else {
			value.parvenuString.spanStyles.fillsRange(
				start = value.selection.start,
				end = value.selection.end,
				block = spanEqualPredicate
			)
		}
	}

	block(
		enabled = enabled,
		onToggle = {
			val selection = value.selection

			if (!enabled) {
				onValueChange(value.plusSpanStyle(
					ParvenuAnnotatedString.Range(
						item = spanFactory(),
						start = selection.start, end = selection.end,
						startInclusive = false, endInclusive = true
					)
				))
			} else {
				onValueChange(value.copy(
					parvenuString = ParvenuAnnotatedString(
						text = value.parvenuString.text,
						spanStyles = value.parvenuString.spanStyles.minusSpansInRange(
							start = selection.start,
							endExclusive = selection.end,
							predicate = spanEqualPredicate
						),
						paragraphStyles = emptyList()
					)
				))
			}
		}
	)
}
