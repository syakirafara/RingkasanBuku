<<<<<<< HEAD
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

## Build

1. Buka Command Prompt di folder proyek.
2. Jalankan:

   ```bat
   build.bat
   ```

   Skrip ini mengompilasi seluruh sumber Java di `src\` ke `bin\` dan membuat
   `RingkasanBuku.jar` yang runnable.

## Menjalankan

- **Cara 1:** klik dua kali `run.bat`
- **Cara 2:** `java -jar RingkasanBuku.jar`
- **Cara 3 (tanpa JAR):** `java -cp bin com.ringkasan.MainApp`

## Cara Penggunaan

1. Masukkan teks ke area **Teks Input** (ketik/paste, **Muat File**, atau **Tempel Clipboard**).
2. Pilih **Metode**: Rule-based (tanpa internet) atau API-based (butuh token HuggingFace).
3. Pilih **Panjang** ringkasan.
4. Klik **RINGKAS** — hasil tampil di sisi kanan.
5. Klik **Simpan Ringkasan** untuk ekspor `.txt` / `.pdf`.
6. Klik **Riwayat** untuk melihat semua ringkasan dalam sesi.

## API Token HuggingFace (opsional, untuk metode API-based)

1. Daftar gratis di <https://huggingface.co>
2. Buka **Settings → Access Tokens → Create new token** (role *Read* sudah cukup).
3. Salin token yang diawali `hf_`, lalu tempel ke field **API Token (HF)** di aplikasi.

Model default: `facebook/bart-large-cnn`. Bila respons HTTP 503, model sedang dimuat
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
=======
APLIKASI RINGKASAN BUKU OTOMATIS (JAVA + SWING)
================================================

Ringkasan
---------
Aplikasi desktop untuk meringkas teks panjang (bab buku / artikel) menggunakan
dua metode: rule-based (frekuensi kata, offline) atau API-based (HuggingFace).
Hasil ringkasan dapat disimpan ke file .txt atau .pdf, dan seluruh ringkasan
dalam sesi tersimpan di riwayat.

Persyaratan
-----------
- JDK 8 atau lebih baru (disarankan JDK 11+).
- Sistem operasi Windows (untuk skrip .bat; di Linux/macOS kompilasi manual
  dengan perintah yang sama).
- Koneksi internet hanya untuk metode API-based.
- Tidak ada library eksternal yang diperlukan.

Cara Membangun (Build)
----------------------
1. Buka Command Prompt di folder TA-PBO.
2. Jalankan:  build.bat
   - Kompilasi seluruh sumber Java di src\ ke bin\.
   - Membuat RingkasanBuku.jar yang runnable.

Cara Menjalankan
----------------
- Cara 1: klik dua kali run.bat
- Cara 2: di Command Prompt:  java -jar RingkasanBuku.jar
- Cara 3 (tanpa JAR):         java -cp bin com.ringkasan.MainApp

Menggunakan Aplikasi
--------------------
1. Masukkan teks ke area "Teks Input":
   - Ketik / paste manual, ATAU
   - Klik "Muat File" untuk membaca file .txt, ATAU
   - Klik "Tempel Clipboard" untuk mengambil teks dari clipboard.
2. Pilih dropdown "Metode":
   - Rule-based            -> tidak butuh internet.
   - API-based (HuggingFace) -> butuh token (lihat di bawah).
3. Pilih panjang ringkasan pada dropdown "Panjang".
4. Klik tombol RINGKAS. Hasil tampil di sisi kanan.
5. Klik "Simpan Ringkasan" untuk ekspor .txt atau .pdf.
6. Klik "Riwayat" untuk melihat semua ringkasan dalam sesi.

Cara Mendapatkan API Token HuggingFace
--------------------------------------
1. Daftar gratis di https://huggingface.co
2. Buka:  Settings -> Access Tokens -> Create new token
3. Role: Read sudah cukup. Salin token yang diawali "hf_".
4. Tempel ke field "API Token (HF)" di aplikasi.

Catatan: model default yang dipakai adalah facebook/bart-large-cnn.
Bila respons HTTP 503, model sedang dimuat di server HF; tunggu ~20 detik
lalu coba lagi.

Struktur Proyek
---------------
src/com/ringkasan/
  MainApp.java                       -> entry point
  core/
    Summarizer.java                  -> interface
    SummarizationException.java
    SummaryMethod.java               -> enum panjang ringkasan
    AbstractSummarizer.java          -> base class (inheritance)
    RuleBasedSummarizer.java         -> algoritma frekuensi kata
    ApiBasedSummarizer.java          -> integrasi HuggingFace
  io/
    TextInputHandler.java            -> baca file/clipboard
    SummaryFormatter.java            -> format display/export
    Exporter.java                    -> interface ekspor
    TxtExporter.java                 -> ekspor .txt
    PdfExporter.java                 -> ekspor .pdf (pure Java, no deps)
  history/
    SummaryRecord.java               -> entitas ringkasan (immutable)
    SummaryHistoryManager.java       -> manajemen riwayat
  ui/
    MainFrame.java                   -> jendela utama Swing
    HistoryDialog.java               -> dialog riwayat

Demonstrasi Konsep OOP
----------------------
- Encapsulation: field private di SummaryRecord, getter publik saja.
- Inheritance  : RuleBasedSummarizer & ApiBasedSummarizer mewarisi
                 AbstractSummarizer.
- Interface    : Summarizer dan Exporter (kontrak murni).
- Polymorphism : MainFrame#buildSummarizer() mengembalikan tipe Summarizer
                 tanpa tahu implementasi konkretnya.
- Enum         : SummaryMethod untuk daftar pilihan panjang ringkasan.

Distribusi
----------
- RingkasanBuku.jar dapat dijalankan langsung di komputer manapun yang
  memiliki JRE/JDK.
- Untuk membuat .exe dapat menggunakan launch4j atau jpackage (JDK 14+).
>>>>>>> a243e83149b51254f530b5720f9f31662155403c
