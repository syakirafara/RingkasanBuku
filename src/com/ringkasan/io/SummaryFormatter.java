package com.ringkasan.io;

import com.ringkasan.history.SummaryRecord;

import java.time.format.DateTimeFormatter;

/**
 * Memformat ringkasan untuk dua keperluan berbeda:
 *   - tampilan di GUI (apa adanya, dirapikan).
 *   - ekspor ke file (.txt/.pdf) dengan header metadata.
 */
public class SummaryFormatter {

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");

    // Untuk tampil di GUI: cukup dirapikan (hapus spasi di awal/akhir).
    public String formatForDisplay(String summary) {
        if (summary == null) return "";
        return summary.trim();
    }

    // Untuk disimpan ke file: tambahkan header metadata (tanggal, metode, panjang)
    // lalu hasil ringkasan dan teks asli. Memakai StringBuilder untuk menyusun teks.
    public String formatForExport(SummaryRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Ringkasan Buku Otomatis ===\n");
        sb.append("Tanggal : ").append(record.getTimestamp().format(DATE_FMT)).append('\n');
        sb.append("Metode  : ").append(record.getMethod()).append('\n');
        sb.append("Sumber  : ").append(record.getOriginalText().length()).append(" karakter\n");
        sb.append('\n');
        sb.append("--- Hasil Ringkasan ---\n");
        sb.append(record.getSummary()).append('\n');
        sb.append('\n');
        sb.append("--- Teks Asli ---\n");
        sb.append(record.getOriginalText()).append('\n');
        return sb.toString();
    }
}
