@echo off
REM ========================================================================
REM SATU KLIK: membangun (build) lalu menjalankan aplikasi.
REM File ini berdiri sendiri (tidak butuh build.bat / run.bat).
REM Tahan terhadap path folder yang mengandung spasi.
REM ========================================================================
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ========================================================
echo   APLIKASI RINGKASAN BUKU OTOMATIS
echo   Langkah 1/2: Membangun aplikasi...
echo ========================================================
echo.

if not exist bin mkdir bin
del /q /s bin\*.class >nul 2>&1

REM Kumpulkan daftar file .java, tulis ke argfile dengan tanda kutip dan
REM garis miring "/" (di argfile javac, "\" dianggap karakter escape).
dir /s /b src\*.java > sources.raw
if exist sources.tmp del sources.tmp >nul 2>&1
for /f "usebackq delims=" %%F in ("sources.raw") do (
    set "p=%%F"
    set "p=!p:\=/!"
    echo "!p!">> sources.tmp
)
javac -encoding UTF-8 -d bin @sources.tmp
set COMPILE_RESULT=%ERRORLEVEL%
del sources.raw sources.tmp >nul 2>&1

if not "%COMPILE_RESULT%"=="0" (
    echo.
    echo ********************************************************
    echo   BUILD GAGAL. Baca pesan error di atas.
    echo   Screenshot layar ini bila perlu bantuan.
    echo ********************************************************
    echo.
    pause
    exit /b 1
)

echo Mengemas RingkasanBuku.jar...
echo Main-Class: com.ringkasan.MainApp> MANIFEST.MF
jar cfm RingkasanBuku.jar MANIFEST.MF -C bin .
del MANIFEST.MF >nul 2>&1

echo.
echo ========================================================
echo   Langkah 2/2: Menjalankan aplikasi...
echo   (Jendela aplikasi akan muncul. Jangan tutup jendela
echo    hitam ini selama memakai aplikasi.)
echo ========================================================
echo.

java -jar RingkasanBuku.jar

echo.
echo Aplikasi ditutup. Tekan tombol apa saja untuk keluar.
pause >nul
