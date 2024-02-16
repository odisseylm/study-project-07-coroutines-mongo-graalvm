package com.mvv.gradle.util


fun CharSequence?.isNotBlank(): Boolean = !this.isNullOrBlank()
fun sysProp(key: String): String? = System.getProperty(key)
fun isSysPropNotBlank(key: String): Boolean = sysProp(key).isNotBlank()
fun <T> Collection<T>.containsOneOf(vararg values: T): Boolean = values.any { it in this }


fun <T> Any.getField(fieldName: String): T {
    return try { getFieldImpl<T>(this::class.java, fieldName) }
    catch (ex: NoSuchFieldException) {
        val superclass = this::class.java.superclass
        if (superclass != Any::class.java) getFieldImpl<T>(superclass, fieldName)
        else throw ex
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> Any.getFieldImpl(cls: Class<*>, fieldName: String): T =
    cls.getDeclaredField(fieldName)
        .also { it.trySetAccessible() }
        .get(this) as T

