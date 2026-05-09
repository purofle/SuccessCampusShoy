package com.github.purofle.sandauschool.crypt


/**
 * from https://github.com/ZenLiuCN/lz-string4k
 */


typealias call = () -> Unit

object LZ4K {
    private const val KEY_STR = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="


    private data class Data(
        var value: Char = '0',
        var position: Int = 0,
        var index: Int = 1
    )

    private fun Int.power() = 1 shl this

    private val Int.string get() = this.toChar().toString()
    private fun decompressInternal(
        length: Int,
        resetValue: Int = 32,
        getNextValue: (idx: Int) -> Char
    ): String? {
        val builder = StringBuilder()
        val dictionary = mutableListOf(0.string, 1.string, 2.string)
        var bits = 0
        var maxPower: Int
        var power: Int
        val data = Data(getNextValue(0), resetValue, 1)
        var resb: Int
        var c = ""
        var w: String
        var entry: String
        var numBits = 3
        var enlargeIn = 4
        var dictSize = 4
        var next: Int
        fun doPower(initBits: Int, initPower: Int, initMaxPowerFactor: Int, mode: Int = 0) {
            bits = initBits
            maxPower = initMaxPowerFactor.power()
            power = initPower
            while (power != maxPower) {
                resb = data.value.code and data.position
                data.position = data.position shr 1
                if (data.position == 0) {
                    data.position = resetValue
                    data.value = getNextValue(data.index++)
                }
                bits = bits or (if (resb > 0) 1 else 0) * power
                power = power shl 1
            }
            when (mode) {
                0 -> Unit
                1 -> c = bits.string
                2 -> {
                    dictionary.add(dictSize++, bits.string)
                    next = (dictSize - 1)
                    enlargeIn--
                }
            }
        }

        fun checkEnlargeIn() {
            if (enlargeIn == 0) {
                enlargeIn = numBits.power()
                numBits++
            }
        }
        doPower(bits, 1, 2)
        next = bits
        when (next) {
            0 -> doPower(0, 1, 8, 1)
            1 -> doPower(0, 1, 16, 1)
            2 -> return ""
        }
        dictionary.add(3, c)
        w = c
        builder.append(w)
        while (true) {
            if (data.index > length) {
                return ""
            }
            doPower(0, 1, numBits)
            next = bits
            when (next) {
                0 -> doPower(0, 1, 8, 2)
                1 -> doPower(0, 1, 16, 2)
                2 -> return builder.toString()
            }
            checkEnlargeIn()
            entry = when {
                dictionary.size > next -> dictionary[next]
                next == dictSize -> w + w[0]
                else -> return null
            }
            builder.append(entry)
            // Add w+entry[0] to the dictionary.
            dictionary.add(dictSize++, w + entry[0])
            enlargeIn--
            w = entry
            checkEnlargeIn()
        }


    }

    fun decompressFromBase64(input: String) = when {
        input.isBlank() -> null
        else -> decompressInternal(input.length) {
            KEY_STR.indexOf(input[it]).toChar()
        }
    }
}