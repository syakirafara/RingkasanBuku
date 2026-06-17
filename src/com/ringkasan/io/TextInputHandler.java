package com.ringkasan.io;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

/**
 * Menangani sumber-sumber input teks bagi aplikasi.
 * Mendukung pembacaan dari file (.txt) dan dari clipboard sistem.
 */
public class TextInputHandler {

    /** Membaca file teks UTF-8 dan mengembalikan isinya sebagai String. */
    public String loadFromFile(File file) throws IOException {
        // Validasi: file harus ada dan benar-benar berupa file (bukan folder).
        if (file == null || !file.exists()) {
            throw new IOException("File tidak ditemukan.");
        }
        if (!file.isFile()) {
            throw new IOException("Path bukan file: " + file.getAbsolutePath());
        }
        // Baca semua byte file, lalu ubah jadi teks dengan encoding UTF-8.
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /** Mengambil teks dari clipboard sistem. Mengembalikan string kosong jika clipboard tidak berisi teks. */
    public String loadFromClipboard() throws IOException {
        try {
            // Ambil clipboard milik OS, lalu lihat isinya.
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(null);
            // Cek apakah isi clipboard berupa teks (stringFlavor). Jika ya, ambil teksnya.
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                Object data = contents.getTransferData(DataFlavor.stringFlavor);
                return data == null ? "" : data.toString();
            }
            return ""; // clipboard berisi gambar/file/kosong -> kembalikan string kosong
        } catch (UnsupportedFlavorException e) {
            throw new IOException("Clipboard tidak berisi teks.", e);
        } catch (IllegalStateException e) {
            throw new IOException("Clipboard tidak dapat diakses saat ini.", e);
        }
    }
}
