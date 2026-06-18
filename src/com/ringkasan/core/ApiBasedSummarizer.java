package com.ringkasan.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Summarizer berbasis API HuggingFace Inference.
 * Memanggil endpoint:
 *   POST https://router.huggingface.co/hf-inference/models/&lt;model&gt;
 * dengan header Authorization: Bearer &lt;token&gt; dan body JSON
 *   { "inputs": "...", "parameters": { "min_length": .., "max_length": .. } }
 *
 * Implementasi sengaja memakai HttpURLConnection bawaan JDK + parser JSON
 * minimalis berbasis regex sehingga proyek tidak butuh dependensi eksternal.
 */
public class ApiBasedSummarizer extends AbstractSummarizer {

    public static final String DEFAULT_MODEL = "facebook/bart-large-cnn";
    // Endpoint Inference HuggingFace yang baru (router). Endpoint lama
    // "api-inference.huggingface.co" sudah tidak aktif sejak 2024/2025.
    private static final String ENDPOINT = "https://router.huggingface.co/hf-inference/models/";

    // Batas aman panjang teks input. Model BART hanya menerima ~1024 token
    // (posisi embedding). Bila teks lebih panjang, server membalas HTTP 400
    // "index out of range in self". Karena teks Bahasa Indonesia cenderung
    // pecah menjadi banyak token, batas karakter dibuat konservatif.
    private static final int MAX_INPUT_CHARS = 1800;

    // Batas jumlah potongan (chunk) untuk teks sangat panjang. Tiap chunk = 1
    // panggilan API, jadi dibatasi agar proses tidak terlalu lama saat demo.
    private static final int MAX_CHUNKS = 6;

    private final String apiToken;
    private final String model;

    // Konstruktor ringkas: pakai model default (bart-large-cnn).
    // "this(...)" memanggil konstruktor di bawahnya (constructor chaining).
    public ApiBasedSummarizer(String apiToken) {
        this(apiToken, DEFAULT_MODEL);
    }

    // Konstruktor lengkap: bisa pilih model sendiri. Jika model kosong -> pakai default.
    public ApiBasedSummarizer(String apiToken, String model) {
        this.apiToken = apiToken;
        this.model = (model == null || model.trim().isEmpty()) ? DEFAULT_MODEL : model.trim();
    }

    // Method utama: kirim teks ke server HuggingFace lalu kembalikan ringkasannya.
    @Override
    public String summarize(String text) throws SummarizationException {
        // --- Validasi teks & token ---
        if (text == null || text.trim().isEmpty()) {
            throw new SummarizationException("Teks input kosong.");
        }
        if (apiToken == null || apiToken.trim().isEmpty()) {
            throw new SummarizationException(
                "API token HuggingFace belum diatur. Isi field 'API Token' atau gunakan metode Rule-based.");
        }

        // --- Tentukan panjang min/max ringkasan sesuai pilihan user ---
        int minLen, maxLen;
        switch (method) {
            case SHORT:    minLen = 25;  maxLen = 80;  break;
            case DETAILED: minLen = 100; maxLen = 250; break;
            default:       minLen = 50;  maxLen = 150; break;
        }

        // --- Pecah teks panjang menjadi beberapa bagian (chunk) yang muat di model ---
        List<String> chunks = splitIntoChunks(text);

        // Kasus umum: teks pendek -> cukup satu panggilan API.
        if (chunks.size() == 1) {
            return callApi(chunks.get(0), minLen, maxLen);
        }

        // --- Teks panjang: ringkas tiap bagian, lalu gabungkan hasilnya ---
        // Ini teknik "map" pada pola map-reduce summarization.
        StringBuilder combined = new StringBuilder();
        for (String chunk : chunks) {
            String part = callApi(chunk, minLen, maxLen);
            if (combined.length() > 0) combined.append(' ');
            combined.append(part.trim());
        }
        return combined.toString().trim();
    }

