package com.gokuxedition.arrowlogic.domain

import org.junit.Assert.*
import org.junit.Test

class DeterminismTest {
    @Test fun sameInputsProduceSameSeedAndSequence() {
        val level = LevelNumber.parse("10000000000000000000000000000000000001")
        val a = LevelSeed.derive("local-root", 1, level)
        val b = LevelSeed.derive("local-root", 1, level)
        assertEquals(a, b)
        val first = DeterministicRandom(a)
        val second = DeterministicRandom(b)
        repeat(100) { assertEquals(first.nextLong(), second.nextLong()) }
    }

    @Test fun differentLevelsProduceDifferentSeeds() {
        val a = LevelSeed.derive("local-root", 1, LevelNumber.parse("1"))
        val b = LevelSeed.derive("local-root", 1, LevelNumber.parse("2"))
        assertNotEquals(a, b)
    }
}
