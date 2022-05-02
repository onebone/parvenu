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
	data class Range<T>(
		val item: T,
		val start: Int, val end: Int,
		val startInclusive: Boolean, val endInclusive: Boolean
	) {
		operator fun contains(index: Int): Boolean =
			(start < index || (startInclusive && start == index))
					&& (index < end || (endInclusive && end == index))
	}
}

fun ParvenuAnnotatedString.toAnnotatedString(): AnnotatedString = AnnotatedString(
	text = text,
	spanStyles = spanStyles.map { it.toAnnotatedStringRange() },
	paragraphStyles = paragraphStyles.map { it.toAnnotatedStringRange() }
)

fun <T> ParvenuAnnotatedString.Range<T>.toAnnotatedStringRange() = AnnotatedString.Range(
	item = item,
	start = start, end = end
)
