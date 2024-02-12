package com.mvv.demo2

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort

class GraalVMTest {

    @Test
    fun forGraalVM() {
        val cls = Class.forName("org.springframework.data.domain.Unpaged")
        val instance = cls.getDeclaredConstructor(Sort::class.java)
            .also { it.trySetAccessible() }
            .newInstance(Sort.by("prop1"))
        Assertions.assertThat(instance).isNotNull()
    }

}
