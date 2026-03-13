package com.nocloudchat.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RateLimiterTest {

    /**
     * Creates a [RateLimiter] whose clock is backed by a mutable [time] variable so
     * tests can advance time without sleeping. Returns the limiter and a `setTime` function.
     */
    private fun mutableLimiter(
        max: Int = 3,
        windowMs: Long = 10_000L,
    ): Pair<RateLimiter, (Long) -> Unit> {
        var time = 0L
        val rl = RateLimiter(maxCount = max, windowMs = windowMs, clock = { time })
        return rl to { t: Long -> time = t }
    }

    @Test
    fun `first call for a new key is never limited`() {
        val (rl, _) = mutableLimiter()
        assertFalse(rl.isLimited("192.168.1.1"))
    }

    @Test
    fun `calls up to maxCount within window are allowed`() {
        val (rl, setTime) = mutableLimiter(max = 3)
        setTime(1_000L)
        assertFalse(rl.isLimited("ip"))
        assertFalse(rl.isLimited("ip"))
        assertFalse(rl.isLimited("ip"))
    }

    @Test
    fun `call exceeding maxCount within window is limited`() {
        val (rl, setTime) = mutableLimiter(max = 3)
        setTime(1_000L)
        repeat(3) { rl.isLimited("ip") }
        assertTrue(rl.isLimited("ip"))
    }

    @Test
    fun `call after window expires resets the counter`() {
        val (rl, setTime) = mutableLimiter(max = 2, windowMs = 5_000L)
        setTime(1_000L)
        repeat(2) { rl.isLimited("ip") }
        // Exhaust the limit
        assertTrue(rl.isLimited("ip"))

        // Advance beyond the window
        setTime(7_000L)
        assertFalse(rl.isLimited("ip"))
    }

    @Test
    fun `different keys have independent counters`() {
        val (rl, setTime) = mutableLimiter(max = 2)
        setTime(1_000L)
        repeat(2) { rl.isLimited("ip-A") }
        // ip-A is now limited, but ip-B should be fresh
        assertTrue(rl.isLimited("ip-A"))
        assertFalse(rl.isLimited("ip-B"))
    }

    @Test
    fun `prune removes entries older than the given cutoff`() {
        val (rl, setTime) = mutableLimiter(max = 5, windowMs = 10_000L)
        setTime(1_000L)
        rl.isLimited("old-ip")    // window started at t=1000

        setTime(15_000L)
        rl.isLimited("new-ip")   // window started at t=15000

        // Prune everything older than t=10000
        rl.prune(before = 10_000L)

        // old-ip was pruned — next call should start a fresh window, not be limited
        assertFalse(rl.isLimited("old-ip"))
        // new-ip was not pruned — its counter carries over (count=2, still allowed)
        assertFalse(rl.isLimited("new-ip"))
    }

    @Test
    fun `rate limit boundary - exactly maxCount allowed, maxCount+1 blocked`() {
        val (rl, setTime) = mutableLimiter(max = 5)
        setTime(0L)
        val results = (1..6).map { rl.isLimited("ip") }
        // First 5 allowed, 6th blocked
        assertEquals(listOf(false, false, false, false, false, true), results)
    }
}
