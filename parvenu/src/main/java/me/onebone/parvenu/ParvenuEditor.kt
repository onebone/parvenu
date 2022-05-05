package me.onebone.parvenu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.math.max
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
	val textFieldValue = remember(value) { value.toTextFieldValue() }

	block(
		value = textFieldValue,
		onValueChange = { newValue ->
			val textLengthDelta = newValue.text.length - value.parvenuString.text.length
			val newSpanStyles = value.parvenuString.spanStyles.offsetSpansAccordingToSelectionChange(
				textLengthDelta, value.selection, newValue.selection
			) { start, end ->
				// selection is removing an empty span
				// e.g.) "abc []|def"  (let '[]' be an empty span and '|' be a cursor)
				//   ~~> "abc|def"
				start == end
			}

			val newParagraphStyles = value.parvenuString.paragraphStyles.offsetSpansAccordingToSelectionChange(
				textLengthDelta, value.selection, newValue.selection
			) { _, _ ->
				// whole paragraph span should be removed if the start of the span is deleted
				// e.g.) "abc[|paragraph]"  (let '[...]' be a paragraph span and '|' be a cursor)
				//   ~~> "abcparagraph"
				true
			}

			if (newSpanStyles == null && newParagraphStyles == null) {
				onValueChange(
					value.copy(
						selection = newValue.selection,
						composition = newValue.composition
					)
				)
			} else {
				onValueChange(
					ParvenuEditorValue(
						parvenuString = ParvenuString(
							text = newValue.text,
							spanStyles = newSpanStyles ?: value.parvenuString.spanStyles,
							paragraphStyles = newParagraphStyles ?: value.parvenuString.paragraphStyles
						),
						selection = newValue.selection,
						composition = newValue.composition
					)
				)
			}
		}
	)
}

/**
 * Move spans according to text edits. Returns `null` if only a selection has been changed and
 * span ranges remain unchanged.
 *
 * @param onDeleteStart If deleting a start of the span, the whole span is removed if the lambda
 *  returns `true`. This is needed to switch a strategy between span styles and paragraph styles.
 *  A span styles should be removed only if the range is empty, while a paragraph style should be
 *  removed immediately when the start of the span is deleted.
 */
internal fun <T> List<ParvenuString.Range<T>>.offsetSpansAccordingToSelectionChange(
	textLengthDelta: Int,
	oldSelection: TextRange,
	newSelection: TextRange,
	onDeleteStart: (start: Int, end: Int) -> Boolean
): List<ParvenuString.Range<T>>? {
	val textChanged = hasTextChanged(textLengthDelta, oldSelection, newSelection)

	return if (!textChanged) {
		null
	} else {
		val addStart = oldSelection.min
		val addEnd = newSelection.max
		val addLength = addEnd - addStart

		val selMin = min(addStart, addEnd)
		val selMax = max(addStart, addEnd)

		val removedLength = if (oldSelection.collapsed) {
			oldSelection.min - newSelection.min
		} else {
			oldSelection.length
		}

		mapNotNull { range ->
			if (range.end < selMin) {
				range
			} else if (selMax < range.start) {
				range.copy(
					start = range.start + addLength - removedLength,
					end = range.end + addLength - removedLength
				)
			} else {
				var start = range.start
				var end = range.end

				if (removedLength > 0 && selMin < start) {
					if (onDeleteStart(start, end)) return@mapNotNull null

					start -= min(removedLength, start - selMin)
				}

				if (removedLength > 0 && selMin < end) {
					end -= min(removedLength, end - selMin)
				}

				if (addLength > 0 && addStart <= range.start) {
					start += min(addLength, range.start - addStart)
				}

				if (addLength > 0 && shouldExpandSpanOnTextAddition(range, oldSelection.min)) {
					end += addLength
				} else if (addLength > 0 && addStart <= range.end) {
					end += min(addLength, range.end - addStart)
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
	}
}

/**
 * Infers if a text has changed by inspecting [textLengthDelta], [oldSelection] and [newSelection].
 */
internal fun hasTextChanged(
	textLengthDelta: Int,
	oldSelection: TextRange,
	newSelection: TextRange
): Boolean {
	// (0) new selection is expanded -- there is no possible case where text is modified if the new selection is expanded
	if (!newSelection.collapsed) return false

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
 * example, range=[3, 3), cursor=3, the range is empty but the given input returns true
 * because as it is startInclusive therefore a span should expand on text addition.
 */
internal fun shouldExpandSpanOnTextAddition(
	range: ParvenuString.Range<*>,
	cursor: Int
): Boolean {
	return (range.start < cursor && cursor < range.end)
			|| (range.startInclusive && range.start == cursor)
			|| (range.endInclusive && range.end == cursor)
}
