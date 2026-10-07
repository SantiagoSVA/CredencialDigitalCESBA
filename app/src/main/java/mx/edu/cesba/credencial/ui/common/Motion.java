package mx.edu.cesba.credencial.ui.common;

import android.animation.ObjectAnimator;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/** Animaciones breves que respetan la preferencia del sistema de reducir movimiento. */
public final class Motion {
    private Motion() {}
    public static boolean enabled(View view) {
        return Settings.Global.getFloat(view.getContext().getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0;
    }
    public static void enter(View view, long delay) {
        if (!enabled(view)) return;
        view.setAlpha(0f);
        view.setTranslationY(24f * view.getResources().getDisplayMetrics().density);
        view.animate().alpha(1f).translationY(0f).setStartDelay(delay).setDuration(450)
                .setInterpolator(new DecelerateInterpolator()).start();
    }
    public static void press(View view) {
        view.setOnTouchListener((target, event) -> {
            if (enabled(target)) {
                float scale = event.getActionMasked() == MotionEvent.ACTION_DOWN ? 0.97f : 1f;
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                        || event.getActionMasked() == MotionEvent.ACTION_UP
                        || event.getActionMasked() == MotionEvent.ACTION_CANCEL)
                    target.animate().scaleX(scale).scaleY(scale).setDuration(110).start();
            }
            return false;
        });
    }
    public static void focus(View field, View container) {
        field.setOnFocusChangeListener((view, focused) -> {
            if (enabled(container)) container.animate().scaleX(focused ? 1.012f : 1f)
                    .scaleY(focused ? 1.012f : 1f).setDuration(180).start();
        });
    }
    public static void shake(View view) {
        if (!enabled(view)) return;
        float offset = 4f * view.getResources().getDisplayMetrics().density;
        ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0f, -offset, offset, -offset / 2, offset / 2, 0f)
                .setDuration(260).start();
    }
    public static void check(View icon) {
        if (icon == null || !enabled(icon)) return;
        icon.setScaleX(0.6f); icon.setScaleY(0.6f); icon.setAlpha(0f);
        icon.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(220).start();
    }
}
