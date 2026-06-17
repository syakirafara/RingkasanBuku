package com.ringkasan.io;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Ekspor sederhana ke file teks UTF-8.
 */
public class TxtExporter implements Exporter {

    // Tulis teks langsung ke file dengan encoding UTF-8 (1 baris, sederhana).
    @Override
    public void export(String content, File file) throws IOException {
        if (content == null) content = "";
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getExtension() {
        return "txt";
    }

    @Override
    public String getDescription() {
        return "Text File (*.txt)";
    }
}
