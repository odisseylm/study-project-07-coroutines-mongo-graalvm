package com.mvv.demo2

//import org.springframework.data.domain.Sort
import org.springframework.util.ClassUtils

/*
fun forGraalVM() {
    run {
    }
}
*/

fun loadClass1(className: String): Class<*> {
    val cls = Class.forName(className)
    Class.forName(className, false, cls.classLoader)
    Class.forName(className, true, cls.classLoader)
    ClassUtils.forName(className, cls.classLoader)
    ClassUtils.resolveClassName(className, cls.classLoader)
    return cls
}
