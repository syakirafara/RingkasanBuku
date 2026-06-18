import com.ringkasan.io.PdfExporter;
import com.ringkasan.io.TxtExporter;
import java.io.File;
public class TestPdf {
  public static void main(String[] a) throws Exception {
    String teks = "Ini ringkasan uji coba.\nBaris kedua untuk memastikan multi-baris jalan. "
      + "Kalimat panjang sengaja dibuat agar word-wrap bekerja dengan baik dan rapi di dalam halaman PDF yang dihasilkan secara manual tanpa library apa pun.";
    new PdfExporter().export(teks, new File("_tmptest/hasil_uji.pdf"));
    new TxtExporter().export(teks, new File("_tmptest/hasil_uji.txt"));
    System.out.println("EXPORT OK");
  }
}
