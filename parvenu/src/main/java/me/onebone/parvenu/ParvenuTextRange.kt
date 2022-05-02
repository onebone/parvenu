package me.onebone.parvenu

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextRange

@Immutable
class ParvenuTextRange(
	val range: TextRange,
	val startInclusive: Boolean,
	val endInclusive: Boolean
)

fun ParvenuTextRange.toTextRange() = TextRange(
	start = range.start, end = range.end
)
