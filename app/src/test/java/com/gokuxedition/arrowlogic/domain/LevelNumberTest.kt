package com.gokuxedition.arrowlogic.domain

import org.junit.Assert.*
import org.junit.Test

class LevelNumberTest {
    @Test fun incrementsBeyondLongRange() {
        val level = LevelNumber.parse("9223372036854775808")
        assertEquals("9223372036854775809", level.next().decimal)
    }

    @Test fun canonicalizesLeadingZeroes() {
        assertEquals("12", LevelNumber.parse("00012").decimal)
    }
}
