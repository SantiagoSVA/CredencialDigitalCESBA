package mx.edu.cesba.credencial.ui.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/** Anillos sutiles: decoración editorial, sin interacción adicional. */
public final class EditorialBackdropView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public EditorialBackdropView(Context context, AttributeSet attrs) { super(context, attrs); }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(getResources().getDisplayMetrics().density);
        paint.setColor(0x227DA1CE);
        float radius = getWidth() * 0.66f;
        for (int i = 0; i < 3; i++)
            canvas.drawCircle(getWidth() * 1.18f, getHeight() * 0.34f, radius + i * 34f, paint);
    }
}
