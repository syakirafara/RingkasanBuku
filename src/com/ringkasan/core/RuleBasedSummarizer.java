package com.ringkasan.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Summarizer rule-based dengan algoritma frekuensi kata (klasik):
 *   1. Pecah teks menjadi kalimat dan token kata.
 *   2. Hitung frekuensi kemunculan setiap kata (abaikan stopword & token pendek).
 *   3. Normalisasi frekuensi ke skala 0..1.
 *   4. Skor tiap kalimat = jumlah frekuensi-ternormalisasi kata penyusunnya.
 *   5. Ambil N kalimat ber-skor tertinggi, kembalikan dalam urutan asli.
 *
 * Algoritma ini tidak butuh koneksi internet, cocok untuk demo offline.
 */
public class RuleBasedSummarizer extends AbstractSummarizer {

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
        // Stopwords Bahasa Indonesia
        "yang", "dan", "di", "ke", "dari", "untuk", "pada", "dengan", "adalah",
        "akan", "ini", "itu", "atau", "juga", "tidak", "saja", "oleh", "dalam",
        "karena", "sehingga", "sebagai", "tersebut", "dapat", "telah", "sudah",
        "masih", "lebih", "sangat", "semua", "setiap", "hanya", "ada", "tetapi",
        "namun", "jika", "maka", "agar", "supaya", "saat", "ketika", "bagi",
        "menjadi", "dia", "mereka", "kami", "kita", "anda", "kamu", "saya",
        "harus", "bisa", "para", "kepada", "tentang", "bahwa", "begitu", "antara",
        // Stopwords English
        "the", "is", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
        "of", "with", "by", "from", "as", "this", "that", "these", "those", "it",
        "its", "be", "are", "was", "were", "been", "being", "have", "has", "had",
        "do", "does", "did", "will", "would", "can", "could", "should", "may",
        "might", "must", "not", "no", "so", "if", "then", "than", "into", "about"
    ));

    // Method utama: jalankan algoritma frekuensi kata (5 langkah, lihat di bawah).
    @Override
    public String summarize(String text) throws SummarizationException {
        // --- Validasi: teks tidak boleh kosong ---
        if (text == null || text.trim().isEmpty()) {
            throw new SummarizationException("Teks input kosong.");
        }

        // --- LANGKAH 1: pecah teks menjadi array kalimat (helper dari kelas induk) ---
        String[] sentences = splitSentences(text);
        if (sentences.length == 0) {
            throw new SummarizationException("Tidak ada kalimat yang terdeteksi.");
        }
        // Kalau cuma 1 kalimat, tidak ada yang perlu diringkas -> kembalikan apa adanya.
        if (sentences.length == 1) {
            return sentences[0];
        }

        // --- LANGKAH 2: hitung frekuensi tiap kata ---
        // freq = Map<kata, jumlah kemunculan>. Stopword & kata < 3 huruf diabaikan.
        Map<String, Integer> freq = new HashMap<>();
        for (String sentence : sentences) {
            for (String word : tokenize(sentence)) {
                if (STOPWORDS.contains(word) || word.length() < 3) continue;
                freq.merge(word, 1, Integer::sum); // tambah 1, atau set 1 jika kata baru
            }
        }

        // Kasus khusus: kalau semua kata tersaring (tidak ada kata berbobot),
        // ambil saja N kalimat pertama sebagai cadangan.
        if (freq.isEmpty()) {
            int n = Math.min(method.getTargetSentences(), sentences.length);
            StringBuilder fallback = new StringBuilder();
            for (int i = 0; i < n; i++) {
                if (i > 0) fallback.append(' ');
                fallback.append(sentences[i]);
            }
            return fallback.toString();
        }

        // --- LANGKAH 3: normalisasi frekuensi ke skala 0..1 (bagi dengan frekuensi tertinggi) ---
        int maxFreq = Collections.max(freq.values());
        Map<String, Double> norm = new HashMap<>();
        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            norm.put(e.getKey(), e.getValue() / (double) maxFreq);
        }

        // --- LANGKAH 4: hitung skor tiap kalimat = jumlah bobot kata di dalamnya ---
        double[] scores = new double[sentences.length];
        for (int i = 0; i < sentences.length; i++) {
            for (String word : tokenize(sentences[i])) {
                Double w = norm.get(word);
                if (w != null) scores[i] += w;
            }
        }

        // --- LANGKAH 5: ambil N kalimat skor tertinggi ---
        // Buat array indeks [0,1,2,...] lalu urutkan DESCENDING berdasarkan skor.
        Integer[] orderByScore = new Integer[sentences.length];
        for (int i = 0; i < sentences.length; i++) orderByScore[i] = i;
        Arrays.sort(orderByScore, (a, b) -> Double.compare(scores[b], scores[a]));

        // Ambil "take" indeks teratas, lalu urutkan ASCENDING lagi supaya
        // hasil ringkasan tetap mengikuti urutan asli teks (agar koheren).
        int take = Math.min(method.getTargetSentences(), sentences.length);
        List<Integer> selected = new ArrayList<>(Arrays.asList(orderByScore).subList(0, take));
        Collections.sort(selected);

        // Gabungkan kalimat-kalimat terpilih menjadi satu string ringkasan.
        StringBuilder sb = new StringBuilder();
        for (int idx : selected) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(sentences[idx].trim());
        }
        return sb.toString();
    }

    // Pecah satu kalimat menjadi daftar kata (huruf kecil), buang tanda baca/simbol.
    // Regex [^\p{L}0-9]+ = "pisah di mana pun yang BUKAN huruf/angka".
    private List<String> tokenize(String sentence) {
        List<String> out = new ArrayList<>();
        for (String w : sentence.toLowerCase().split("[^\\p{L}0-9]+")) {
            if (!w.isEmpty()) out.add(w);
        }
        return out;
    }

    // Nama algoritma untuk ditampilkan di UI.
    @Override
    public String getName() {
        return "Rule-based (Frekuensi Kata)";
    }
}
