package com.nocloudchat.network

import java.util.concurrent.ConcurrentHashMap

private data class RateEntry(val count: Int, val windowStart: Long)

/**
 * Thread-safe sliding-window rate limiter.
 *
 * @param maxCount  Maximum number of allowed calls within [windowMs].
 * @param windowMs  Length of the sliding window in milliseconds.
 * @param clock     Injectable time source; defaults to [System.currentTimeMillis].
 */
internal class RateLimiter(
    private val maxCount: Int = 5,
    private val windowMs: Long = 10_000L,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val map = ConcurrentHashMap<String, RateEntry>()

    /**
     * Returns `true` if [key] has exceeded the rate limit and the call should be dropped.
     * Records the call if it is within the limit.
     */
    fun isLimited(key: String): Boolean {
        val now = clock()
        val entry = map[key]
        return if (entry == null || now - entry.windowStart > windowMs) {
            map[key] = RateEntry(1, now)
            false
        } else if (entry.count >= maxCount) {
            true
        } else {
            map[key] = entry.copy(count = entry.count + 1)
            false
        }
    }

    /** Removes entries whose window started before [before] (used during periodic cleanup). */
    fun prune(before: Long) {
        map.entries.removeIf { it.value.windowStart < before }
    }
}
