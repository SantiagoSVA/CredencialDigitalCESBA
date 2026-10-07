package mx.edu.cesba.credencial.ui.credential;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/** Lienzo horizontal de 640×404 dp que se escala completo sin recortar sus dos caras. */
public final class CardStageView extends ViewGroup {
    public CardStageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClipChildren(false); setClipToPadding(false);
    }
    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        float density = getResources().getDisplayMetrics().density;
        int width = MeasureSpec.getSize(widthSpec);
        int height = Math.round(width * 404f / 640f);
        setMeasuredDimension(width, resolveSize(height, heightSpec));
        int virtualWidth = Math.round(640f * density), virtualHeight = Math.round(404f * density);
        for (int i = 0; i < getChildCount(); i++)
            getChildAt(i).measure(MeasureSpec.makeMeasureSpec(virtualWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(virtualHeight, MeasureSpec.EXACTLY));
    }
    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int childLeft = (getWidth() - child.getMeasuredWidth()) / 2;
            int childTop = (getHeight() - child.getMeasuredHeight()) / 2;
            child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(), childTop + child.getMeasuredHeight());
            float scale = Math.min(getWidth() / (float) child.getMeasuredWidth(),
                    getHeight() / (float) child.getMeasuredHeight());
            child.setPivotX(child.getMeasuredWidth() / 2f); child.setPivotY(child.getMeasuredHeight() / 2f);
            child.setScaleX(scale); child.setScaleY(scale);
            // La perspectiva se aplica dentro de la tarjeta, dejando su tamaño externo estable.
        }
    }
}
