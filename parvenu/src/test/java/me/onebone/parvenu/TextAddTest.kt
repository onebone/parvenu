package me.onebone.parvenu

import androidx.compose.ui.text.TextRange
import me.onebone.parvenu.util.exclusiveInclusive
import org.junit.Assert.assertEquals
import org.junit.Test

public class TextAddTest {
	@Test
	public fun spanAddOneCharacter() {
		val oldSpanStyles = listOf(
			exclusiveInclusive(3, 6),
			exclusiveInclusive(1, 3),
			exclusiveInclusive(5, 6),
			exclusiveInclusive(5, 5),
			exclusiveInclusive(8, 10),
			exclusiveInclusive(4, 5)
		)

		val oldSelection = TextRange(5)
		val newSelection = TextRange(6)

		val expectedSpanStyles = listOf(
			exclusiveInclusive(3, 7),
			exclusiveInclusive(1, 3),
			exclusiveInclusive(6, 7),
			exclusiveInclusive(5, 6),
			exclusiveInclusive(9, 11),
			exclusiveInclusive(4, 6)
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
	public fun spanAddString() {
		val oldSpanStyles = listOf(
			exclusiveInclusive(3, 6),
			exclusiveInclusive(1, 5),
			exclusiveInclusive(5, 9),
			exclusiveInclusive(7, 9),
			exclusiveInclusive(9, 11),
			exclusiveInclusive(8, 12),
			exclusiveInclusive(2, 15)
		)

		val oldSelection = TextRange(5)
		val newSelection = TextRange(9)

		val expectedSpanStyles = listOf(
			exclusiveInclusive(3, 10),
			exclusiveInclusive(1, 9),
			exclusiveInclusive(9, 13),
			exclusiveInclusive(11, 13),
			exclusiveInclusive(13, 15),
			exclusiveInclusive(12, 16),
			exclusiveInclusive(2, 19)
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
		val TestTextChanged: (Int, Int) -> Boolean = { _, _ -> error("should not be called") }
	}
}
