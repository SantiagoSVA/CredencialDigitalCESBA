package mx.edu.cesba.credencial.ui.credential;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/** Brillo discreto ligado a la inclinación; conserva el contraste y la lectura del QR. */
public final class SheenView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float tilt;
    private boolean reverse;
    public SheenView(Context context, AttributeSet attrs) { super(context, attrs); setClickable(false); }
    public void setTilt(float value) { tilt = value; invalidate(); }
    public void setReverse(boolean value) { reverse = value; invalidate(); }
    @Override protected void onDraw(Canvas canvas) {
        // Sin brillo sobre el reverso para preservar el blanco/negro exacto del QR.
        if (reverse) return;
        float center = getWidth() * (0.4f + tilt / 20f);
        paint.setShader(new LinearGradient(center - getWidth() * 0.24f, 0,
                center + getWidth() * 0.24f, getHeight(),
                new int[] {0x00FFFFFF, 0x16FFFFFF, 0x00FFFFFF},
                new float[] {0f, 0.5f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }
}
