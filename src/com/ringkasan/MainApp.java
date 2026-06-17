package com.ringkasan;

import com.ringkasan.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point aplikasi. Mengatur look & feel sistem agar tampilan
 * cocok dengan OS pengguna, lalu menampilkan {@link MainFrame} di EDT.
 */
public class MainApp {

    // Method pertama yang dijalankan saat program start (java MainApp).
    public static void main(String[] args) {
        try {
            // Atur tampilan Swing agar mengikuti tema OS (Windows/Mac/Linux).
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Jika gagal, biarkan Swing pakai tema bawaan "Metal".
        }
        // invokeLater: semua kode UI Swing WAJIB jalan di Event Dispatch Thread (EDT)
        // agar thread-safe. Di sini kita buat MainFrame lalu tampilkan.
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
