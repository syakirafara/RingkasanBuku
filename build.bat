@echo off
REM ========================================================================
REM Build script untuk Aplikasi Ringkasan Buku Otomatis (Java + Swing).
REM Mengkompilasi seluruh file .java di src\ ke folder bin\.
REM Lalu mengemas hasil .class menjadi RingkasanBuku.jar yang runnable.
REM ========================================================================

setlocal
cd /d "%~dp0"

if not exist bin mkdir bin

echo [1/3] Membersihkan folder bin...
del /q /s bin\*.class >nul 2>&1

echo [2/3] Mengkompilasi sumber Java...
dir /s /b src\*.java > sources.tmp
javac -encoding UTF-8 -d bin @sources.tmp
set COMPILE_RESULT=%ERRORLEVEL%
del sources.tmp >nul 2>&1

if not "%COMPILE_RESULT%"=="0" (
    echo.
    echo Kompilasi GAGAL. Periksa pesan error di atas.
    exit /b %COMPILE_RESULT%
)

echo [3/3] Mengemas RingkasanBuku.jar...
echo Main-Class: com.ringkasan.MainApp> MANIFEST.MF
jar cfm RingkasanBuku.jar MANIFEST.MF -C bin .
set JAR_RESULT=%ERRORLEVEL%
del MANIFEST.MF >nul 2>&1

if not "%JAR_RESULT%"=="0" (
    echo.
    echo Pembuatan JAR gagal.
    exit /b %JAR_RESULT%
)

echo.
echo SELESAI. Jalankan aplikasi dengan: run.bat   atau   java -jar RingkasanBuku.jar
endlocal
