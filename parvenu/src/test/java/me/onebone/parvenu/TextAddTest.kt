package me.onebone.parvenu

import androidx.compose.ui.text.TextRange
import org.junit.Assert.assertEquals
import org.junit.Test

public class TextAddTest {
	@Test
	public fun addOneCharacter() {
		val oldSpanStyles = listOf(
			ParvenuString.Range(TestItem, 3, 6, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 1, 3, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 5, 6, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 8, 10, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 4, 5, startInclusive = false, endInclusive = true)
		)

		val oldSelection = TextRange(5)
		val newSelection = TextRange(6)

		val expectedSpanStyles = listOf(
			ParvenuString.Range(TestItem, 3, 7, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 1, 3, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 6, 7, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 9, 11, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 4, 6, startInclusive = false, endInclusive = true)
		)

		val actualSpanStyles = oldSpanStyles.offsetSpansAccordingToSelectionChange(
			textLengthDelta = 1,
			textChanged = TestTextChanged,
			oldSelection = oldSelection,
			newSelection = newSelection,
			onDeleteStart = SpanOnDeleteStart
		)

		assertEquals(expectedSpanStyles, actualSpanStyles)
	}

	@Test
	public fun addString() {
		val oldSpanStyles = listOf(
			ParvenuString.Range(TestItem, 3, 6, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 1, 5, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 5, 9, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 7, 9, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 9, 11, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 8, 12, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 2, 15, startInclusive = false, endInclusive = true)
		)

		val oldSelection = TextRange(5)
		val newSelection = TextRange(9)

		val expectedSpanStyles = listOf(
			ParvenuString.Range(TestItem, 3, 10, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 1, 9, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 9, 13, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 11, 13, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 13, 15, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 12, 16, startInclusive = false, endInclusive = true),
			ParvenuString.Range(TestItem, 2, 19, startInclusive = false, endInclusive = true)
		)

		val actualSpanStyles = oldSpanStyles.offsetSpansAccordingToSelectionChange(
			textLengthDelta = 4,
			textChanged = TestTextChanged,
			oldSelection = oldSelection,
			newSelection = newSelection,
			onDeleteStart = SpanOnDeleteStart
		)

		assertEquals(expectedSpanStyles, actualSpanStyles)
	}

	private companion object {
		val TestItem = object {
			override fun toString(): String = "Item"
		}
		val TestTextChanged: (Int, Int) -> Boolean = { _, _ -> error("should not be called") }
	}
}
