package com.ringkasan.ui;

import com.ringkasan.core.ApiBasedSummarizer;
import com.ringkasan.core.RuleBasedSummarizer;
import com.ringkasan.core.Summarizer;
import com.ringkasan.core.SummaryMethod;
import com.ringkasan.history.SummaryHistoryManager;
import com.ringkasan.history.SummaryRecord;
import com.ringkasan.io.Exporter;
import com.ringkasan.io.PdfExporter;
import com.ringkasan.io.SummaryFormatter;
import com.ringkasan.io.TextInputHandler;
import com.ringkasan.io.TxtExporter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;

/**
 * Jendela utama aplikasi. Mengikat seluruh komponen MVC sederhana:
 *   - kiri  : area input teks + tombol sumber input.
 *   - kanan : area output ringkasan + tombol ekspor.
 *   - atas  : pengaturan metode, panjang ringkasan, API token.
 *   - bawah : status bar.
 *
 * Proses summarization dijalankan di SwingWorker agar UI tidak macet
 * saat memanggil API yang lambat.
 */
public class MainFrame extends JFrame {

    private static final String SOURCE_RULE = "Rule-based";
    private static final String SOURCE_API  = "API-based (HuggingFace)";

    private final JTextArea inputArea  = new JTextArea();
    private final JTextArea outputArea = new JTextArea();
    private final JComboBox<String> methodCombo = new JComboBox<>(new String[] { SOURCE_RULE, SOURCE_API });
    private final JComboBox<SummaryMethod> lengthCombo = new JComboBox<>(SummaryMethod.values());
    private final JPasswordField apiTokenField = new JPasswordField();
    private final JLabel statusLabel = new JLabel("Siap.");

    private final JButton summarizeBtn = new JButton("RINGKAS");
    private final JButton loadFileBtn  = new JButton("Muat File");
    private final JButton clipboardBtn = new JButton("Tempel Clipboard");
    private final JButton clearInputBtn = new JButton("Bersihkan");
    private final JButton saveBtn      = new JButton("Simpan Ringkasan");
    private final JButton historyBtn   = new JButton("Riwayat");
    private final JButton helpBtn      = new JButton("Bantuan");

    private final TextInputHandler inputHandler = new TextInputHandler();
    private final SummaryHistoryManager historyManager = new SummaryHistoryManager();
    private final SummaryFormatter formatter = new SummaryFormatter();

    private SummaryRecord currentRecord;

    // Konstruktor jendela: atur properti dasar window lalu jalankan 3 langkah penting.
    public MainFrame() {
        super("Aplikasi Ringkasan Buku Otomatis"); // judul window
        setDefaultCloseOperation(EXIT_ON_CLOSE);   // klik X = keluar aplikasi
        setSize(1024, 680);
        setMinimumSize(new Dimension(820, 520));
        setLocationRelativeTo(null);               // tampil di tengah layar
        buildUi();                  // 1) susun komponen
        wireActions();              // 2) pasang aksi tombol
        updateApiFieldVisibility(); // 3) atur kondisi awal field token
    }

    // Susun tata letak utama: NORTH (pengaturan), CENTER (input/output), SOUTH (status bar).
    private void buildUi() {
        setLayout(new BorderLayout(8, 8));
        add(buildTopPanel(),    BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildStatusBar(),   BorderLayout.SOUTH);
    }

    // Panel atas "Pengaturan": dropdown Metode, dropdown Panjang, dan field API Token.
    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Pengaturan"),
            BorderFactory.createEmptyBorder(4, 8, 6, 8)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Metode:"), gbc);
        gbc.gridx = 1;
        panel.add(methodCombo, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Panjang:"), gbc);
        gbc.gridx = 3;
        panel.add(lengthCombo, gbc);

        gbc.gridx = 4;
        panel.add(new JLabel("API Token (HF):"), gbc);
        gbc.gridx = 5; gbc.weightx = 1.0;
        apiTokenField.setToolTipText("Token HuggingFace (hf_...) — hanya dipakai bila metode API-based dipilih");
        panel.add(apiTokenField, gbc);

        return panel;
    }

