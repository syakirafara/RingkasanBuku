package com.ringkasan.core;

/**
 * Pilihan panjang ringkasan yang ditampilkan di dropdown UI.
 * Disimpan sebagai enum agar opsi tetap terkontrol dan mudah diperluas.
 */
public enum SummaryMethod {

    // 3 pilihan tetap. Tiap konstanta menyimpan: teks tampilan + jumlah kalimat target.
    SHORT("Ringkasan Pendek (2 kalimat)", 2),
    MEDIUM("Ringkasan Sedang (4 kalimat)", 4),
    DETAILED("Ringkasan Detail (6 kalimat)", 6);

    private final String displayName;     // teks yang muncul di dropdown
    private final int targetSentences;    // berapa kalimat yang diambil

    // Konstruktor enum: dipanggil otomatis untuk SHORT, MEDIUM, DETAILED di atas.
    SummaryMethod(String displayName, int targetSentences) {
        this.displayName = displayName;
        this.targetSentences = targetSentences;
    }

    // Getter teks tampilan.
    public String getDisplayName() {
        return displayName;
    }

    // Getter jumlah kalimat target (dipakai algoritma untuk menentukan N kalimat).
    public int getTargetSentences() {
        return targetSentences;
    }

    // Dipanggil otomatis oleh JComboBox saat menampilkan item -> tampilkan displayName.
    @Override
    public String toString() {
        return displayName;
    }
}
