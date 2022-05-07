package me.onebone.parvenu

import androidx.compose.ui.text.TextRange
import me.onebone.parvenu.util.inclusiveExclusive
import org.junit.Assert.assertNull
import org.junit.Test

/*
 * Test cases where text remains unchanged while selection is changed
 */
public class SelectionMovementTest {
	@Test
	public fun oneSelectionForward() {
		val oldSpanStyles = listOf(
			inclusiveExclusive(3, 6),
			inclusiveExclusive(5, 8),
			inclusiveExclusive(1, 4),
			inclusiveExclusive(5, 6)
		)

		val textLengthDelta = 0

		val oldSelection = TextRange(5)
		val newSelection = TextRange(6)

		val actualSpanStyles = oldSpanStyles.offsetSpansAccordingToSelectionChange(
			textLengthDelta, TextChangedLambda, oldSelection, newSelection, SpanOnDeleteStart
		)

		assertNull(actualSpanStyles)
	}

	@Test
	public fun oneSelectionBehind() {
		val oldSpanStyles = listOf(
			inclusiveExclusive(3, 6),
			inclusiveExclusive(5, 8),
			inclusiveExclusive(1, 4),
			inclusiveExclusive(4, 5)
		)

		val textLengthDelta = 0

		val oldSelection = TextRange(5)
		val newSelection = TextRange(4)

		val actualSpanStyles = oldSpanStyles.offsetSpansAccordingToSelectionChange(
			textLengthDelta, TextChangedLambda, oldSelection, newSelection, SpanOnDeleteStart
		)

		assertNull(actualSpanStyles)
	}

	@Test
	public fun transitionExpandToCollapsed() {
		val oldSpanStyles = listOf(
			inclusiveExclusive(3, 6),
			inclusiveExclusive(5, 8),
			inclusiveExclusive(1, 4),
			inclusiveExclusive(4, 5)
		)

		val textLengthDelta = 0

		val oldSelection = TextRange(3, 6)
		val newSelection = TextRange(6)

		val actualSpanStyles = oldSpanStyles.offsetSpansAccordingToSelectionChange(
			textLengthDelta, TextChangedLambda, oldSelection, newSelection, SpanOnDeleteStart
		)

		assertNull(actualSpanStyles)
	}

	private companion object {
		val TextChangedLambda: (Int, Int) -> Boolean = { _, _ -> false }
	}
}
