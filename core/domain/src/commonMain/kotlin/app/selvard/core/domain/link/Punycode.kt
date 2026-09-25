package app.selvard.core.domain.link

/**
 * RFC 3492 punycode decoder (pure Kotlin, platform-neutral). Used only to reveal
 * the Unicode form of xn-- labels so homoglyph tricks become inspectable.
 * Returns null on malformed input; never throws.
 */
object Punycode {
    private const val BASE = 36
    private const val T_MIN = 1
    private const val T_MAX = 26
    private const val SKEW = 38
    private const val DAMP = 700
    private const val INITIAL_BIAS = 72
    private const val INITIAL_N = 128
    private const val PREFIX = "xn--"

    fun hasPrefix(label: String): Boolean = label.startsWith(PREFIX)

    fun decodePunycodeLabel(label: String): String? {
        val input = if (hasPrefix(label)) label.removePrefix(PREFIX) else label
        if (input.isEmpty()) return null
        val basicEnd = input.indexOfLast { it == '-' }
        val output = StringBuilder()
        decodeBasicPart(input, basicEnd, output) ?: return null
        var n = INITIAL_N
        var i = 0
        var bias = INITIAL_BIAS
        var pos = basicEnd + 1
        while (pos < input.length) {
            var oldI = i
            val step = decodeDigits(input, pos, i, bias) ?: return null
            i = step.i
            pos = step.pos
            bias = adapt(i - oldI, output.length + 1, oldI == 0)
            n = n + i / (output.length + 1)
            i %= output.length + 1
            if (n !in 1 until 0x10FFFF) return null
            output.insert(i, n.toChar())
            i++
        }
        return output.toString()
    }

    private class DigitStep(val i: Int, val pos: Int)

    /** RFC 3492 digit-decoding loop; null on malformed or overflowed input. */
    private fun decodeDigits(input: String, start: Int, initialI: Int, bias: Int): DigitStep? {
        var i = initialI
        var pos = start
        var w = 1
        var k = BASE
        while (true) {
            if (pos >= input.length) return null
            val digit = decodeDigit(input[pos]) ?: return null
            pos++
            i += digit * w
            if (i < 0) return null
            val t = if (k <= bias) T_MIN else if (k >= bias + T_MAX) T_MAX else k - bias
            if (digit < t) break
            w *= BASE - t
            if (w < 0) return null
            k += BASE
        }
        return DigitStep(i, pos)
    }

    /** Decodes a host label if punycode, else returns it unchanged. Null on malformed punycode. */
    fun revealLabel(label: String): String? =
        if (hasPrefix(label)) decodePunycodeLabel(label) else label

    private fun decodeDigit(c: Char): Int? = when (c) {
        in 'a'..'z' -> c - 'a'
        in 'A'..'Z' -> c - 'A'
        in '0'..'9' -> c - '0' + 26
        else -> null
    }

    /** Appends the ASCII prefix of a punycode label; null if the prefix is invalid. */
    private fun decodeBasicPart(input: String, basicEnd: Int, output: StringBuilder): StringBuilder? {
        if (basicEnd >= 0) {
            for (i in 0 until basicEnd) {
                val c = input[i]
                if (c.code !in 0 until INITIAL_N) return null
                output.append(c)
            }
        } else if (input.any { it.code !in 0 until INITIAL_N }) {
            return null
        }
        return output
    }

    private fun adapt(delta: Int, numPoints: Int, firstTime: Boolean): Int {
        var d = if (firstTime) delta / DAMP else delta / 2
        d += d / numPoints
        var k = 0
        while (d > ((BASE - T_MIN) * T_MAX) / 2) {
            d /= BASE - T_MIN
            k += BASE
        }
        return k + (((BASE - T_MIN + 1) * d) / (d + SKEW))
    }
}
