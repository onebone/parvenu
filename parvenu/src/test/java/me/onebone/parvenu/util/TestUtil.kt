package me.onebone.parvenu.util

import me.onebone.parvenu.ParvenuString

private val TestSpanItem = object {
	override fun toString(): String = "Item"
}

public fun exclusiveInclusive(start: Int, end: Int): ParvenuString.Range<*> = ParvenuString.Range(
	item = TestSpanItem,
	start = start, end = end,
	startInclusive = false, endInclusive = true
)

public fun exclusiveExclusive(start: Int, end: Int): ParvenuString.Range<*> = ParvenuString.Range(
	item = TestSpanItem,
	start = start, end = end,
	startInclusive = false, endInclusive = false
)

public fun inclusiveExclusive(start: Int, end: Int): ParvenuString.Range<*> = ParvenuString.Range(
	item = TestSpanItem,
	start = start, end = end,
	startInclusive = true, endInclusive = false
)

public fun inclusiveInclusive(start: Int, end: Int): ParvenuString.Range<*> = ParvenuString.Range(
	item = TestSpanItem,
	start = start, end = end,
	startInclusive = true, endInclusive = true
)
