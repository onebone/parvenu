package me.onebone.parvenu

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle

@Immutable
class ParvenuAnnotatedString(
	val text: String,
	val spanStyles: List<Range<SpanStyle>> = emptyList(),
	val paragraphStyles: List<Range<ParagraphStyle>> = emptyList()
) {
	@Immutable
	data class Range<out T>(
		val item: T,
		val start: Int, val end: Int,
		val startInclusive: Boolean, val endInclusive: Boolean
	) {
		operator fun contains(index: Int): Boolean =
			(start < index || (startInclusive && start == index))
					&& (index < end || (endInclusive && end == index))

		operator fun contains(other: Range<*>): Boolean =
			contains(other.start) && contains(other.end)
	}

	fun copy(
		text: String = this.text,
		spanStyles: List<Range<SpanStyle>> = this.spanStyles,
		paragraphStyles: List<Range<ParagraphStyle>> = this.paragraphStyles
	): ParvenuAnnotatedString = ParvenuAnnotatedString(
		text = text,
		spanStyles = spanStyles,
		paragraphStyles = paragraphStyles
	)
}

fun ParvenuAnnotatedString.toAnnotatedString(): AnnotatedString = AnnotatedString(
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

fun <T> ParvenuAnnotatedString.Range<T>.toAnnotatedStringRange() = AnnotatedString.Range(
	item = item,
	start = start, end = end
)

fun <T> Iterable<ParvenuAnnotatedString.Range<T>>.fillsRange(first: Int, end: Int, block: (T) -> Boolean): Boolean {
	val ranges = filter { block(it.item) }.sortedBy { it.start }
	var leftover = first..end

	for (range in ranges) {
		if (leftover.first < range.start) return false
		if (end <= range.end) return true

		leftover = range.end..end
	}

	return false
}

@PublishedApi internal val NonEmptyRangePredicate: (ParvenuAnnotatedString.Range<*>) -> Boolean = {
	it.start != it.end
}

inline fun <T> List<ParvenuAnnotatedString.Range<T>>.minusSpansInRange(
	start: Int,
	end: Int,
	predicate: (T) -> Boolean
): List<ParvenuAnnotatedString.Range<T>> = flatMap { range ->
	if (!predicate(range.item)) return listOf(range)

	if (start <= range.start && range.end <= end) {
		emptyList()
	} else if (range.start <= start && end <= range.end) {
		// SELECTION:      -----
		// RANGE    :    ----------
		// REMAINDER:    __     ___
		listOf(
			range.copy(end = start),
			range.copy(start = end)
		).filter(NonEmptyRangePredicate)
	} else if (start in range) {
		// SELECTION:     ---------
		// RANGE    : --------
		// REMAINDER: ____
		listOf(range.copy(end = start))
			.filter(NonEmptyRangePredicate)
	} else if (end in range) {
		// SELECTION: ---------
		// RANGE    :      --------
		// REMAINDER:          ____
		listOf(range.copy(start = end, end = range.end))
			.filter(NonEmptyRangePredicate)
	} else {
		listOf(range)
	}
}
