package com.mbisecad.fdt

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

/** Standard JUnit entry point for the same checks used by the standalone runner. */
class FdtCoreJUnitTest {
    @Test
    fun `all calculation core checks pass`() {
        val failures = runAll()
        assertTrue(failures.isEmpty(), failures.joinToString("\n"))
    }
}
