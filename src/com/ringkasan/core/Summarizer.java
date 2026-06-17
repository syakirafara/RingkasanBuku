package com.ringkasan.core;

/**
 * Kontrak (interface) untuk semua algoritma summarization.
 * Setiap implementasi wajib menyediakan metode summarize() dan nama tampilan.
 * Interface ini memungkinkan polymorphism: pengguna kelas tidak perlu tahu
 * apakah ringkasan dihasilkan secara rule-based atau via API eksternal.
 */
public interface Summarizer {

    // Method inti: terima teks asli, kembalikan hasil ringkasannya.
    // Setiap algoritma (Rule-based / API-based) WAJIB mengisi method ini.
    String summarize(String text) throws SummarizationException;

    // Nama algoritma untuk ditampilkan di UI / status bar.
    String getName();
}
