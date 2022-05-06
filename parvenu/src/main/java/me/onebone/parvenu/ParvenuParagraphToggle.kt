package me.onebone.parvenu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.ParagraphStyle

@Composable
public fun ParvenuParagraphToggle(
	value: ParvenuEditorValue,
	onValueChange: (ParvenuEditorValue) -> Unit,
	paragraphFactory: () -> ParagraphStyle,
	paragraphEqualPredicate: (ParagraphStyle) -> Boolean,
	block: @Composable (
		enabled: Boolean,
		onToggle: () -> Unit
	) -> Unit
) {
	val enabled = remember(value) {
		if (value.selection.collapsed) {
			value.parvenuString.paragraphStyles.any { range ->
				paragraphEqualPredicate(range.item) && shouldExpandSpanOnTextAddition(range, value.selection.start)
			}
		} else {
			value.parvenuString.paragraphStyles.fillsRange(
				start = value.selection.min,
				end = value.selection.max,
				predicate = paragraphEqualPredicate
			)
		}
	}

	block(
		enabled = enabled,
		onToggle = {
			val selection = value.selection

			if (!enabled) {
				onValueChange(value.plusParagraphStyle(
					ParvenuString.Range(
						item = paragraphFactory(),
						start = selection.min, end = selection.max,
						startInclusive = true, endInclusive = true
					)
				))
			} else {
				onValueChange(value.copy(
					parvenuString = value.parvenuString.copy(
						paragraphStyles = value.parvenuString.paragraphStyles.removeIntersectingWithRange(
							start = selection.min,
							end = selection.max,
							predicate = paragraphEqualPredicate
						)
					)
				))
			}
		}
	)
}
