package mx.edu.cesba.credencial.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.EnumMap;
import java.util.Map;
import mx.edu.cesba.credencial.model.StudentData;
import mx.edu.cesba.credencial.validation.StudentValidator;

/** JSON UTF-8 y margen blanco de cuatro módulos: el logo queda separado del QR. */
public final class QrEncoder {
    private QrEncoder() {}
    public static String payload(StudentData data, boolean photoReadable) throws JSONException {
        if (!StudentValidator.errors(data, photoReadable).isEmpty())
            throw new IllegalArgumentException("Datos de credencial no válidos.");
        return new JSONObject().put("nombre", data.name).put("matricula", data.studentId)
                .put("carrera", data.career).put("grupo", data.group).toString();
    }
    public static BitMatrix encode(StudentData data, boolean photoReadable, int size)
            throws JSONException, WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 4);
        return new QRCodeWriter().encode(payload(data, photoReadable), BarcodeFormat.QR_CODE, size, size, hints);
    }
}
