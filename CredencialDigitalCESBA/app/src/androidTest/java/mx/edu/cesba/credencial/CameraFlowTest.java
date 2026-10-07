package mx.edu.cesba.credencial;

import android.Manifest;
import android.app.Instrumentation;
import android.content.Context;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.atomic.AtomicBoolean;
import mx.edu.cesba.credencial.ui.camera.CameraActivity;
import mx.edu.cesba.credencial.ui.form.StudentFormActivity;
import static org.junit.Assert.*;

/** Ejecutar en un dispositivo o emulador con al menos una cámara disponible. */
@RunWith(AndroidJUnit4.class)
public class CameraFlowTest {
    @Rule public GrantPermissionRule cameraPermission = GrantPermissionRule.grant(Manifest.permission.CAMERA);

    @Test public void capturedPhotoReturnsToFormAndCanBeRetaken() throws Exception {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Context context = instrumentation.getTargetContext();
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(CameraActivity.class.getName(), null, false);
        try (ActivityScenario<StudentFormActivity> scenario = ActivityScenario.launch(StudentFormActivity.class)) {
            scenario.onActivity(activity -> activity.findViewById(R.id.photoButton).performClick());
            CameraActivity camera = (CameraActivity) monitor.waitForActivityWithTimeout(15000);
            assertNotNull("Debe abrirse CameraX", camera);
            AtomicBoolean ready = new AtomicBoolean();
            for (int attempt = 0; attempt < 100 && !ready.get(); attempt++) {
                instrumentation.runOnMainSync(() -> ready.set(camera.findViewById(R.id.captureButton).isEnabled()));
                if (!ready.get()) Thread.sleep(200);
            }
            assertTrue("Debe habilitarse la captura con la cámara preparada", ready.get());
            instrumentation.runOnMainSync(() -> camera.findViewById(R.id.captureButton).performClick());
            AtomicBoolean returned = new AtomicBoolean();
            for (int attempt = 0; attempt < 100 && !returned.get(); attempt++) {
                scenario.onActivity(activity -> returned.set("Volver a tomar foto".contentEquals(
                        ((TextView) activity.findViewById(R.id.photoButton)).getText())));
                if (!returned.get()) Thread.sleep(200);
            }
            assertTrue("La foto debe aparecer en el formulario", returned.get());
            scenario.onActivity(activity -> {
                assertEquals("Fotografía lista.", ((TextView) activity.findViewById(R.id.photoError)).getText().toString());
                assertFalse("La foto sola no basta", activity.findViewById(R.id.generateButton).isEnabled());
                activity.findViewById(R.id.photoButton).performClick();
            });
            assertNotNull("Debe poder repetirse la captura", monitor.waitForActivityWithTimeout(15000));
            instrumentation.runOnMainSync(() -> {
                android.app.Activity current = monitor.getLastActivity();
                if (current != null) current.finish();
            });
        } finally { instrumentation.removeMonitor(monitor); }
    }
}
