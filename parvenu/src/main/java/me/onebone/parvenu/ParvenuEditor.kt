package me.onebone.parvenu

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.TextFieldValue

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
			val selectionDelta = newSelection.min - oldSelection.min + newSelection.length

			val onlySelectionMoved = addedLength == 0 && selectionDelta != 0

			if (onlySelectionMoved) {
				onValueChange(
					ParvenuEditorValue(
						parvenuString = value.parvenuString,
						selection = it.selection,
						composition = it.composition
					)
				)
			} else {
				val newSpanStyles = if (oldSelection.collapsed) {
					val cursor = oldSelection.start

					value.parvenuString.spanStyles.mapNotNull { range ->
						val shouldExpandSpan = shouldExpandSpanOnTextAddition(range, cursor)

						if (shouldExpandSpan || cursor in range) {
							if (range.end + selectionDelta < range.start) {
								null
							} else if (addedLength < 0 || shouldExpandSpan) {
								range.copy(end = range.end + addedLength)
							} else {
								range
							}
						} else if (cursor <= range.start) {
							// if cursor == range.start, then range.startInclusive == false.
							range.copy(
								start = range.start + addedLength,
								end = range.end + addedLength
							)
						} else if (addedLength < 0) {
							if (range.end + addedLength < range.start) {
								null
							} else {
								range.copy(
									end = range.end + addedLength
								)
							}
						} else {
							range
						}
					}
				} else {
					value.parvenuString.spanStyles.map { range ->
						if (oldSelection.min in range && oldSelection.max in range) {
							range.copy(end = range.end + addedLength)
						} else if (oldSelection.min in range) {
							range.copy(end = oldSelection.min)
						} else if (oldSelection.max in range) {
							range.copy(
								start = oldSelection.max - oldSelection.length,
								end = range.end - oldSelection.length
							)
						} else if (oldSelection.min <= range.start) {
							range.copy(
								start = range.start + addedLength,
								end = range.end + addedLength
							)
						} else {
							range
						}
					}
				}

				onValueChange(
					ParvenuEditorValue(
						parvenuString = ParvenuAnnotatedString(
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

/**
 * Returns `true` if the [range] should expand if a text is added at the [cursor].
 *
 * The behavior is slightly different from that of [ParvenuAnnotatedString.Range.contains], for
 * example, range=[3, 3), cursor=3 returns true because a cursor should expand the span even if
 * the range is empty as it is startInclusive.
 */
internal fun shouldExpandSpanOnTextAddition(
	range: ParvenuAnnotatedString.Range<*>,
	cursor: Int
): Boolean {
	return (range.start < cursor && cursor < range.end)
			|| (range.startInclusive && range.start == cursor)
			|| (range.endInclusive && range.end == cursor)
}
