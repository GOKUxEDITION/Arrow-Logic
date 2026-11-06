package com.gokuxedition.arrowlogic.domain

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/** Stable seed derivation for arbitrary-precision levels and deterministic duplicate retries. */
object LevelSeed {
    fun derive(
        rootSeed: String,
        generatorVersion: Int,
        level: LevelNumber,
        retryCounter: Int = 0
    ): Long {
        require(rootSeed.isNotBlank())
        require(generatorVersion > 0)
        require(retryCounter >= 0)
        val input = "arrow-logic|$generatorVersion|$rootSeed|${level.decimal}|retry:$retryCounter"
            .toByteArray(StandardCharsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(input)
        return ByteBuffer.wrap(digest, 0, Long.SIZE_BYTES).long
    }
}
