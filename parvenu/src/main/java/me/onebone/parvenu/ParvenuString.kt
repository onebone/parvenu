package me.onebone.parvenu

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle

@Immutable
public class ParvenuString(
	public val text: String,
	public val spanStyles: List<Range<SpanStyle>> = emptyList(),
	public val paragraphStyles: List<Range<ParagraphStyle>> = emptyList()
) {
	init {
		spanStyles.forEach { range ->
			require(0 <= range.start && range.end <= text.length) {
				"span style is out of boundary (style=${range.toReadableString()}, text length=${text.length})"
			}
		}

		paragraphStyles.forEach { range ->
			require(0 <= range.start && range.end <= text.length) {
				"paragraph style is out of boundary (style=${range.toReadableString()}, text length=${text.length})"
			}
		}
	}

	@Immutable
	public data class Range<out T>(
		val item: T,
		val start: Int, val end: Int,
		val startInclusive: Boolean, val endInclusive: Boolean
	) {
		init {
			require(start <= end) {
				"invalid range ($start > $end)"
			}
		}

		public operator fun contains(index: Int): Boolean =
			(start < index || (startInclusive && start == index))
					&& (index < end || (endInclusive && end == index))

		public operator fun contains(other: Range<*>): Boolean =
			contains(other.start) && contains(other.end)
	}

	public fun copy(
		text: String = this.text,
		spanStyles: List<Range<SpanStyle>> = this.spanStyles,
		paragraphStyles: List<Range<ParagraphStyle>> = this.paragraphStyles
	): ParvenuString = ParvenuString(
		text = text,
		spanStyles = spanStyles,
		paragraphStyles = paragraphStyles
	)
}

internal fun ParvenuString.Range<*>.toReadableString(): String =
	"${if (startInclusive) '[' else '('}$start..$end${if (endInclusive) ']' else ')'}"

public fun ParvenuString.toAnnotatedString(): AnnotatedString = AnnotatedString(
	text = text,
	spanStyles = spanStyles.mapNotNull {
		if (it.start == it.end) {
			// AnnotatedString might behave wrongly if there are some zero-length spans
			null
		} else {
			it.toAnnotatedStringRange()
		}
	},
	paragraphStyles = paragraphStyles.map { it.toAnnotatedStringRange() }
)

public fun <T> ParvenuString.Range<T>.toAnnotatedStringRange(): AnnotatedString.Range<T> = AnnotatedString.Range(
	item = item,
	start = start, end = end
)

public fun <T> Iterable<ParvenuString.Range<T>>.fillsRange(start: Int, end: Int, block: (T) -> Boolean): Boolean {
	val ranges = filter { block(it.item) }.sortedBy { it.start }
	var leftover = start..end

	for (range in ranges) {
		if (range.end < leftover.first) continue

		if (leftover.first < range.start) return false
		if (end <= range.end) return true

		leftover = range.end..end
	}

	return false
}

@PublishedApi internal val NonEmptyRangePredicate: (ParvenuString.Range<*>) -> Boolean = {
	it.start != it.end
}

/**
 * Removes spans in range [[start], [endExclusive]).
 */
public inline fun <T> List<ParvenuString.Range<T>>.minusSpansInRange(
	start: Int,
	endExclusive: Int,
	predicate: (T) -> Boolean
): List<ParvenuString.Range<T>> = flatMap { range ->
	if (!predicate(range.item)) return@flatMap listOf(range)

	if (start <= range.start && range.end < endExclusive) {
		emptyList()
	} else if (range.start <= start && endExclusive <= range.end) {
		// SELECTION:      -----
		// RANGE    :    ----------
		// REMAINDER:    __     ___
		listOf(
			range.copy(end = start, endInclusive = false),
			range.copy(start = endExclusive)
		).filter(NonEmptyRangePredicate)
	} else if (start in range) {
		// SELECTION:     ---------
		// RANGE    : --------
		// REMAINDER: ____
		listOf(range.copy(end = start, endInclusive = false))
			.filter(NonEmptyRangePredicate)
	} else if (endExclusive in range) {
		// SELECTION: ---------
		// RANGE    :      --------
		// REMAINDER:          ____
		listOf(range.copy(start = endExclusive))
			.filter(NonEmptyRangePredicate)
	} else {
		listOf(range)
	}
}
