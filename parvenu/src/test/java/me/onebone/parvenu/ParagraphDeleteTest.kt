package me.onebone.parvenu

import androidx.compose.ui.text.TextRange
import me.onebone.parvenu.util.inclusiveInclusive
import org.junit.Assert.assertEquals
import org.junit.Test

public class ParagraphDeleteTest {
	@Test
	public fun paragraphDeleteOneCharacter() {
		val oldStyles = listOf(
			inclusiveInclusive(4, 8),
			inclusiveInclusive(3, 7),
			inclusiveInclusive(5, 10),
			inclusiveInclusive(8, 11),
			inclusiveInclusive(7, 15)
		)

		val oldSelection = TextRange(8)
		val newSelection = TextRange(7)

		val expectedStyles = listOf(
			inclusiveInclusive(4, 7),
			inclusiveInclusive(3, 7),
			inclusiveInclusive(5, 9),
			// 8, 11 -> removed
			inclusiveInclusive(7, 14)
		)

		val actualStyles = oldStyles.offsetSpansAccordingToSelectionChange(
			textLengthDelta = -1,
			textChangedInRange = TestTextChanged,
			oldSelection = oldSelection,
			newSelection = newSelection,
			onDeleteStart = ParagraphOnDeleteStart
		)

		assertEquals(expectedStyles, actualStyles)
	}

	@Test
	public fun paragraphDeleteString() {
		val oldStyles = listOf(
			inclusiveInclusive(6, 10),
			inclusiveInclusive(4, 7),
			inclusiveInclusive(3, 6)
		)

		val oldSelection = TextRange(4, 8)
		val newSelection = TextRange(4)

		val expectedStyles = listOf(
			// 6, 10 -> removed
			inclusiveInclusive(4, 4),
			inclusiveInclusive(3, 4)
		)

		val actualStyles = oldStyles.offsetSpansAccordingToSelectionChange(
			textLengthDelta = -4,
			textChangedInRange = TestTextChanged,
			oldSelection = oldSelection,
			newSelection = newSelection,
			onDeleteStart = ParagraphOnDeleteStart
		)

		assertEquals(expectedStyles, actualStyles)
	}

	private companion object {
		val TestTextChanged: (Int, Int) -> Boolean = { _, _ -> error("should not be called") }
	}
}
