package com.ringkasan.core;

/**
 * Kelas dasar (abstract) yang berbagi state dan helper antar implementasi.
 * Mendemonstrasikan inheritance dan encapsulation: field method bersifat
 * protected, diatur via setter publik dengan validasi sederhana.
 */
public abstract class AbstractSummarizer implements Summarizer {

    // Field dipakai bersama oleh semua anak kelas. protected = boleh diakses subclass,
    // tapi tidak publik (encapsulation). Default-nya MEDIUM.
    protected SummaryMethod method = SummaryMethod.MEDIUM;

    // Setter dengan validasi: tolak null agar state objek selalu valid.
    public void setMethod(SummaryMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("SummaryMethod tidak boleh null.");
        }
        this.method = method;
    }

    // Getter panjang ringkasan yang sedang dipilih.
    public SummaryMethod getMethod() {
        return method;
    }

    /**
     * Helper bersama: memecah teks menjadi kalimat berdasarkan tanda baca akhir.
     * Karena ditaruh di kelas abstract, RuleBased & ApiBased bisa pakai ulang
     * tanpa menulis ulang (contoh manfaat inheritance).
     */
    protected String[] splitSentences(String text) {
        if (text == null) return new String[0];
        // Rapikan spasi berlebih (enter, tab, dobel spasi) jadi satu spasi.
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) return new String[0];
        // Pisah SETELAH tanda . ! ? (lookbehind) sehingga tanda baca ikut di kalimat.
        return normalized.split("(?<=[.!?])\\s+");
    }
}
