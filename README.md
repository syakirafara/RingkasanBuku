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
