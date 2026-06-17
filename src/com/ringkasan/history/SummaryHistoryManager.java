package com.ringkasan.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Wadah riwayat ringkasan yang dihasilkan selama sesi aplikasi berjalan.
 * Sederhana, in-memory; bisa diperluas ke persistent storage dengan
 * menambahkan loadFromDisk()/saveToDisk() tanpa mengubah API publik.
 */
public class SummaryHistoryManager {

    // Penyimpanan riwayat di memori (list). Hilang saat aplikasi ditutup.
    private final List<SummaryRecord> records = new ArrayList<>();

    // Tambah satu record baru ke akhir list (abaikan jika null).
    public void add(SummaryRecord record) {
        if (record != null) records.add(record);
    }

    /** Mengembalikan daftar terurut dari yang terbaru ke terlama. */
    public List<SummaryRecord> getAllNewestFirst() {
        // Salin dulu (jangan ubah list asli), lalu balik urutannya.
        List<SummaryRecord> copy = new ArrayList<>(records);
        Collections.reverse(copy);
        // unmodifiableList: pemanggil boleh baca tapi tidak boleh mengubah (lindungi data internal).
        return Collections.unmodifiableList(copy);
    }

    // Versi urutan asli (lama -> baru), juga read-only.
    public List<SummaryRecord> getAll() {
        return Collections.unmodifiableList(records);
    }

    // Hapus seluruh riwayat.
    public void clear() {
        records.clear();
    }

    // Jumlah entri riwayat.
    public int size() {
        return records.size();
    }

    // True jika belum ada riwayat sama sekali.
    public boolean isEmpty() {
        return records.isEmpty();
    }
}
