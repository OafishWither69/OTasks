// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app

import androidx.compose.ui.graphics.Color
import com.otasks.app.ui.theme.DarkColors
import com.otasks.app.ui.theme.ItemDark
import com.otasks.app.ui.theme.ItemLight
import com.otasks.app.ui.theme.LightColors
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class PaletteDistanceTest {
    @Test fun paletteColoursAreDistinctFromEachOtherAndErrors() {
        listOf(ItemLight to LightColors, ItemDark to DarkColors).forEach { (palette, theme) ->
            palette.forEachIndexed { index, color ->
                listOf(theme.danger, theme.dangerText).forEach { error ->
                    assertTrue("danger distance $index: ${delta(color,error)}", delta(color, error) >= 12)
                }
                palette.drop(index + 1).forEach { other ->
                    assertTrue("palette distance $index: ${delta(color,other)}", delta(color, other) >= 10)
                }
            }
        }
    }

    private fun lab(c: Color): DoubleArray {
        fun linear(v: Float): Double = if (v <= 0.04045) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        val r = linear(c.red)
        val g = linear(c.green)
        val b = linear(c.blue)

        fun f(v: Double): Double = if (v > 216.0 / 24389) v.pow(1.0 / 3) else (24389.0 / 27 * v + 16) / 116
        val x = f((0.4124564 * r + 0.3575761 * g + 0.1804375 * b) / 0.95047)
        val y = f(0.2126729 * r + 0.7151522 * g + 0.0721750 * b)
        val z = f((0.0193339 * r + 0.1191920 * g + 0.9503041 * b) / 1.08883)
        return doubleArrayOf(116 * y - 16, 500 * (x - y), 200 * (y - z))
    }

    // CIEDE2000, D65 sRGB; the reference-pair check guards the implementation.
    private fun delta(
        first: Color,
        second: Color,
    ): Double = distance(lab(first), lab(second))

    private fun distance(
        x: DoubleArray,
        y: DoubleArray,
    ): Double {
        val c1 = hypot(x[1], x[2])
        val c2 = hypot(y[1], y[2])
        val cm = (c1 + c2) / 2
        val g = 0.5 * (1 - sqrt(cm.pow(7) / (cm.pow(7) + 25.0.pow(7))))
        val a1 = (1 + g) * x[1]
        val a2 = (1 + g) * y[1]
        val cp1 = hypot(a1, x[2])
        val cp2 = hypot(a2, y[2])

        fun hue(
            a: Double,
            b: Double,
        ): Double = if (a == 0.0 && b == 0.0) 0.0 else (Math.toDegrees(atan2(b, a)) + 360) % 360
        val h1 = hue(a1, x[2])
        val h2 = hue(a2, y[2])
        val dh =
            if (cp1 * cp2 ==
                0.0
            ) {
                0.0
            } else if (abs(h2 - h1) <= 180) {
                h2 - h1
            } else if (h2 - h1 > 180) {
                h2 - h1 - 360
            } else {
                h2 - h1 + 360
            }
        val dl = y[0] - x[0]
        val dc = cp2 - cp1
        val dhp = 2 * sqrt(cp1 * cp2) * sin(Math.toRadians(dh / 2))
        val lm = (x[0] + y[0]) / 2
        val cpm = (cp1 + cp2) / 2
        val hm =
            if (cp1 * cp2 == 0.0) {
                h1 + h2
            } else if (abs(h1 - h2) <= 180) {
                (h1 + h2) / 2
            } else if (h1 + h2 < 360) {
                (h1 + h2 + 360) / 2
            } else {
                (h1 + h2 - 360) / 2
            }

        fun cosDeg(v: Double) = cos(Math.toRadians(v))
        val t = 1 - 0.17 * cosDeg(hm - 30) + 0.24 * cosDeg(2 * hm) + 0.32 * cosDeg(3 * hm + 6) - 0.20 * cosDeg(4 * hm - 63)
        val sl = 1 + 0.015 * (lm - 50).pow(2) / sqrt(20 + (lm - 50).pow(2))
        val sc = 1 + 0.045 * cpm
        val sh = 1 + 0.015 * cpm * t
        val rt = -2 * sqrt(cpm.pow(7) / (cpm.pow(7) + 25.0.pow(7))) * sin(Math.toRadians(60 * exp(-((hm - 275) / 25).pow(2))))
        return sqrt((dl / sl).pow(2) + (dc / sc).pow(2) + (dhp / sh).pow(2) + rt * (dc / sc) * (dhp / sh))
    }

    @Test fun publishedReferencePair() {
        org.junit.Assert.assertEquals(2.0425, distance(doubleArrayOf(50.0, 2.6772, -79.7751), doubleArrayOf(50.0, 0.0, -82.7485)), 0.0001)
    }
}