    // Panel tengah: split kiri (input) - kanan (output), plus tombol RINGKAS di bawah.
    private JSplitPane buildCenterPanel() {
        // ----- Sisi kiri: input -----
        JPanel inputPanel = new JPanel(new BorderLayout(4, 4));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Teks Input"));

        JPanel inputToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        inputToolbar.add(loadFileBtn);
        inputToolbar.add(clipboardBtn);
        inputToolbar.add(clearInputBtn);
        inputPanel.add(inputToolbar, BorderLayout.NORTH);

        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);

        // ----- Sisi kanan: output -----
        JPanel outputPanel = new JPanel(new BorderLayout(4, 4));
        outputPanel.setBorder(BorderFactory.createTitledBorder("Ringkasan"));

        JPanel outputToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        outputToolbar.add(saveBtn);
        outputToolbar.add(historyBtn);
        outputToolbar.add(helpBtn);
        outputPanel.add(outputToolbar, BorderLayout.NORTH);

        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        outputPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // ----- Pemisah -----
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, outputPanel);
        split.setResizeWeight(0.5);

        // ----- Tombol RINGKAS di tengah-bawah split -----
        JPanel container = new JPanel(new BorderLayout());
        container.add(split, BorderLayout.CENTER);

        JPanel ringkasRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        summarizeBtn.setFont(summarizeBtn.getFont().deriveFont(Font.BOLD, 14f));
        summarizeBtn.setPreferredSize(new Dimension(220, 38));
        ringkasRow.add(summarizeBtn);
        container.add(ringkasRow, BorderLayout.SOUTH);

        // Wrap container into a fake JSplitPane so we satisfy return type
        JSplitPane wrapper = new JSplitPane(JSplitPane.VERTICAL_SPLIT, container, Box.createVerticalStrut(0));
        wrapper.setDividerSize(0);
        wrapper.setResizeWeight(1.0);
        wrapper.setEnabled(false);
        return wrapper;
    }

    // Status bar bawah: label info "Siap.", "Sedang meringkas...", dll.
    private JPanel buildStatusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        p.add(statusLabel, BorderLayout.CENTER);
        return p;
    }

    // Pasang event listener ke tiap tombol. Lambda "e -> onXxx()" = "saat diklik, jalankan onXxx()".
    private void wireActions() {
        summarizeBtn.addActionListener(e -> onSummarize());
        loadFileBtn.addActionListener(e -> onLoadFile());
        clipboardBtn.addActionListener(e -> onLoadClipboard());
        // Tombol Bersihkan: aksi singkat ditulis langsung (inline), kosongkan kedua area.
        clearInputBtn.addActionListener(e -> { inputArea.setText(""); outputArea.setText(""); setStatus("Input dan ringkasan dibersihkan."); });
        saveBtn.addActionListener(e -> onSave());
        historyBtn.addActionListener(e -> onHistory());
        helpBtn.addActionListener(e -> onHelp());
        // Saat dropdown Metode berubah, perbarui kondisi field token.
        methodCombo.addActionListener(e -> updateApiFieldVisibility());
    }

    // Aktif/nonaktifkan field API Token tergantung metode terpilih.
    // Rule-based -> token tidak dipakai (abu-abu); API-based -> token aktif (putih).
    private void updateApiFieldVisibility() {
        boolean api = SOURCE_API.equals(methodCombo.getSelectedItem());
        apiTokenField.setEnabled(api);
        apiTokenField.setBackground(api ? java.awt.Color.WHITE : new java.awt.Color(0xEE, 0xEE, 0xEE));
    }

    /* ------------------------------------------------------------------ */
    /* Aksi                                                                */
    /* ------------------------------------------------------------------ */

    // Aksi tombol "Muat File": buka dialog pilih file -> baca -> tampilkan di area input.
    private void onLoadFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Pilih file teks (.txt)");
        fc.setFileFilter(new FileNameExtensionFilter("Text File (*.txt)", "txt"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            String text = inputHandler.loadFromFile(fc.getSelectedFile());
            inputArea.setText(text);
            inputArea.setCaretPosition(0);
            setStatus("File dimuat: " + fc.getSelectedFile().getName()
                    + " (" + text.length() + " karakter)");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat file:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Aksi tombol "Tempel Clipboard": ambil teks dari clipboard -> tampilkan di area input.
    private void onLoadClipboard() {
        try {
            String text = inputHandler.loadFromClipboard();
            if (text == null || text.isEmpty()) {
                setStatus("Clipboard kosong / tidak berisi teks.");
                return;
            }
            inputArea.setText(text);
            inputArea.setCaretPosition(0);
            setStatus("Teks ditempel dari clipboard (" + text.length() + " karakter)");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengakses clipboard:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== JANTUNG APLIKASI: aksi tombol "RINGKAS" =====
    private void onSummarize() {
        // 1) Ambil & validasi teks input.
        final String text = inputArea.getText();
        if (text == null || text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Teks input masih kosong.",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 2) Pilih algoritma (Rule-based / API-based). Ini contoh POLYMORPHISM:
        //    tipe-nya Summarizer, isinya bisa salah satu implementasi.
        final Summarizer summarizer = buildSummarizer();
        if (summarizer == null) return; // mis. API dipilih tapi token kosong

        // 3) Kunci tombol & beri tahu user prosesnya sedang berjalan.
        summarizeBtn.setEnabled(false);
        setStatus("Sedang meringkas dengan " + summarizer.getName() + "...");
        outputArea.setText("");

        // 4) Jalankan proses berat di thread terpisah (SwingWorker) supaya UI TIDAK macet.
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            // doInBackground() jalan di BACKGROUND thread -> lakukan proses lambat di sini.
            @Override
            protected String doInBackground() throws Exception {
                return summarizer.summarize(text); // tak peduli rule/api -> polymorphism
            }
            // done() jalan kembali di EDT -> aman untuk menyentuh komponen UI.
            @Override
            protected void done() {
                try {
                    String result = get(); // ambil hasil; jika tadi error, dilempar di sini
                    // Tampilkan hasil ke area output.
                    outputArea.setText(formatter.formatForDisplay(result));
                    outputArea.setCaretPosition(0);
                    // Bungkus jadi 1 record lalu simpan ke riwayat.
                    currentRecord = new SummaryRecord(text, result, summarizer.getName());
                    historyManager.add(currentRecord);
                    setStatus("Ringkasan selesai (" + result.length()
                            + " karakter). Total riwayat: " + historyManager.size());
                } catch (Exception ex) {
                    // Ambil penyebab asli error (mis. SummarizationException) lalu tampilkan dialog.
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String msg = cause.getMessage() == null ? cause.toString() : cause.getMessage();
                    JOptionPane.showMessageDialog(MainFrame.this,
                        "Gagal meringkas:\n" + msg, "Error", JOptionPane.ERROR_MESSAGE);
                    setStatus("Gagal: " + msg);
                } finally {
                    summarizeBtn.setEnabled(true); // apa pun hasilnya, aktifkan kembali tombol
                }
            }
        };
        worker.execute(); // mulai jalankan worker
    }

    // Pabrik objek Summarizer: baca pilihan user, kembalikan implementasi yang sesuai.
    // Mengembalikan null bila syarat belum terpenuhi (mis. token API kosong).
    private Summarizer buildSummarizer() {
        SummaryMethod length = (SummaryMethod) lengthCombo.getSelectedItem();
        String source = (String) methodCombo.getSelectedItem();
        if (SOURCE_API.equals(source)) {
            // getPassword() mengembalikan char[]; ubah ke String lalu rapikan.
            String token = new String(apiTokenField.getPassword()).trim();
            if (token.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Metode API-based memerlukan API Token HuggingFace.\n"
                    + "Isi field 'API Token (HF)' di bagian atas, atau pilih metode Rule-based.",
                    "API Token Diperlukan", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            ApiBasedSummarizer s = new ApiBasedSummarizer(token);
            s.setMethod(length);   // set panjang ringkasan
            return s;
        }
        // Default: Rule-based (offline, tanpa token).
        RuleBasedSummarizer s = new RuleBasedSummarizer();
        s.setMethod(length);
        return s;
    }

    // Aksi tombol "Simpan Ringkasan": pilih txt/pdf -> pilih Exporter yang sesuai -> tulis file.
    // Ini contoh POLYMORPHISM kedua (interface Exporter).
    private void onSave() {
        // Harus sudah ada hasil ringkasan dulu.
        if (currentRecord == null) {
            JOptionPane.showMessageDialog(this,
                "Belum ada ringkasan untuk disimpan.\nKlik RINGKAS terlebih dahulu.",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Ringkasan");
        FileNameExtensionFilter txt = new FileNameExtensionFilter("Text File (*.txt)", "txt");
        FileNameExtensionFilter pdf = new FileNameExtensionFilter("PDF Document (*.pdf)", "pdf");
        fc.addChoosableFileFilter(txt);
        fc.addChoosableFileFilter(pdf);
        fc.setFileFilter(txt);
        fc.setSelectedFile(new File("ringkasan.txt"));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        // Pilih implementasi Exporter sesuai filter, dan pastikan ekstensi file benar.
        Exporter exporter;
        if (fc.getFileFilter() == pdf) {
            exporter = new PdfExporter();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }
        } else {
            exporter = new TxtExporter();
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
        }

        try {
            // Kode pemanggil seragam: tidak peduli txt atau pdf, cukup panggil export().
            exporter.export(formatter.formatForExport(currentRecord), file);
            setStatus("Tersimpan: " + file.getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                "Berhasil disimpan ke:\n" + file.getAbsolutePath(),
                "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Gagal menyimpan file:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            setStatus("Gagal menyimpan: " + ex.getMessage());
        }
    }

    // Aksi tombol "Riwayat": buka dialog modal berisi semua ringkasan sesi ini.
    private void onHistory() {
        HistoryDialog dlg = new HistoryDialog(this, historyManager);
        dlg.setVisible(true);
    }

    // Aksi tombol "Bantuan": tampilkan petunjuk pemakaian dalam dialog.
    private void onHelp() {
        String help =
            "PETUNJUK PENGGUNAAN\n"
            + "===================\n\n"
            + "1. Masukkan teks ke area 'Teks Input' melalui salah satu cara:\n"
            + "   - Ketik / paste manual.\n"
            + "   - Klik 'Muat File' untuk membaca file .txt.\n"
            + "   - Klik 'Tempel Clipboard' untuk mengambil teks dari clipboard.\n\n"
            + "2. Pilih metode pada dropdown 'Metode':\n"
            + "   - Rule-based: ringkasan offline berdasarkan frekuensi kata.\n"
            + "   - API-based: memanggil HuggingFace (butuh API token hf_...).\n\n"
            + "3. Pilih panjang ringkasan pada dropdown 'Panjang'.\n\n"
            + "4. Klik tombol 'RINGKAS'. Hasil akan tampil di sisi kanan.\n\n"
            + "5. Klik 'Simpan Ringkasan' untuk mengekspor sebagai .txt atau .pdf.\n\n"
            + "6. Klik 'Riwayat' untuk melihat semua ringkasan dalam sesi ini.\n\n"
            + "Catatan API Token HuggingFace:\n"
            + "   Daftar gratis di https://huggingface.co lalu buka Settings >\n"
            + "   Access Tokens > Create new token (role: Read).\n"
            + "   Token diawali 'hf_'. Isikan token tersebut sebelum klik RINGKAS.";
        JTextArea area = new JTextArea(help);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(560, 380));
        JOptionPane.showMessageDialog(this, sp, "Bantuan", JOptionPane.INFORMATION_MESSAGE);
    }

    // Helper kecil: ubah teks pada status bar bawah.
    private void setStatus(String text) {
        statusLabel.setText(text);
    }
}
