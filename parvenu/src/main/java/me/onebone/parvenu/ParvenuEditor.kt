package me.onebone.parvenu

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.math.min

@Composable
public fun ParvenuEditor(
	value: ParvenuEditorValue,
	onValueChange: (ParvenuEditorValue) -> Unit,
	block: @Composable (
		value: TextFieldValue,
		onValueChange: (TextFieldValue) -> Unit
	) -> Unit
) {
	block(
		value = value.toTextFieldValue(),
		onValueChange = {
			val oldSelection = value.selection
			val newSelection = it.selection

			val addedLength = it.text.length - value.parvenuString.text.length

			val textChanged = hasTextChanged(addedLength, oldSelection, newSelection)

			if (!textChanged) {
				onValueChange(
					ParvenuEditorValue(
						parvenuString = value.parvenuString,
						selection = it.selection,
						composition = it.composition
					)
				)
			} else {
				val addStart = oldSelection.min
				val addEnd = newSelection.max
				val addLength = addEnd - addStart

				val selMin = min(addStart, addEnd)

				val removedLength = if (oldSelection.collapsed) {
					oldSelection.min - newSelection.min
				} else {
					oldSelection.length
				}

				val newSpanStyles = value.parvenuString.spanStyles.mapNotNull { range ->
					if (range.end < selMin) {
						range
					} else {
						var start = range.start
						var end = range.end

						if (removedLength > 0 && selMin < start) {
							// selection is removing an empty span
							// e.g.) "abc []|def"  (let '[]' be an empty span and '|' be a cursor)
							//   ~~> "abc|def"
							if (selMin == start && start == end) return@mapNotNull null

							start -= min(removedLength, start - selMin)
						}

						if (removedLength > 0 && selMin < end) {
							end -= min(removedLength, end - selMin)
						}

						if (addLength > 0 && addStart <= range.start) {
							start += min(addLength, range.start - addStart)
						}

						if (addLength > 0 && addStart <= range.start) {
							end += min(addLength, range.end - addStart)
						}

						if (addLength > 0 && shouldExpandSpanOnTextAddition(range, oldSelection.min)) {
							end += addLength
						}

						if (end < start) {
							null
						} else {
							if (range.start == start && range.end == end) {
								range
							} else {
								range.copy(start = start, end = end)
							}
						}
					}
				}

				onValueChange(
					ParvenuEditorValue(
						parvenuString = ParvenuString(
							text = it.text,
							spanStyles = newSpanStyles,
							paragraphStyles = emptyList() // TODO
						),
						selection = it.selection,
						composition = it.composition
					)
				)
			}
		}
	)
}

internal fun hasTextChanged(
	textLengthDelta: Int,
	oldSelection: TextRange,
	newSelection: TextRange
): Boolean {
	// (1) replaced -- texts in [oldSelection] is removed and added by newSelection.max - oldSelection.min
	//   this case also covers batch deletion when newSelection.max - oldSelection.min == 0

	// e.g.)
	// ORIGINAL: "foo bar baz"
	// OLD     :     ____
	// NEW     :           |
	// REPLACED: "foohellowbaz"
	//  IMPLIES: ------------
	// ADDED   :     <---->
	// REMOVED :     <-->
	if (-oldSelection.length + newSelection.max - oldSelection.min == textLengthDelta) {
		return true
	}

	// (2) collapsed -- collapsed to collapsed selection with cursor moving forward
	if (oldSelection.collapsed && newSelection.collapsed
		&& textLengthDelta == newSelection.start - oldSelection.start) {
		return true
	}

	return false
}

/**
 * Returns `true` if the [range] should expand if a text is added at the [cursor].
 *
 * The behavior is slightly different from that of [ParvenuString.Range.contains], for
 * example, range=[3, 3), cursor=3 returns true because a cursor should expand the span even if
 * the range is empty as it is startInclusive.
 */
internal fun shouldExpandSpanOnTextAddition(
	range: ParvenuString.Range<*>,
	cursor: Int
): Boolean {
	return (range.start < cursor && cursor < range.end)
			|| (range.startInclusive && range.start == cursor)
			|| (range.endInclusive && range.end == cursor)
}
