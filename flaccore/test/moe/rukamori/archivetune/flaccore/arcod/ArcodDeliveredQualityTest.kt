package moe.rukamori.archivetune.flaccore.arcod

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/*
 * Ported from Stash (GPL-3.0)
 * Original source: com.stash.core.media.streaming.ArcodDeliveredQualityTest.kt
 */

class ArcodDeliveredQualityTest {

    @Test fun `MAX with 24 over 88_2 master delivers 24 over 88200`() {
        val d = ArcodDeliveredQuality.of(qobuzCode = 27, maxBitDepth = 24, maxSamplingRateKhz = 88.2)
        assertEquals(24, d.bitsPerSample)
        assertEquals(88_200, d.sampleRateHz)
    }

    @Test fun `MAX with CD-only master delivers 16 over 44100`() {
        val d = ArcodDeliveredQuality.of(qobuzCode = 27, maxBitDepth = 16, maxSamplingRateKhz = 44.1)
        assertEquals(16, d.bitsPerSample)
        assertEquals(44_100, d.sampleRateHz)
    }

    @Test fun `CD tier clamps a 24 over 192 master down to 16 over 44100`() {
        val d = ArcodDeliveredQuality.of(qobuzCode = 6, maxBitDepth = 24, maxSamplingRateKhz = 192.0)
        assertEquals(16, d.bitsPerSample)
        assertEquals(44_100, d.sampleRateHz)
    }

    @Test fun `HI_RES tier clamps rate to 96000 but keeps 24 bits`() {
        val d = ArcodDeliveredQuality.of(qobuzCode = 7, maxBitDepth = 24, maxSamplingRateKhz = 192.0)
        assertEquals(24, d.bitsPerSample)
        assertEquals(96_000, d.sampleRateHz)
    }

    @Test fun `null maxBitDepth yields null bits`() {
        val d = ArcodDeliveredQuality.of(qobuzCode = 27, maxBitDepth = null, maxSamplingRateKhz = 88.2)
        assertNull(d.bitsPerSample)
        assertEquals(88_200, d.sampleRateHz)
    }

    @Test fun `null maxSamplingRate yields null rate`() {
        val d = ArcodDeliveredQuality.of(qobuzCode = 27, maxBitDepth = 24, maxSamplingRateKhz = null)
        assertEquals(24, d.bitsPerSample)
        assertNull(d.sampleRateHz)
    }

    @Test fun `both null yields both null`() {
        val d = ArcodDeliveredQuality.of(qobuzCode = 27, maxBitDepth = null, maxSamplingRateKhz = null)
        assertNull(d.bitsPerSample)
        assertNull(d.sampleRateHz)
    }

    @Test fun `unknown code behaves like MAX with a 24 over 192 ceiling`() {
        val d = ArcodDeliveredQuality.of(qobuzCode = 0, maxBitDepth = 24, maxSamplingRateKhz = 192.0)
        assertEquals(24, d.bitsPerSample)
        assertEquals(192_000, d.sampleRateHz)
    }
}