    // Satu panggilan API HuggingFace untuk satu potong teks. Mengembalikan ringkasannya.
    private String callApi(String text, int minLen, int maxLen) throws SummarizationException {
        // --- Susun body request dalam format JSON (dirakit manual sebagai String) ---
        String payload = "{\"inputs\":" + jsonString(text)
                + ",\"parameters\":{\"min_length\":" + minLen
                + ",\"max_length\":" + maxLen
                + ",\"do_sample\":false}}";

        HttpURLConnection conn = null;
        try {
            // --- Buka koneksi HTTP ke endpoint model ---
            URL url = URI.create(ENDPOINT + model).toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            // Header Authorization wajib berisi token (Bearer).
            conn.setRequestProperty("Authorization", "Bearer " + apiToken.trim());
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);              // tandai bahwa kita akan mengirim body
            conn.setConnectTimeout(20_000);      // batas waktu konek 20 detik
            conn.setReadTimeout(60_000);         // batas waktu baca respons 60 detik

            // --- Kirim payload JSON ke server ---
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            // --- Baca respons. Jika status 2xx -> sukses (inputStream), selain itu -> errorStream ---
            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            String body = readAll(is);

            // --- Tangani error HTTP dengan pesan yang ramah ---
            if (code < 200 || code >= 300) {
                String hint = (code == 401) ? " (token tidak valid)"
                            : (code == 503) ? " (model sedang dimuat, coba lagi ~20 detik)"
                            : "";
                throw new SummarizationException(
                    "HuggingFace API HTTP " + code + hint + ": " + trim(body, 500));
            }

            // --- Ambil teks ringkasan dari JSON respons (cari "summary_text") ---
            String result = extractField(body, "summary_text");
            if (result == null) result = extractField(body, "generated_text"); // model lain
            if (result == null) {
                throw new SummarizationException("Tidak dapat mem-parsing respons: " + trim(body, 300));
            }
            return result.trim();
        } catch (IOException e) {
            // Bungkus error jaringan menjadi SummarizationException (cause disimpan).
            throw new SummarizationException("Gagal memanggil API: " + e.getMessage(), e);
        } finally {
            // Apa pun yang terjadi, tutup koneksi agar tidak bocor.
            if (conn != null) conn.disconnect();
        }
    }

    // Nama algoritma untuk ditampilkan di UI (sekaligus menyebut model yang dipakai).
    @Override
    public String getName() {
        return "API-based (HuggingFace / " + model + ")";
    }

    // Pecah teks panjang menjadi beberapa bagian (chunk), masing-masing tidak
    // melebihi MAX_INPUT_CHARS agar muat di model. Pemotongan diusahakan di
    // batas akhir kalimat (. ! ?) supaya tiap bagian tetap utuh maknanya;
    // bila tidak ada, dipotong di spasi terakhir agar kata tidak terbelah.
    private static List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        String remaining = text.trim();
        while (remaining.length() > MAX_INPUT_CHARS && chunks.size() < MAX_CHUNKS - 1) {
            String window = remaining.substring(0, MAX_INPUT_CHARS);
            // Cari batas kalimat terakhir di dalam jendela.
            int cut = Math.max(window.lastIndexOf(". "),
                      Math.max(window.lastIndexOf("! "), window.lastIndexOf("? ")));
            if (cut < MAX_INPUT_CHARS / 2) cut = window.lastIndexOf(' '); // jatuh ke spasi
            cut = (cut <= 0) ? MAX_INPUT_CHARS : cut + 1;                 // sertakan pemisah
            chunks.add(remaining.substring(0, cut).trim());
            remaining = remaining.substring(cut).trim();
        }
        // Sisa terakhir: bila masih melebihi batas (karena sudah mentok MAX_CHUNKS),
        // potong aman agar panggilan terakhir tetap valid.
        if (remaining.length() > MAX_INPUT_CHARS) {
            String cut = remaining.substring(0, MAX_INPUT_CHARS);
            int lastSpace = cut.lastIndexOf(' ');
            remaining = (lastSpace > MAX_INPUT_CHARS / 2 ? cut.substring(0, lastSpace) : cut).trim();
        }
        if (!remaining.isEmpty()) chunks.add(remaining);
        return chunks;
    }

    // Potong string bila terlalu panjang (dipakai agar pesan error tidak kepanjangan).
    private static String trim(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    // Baca seluruh isi InputStream menjadi satu String UTF-8 (dibaca per blok 4 KB).
    private static String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int n;
        while ((n = is.read(chunk)) > 0) buf.write(chunk, 0, n);
        return new String(buf.toByteArray(), StandardCharsets.UTF_8);
    }

    /** Encode string ke JSON literal yang valid (termasuk escape karakter kontrol). */
    private static String jsonString(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /** Ekstraksi field bertipe string dari respons JSON (cukup untuk HF). */
    private static String extractField(String json, String key) {
        if (json == null) return null;
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher m = p.matcher(json);
        if (m.find()) return unescapeJson(m.group(1));
        return null;
    }

    // Kebalikan dari escape: ubah \n \t \" \\ dan kode \\uXXXX di string JSON kembali ke karakter asli.
    private static String unescapeJson(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                switch (next) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'u':
                        if (i + 4 < s.length()) {
                            try {
                                sb.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                                i += 4;
                            } catch (NumberFormatException ex) {
                                sb.append(next);
                            }
                        }
                        break;
                    default: sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
