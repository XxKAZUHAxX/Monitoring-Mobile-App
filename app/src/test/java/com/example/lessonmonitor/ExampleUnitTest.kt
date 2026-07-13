package com.example.lessonmonitor

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Sanity-check test proving the `test` source set + JUnit/MockK/Turbine
 * dependencies are wired correctly. Replaced by real ViewModel/Repository
 * tests as each feature milestone lands.
 */
class ExampleUnitTest {
    @Test
    fun additionIsCorrect() {
        assertEquals(4, 2 + 2)
    }
}
