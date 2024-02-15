package com.mvv.gradle.util


fun CharSequence?.isNotBlank(): Boolean = !this.isNullOrBlank()
fun sysProp(key: String): String? = System.getProperty(key)
fun isSysPropNotBlank(key: String): Boolean = sysProp(key).isNotBlank()
fun <T> Collection<T>.containsOneOf(vararg values: T): Boolean = values.any { it in this }


