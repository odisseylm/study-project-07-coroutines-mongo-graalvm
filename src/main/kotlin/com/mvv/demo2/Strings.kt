package com.mvv.demo2


fun <S: CharSequence> S.endsWithOneOf(suffixes: Iterable<String>): Boolean = suffixes.any { this.endsWith(it) }
fun <S: CharSequence> S.endsWithOneOf(vararg suffixes: String): Boolean = endsWithOneOf(suffixes.asIterable())
