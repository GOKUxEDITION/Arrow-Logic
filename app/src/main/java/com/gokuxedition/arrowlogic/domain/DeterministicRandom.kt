package com.gokuxedition.arrowlogic.domain

/** Versioned SplitMix64 generator. Do not change its algorithm without incrementing generatorVersion. */
class DeterministicRandom(seed: Long) {
    private var state: Long = seed

    fun nextLong(): Long {
        state += 0x9E3779B97F4A7C15UL.toLong()
        var z = state
        z = (z xor (z ushr 30)) * 0xBF58476D1CE4E5B9UL.toLong()
        z = (z xor (z ushr 27)) * 0x94D049BB133111EBUL.toLong()
        return z xor (z ushr 31)
    }

    fun nextInt(bound: Int): Int {
        require(bound > 0)
        return ((nextLong() ushr 1) % bound.toLong()).toInt()
    }

    fun nextBoolean(): Boolean = (nextLong() and 1L) == 1L
}
