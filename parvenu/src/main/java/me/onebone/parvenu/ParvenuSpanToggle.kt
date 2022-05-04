package me.onebone.parvenu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.SpanStyle

@Composable
public fun ParvenuSpanToggle(
	value: ParvenuEditorValue,
	onValueChange: (ParvenuEditorValue) -> Unit,
	spanFactory: () -> SpanStyle,
	spanEqualPredicate: (SpanStyle) -> Boolean,
	block: @Composable (
		enabled: Boolean,
		onToggle: () -> Unit
	) -> Unit
) {
	val enabled = remember(value) {
		if (value.selection.collapsed) {
			value.parvenuString.spanStyles.any { range ->
				spanEqualPredicate(range.item) && shouldExpandSpanOnTextAddition(range, value.selection.start)
			}
		} else {
			value.parvenuString.spanStyles.fillsRange(
				start = value.selection.min,
				end = value.selection.max,
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
					ParvenuString.Range(
						item = spanFactory(),
						start = selection.min, end = selection.max,
						startInclusive = false, endInclusive = true
					)
				))
			} else {
				onValueChange(value.copy(
					parvenuString = ParvenuString(
						text = value.parvenuString.text,
						spanStyles = value.parvenuString.spanStyles.minusSpansInRange(
							start = selection.min,
							endExclusive = selection.max,
							predicate = spanEqualPredicate
						),
						paragraphStyles = emptyList()
					)
				))
			}
		}
	)
}
