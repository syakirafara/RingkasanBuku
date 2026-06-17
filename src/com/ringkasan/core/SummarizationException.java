package com.ringkasan.core;

/**
 * Exception khusus untuk proses summarization (input invalid, API gagal, dsb).
 */
public class SummarizationException extends Exception {

    // Konstruktor 1: hanya pesan error (mis. "Teks input kosong.").
    public SummarizationException(String message) {
        super(message);
    }

    // Konstruktor 2: pesan + penyebab asli (cause), berguna untuk membungkus
    // error lain seperti IOException saat memanggil API.
    public SummarizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
