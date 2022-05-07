package me.onebone.parvenu

import androidx.compose.ui.text.TextRange
import me.onebone.parvenu.util.exclusiveInclusive
import org.junit.Assert.assertEquals
import org.junit.Test

public class TextDeleteTest {
	@Test
	public fun spanDeleteOneCharacter() {
		val oldSpanStyles = listOf(
			exclusiveInclusive(4, 8),
			exclusiveInclusive(5, 8),
			exclusiveInclusive(1, 4),
			exclusiveInclusive(0, 2),
			exclusiveInclusive(3, 6),
			exclusiveInclusive(4, 5),
			exclusiveInclusive(3, 5)
		)

		val oldSelection = TextRange(5)
		val newSelection = TextRange(4)

		val expectedSpanStyles = listOf(
			exclusiveInclusive(4, 7),
			exclusiveInclusive(4, 7),
			exclusiveInclusive(1, 4),
			exclusiveInclusive(0, 2),
			exclusiveInclusive(3, 5),
			exclusiveInclusive(4, 4),
			exclusiveInclusive(3, 4)
		)

		val actualSpanStyles = oldSpanStyles.offsetSpansAccordingToSelectionChange(
			textLengthDelta = -1,
			textChanged = TestTextChanged,
			oldSelection = oldSelection,
			newSelection = newSelection,
			onDeleteStart = SpanOnDeleteStart
		)

		assertEquals(expectedSpanStyles, actualSpanStyles)
	}

	@Test
	public fun spanDeleteString() {
		val oldSpanStyles = listOf(
			exclusiveInclusive(4, 8),
			exclusiveInclusive(5, 8),
			exclusiveInclusive(7, 10),
			exclusiveInclusive(9, 11),
			exclusiveInclusive(5, 7),
			exclusiveInclusive(4, 7),
			exclusiveInclusive(2, 7),
			exclusiveInclusive(5, 6),
			exclusiveInclusive(1, 4),
			exclusiveInclusive(0, 2),
			exclusiveInclusive(3, 6),
			exclusiveInclusive(4, 5),
			exclusiveInclusive(3, 5)
		)

		val oldSelection = TextRange(7)
		val newSelection = TextRange(4)

		val expectedSpanStyles = listOf(
			exclusiveInclusive(4, 5),
			exclusiveInclusive(4, 5),
			exclusiveInclusive(4, 7),
			exclusiveInclusive(6, 8),
			// removed -> 5, 7
			exclusiveInclusive(4, 4),
			exclusiveInclusive(2, 4),
			// removed -> 5, 6
			exclusiveInclusive(1, 4),
			exclusiveInclusive(0, 2),
			exclusiveInclusive(3, 4),
			exclusiveInclusive(4, 4),
			exclusiveInclusive(3, 4)
		)

		val actualSpanStyles = oldSpanStyles.offsetSpansAccordingToSelectionChange(
			textLengthDelta = -3,
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
