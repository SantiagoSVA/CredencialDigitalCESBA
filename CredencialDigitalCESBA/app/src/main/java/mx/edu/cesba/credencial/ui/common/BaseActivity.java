package mx.edu.cesba.credencial.ui.common;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import mx.edu.cesba.credencial.R;

/** Insets para Android 15 y teclado; navegación del sistema, sin botones adicionales. */
public abstract class BaseActivity extends AppCompatActivity {
    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.screen_enter, R.anim.screen_exit);
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.screen_back_enter, R.anim.screen_back_exit);
        }
    }
    protected void fitSystemBars(View root, boolean darkBackground) {
        WindowCompat.getInsetsController(getWindow(), root).setAppearanceLightStatusBars(!darkBackground);
        WindowCompat.getInsetsController(getWindow(), root).setAppearanceLightNavigationBars(!darkBackground);
        int left = root.getPaddingLeft(), top = root.getPaddingTop();
        int right = root.getPaddingRight(), bottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            int keyboard = windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            view.setPadding(left + bars.left, top + bars.top, right + bars.right,
                    bottom + Math.max(bars.bottom, keyboard));
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }
    protected void openScreen(Intent intent) {
        startActivity(intent);
        if (Build.VERSION.SDK_INT < 34)
            overridePendingTransition(R.anim.screen_enter, R.anim.screen_exit);
    }
    @Override public void finish() {
        super.finish();
        if (Build.VERSION.SDK_INT < 34)
            overridePendingTransition(R.anim.screen_back_enter, R.anim.screen_back_exit);
    }
}
