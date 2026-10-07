package mx.edu.cesba.credencial;

import org.json.JSONObject;
import org.junit.Test;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import java.util.Collections;
import mx.edu.cesba.credencial.model.CareerCatalog;
import mx.edu.cesba.credencial.model.StudentData;
import mx.edu.cesba.credencial.qr.QrEncoder;
import static org.junit.Assert.*;

public class QrEncoderTest {
    private StudentData data(String career) {
        return new StudentData("María José Núñez", "000123456", career, "Cuatrimestre", 12,
                "6A", "maria@cesba.edu.mx", "Alumno", "photo.jpg");
    }
    @Test public void roundTripAllSixteenCareersKeepsJsonAndAccents() throws Exception {
        assertEquals(16, CareerCatalog.all().size());
        assertEquals(16, CareerCatalog.all().values().stream().distinct().count());
        for (String career : CareerCatalog.all().keySet()) {
            BitMatrix matrix = QrEncoder.encode(data(career), true, 900);
            int width = matrix.getWidth();
            int[] pixels = new int[width * matrix.getHeight()];
            for (int y = 0; y < matrix.getHeight(); y++) for (int x = 0; x < width; x++)
                pixels[y * width + x] = matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(width, matrix.getHeight(), pixels)));
            JSONObject result = new JSONObject(new QRCodeReader().decode(bitmap,
                    Collections.singletonMap(DecodeHintType.CHARACTER_SET, "UTF-8")).getText());
            assertEquals("María José Núñez", result.getString("nombre"));
            assertEquals("000123456", result.getString("matricula"));
            assertEquals(career, result.getString("carrera"));
            assertEquals("6A", result.getString("grupo"));
        }
    }
    @Test(expected = IllegalArgumentException.class) public void qrRevalidatesMissingPhoto() throws Exception {
        QrEncoder.encode(data("Derecho"), false, 900);
    }
    @Test(expected = IllegalArgumentException.class) public void qrRevalidatesInvalidData() throws Exception {
        QrEncoder.encode(data("No existe"), true, 900);
    }
}
