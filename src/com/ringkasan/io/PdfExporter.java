package com.ringkasan.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Penulis PDF minimalis berbasis spesifikasi PDF 1.4 — TANPA library eksternal.
 * Mendukung teks multi-halaman dengan font Helvetica (Type1 standar, encoding
 * WinAnsi). Karakter di luar Latin-1 (mis. emoji, aksara non-Latin) akan
 * disubstitusi dengan '?' karena keterbatasan encoding standar PDF.
 *
 * Struktur PDF yang dihasilkan:
 *   1 = Catalog
 *   2 = Pages
 *   3..3+N-1 = Page objects (N = jumlah halaman)
 *   3+N..3+2N-1 = Contents stream tiap halaman
 *   3+2N = Font Helvetica
 */
public class PdfExporter implements Exporter {

    private static final int PAGE_WIDTH  = 612;   // Letter, 8.5"
    private static final int PAGE_HEIGHT = 792;   // Letter, 11"
    private static final int MARGIN      = 50;
    private static final int FONT_SIZE   = 11;
    private static final int LINE_HEIGHT = 14;
    private static final int MAX_CHARS_PER_LINE = 90;
    private static final int LINES_PER_PAGE = (PAGE_HEIGHT - 2 * MARGIN) / LINE_HEIGHT;

    // Method utama: ubah teks menjadi berkas PDF valid dan tulis ke file.
    // Alur: bungkus baris -> bagi per halaman -> rakit objek-objek PDF -> tulis xref & trailer.
    @Override
    public void export(String content, File file) throws IOException {
        if (content == null) content = "";

        // 1) Pecah teks panjang jadi baris-baris yang muat selebar halaman (word-wrap).
        List<String> wrapped = wrapText(content);
        // 2) Bagi baris-baris itu menjadi beberapa halaman.
        List<List<String>> pages = paginate(wrapped, LINES_PER_PAGE);
        if (pages.isEmpty()) pages.add(new ArrayList<>()); // minimal 1 halaman kosong

        // Buffer untuk menampung byte PDF. offsets = posisi byte tiap objek (untuk tabel xref).
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        List<Long> offsets = new ArrayList<>();
        writeAscii(pdf, "%PDF-1.4\n%âãÏÓ\n"); // header wajib + komentar biner penanda PDF

        // Penomoran objek PDF. Tiap objek punya nomor unik yang saling mereferensi.
        int objId = 1;
        int n = pages.size();
        int catalogId = objId++;
        int pagesId   = objId++;
        int firstPageId    = objId;       objId += n;
        int firstContentId = objId;       objId += n;
        int fontId    = objId++;

        // 1: Catalog
        offsets.add((long) pdf.size());
        writeAscii(pdf, catalogId + " 0 obj\n<< /Type /Catalog /Pages " + pagesId + " 0 R >>\nendobj\n");

        // 2: Pages
        StringBuilder kids = new StringBuilder("[");
        for (int i = 0; i < n; i++) {
            if (i > 0) kids.append(' ');
            kids.append(firstPageId + i).append(" 0 R");
        }
        kids.append(']');
        offsets.add((long) pdf.size());
        writeAscii(pdf, pagesId + " 0 obj\n<< /Type /Pages /Kids " + kids
                + " /Count " + n + " >>\nendobj\n");

        // Page objects
        for (int i = 0; i < n; i++) {
            offsets.add((long) pdf.size());
            writeAscii(pdf, (firstPageId + i) + " 0 obj\n"
                    + "<< /Type /Page /Parent " + pagesId + " 0 R "
                    + "/MediaBox [0 0 " + PAGE_WIDTH + " " + PAGE_HEIGHT + "] "
                    + "/Contents " + (firstContentId + i) + " 0 R "
                    + "/Resources << /Font << /F1 " + fontId + " 0 R >> >> >>\n"
                    + "endobj\n");
        }

        // Content streams
        for (int i = 0; i < n; i++) {
            byte[] stream = buildContentStream(pages.get(i));
            offsets.add((long) pdf.size());
            writeAscii(pdf, (firstContentId + i) + " 0 obj\n<< /Length " + stream.length + " >>\nstream\n");
            pdf.write(stream);
            writeAscii(pdf, "\nendstream\nendobj\n");
        }

        // Font
        offsets.add((long) pdf.size());
        writeAscii(pdf, fontId + " 0 obj\n<< /Type /Font /Subtype /Type1 "
                + "/BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n");

        // xref = tabel indeks berisi posisi byte tiap objek, dipakai pembaca PDF untuk
        // melompat langsung ke objek tertentu tanpa membaca seluruh file.
        long xrefStart = pdf.size();
        int totalObjects = offsets.size() + 1; // +1 untuk objek 0 (entri "free" wajib)
        writeAscii(pdf, "xref\n0 " + totalObjects + "\n");
        writeAscii(pdf, "0000000000 65535 f \n"); // baris objek 0
        for (long off : offsets) {
            writeAscii(pdf, String.format("%010d 00000 n \n", off)); // posisi tiap objek (10 digit)
        }

        // trailer = penutup file: tunjuk akar dokumen (Catalog) & posisi awal xref.
        writeAscii(pdf, "trailer\n<< /Size " + totalObjects
                + " /Root " + catalogId + " 0 R >>\nstartxref\n" + xrefStart + "\n%%EOF\n");

        // Tulis seluruh byte yang sudah dirakit ke file fisik.
        try (FileOutputStream fos = new FileOutputStream(file)) {
            pdf.writeTo(fos);
        }
    }

