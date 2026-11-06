package com.gokuxedition.arrowlogic.domain

import java.math.BigInteger

/**
 * Positive arbitrary-precision level index. Persist as canonical decimal TEXT, never Int/Long.
 * This avoids a numeric representation ceiling becoming a designed campaign endpoint.
 */
@JvmInline
value class LevelNumber private constructor(val decimal: String) : Comparable<LevelNumber> {
    val asBigInteger: BigInteger get() = BigInteger(decimal)

    override fun compareTo(other: LevelNumber): Int = asBigInteger.compareTo(other.asBigInteger)
    fun next(): LevelNumber = of(asBigInteger + BigInteger.ONE)

    companion object {
        fun of(value: BigInteger): LevelNumber {
            require(value.signum() > 0) { "Level number must be positive" }
            return LevelNumber(value.toString())
        }

        fun parse(value: String): LevelNumber {
            require(value.isNotBlank() && value.all(Char::isDigit)) { "Level must be a positive decimal integer" }
            return of(BigInteger(value))
        }

        val FIRST: LevelNumber = of(BigInteger.ONE)
    }
}
