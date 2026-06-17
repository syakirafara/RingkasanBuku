@echo off
REM Menjalankan aplikasi. Bila JAR sudah dibuat (build.bat) akan
REM dijalankan via -jar; jika belum, dijalankan via folder bin\.

setlocal
cd /d "%~dp0"

if exist RingkasanBuku.jar (
    java -jar RingkasanBuku.jar
    goto :EOF
)

if exist bin\com\ringkasan\MainApp.class (
    java -cp bin com.ringkasan.MainApp
    goto :EOF
)

echo Belum ada hasil build. Jalankan build.bat terlebih dahulu.
exit /b 1