    @Override
    public String getExtension() {
        return "pdf";
    }

    @Override
    public String getDescription() {
        return "PDF Document (*.pdf)";
    }

    /* ------------------------------------------------------------------ */
    /* Helper                                                              */
    /* ------------------------------------------------------------------ */

    // Bangun "content stream" satu halaman: perintah menggambar teks dalam bahasa PDF.
    //   BT/ET   = Begin/End Text     Tf = pilih font+ukuran     TL = jarak antar baris
    //   Td      = posisi awal teks   Tj = cetak satu baris      T* = pindah ke baris berikutnya
    private byte[] buildContentStream(List<String> lines) {
        StringBuilder cs = new StringBuilder();
        cs.append("BT\n");
        cs.append("/F1 ").append(FONT_SIZE).append(" Tf\n");
        cs.append(LINE_HEIGHT).append(" TL\n");
        // Mulai dari kiri-atas (koordinat PDF dihitung dari bawah, makanya TINGGI - MARGIN).
        cs.append(MARGIN).append(' ').append(PAGE_HEIGHT - MARGIN).append(" Td\n");
        for (String line : lines) {
            cs.append('(').append(escapePdf(line)).append(") Tj\n");
            cs.append("T*\n");
        }
        cs.append("ET\n");
        return cs.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    // Escape karakter khusus PDF: ( ) dan \ harus diawali backslash, kalau tidak file rusak.
    private static String escapePdf(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '(':  sb.append("\\(");  break;
                case ')':  sb.append("\\)");  break;
                default:
                    if (c > 0xFF) sb.append('?'); // di luar Latin-1
                    else sb.append(c);
            }
        }
        return sb.toString();
    }

    // Word-wrap: pecah teks agar tiap baris tidak melebihi MAX_CHARS_PER_LINE karakter.
    // Kata yang lebih panjang dari satu baris dipotong paksa.
    private static List<String> wrapText(String content) {
        List<String> out = new ArrayList<>();
        for (String paragraph : content.split("\n", -1)) {
            if (paragraph.isEmpty()) {
                out.add("");
                continue;
            }
            StringBuilder current = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                // jika satu kata terlalu panjang, potong paksa
                while (word.length() > MAX_CHARS_PER_LINE) {
                    if (current.length() > 0) {
                        out.add(current.toString());
                        current.setLength(0);
                    }
                    out.add(word.substring(0, MAX_CHARS_PER_LINE));
                    word = word.substring(MAX_CHARS_PER_LINE);
                }
                int needed = current.length() == 0 ? word.length() : current.length() + 1 + word.length();
                if (needed > MAX_CHARS_PER_LINE) {
                    out.add(current.toString());
                    current.setLength(0);
                }
                if (current.length() > 0) current.append(' ');
                current.append(word);
            }
            if (current.length() > 0) out.add(current.toString());
        }
        return out;
    }

    // Bagi daftar baris menjadi kelompok-kelompok (halaman), masing-masing maksimal perPage baris.
    private static List<List<String>> paginate(List<String> lines, int perPage) {
        List<List<String>> pages = new ArrayList<>();
        for (int i = 0; i < lines.size(); i += perPage) {
            pages.add(new ArrayList<>(lines.subList(i, Math.min(i + perPage, lines.size()))));
        }
        return pages;
    }

    // Tulis String sebagai byte ke buffer PDF (pakai Latin-1 agar 1 char = 1 byte, struktur tetap presisi).
    private static void writeAscii(ByteArrayOutputStream out, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.ISO_8859_1);
        out.write(bytes, 0, bytes.length);
    }
}
