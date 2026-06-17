package com.ringkasan.io;

import java.io.File;
import java.io.IOException;

/**
 * Kontrak ekspor hasil ringkasan ke file. Implementasi konkret berbeda
 * format (.txt, .pdf), namun antarmuka pemanggilnya tetap seragam.
 */
public interface Exporter {

    // Tulis "content" ke "file". Tiap implementasi (txt/pdf) menulis dengan caranya sendiri.
    void export(String content, File file) throws IOException;

    // Ekstensi file (mis. "txt" / "pdf").
    String getExtension();

    // Deskripsi untuk filter dialog Simpan (mis. "PDF Document (*.pdf)").
    String getDescription();
}
