package mx.edu.cesba.credencial.ui.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/** Marcas de encuadre; no modifica la fotografía capturada. */
public final class CameraGuideView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public CameraGuideView(Context context, AttributeSet attrs) { super(context, attrs); }
    @Override protected void onDraw(Canvas canvas) {
        paint.setColor(0xCCFFFFFF);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f * getResources().getDisplayMetrics().density);
        float x = getWidth() * 0.13f, y = getHeight() * 0.12f, len = getWidth() * 0.1f;
        for (int horizontal : new int[] {-1, 1}) for (int vertical : new int[] {-1, 1}) {
            float cx = horizontal < 0 ? x : getWidth() - x;
            float cy = vertical < 0 ? y : getHeight() - y;
            canvas.drawLine(cx, cy, cx - horizontal * len, cy, paint);
            canvas.drawLine(cx, cy, cx, cy - vertical * len, paint);
        }
    }
}
