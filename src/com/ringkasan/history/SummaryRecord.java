package com.ringkasan.history;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entitas immutable yang merepresentasikan satu entri riwayat ringkasan.
 * Field bersifat final dan hanya dapat dibaca lewat getter — contoh nyata
 * dari prinsip encapsulation.
 */
public final class SummaryRecord {

    private static final DateTimeFormatter PREVIEW_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String originalText;
    private final String summary;
    private final String method;
    private final LocalDateTime timestamp;

    // Konstruktor: isi semua field sekaligus (final = sekali set, tak bisa diubah lagi).
    // Timestamp diisi otomatis dengan waktu saat record dibuat.
    public SummaryRecord(String originalText, String summary, String method) {
        this.originalText = originalText == null ? "" : originalText;
        this.summary = summary == null ? "" : summary;
        this.method = method == null ? "(tidak diketahui)" : method;
        this.timestamp = LocalDateTime.now();
    }

    // Getter saja, tanpa setter -> objek bersifat read-only (immutable / encapsulation).
    public String getOriginalText() { return originalText; }
    public String getSummary()      { return summary; }
    public String getMethod()       { return method; }
    public LocalDateTime getTimestamp() { return timestamp; }

    /** Ringkasan satu baris untuk ditampilkan di list riwayat. */
    public String getPreview() {
        // Potong jadi maksimal 70 karakter + "..." agar muat satu baris.
        String snippet = summary.length() > 70 ? summary.substring(0, 70) + "..." : summary;
        // Buang enter agar tidak merusak tampilan list.
        snippet = snippet.replace('\n', ' ').replace('\r', ' ');
        return timestamp.format(PREVIEW_FMT) + "  |  " + method + "  |  " + snippet;
    }

    // Dipanggil JList saat menampilkan record -> tampilkan preview satu baris.
    @Override
    public String toString() {
        return getPreview();
    }
}
