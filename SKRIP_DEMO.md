# 🎬 Skrip Demo — Aplikasi Ringkasan Buku Otomatis

Panduan ngomong saat presentasi. Tinggal ikuti sambil ngeklik. Dibuat natural,
jadi bisa diparafrase pakai bahasamu sendiri.

---

## 1. Pembukaan (sambil buka aplikasi)

> "Selamat pagi/siang Bapak/Ibu. Saya **[nama]**. Aplikasi saya berjudul
> **Ringkasan Buku Otomatis**, dibuat dengan **Java + Swing**. Fungsinya
> meringkas teks panjang seperti bab buku atau artikel secara otomatis, dengan
> **dua metode**: Rule-based yang offline, dan API-based yang memakai AI dari
> HuggingFace."

## 2. Demo Rule-based (tunjukkan ini DULU — paling aman)

> "Pertama saya tunjukkan metode **Rule-based**. Metode ini **tidak butuh
> internet** — murni algoritma buatan saya sendiri."

*(Klik "Muat File" → pilih contoh_teks.txt, atau paste teks)*

> "Saya pilih metode Rule-based, panjang ringkasannya... [pilih]. Lalu klik
> **RINGKAS**."

*(Hasil muncul)*

> "Cara kerjanya: aplikasi menghitung **frekuensi kata** penting di tiap
> kalimat — kata umum seperti 'yang', 'dan', 'di' diabaikan. Kalimat dengan kata
> terpenting dianggap paling mewakili isi, lalu diambil dan disusun ulang sesuai
> urutan asli."

👉 **Perhatikan:** saat Rule-based, kolom API Token **otomatis hilang** karena
memang tidak dipakai.

## 3. Demo API-based (fitur unggulan)

> "Sekarang metode **API-based**, yang memanggil model AI **bart-large-cnn**
> dari HuggingFace."

*(Ganti metode → kolom token muncul)*

> "Begitu saya pilih API-based, kolom token **muncul**. Saya masukkan token, lalu
> RINGKAS."

*(Sambil menunggu)*

> "Karena model AI ini punya batas panjang input, untuk teks panjang saya
> terapkan teknik **map-reduce**: teksnya dipecah jadi beberapa bagian, tiap
> bagian diringkas, lalu digabung. Bedanya dengan Rule-based: ini bisa
> **menyusun kalimat baru**, bukan cuma mengambil kalimat asli."

## 4. Simpan hasil (txt & pdf)

> "Hasilnya bisa disimpan. Klik **Simpan Ringkasan** — bisa format **.txt** atau
> **.pdf**."

*(Pilih PDF, simpan, buka filenya)*

> "Yang menarik, fitur PDF ini saya buat **murni dengan kode sendiri tanpa
> library tambahan**, mengikuti spesifikasi PDF 1.4."

## 5. Riwayat

> "Semua ringkasan dalam sesi ini tersimpan. Klik **Riwayat** untuk melihatnya."

## 6. Penutup — KONSEP OOP (ini yang dinilai!)

> "Aplikasi ini menerapkan konsep OOP:
> - **Interface** `Summarizer` sebagai kontrak,
> - **Abstract class** `AbstractSummarizer` untuk kode bersama,
> - **Inheritance** — Rule-based dan API-based mewarisinya,
> - **Polymorphism** — program memanggil `.summarize()` tanpa peduli metode mana
>   yang dipakai,
> - **Encapsulation** — data dilindungi lewat getter/setter,
> - dan **Enum** untuk pilihan panjang ringkasan.
>
> Sekian, terima kasih. Saya siap menjawab pertanyaan."

---

## ⚡ Tips biar demo mulus

1. **Tes token + internet 5 menit sebelum maju** (biar model sudah "panas", tidak kena 503).
2. **Mulai dari Rule-based** — kalau internet bermasalah, ini anti-gagal.
3. Siapkan **1 teks pendek** (buat API cepat) & **1 teks panjang** (buat memamerkan chunking).
4. Kalau API error saat demo, **santai**: "Untuk teks panjang, metode Rule-based
   lebih andal karena tanpa batasan model — ini justru menunjukkan kenapa saya
   sediakan dua metode."

---

## 🛡️ Antisipasi Pertanyaan Dosen

| Pertanyaan | Jawaban singkat |
|------------|-----------------|
| "Kalau internet mati?" | "Tetap jalan pakai Rule-based, full offline." |
| "Kenapa stopword dibuang?" | "Biar kata umum tak ikut menentukan skor kalimat." |
| "Beda Rule-based vs API?" | "Rule-based extractive (ambil kalimat asli); API abstractive (menyusun kalimat baru)." |
| "Kenapa teks panjang dipotong/dipecah?" | "Model bart-large-cnn dibatasi ~1024 token; saya pakai map-reduce agar seluruh isi tetap terangkum." |
| "PDF-nya pakai library apa?" | "Tanpa library — ditulis manual sesuai spesifikasi PDF 1.4." |
| "Struktur folder beda dari teman?" | "Beda cara build saja (manual vs Maven), konsep OOP-nya sama lengkap." |
