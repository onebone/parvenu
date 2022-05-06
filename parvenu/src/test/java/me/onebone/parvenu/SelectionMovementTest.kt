package me.onebone.parvenu

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import org.junit.Assert.assertNull
import org.junit.Test

/*
 * Test cases where text remains unchanged while selection is changed
 */
public class SelectionMovementTest {
	@Test
	public fun oneSelectionForward() {
		val oldSpanStyles = listOf(
			ParvenuString.Range(TestSpanStyle, 3, 6, startInclusive = true, endInclusive = false),
			ParvenuString.Range(TestSpanStyle, 5, 8, startInclusive = true, endInclusive = false),
			ParvenuString.Range(TestSpanStyle, 1, 4, startInclusive = true, endInclusive = false),
			ParvenuString.Range(TestSpanStyle, 5, 6, startInclusive = true, endInclusive = false)
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
			ParvenuString.Range(TestSpanStyle, 3, 6, startInclusive = true, endInclusive = false),
			ParvenuString.Range(TestSpanStyle, 5, 8, startInclusive = true, endInclusive = false),
			ParvenuString.Range(TestSpanStyle, 1, 4, startInclusive = true, endInclusive = false),
			ParvenuString.Range(TestSpanStyle, 4, 5, startInclusive = true, endInclusive = false)
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
			ParvenuString.Range(TestSpanStyle, 3, 6, startInclusive = true, endInclusive = false),
			ParvenuString.Range(TestSpanStyle, 5, 8, startInclusive = true, endInclusive = false),
			ParvenuString.Range(TestSpanStyle, 1, 4, startInclusive = true, endInclusive = false),
			ParvenuString.Range(TestSpanStyle, 4, 5, startInclusive = true, endInclusive = false)
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
		val TestSpanStyle = SpanStyle()
		val TextChangedLambda: (Int, Int) -> Boolean = { _, _ -> false }
	}
}
