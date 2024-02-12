package com.mvv.demo2

import com.mvv.tests.useAssertJSoftAssertions
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Disabled

class BuildToolHelperTest {

    @Test
    @Disabled("for manual testing/debug")
    fun test_getProjectDirectory() { useAssertJSoftAssertions {

        val a = BuildToolHelper.getProjectDirectory(Demo2Application::class)
        assertThat(a.toString()).isEqualTo("ffd")

        val a2 = BuildToolHelper.getProjectDirectory(BuildToolHelperTest::class)
        assertThat(a2.toString()).isEqualTo("ffd")
    } }
}
