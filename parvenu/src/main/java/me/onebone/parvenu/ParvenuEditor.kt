package me.onebone.parvenu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
	val textFieldValue = remember(value) { value.toTextFieldValue() }

	block(
		value = textFieldValue,
		onValueChange = { newValue ->
			val textChangedInRange: (Int, Int) -> Boolean = { start, end ->
				assert(value.parvenuString.text.length == newValue.text.length) {
					"the lambda should be called only if old and new length is the same"
				}

				!value.parvenuString.text.equalsInRange(newValue.text, start, end)
			}

			val textLengthDelta = newValue.text.length - value.parvenuString.text.length
			val newSpanStyles = value.parvenuString.spanStyles.offsetSpansAccordingToSelectionChange(
				textLengthDelta, textChangedInRange,
				value.selection, newValue.selection, SpanOnDeleteStart
			)

			val newParagraphStyles = value.parvenuString.paragraphStyles.offsetSpansAccordingToSelectionChange(
				textLengthDelta, textChangedInRange,
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
 * @param textChangedInRange There is a conflict between pasting and selection change where
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
	textChangedInRange: (start: Int, end: Int) -> Boolean,
	oldSelection: TextRange,
	newSelection: TextRange,
	onDeleteStart: (start: Int, end: Int) -> Boolean
): List<ParvenuString.Range<T>>? {
	val hasTextChanged = hasTextChanged(textLengthDelta, textChangedInRange, oldSelection, newSelection)

	return if (!hasTextChanged) {
		null
	} else {
		val addStart = oldSelection.min
		val addEnd = newSelection.max
		val addLength = addEnd - addStart

		val selMin = min(addStart, addEnd)
		val selMax = maxOf(addStart, addEnd, oldSelection.max)

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

				var spanLength = range.length -
						intersectLength(oldSelection.min, oldSelection.max, range.start, range.end)

				if (oldSelection.collapsed) {
					if (removedLength > 0 && range.start < oldSelection.max && selMin < range.end) {
						spanLength -= removedLength
					}
				}

				if (removedLength > 0) {
					if (newSelection.min < range.start && range.start <= oldSelection.min) {
						if (onDeleteStart(range.start, range.end)) return@mapNotNull null
					}

					if (selMin < range.start) {
						start = selMin
					}
				}

				if (addLength > 0) {
					if (shouldExpandSpanOnTextAddition(range, oldSelection.min)) {
						spanLength += addLength
					} else if (
						// We should shift the end off set to the right if:
						// 1) a text is being added in front of the span or
						// 2) if text is being inserted at the start of the span, then we should consider if it is start inclusive
						addStart < range.start
						|| (addStart == range.start && !range.startInclusive)
					) {
						start += addLength
					}
				}

				if (spanLength < 0) {
					null
				} else {
					if (range.start == start && range.length == spanLength) {
						range
					} else if (start < range.start && range.length > 0 && spanLength == 0) {
						// ORIGINAL: "ab{c[def]}g" --> [] = span, {} = cursor selection
						// NEW     : "abg"
						null
					} else {
						// if the span is deleted, then make it end inclusive
						// ORIGINAL: "abc(def)|ghi" --> () = exclusive/exclusive span
						// NEW     : "abc(de]|ghi"     --> (] = exclusive/inclusive span
						val endInclusive = removedLength > 0 && oldSelection.max == range.end

						range.copy(start = start, end = start + spanLength, endInclusive = range.endInclusive || endInclusive)
					}
				}
			}
		}
	}
}

/**
 * An intersecting length between [[lStart], [lEnd]) and [[rStart], [rEnd]).
 */
internal fun intersectLength(
	lStart: Int, lEnd: Int,
	rStart: Int, rEnd: Int
): Int {
	if (rStart in lStart until lEnd) {
		return min(rEnd, lEnd) - rStart
	}

	if (lStart in rStart until rEnd) {
		return min(rEnd, lEnd) - lStart
	}

	return 0
}

/**
 * Infers if a text has changed by inspecting [textLengthDelta], [oldSelection] and [newSelection].
 */
internal fun hasTextChanged(
	textLengthDelta: Int,
	textChangedInRange: (start: Int, end: Int) -> Boolean,
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
		&& textChangedInRange(oldSelection.min, newSelection.max)) {
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
