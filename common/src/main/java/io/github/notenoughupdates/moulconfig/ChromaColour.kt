package io.github.notenoughupdates.moulconfig

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.common.IMinecraft
import java.awt.Color
import kotlin.math.abs
import kotlin.math.roundToInt

@Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION")
data class ChromaColour(
    /**
     * Hue in a range from 0 to 1. For a chroma colour this is added to the time as an offset.
     */
    @Expose
    val hue: Float,
    /**
     * Saturation in a range from 0 to 1
     */
    @Expose
    val saturation: Float,
    /**
     * Brightness in a range from 0 to 1
     */
    @Expose
    val brightness: Float,
    /**
     * If set to 0, this indicates a static colour. If set to a value above 0, indicates the amount of milliseconds that pass until the same colour is met again.
     * This value may be saved lossy.
     */
    @Expose
    val timeForFullRotationInMillis: Int,
    /**
     * Alpha in a range from 0 to 255 (with 255 being fully opaque).
     */
    @Expose
    val alpha: Int,
) {

    private fun evaluateColourWithShift(hueShift: Double): Int {
        if (abs(cachedRGBHueOffset - hueShift) < 1 / Timing.HUE_CIRCLE_DEGREES) return cachedRGB
        val effectiveHue = ((hue.toDouble() + hueShift) % 1).toFloat()
        val ret = (Color.HSBtoRGB(effectiveHue, saturation, brightness) and Rgb.MASK) or (alpha shl Rgb.ALPHA_SHIFT)
        cachedRGBHueOffset = hueShift
        cachedRGB = ret
        return ret
    }

    /**
     * The value of [evaluateColourWithShift] at [cachedRGBHueOffset]
     */
    @Transient
    private var cachedRGB: Int = 0

    /**
     * The last queried value of [evaluateColourWithShift].
     */
    @Transient
    private var cachedRGBHueOffset: Double = Double.NaN

    /**
     * @param offset offset the colour by a hue amount.
     * @return the colour, at the current time if this is a chrome colour
     */
    fun getEffectiveColourRGB(offset: Float): Int {
        var effectiveHueOffset = if (timeForFullRotationInMillis > 0) {
            System.currentTimeMillis() / timeForFullRotationInMillis.toDouble()
        } else {
            .0
        }
        effectiveHueOffset += offset
        return evaluateColourWithShift(effectiveHueOffset)
    }

    /**
     * @param offset offset the colour by a hue amount.
     * @return the colour, at the current time if this is a chrome colour
     */
    fun getEffectiveColour(offset: Float): Color = Color(getEffectiveColourRGB(offset), true)

    /**
     * Unlike [getEffectiveColourRGB], this offset does not change anything if not using an animated colour.
     *
     * @param offset offset the colour by a time amount in milliseconds.
     * @return the colour, at the current time if this is a chrome colour
     */
    fun getEffectiveColourWithTimeOffsetRGB(offset: Int): Int {
        if (timeForFullRotationInMillis == 0) return evaluateColourWithShift(.0)
        val effectiveHue = (System.currentTimeMillis() + offset) / timeForFullRotationInMillis.toDouble()
        return evaluateColourWithShift(effectiveHue)
    }

    /**
     * Unlike [getEffectiveColour], this offset does not change anything if not using an animated colour.
     *
     * @param offset offset the colour by a time amount in milliseconds.
     * @return the colour, at the current time if this is a chrome colour
     */
    fun getEffectiveColourWithTimeOffset(offset: Int): Color = Color(getEffectiveColourWithTimeOffsetRGB(offset), true)

    /**
     * @return the colour, at the current time if this is a chrome colour
     */
    fun getEffectiveColourRGB(): Int = getEffectiveColourWithTimeOffsetRGB(0)

    /**
     * @return the colour, at the current time if this is a chrome colour
     */
    fun getEffectiveColour(): Color = getEffectiveColourWithTimeOffset(0)

    @Deprecated("")
    fun toLegacyString(): String {
        val namedSpeed =
            if (timeForFullRotationInMillis == 0) 0 else getSpeedForMillis(timeForFullRotationInMillis / Timing.MILLIS_PER_SECOND)
        val rgb = evaluateColourWithShift(.0)
        val red = rgb shr Rgb.RED_SHIFT and Rgb.CHANNEL_MASK
        val green = rgb shr Rgb.GREEN_SHIFT and Rgb.CHANNEL_MASK
        val blue = rgb and Rgb.CHANNEL_MASK
        return special(namedSpeed, alpha, red, green, blue)
    }

    companion object {

        @JvmStatic
        @Deprecated("")
        fun special(chromaSpeed: Int, alpha: Int, rgb: Int): String {
            return special(
                chromaSpeed,
                alpha,
                rgb shr Rgb.RED_SHIFT and Rgb.CHANNEL_MASK,
                rgb shr Rgb.GREEN_SHIFT and Rgb.CHANNEL_MASK,
                rgb and Rgb.CHANNEL_MASK,
            )
        }

        @JvmStatic
        @Deprecated("")
        fun special(chromaSpeed: Int, alpha: Int, r: Int, g: Int, b: Int): String {
            val sb = StringBuilder()
            sb.append(chromaSpeed.toString(Legacy.RADIX)).append(":")
            sb.append(alpha.toString(Legacy.RADIX)).append(":")
            sb.append(r.toString(Legacy.RADIX)).append(":")
            sb.append(g.toString(Legacy.RADIX)).append(":")
            sb.append(b.toString(Legacy.RADIX))
            return sb.toString()
        }

        @JvmStatic
        private fun decompose(csv: String): IntArray {
            val split = csv.split(":")

            val arr = IntArray(split.size)

            for (i in split.indices) {
                try {
                    arr[i] = split[split.size - 1 - i].toInt(Legacy.RADIX)
                } catch (e: NumberFormatException) {
                    IMinecraft.INSTANCE.getLogger("ChromaColour").error("Invalid legacy colour value: $csv", e)
                }
            }
            return arr
        }

        @JvmStatic
        @Deprecated("")
        fun specialToSimpleRGB(special: String): Int {
            val (b, g, r, a) = decompose(special)

            return (a and Rgb.CHANNEL_MASK) shl Rgb.ALPHA_SHIFT or
                ((r and Rgb.CHANNEL_MASK) shl Rgb.RED_SHIFT) or
                ((g and Rgb.CHANNEL_MASK) shl Rgb.GREEN_SHIFT) or
                (b and Rgb.CHANNEL_MASK)
        }

        @JvmStatic
        @Deprecated("")
        fun getSpeed(special: String): Int = decompose(special)[Legacy.CHROMA_INDEX]

        @JvmStatic
        @Deprecated("")
        fun getSecondsForSpeed(speed: Int): Float =
            (Rgb.MAX_CHANNEL_VALUE - speed) / Legacy.SPEED_RANGE *
                (Timing.MAX_CHROMA_SECS - Timing.MIN_CHROMA_SECS) + Timing.MIN_CHROMA_SECS

        @Deprecated("")
        fun getSpeedForMillis(seconds: Float): Int {
            val normalizedSpeed = (seconds - Timing.MIN_CHROMA_SECS) /
                (Timing.MAX_CHROMA_SECS - Timing.MIN_CHROMA_SECS) * Legacy.SPEED_RANGE
            return (Rgb.MAX_CHANNEL_VALUE - normalizedSpeed).roundToInt()
        }

        @JvmStatic
        @Deprecated("")
        fun specialToChromaRGB(special: String): Int {
            val (b, g, r, a, chr) = decompose(special)

            val hsv = Color.RGBtoHSB(r, g, b, null)

            if (chr > 0) {
                val seconds = getSecondsForSpeed(chr)
                hsv[0] += ((System.currentTimeMillis().toDouble() / Timing.MILLIS_PER_SECOND / seconds) % 1).toFloat()
                hsv[0] %= 1f
                if (hsv[0] < 0) hsv[0] += 1f
            }

            return (a and Rgb.CHANNEL_MASK) shl Rgb.ALPHA_SHIFT or (Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]) and Rgb.MASK)
        }

        @JvmStatic
        @Deprecated("")
        fun rotateHue(argb: Int, degrees: Int): Int {
            val a = (argb shr Rgb.ALPHA_SHIFT) and Rgb.CHANNEL_MASK
            val r = (argb shr Rgb.RED_SHIFT) and Rgb.CHANNEL_MASK
            val g = (argb shr Rgb.GREEN_SHIFT) and Rgb.CHANNEL_MASK
            val b = argb and Rgb.CHANNEL_MASK

            val hsv = Color.RGBtoHSB(r, g, b, null)

            hsv[0] += degrees / Timing.HUE_CIRCLE_DEGREES.toFloat()
            hsv[0] %= 1f

            return (a and Rgb.CHANNEL_MASK) shl Rgb.ALPHA_SHIFT or (Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]) and Rgb.MASK)
        }

        @JvmStatic
        @Deprecated("")
        fun forLegacyString(stringRepresentation: String): ChromaColour {
            val d = decompose(stringRepresentation)
            assert(d.size == Legacy.FIELD_COUNT)

            val chr = d[Legacy.CHROMA_INDEX]
            val a = d[Legacy.ALPHA_INDEX]
            val r = d[Legacy.RED_INDEX]
            val g = d[Legacy.GREEN_INDEX]
            val b = d[Legacy.BLUE_INDEX]
            return fromRGB(
                r,
                g,
                b,
                if (chr > 0) (getSecondsForSpeed(chr) * Timing.MILLIS_PER_SECOND).toInt() else 0,
                a,
            )
        }

        @JvmStatic
        fun fromStaticRGB(r: Int, g: Int, b: Int, a: Int): ChromaColour = fromRGB(r, g, b, 0, a)

        @JvmStatic
        fun fromRGB(r: Int, g: Int, b: Int, chromaSpeedMillis: Int, a: Int): ChromaColour {
            val floats = Color.RGBtoHSB(r, g, b, null)
            return ChromaColour(floats[0], floats[1], floats[2], chromaSpeedMillis, a)
        }

        private object Rgb {
            const val MASK = 0x00FFFFFF
            const val CHANNEL_MASK = 0xFF
            const val ALPHA_SHIFT = 24
            const val RED_SHIFT = 16
            const val GREEN_SHIFT = 8
            const val MAX_CHANNEL_VALUE = 255
        }

        private object Timing {
            const val HUE_CIRCLE_DEGREES = 360.0
            const val MILLIS_PER_SECOND = 1000F
            const val MIN_CHROMA_SECS = 1
            const val MAX_CHROMA_SECS = 60
        }

        private object Legacy {
            const val RADIX = 10
            const val SPEED_RANGE = 254F
            const val FIELD_COUNT = 5
            const val CHROMA_INDEX = 4
            const val ALPHA_INDEX = 3
            const val RED_INDEX = 2
            const val GREEN_INDEX = 1
            const val BLUE_INDEX = 0
        }
    }
}
