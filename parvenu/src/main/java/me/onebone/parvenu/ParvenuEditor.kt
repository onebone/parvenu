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
			val textChanged: (Int, Int) -> Boolean = { start, end ->
				assert(value.parvenuString.text.length == newValue.text.length)

				!value.parvenuString.text.equalsInRange(newValue.text, start, end)
			}

			val textLengthDelta = newValue.text.length - value.parvenuString.text.length
			val newSpanStyles = value.parvenuString.spanStyles.offsetSpansAccordingToSelectionChange(
				textLengthDelta, textChanged,
				value.selection, newValue.selection, SpanOnDeleteStart
			)

			val newParagraphStyles = value.parvenuString.paragraphStyles.offsetSpansAccordingToSelectionChange(
				textLengthDelta, textChanged,
				value.selection, newValue.selection, ParagraphOnDeleteStart
			)

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

private fun String.equalsInRange(other: String, start: Int, end: Int): Boolean {
	for (i in start until end) {
		if (this[i] != other[i]) return false
	}

	return true
}

internal val SpanOnDeleteStart: (start: Int, end: Int) -> Boolean = { start, end ->
	// selection is removing an empty span
	// e.g.) "abc []|def"  (let '[]' be an empty span and '|' be a cursor)
	//   ~~> "abc|def"
	start == end
}

internal val ParagraphOnDeleteStart: (start: Int, end: Int) -> Boolean = { _, _ ->
	// whole paragraph span should be removed if the start of the span is deleted
	// e.g.) "abc[|paragraph]"  (let '[...]' be a paragraph span and '|' be a cursor)
	//   ~~> "abcparagraph"
	true
}

/**
 * Move spans according to text edits. Returns `null` if only a selection has been changed and
 * span ranges remain unchanged.
 *
 * @param textChanged There is a conflict between pasting and selection change where
 *  oldSelection.max == newSelection.min. To mitigate this issue, we infer if only selection has
 *  changed or text has changed by comparing the string values. Although this method has a limitation
 *  where it cannot distinguish if pasted text is the same as the original one, it is not a common
 *  case? Note that the lambda is only called when the old and new length are the same.
 * @param onDeleteStart If deleting a start of the span, the whole span is removed if the lambda
 *  returns `true`. This is needed to switch a strategy between span styles and paragraph styles.
 *  A span styles should be removed only if the range is empty, while a paragraph style should be
 *  removed immediately when the start of the span is deleted.
 */
internal fun <T> List<ParvenuString.Range<T>>.offsetSpansAccordingToSelectionChange(
	textLengthDelta: Int,
	textChanged: (start: Int, end: Int) -> Boolean,
	oldSelection: TextRange,
	newSelection: TextRange,
	onDeleteStart: (start: Int, end: Int) -> Boolean
): List<ParvenuString.Range<T>>? {
	val hasTextChanged = hasTextChanged(textLengthDelta, textChanged, oldSelection, newSelection)

	return if (!hasTextChanged) {
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
				val offset =
					(if (addLength > 0) addLength else 0) - (if (removedLength > 0) removedLength else 0)

				range.copy(
					start = range.start + offset,
					end = range.end + offset
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

				if (addLength > 0) {
					val shouldExpandOnInsertion = shouldExpandSpanOnTextAddition(range, oldSelection.min)

					// the case where selMax < range.start is already filtered above
					if (addEnd <= range.start || !shouldExpandOnInsertion) {
						start += addLength
					}

					// The end offset should shift to the right if
					// (1) cursor is in front the span's end offset or,
					// (2) length of the span itself should expand.
					if (addStart < range.end || shouldExpandOnInsertion) {
						end += addLength
					}
				}

				if (end < start) {
					null
				} else {
					if (range.start == start && range.end == end) {
						range
					} else if (start < range.start && range.end - range.start > 0 && start == end) {
						// ORIGINAL: "ab{c[def]}g" --> [] = span, {} = cursor selection
						// NEW     : "abg"
						null
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
	textChanged: (start: Int, end: Int) -> Boolean,
	oldSelection: TextRange,
	newSelection: TextRange
): Boolean {
	// (0) new selection is expanded -- there is no possible case where text is modified if the new selection is expanded
	if (!newSelection.collapsed) return false

	// (1) text changed -- text length is not the same
	if (textLengthDelta != 0) return true

	// (2) replaced -- texts in [oldSelection] is removed and added by newSelection.max - oldSelection.min
	//   this case also covers batch deletion when newSelection.max - oldSelection.min == 0.

	// e.g.)
	// ORIGINAL: "foo bar baz"
	// OLD     :     ____
	// NEW     :           |
	// REPLACED: "foohellowbaz"
	//  IMPLIES: ------------
	// ADDED   :     <---->
	// REMOVED :     <-->
	if (-oldSelection.length + newSelection.max - oldSelection.min == textLengthDelta
		&& textChanged(oldSelection.min, newSelection.max)) {
		return true
	}

	// (3) collapsed -- collapsed to collapsed selection with cursor moving forward
	if (oldSelection.collapsed && newSelection.collapsed
		&& textLengthDelta == newSelection.start - oldSelection.start) {
		return true
	}

	return false
}

/**
 * Returns `true` if the [range] should increase its length if a text is added at the [cursor].
 *
 * For example, consider the case where range = (3, 5].
 * 1) If cursor was at 3 and inserted text, then the span should not increase its length, but
 *   only shift to the right because it is NOT start inclusive.
 * 2) If cursor was at 4 and inserted text, then the span should increase its length, because
 *   the text was inserted in the middle of the span.
 * 3) If cursor was at 5 and inserted text, then the span should increase its length, because
 *   the span is end inclusive.
 *
 * The behavior is slightly different from that of [ParvenuString.Range.contains], for
 * example, range=[3, 3), cursor=3, the range is empty but the given input returns true
 * because as it is startInclusive therefore a span should expand on text insertion.
 */
internal fun shouldExpandSpanOnTextAddition(
	range: ParvenuString.Range<*>,
	cursor: Int
): Boolean {
	return (range.start < cursor && cursor < range.end)
			|| (range.startInclusive && range.start == cursor)
			|| (range.endInclusive && range.end == cursor)
}
