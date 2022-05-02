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
			val selectionDelta = newSelection.start - oldSelection.start + newSelection.length

			val selectionMoved = addedLength == 0 && selectionDelta != 0

			val newSpanStyles: List<ParvenuAnnotatedString.Range<SpanStyle>>

			if (oldSelection.collapsed) {
				newSpanStyles = if (selectionMoved) {
					value.parvenuString.spanStyles
				} else {
					val cursor = oldSelection.start

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
						if (oldSelection.start in range && oldSelection.end in range) {
							range.copy(end = range.end + addedLength)
						} else if (oldSelection.start in range) {
							range.copy(end = oldSelection.start)
						} else if (oldSelection.end in range) {
							range.copy(
								start = oldSelection.end - oldSelection.length,
								end = range.end - oldSelection.length
							)
						} else if (oldSelection.start <= range.start) {
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
	)
}
