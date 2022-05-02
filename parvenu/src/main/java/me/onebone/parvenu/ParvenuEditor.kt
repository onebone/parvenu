package me.onebone.parvenu

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun ParvenuEditor(
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
			val selectionDelta = newSelection.start - oldSelection.range.start + newSelection.length

			val selectionMoved = addedLength == 0 && selectionDelta != 0

			val newSpanStyles: List<ParvenuAnnotatedString.Range<SpanStyle>>

			if (oldSelection.range.collapsed) {
				newSpanStyles = if (selectionMoved) {
					value.parvenuString.spanStyles
				} else {
					val cursor = oldSelection.range.start

					value.parvenuString.spanStyles.map { range ->
						if (cursor in range) {
							range.copy(end = range.end + selectionDelta)
						} else if (cursor <= range.start) {
							// if cursor == range.start, then range.startInclusive == false.
							range.copy(
								start = range.start + addedLength,
								end = range.end + addedLength
							)
						} else {
							range
						}
					}
				}
			} else {
				newSpanStyles = if (selectionMoved) {
					value.parvenuString.spanStyles
				} else {
					value.parvenuString.spanStyles.map { range ->
						if (oldSelection.range.start in range && oldSelection.range.end in range) {
							range.copy(end = range.end + addedLength)
						} else if (oldSelection.range.start in range) {
							range.copy(end = oldSelection.range.start)
						} else if (oldSelection.range.end in range) {
							range.copy(
								start = oldSelection.range.end - oldSelection.range.length,
								end = range.end - oldSelection.range.length
							)
						} else if (oldSelection.range.start <= range.start) {
							range.copy(
								start = range.start + addedLength,
								end = range.end + addedLength
							)
						} else {
							range
						}
					}
				}
			}

			val newParvenuSelection = ParvenuTextRange(
				range = newSelection,
				startInclusive = oldSelection.startInclusive,
				endInclusive = oldSelection.endInclusive
			)

			onValueChange(
				ParvenuEditorValue(
					parvenuString = ParvenuAnnotatedString(
						text = it.text,
						spanStyles = newSpanStyles,
						paragraphStyles = emptyList() // TODO
					),
					selection = newParvenuSelection,
					composition = it.composition
				)
			)
		}
	)
}
