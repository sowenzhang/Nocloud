package com.nocloudchat

import com.nocloudchat.network.formatNetworkDisplay
import com.nocloudchat.state.formatFileSize
import kotlin.test.Test
import kotlin.test.assertEquals

class FormattersTest {

    // ── formatFileSize ────────────────────────────────────────────────────────

    @Test
    fun `formatFileSize formats bytes correctly`() {
        assertEquals("0 B", formatFileSize(0))
        assertEquals("1 B", formatFileSize(1))
        assertEquals("1023 B", formatFileSize(1_023))
    }

    @Test
    fun `formatFileSize formats kilobytes correctly`() {
        assertEquals("1 KB", formatFileSize(1_024))
        assertEquals("1 KB", formatFileSize(1_025))   // integer division
        assertEquals("1023 KB", formatFileSize(1_047_552))
    }

    @Test
    fun `formatFileSize formats megabytes correctly`() {
        assertEquals("1.0 MB", formatFileSize(1_048_576))
        assertEquals("1.5 MB", formatFileSize(1_572_864))
        assertEquals("10.0 MB", formatFileSize(10_485_760))
    }

    @Test
    fun `formatFileSize formats gigabytes correctly`() {
        assertEquals("1.0 GB", formatFileSize(1_073_741_824))
        assertEquals("2.5 GB", formatFileSize(2_684_354_560))
    }

    @Test
    fun `formatFileSize boundary — 1024 switches from B to KB`() {
        assertEquals("1023 B", formatFileSize(1_023))
        assertEquals("1 KB", formatFileSize(1_024))
    }

    @Test
    fun `formatFileSize boundary — 1048576 switches from KB to MB`() {
        assertEquals("1023 KB", formatFileSize(1_047_552))
        assertEquals("1.0 MB", formatFileSize(1_048_576))
    }

    @Test
    fun `formatFileSize boundary — 1073741824 switches from MB to GB`() {
        assertEquals("1024.0 MB", formatFileSize(1_073_741_823))
        assertEquals("1.0 GB", formatFileSize(1_073_741_824))
    }

    // ── formatNetworkDisplay ──────────────────────────────────────────────────

    @Test
    fun `formatNetworkDisplay returns 'Unknown network' for null`() {
        assertEquals("Unknown network", formatNetworkDisplay(null))
    }

    @Test
    fun `formatNetworkDisplay formats CIDR with last octet replaced by x`() {
        assertEquals("192.168.1.x", formatNetworkDisplay("192.168.1.0/24"))
    }

    @Test
    fun `formatNetworkDisplay formats different CIDR subnets`() {
        assertEquals("10.0.0.x", formatNetworkDisplay("10.0.0.0/8"))
        assertEquals("172.16.0.x", formatNetworkDisplay("172.16.0.0/12"))
    }

    @Test
    fun `formatNetworkDisplay returns SSID as-is when no slash present`() {
        assertEquals("MyHomeNetwork", formatNetworkDisplay("MyHomeNetwork"))
        assertEquals("Office WiFi", formatNetworkDisplay("Office WiFi"))
    }

    @Test
    fun `formatNetworkDisplay returns empty SSID as-is`() {
        assertEquals("", formatNetworkDisplay(""))
    }
}
