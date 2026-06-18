# Aplikasi Ringkasan Buku Otomatis

Aplikasi desktop (Java + Swing) untuk meringkas teks panjang (bab buku / artikel)
secara otomatis. Tersedia dua metode peringkasan: **rule-based** (frekuensi kata,
sepenuhnya offline) dan **API-based** (HuggingFace). Hasil ringkasan dapat disimpan
ke file `.txt` atau `.pdf`, dan seluruh ringkasan dalam satu sesi tersimpan di riwayat.

> Tugas Akhir mata kuliah **Pemrograman Berbasis Objek**.

## Fitur

- Input teks via ketik manual, muat file `.txt`, atau tempel dari clipboard
- Dua metode ringkasan: Rule-based (offline) & API-based (HuggingFace)
- Pilihan panjang ringkasan (pendek / sedang / panjang)
- Ekspor hasil ke `.txt` dan `.pdf` (pure Java, tanpa library eksternal)
- Riwayat seluruh ringkasan dalam sesi

## Persyaratan

- JDK 8 atau lebih baru (disarankan JDK 11+)
- Windows (untuk skrip `.bat`; di Linux/macOS kompilasi manual dengan perintah yang sama)
- Koneksi internet hanya untuk metode API-based
- Tidak ada library eksternal yang diperlukan

## Build & Menjalankan

**Cara termudah:** klik dua kali **`JALANKAN.bat`**. Skrip ini sekaligus
mengompilasi seluruh sumber Java di `src\` ke `bin\`, membuat
`RingkasanBuku.jar`, lalu menjalankan aplikasinya.

Alternatif manual (bila JAR sudah ada):

- `java -jar RingkasanBuku.jar`
- atau tanpa JAR: `java -cp bin com.ringkasan.MainApp`

## Cara Penggunaan

1. Masukkan teks ke area **Teks Input** (ketik/paste, **Muat File**, atau **Tempel Clipboard**).
2. Pilih **Metode**: Rule-based (tanpa internet) atau API-based (butuh token HuggingFace).
3. Pilih **Panjang** ringkasan.
4. Klik **RINGKAS** — hasil tampil di sisi kanan.
5. Klik **Simpan Ringkasan** untuk ekspor `.txt` / `.pdf`.
6. Klik **Riwayat** untuk melihat semua ringkasan dalam sesi.

## API Token HuggingFace (opsional, untuk metode API-based)

1. Daftar gratis di <https://huggingface.co>
2. Buka **Settings → Access Tokens → Create new token** (tipe *Read* sudah cukup).
3. Salin token yang diawali `hf_`, lalu tempel ke field **API Token (HF)** di aplikasi.

Model default: `facebook/bart-large-cnn`. Karena model ini dibatasi ~1024 token,
teks panjang otomatis dipecah jadi beberapa bagian (map-reduce summarization),
diringkas per bagian, lalu digabung. Bila respons HTTP 503, model sedang dimuat
di server HF — tunggu ~20 detik lalu coba lagi.

## Struktur Proyek

```
src/com/ringkasan/
  MainApp.java                  -> entry point
  core/
    Summarizer.java             -> interface
    SummarizationException.java
    SummaryMethod.java          -> enum panjang ringkasan
    AbstractSummarizer.java     -> base class (inheritance)
    RuleBasedSummarizer.java    -> algoritma frekuensi kata
    ApiBasedSummarizer.java     -> integrasi HuggingFace
  io/
    TextInputHandler.java       -> baca file/clipboard
    SummaryFormatter.java       -> format display/export
    Exporter.java               -> interface ekspor
    TxtExporter.java            -> ekspor .txt
    PdfExporter.java            -> ekspor .pdf (pure Java, no deps)
  history/
    SummaryRecord.java          -> entitas ringkasan (immutable)
    SummaryHistoryManager.java  -> manajemen riwayat
  ui/
    MainFrame.java              -> jendela utama Swing
    HistoryDialog.java          -> dialog riwayat
```

## Demonstrasi Konsep OOP

| Konsep        | Penerapan                                                              |
|---------------|-----------------------------------------------------------------------|
| Encapsulation | Field `private` di `SummaryRecord`, akses lewat getter publik          |
| Inheritance   | `RuleBasedSummarizer` & `ApiBasedSummarizer` mewarisi `AbstractSummarizer` |
| Interface     | `Summarizer` dan `Exporter` (kontrak murni)                           |
| Polymorphism  | `MainFrame#buildSummarizer()` mengembalikan tipe `Summarizer` tanpa tahu implementasi konkret |
| Enum          | `SummaryMethod` untuk daftar pilihan panjang ringkasan                |
