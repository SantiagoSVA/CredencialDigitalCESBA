package mx.edu.cesba.credencial.ui.credential;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/** Dos franjas diagonales arriba a la izquierda y tres esquinas tipo visor. */
public final class CredentialDecorView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int accent = 0xFFF36B21;
    private boolean reverse;
    public CredentialDecorView(Context context, AttributeSet attrs) { super(context, attrs); }
    public void configure(int color, boolean back) { accent = color; reverse = back; invalidate(); }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.scale(getWidth() / 640f, getHeight() / 404f);
        paint.setStyle(Paint.Style.FILL);
        stripe(canvas, 0, 92, accent);
        stripe(canvas, 110, 24, 0xFFF36B21);
        // Azul/naranja institucionales permanecen presentes en ambas caras.
        paint.setStrokeWidth(3f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(reverse ? 0xFFE0E8F2 : 0xFF183D6E);
        corner(canvas, 614, 24, -1, 1);
        corner(canvas, 26, 378, 1, -1);
        corner(canvas, 614, 378, -1, -1);
        paint.setColor(accent);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(208, 102, 254, 108, 3, 3, paint);
        canvas.restore();
    }
    private void stripe(Canvas canvas, float offset, float thickness, int color) {
        paint.setColor(color);
        Path shape = new Path();
        shape.moveTo(offset, 0); shape.lineTo(offset + thickness, 0);
        shape.lineTo(0, offset + thickness); shape.lineTo(0, offset); shape.close();
        canvas.drawPath(shape, paint);
    }
    private void corner(Canvas canvas, float x, float y, int dx, int dy) {
        canvas.drawLine(x, y, x + dx * 20, y, paint);
        canvas.drawLine(x, y, x, y + dy * 20, paint);
    }
}
