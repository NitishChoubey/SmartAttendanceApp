package com.ebf.smartattendanceapp.UltrasonicDetector







class FFT(private val n: Int) {
    init {
        if (n <= 0 || (n and (n - 1)) != 0) {
            throw IllegalArgumentException("n must be a power of 2")
        }
    }

    fun fft(real: DoubleArray, imag: DoubleArray) {
        if (real.size != n || imag.size != n) {
            throw IllegalArgumentException("Arrays must be of size n")
        }

        // Bit-reversal permutation
        var j = 0
        for (i in 0 until n - 1) {
            if (i < j) {
                var temp = real[i]; real[i] = real[j]; real[j] = temp
                temp = imag[i]; imag[i] = imag[j]; imag[j] = temp
            }
            var k = n / 2
            while (k <= j) {
                j -= k
                k /= 2
            }
            j += k
        }

        // Cooley-Tukey FFT
        var len = 2
        while (len <= n) {
            val angle = -2 * Math.PI / len
            val wlen_r = Math.cos(angle)
            val wlen_i = Math.sin(angle)
            var i = 0
            while (i < n) {
                var w_r = 1.0
                var w_i = 0.0
                j = 0
                while (j < len / 2) {
                    val u_r = real[i + j]
                    val u_i = imag[i + j]
                    val v_r = real[i + j + len / 2] * w_r - imag[i + j + len / 2] * w_i
                    val v_i = real[i + j + len / 2] * w_i + imag[i + j + len / 2] * w_r
                    real[i + j] = u_r + v_r
                    imag[i + j] = u_i + v_i
                    real[i + j + len / 2] = u_r - v_r
                    imag[i + j + len / 2] = u_i - v_i

                    val temp = w_r * wlen_r - w_i * wlen_i
                    w_i = w_r * wlen_i + w_i * wlen_r
                    w_r = temp
                    j++
                }
                i += len
            }
            len *= 2
        }
    }
}