package com.ringkasan.ui;

import com.ringkasan.history.SummaryHistoryManager;
import com.ringkasan.history.SummaryRecord;
import com.ringkasan.io.SummaryFormatter;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Font;
import java.util.List;

/**
 * Dialog modal untuk menelusuri ringkasan-ringkasan sebelumnya.
 * Kiri = daftar entri, kanan = detail ringkasan + teks asli.
 */
public class HistoryDialog extends JDialog {

    private final SummaryHistoryManager historyManager;
    private final SummaryFormatter formatter = new SummaryFormatter();
    private final DefaultListModel<SummaryRecord> listModel = new DefaultListModel<>();
    private final JList<SummaryRecord> recordList = new JList<>(listModel);
    private final JTextArea detailArea = new JTextArea();

    // Konstruktor dialog. "true" pada super() = modal (memblok jendela utama sampai ditutup).
    public HistoryDialog(Frame owner, SummaryHistoryManager historyManager) {
        super(owner, "Riwayat Ringkasan", true);
        this.historyManager = historyManager;
        buildUi();         // susun komponen
        loadRecords();     // isi daftar dari history manager
        setSize(820, 480);
        setLocationRelativeTo(owner);
    }

    // Susun tata letak: list di kiri, detail di kanan (dipisah JSplitPane), tombol di bawah.
    private void buildUi() {
        setLayout(new BorderLayout(8, 8));

        recordList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recordList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        recordList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateDetail();
        });

        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        JScrollPane leftScroll = new JScrollPane(recordList);
        leftScroll.setBorder(BorderFactory.createTitledBorder("Daftar Riwayat"));

        JScrollPane rightScroll = new JScrollPane(detailArea);
        rightScroll.setBorder(BorderFactory.createTitledBorder("Detail"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
        split.setResizeWeight(0.4);
        add(split, BorderLayout.CENTER);

        JButton clearBtn = new JButton("Bersihkan Riwayat");
        clearBtn.addActionListener(e -> onClear());
        JButton closeBtn = new JButton("Tutup");
        closeBtn.addActionListener(e -> dispose());

        JPanel southPanel = new JPanel(new BorderLayout());
        JLabel info = new JLabel("Total: " + historyManager.size() + " entri", SwingConstants.LEFT);
        info.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        southPanel.add(info, BorderLayout.WEST);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        buttonRow.add(clearBtn);
        buttonRow.add(closeBtn);
        southPanel.add(buttonRow, BorderLayout.EAST);
        add(southPanel, BorderLayout.SOUTH);
    }

    // Isi ulang list dari riwayat (terbaru di atas). Pilih entri pertama otomatis.
    private void loadRecords() {
        listModel.clear();
        List<SummaryRecord> records = historyManager.getAllNewestFirst();
        for (SummaryRecord r : records) listModel.addElement(r);
        if (!listModel.isEmpty()) recordList.setSelectedIndex(0);
        else detailArea.setText("(Riwayat kosong — buat ringkasan terlebih dahulu)");
    }

    // Dipanggil saat user mengklik entri di list -> tampilkan detail lengkapnya di kanan.
    private void updateDetail() {
        SummaryRecord r = recordList.getSelectedValue();
        if (r == null) { detailArea.setText(""); return; }
        detailArea.setText(formatter.formatForExport(r));
        detailArea.setCaretPosition(0);
    }

    // Aksi tombol "Bersihkan Riwayat": minta konfirmasi dulu, baru hapus.
    private void onClear() {
        if (historyManager.isEmpty()) return;
        int ans = JOptionPane.showConfirmDialog(this,
            "Hapus seluruh riwayat ringkasan?\nTindakan ini tidak dapat dibatalkan.",
            "Konfirmasi", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans == JOptionPane.OK_OPTION) {
            historyManager.clear();
            loadRecords();
        }
    }

    // Ukuran default dialog yang disarankan ke layout manager.
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(820, 480);
    }
}
